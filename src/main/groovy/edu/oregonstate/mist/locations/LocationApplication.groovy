package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.locations.LocationCommand
import edu.oregonstate.mist.locations.db.ArcGisDAO
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.ExtraDataManager
import edu.oregonstate.mist.locations.db.FacilDAO
import edu.oregonstate.mist.locations.db.LibraryDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.locations.health.ArcGisHealthCheck
import edu.oregonstate.mist.locations.health.DiningHealthCheck
import edu.oregonstate.mist.locations.health.ExtensionHealthCheck
import edu.oregonstate.mist.locations.health.LibraryHealthCheck
import edu.oregonstate.mist.locations.resources.LocationResource
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.apache.http.client.HttpClient
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class LocationApplication extends Application<LocationConfiguration> {
    @Override
    public void initialize(Bootstrap bootstrap) {
        super.initialize(bootstrap)
        bootstrap.addCommand(new LocationCommand())
    }

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(LocationConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        ExtraDataManager extraDataManager = new ExtraDataManager()
        environment.lifecycle().manage(extraDataManager)

        // the httpclient from DW provides with many metrics and config options
        HttpClient httpClient = new HttpClientBuilder(environment)
                .using(configuration.getHttpClientConfiguration())
                .build("backend-http-client")

        def configMap = configuration.locationsConfiguration
        final LibraryDAO libraryDAO = new LibraryDAO(configMap, httpClient)
        final LocationDAO locationDAO = new LocationDAO(configMap)
        final LocationUtil locationUtil = new LocationUtil(configMap)
        final ExtensionDAO extensionDAO = new ExtensionDAO(configMap, locationUtil)
        final DiningDAO diningDAO = new DiningDAO(configuration, locationUtil)
        final ArcGisDAO arcGisDAO = new ArcGisDAO(configMap, locationUtil)
        final CampusMapDAO campusMapDAO = new CampusMapDAO(configMap, locationUtil)
        final DBIFactory factory = new DBIFactory()
        final DBI jdbi = factory.build(environment, configuration.getDatabase(), "jdbi")
        FacilDAO facilDAO = jdbi.onDemand(FacilDAO.class)

        ExtraDataDAO extraDataDAO = new ExtraDataDAO(configuration, locationUtil,
                extraDataManager)

        addHealthChecks(environment, configuration, libraryDAO)

        Boolean useHttpCampusMap = Boolean.parseBoolean(
                configuration.locationsConfiguration.get("useHttpCampusMap"))

        environment.jersey().register(new LocationResource(diningDAO, locationDAO,
                extensionDAO, arcGisDAO, extraDataDAO, extraDataManager, libraryDAO,
                useHttpCampusMap, campusMapDAO, facilDAO))
    }

    private static void addHealthChecks(Environment environment,
                                        LocationConfiguration configuration,
                                        LibraryDAO libraryDAO) {
        environment.healthChecks().register("dining",
                new DiningHealthCheck(configuration.locationsConfiguration))
        environment.healthChecks().register("extension",
                new ExtensionHealthCheck(configuration.locationsConfiguration))
        environment.healthChecks().register("arcgis",
                new ArcGisHealthCheck(configuration.locationsConfiguration))
        environment.healthChecks().register("library", new LibraryHealthCheck(libraryDAO))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new LocationApplication().run(arguments)
    }
}

package edu.oregonstate.mist.locations

import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.api.BuildInfoManager
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.locations.db.ArcGisDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.locations.health.ArcGisHealthCheck
import edu.oregonstate.mist.locations.health.DiningHealthCheck
import edu.oregonstate.mist.locations.health.ExtensionHealthCheck
import edu.oregonstate.mist.locations.resources.LocationResource

import edu.oregonstate.mist.api.PrettyPrintResponseFilter
import edu.oregonstate.mist.api.jsonapi.GenericExceptionMapper
import edu.oregonstate.mist.api.jsonapi.NotFoundExceptionMapper
import io.dropwizard.Application
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.basic.BasicCredentialAuthFilter
import io.dropwizard.jersey.errors.LoggingExceptionMapper
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import javax.ws.rs.WebApplicationException

/**
 * Main application class.
 */
class LocationApplication extends Application<LocationConfiguration> {
    /**
     * Initializes application bootstrap.
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle())
    }

    /**
     * Registers lifecycle managers and Jersey exception mappers
     * and container response filters
     *
     * @param environment
     * @param buildInfoManager
     */
    protected void registerAppManagerLogic(Environment environment,
                                           BuildInfoManager buildInfoManager) {

        environment.lifecycle().manage(buildInfoManager)

        environment.jersey().register(new NotFoundExceptionMapper())
        environment.jersey().register(new GenericExceptionMapper())
        environment.jersey().register(new LoggingExceptionMapper<WebApplicationException>(){})
        environment.jersey().register(new PrettyPrintResponseFilter())
    }

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(LocationConfiguration configuration, Environment environment) {
        Resource.loadProperties()
        BuildInfoManager buildInfoManager = new BuildInfoManager()
        registerAppManagerLogic(environment, buildInfoManager)

        final DBIFactory factory = new DBIFactory()

//      final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(),"jdbi")
//      final CampusMapLocationDAO campusMapLocationDAO = jdbi.onDemand(CampusMapLocationDAO.class)

        final LocationDAO locationDAO = new LocationDAO(configuration.locationsConfiguration)
        final LocationUtil locationUtil = new LocationUtil(configuration.locationsConfiguration)
        final ExtensionDAO extensionDAO =
                new ExtensionDAO(configuration.locationsConfiguration, locationUtil)
        final DiningDAO diningDAO =
                new DiningDAO(configuration.locationsConfiguration, locationUtil)
        final ArcGisDAO arcGisDAO =
                new ArcGisDAO(configuration.locationsConfiguration, locationUtil)

        environment.healthChecks().register("dining",
                new DiningHealthCheck(configuration.locationsConfiguration))
        environment.healthChecks().register("extension",
                new ExtensionHealthCheck(configuration.locationsConfiguration))
        environment.healthChecks().register("arcgis",
                new ArcGisHealthCheck(configuration.locationsConfiguration))

        environment.jersey().register(new LocationResource(null, diningDAO, locationDAO,
                extensionDAO, arcGisDAO))
        environment.jersey().register(new InfoResource(buildInfoManager.getInfo()))
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<AuthenticatedUser>()
                      .setAuthenticator(new BasicAuthenticator(configuration.getCredentialsList()))
                      .setRealm('LocationApplication')
                      .buildAuthFilter()
        ))
        environment.jersey().register(new AuthValueFactoryProvider.Binder
                <AuthenticatedUser>(AuthenticatedUser.class))
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

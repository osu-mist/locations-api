package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.locations.db.CampusMapLocationDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.locations.resources.LocationResource
import edu.oregonstate.mist.locations.resources.SampleResource
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.basic.BasicAuthFactory
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.skife.jdbi.v2.DBI

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
    public void initialize(Bootstrap<LocationConfiguration> bootstrap) {}

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(LocationConfiguration configuration, Environment environment) {
        Resource.loadProperties('resource.properties')
        final DBIFactory factory = new DBIFactory()
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(),"jdbi")
        final CampusMapLocationDAO campusMapLocationDAO = jdbi.onDemand(CampusMapLocationDAO.class)
        final DiningDAO diningDAO = new DiningDAO(configuration.locationsConfiguration)
        final LocationDAO locationDAO = new LocationDAO(configuration.locationsConfiguration)
        final ExtensionDAO extensionDAO = new ExtensionDAO(configuration.locationsConfiguration)

        environment.jersey().register(new SampleResource())
        environment.jersey().register(new LocationResource(campusMapLocationDAO, diningDAO, locationDAO,
                extensionDAO))
        environment.jersey().register(
                AuthFactory.binder(
                        new BasicAuthFactory<AuthenticatedUser>(
                                new BasicAuthenticator(configuration.getCredentialsList()),
                                'LocationApplication',
                                AuthenticatedUser.class)))
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

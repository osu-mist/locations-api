package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.locations.resources.LocationResource
import edu.oregonstate.mist.locations.resources.SampleResource
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.skife.jdbi.v2.DBI

class LocationApplication extends Application<LocationConfiguration>{
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {}

    @Override
    public void run(LocationConfiguration configuration, Environment environment) {
        final DBIFactory factory = new DBIFactory()
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(),"jdbi")
        final LocationDAO locationDAO = jdbi.onDemand(LocationDAO.class)

        environment.jersey().register(new SampleResource())
        environment.jersey().register(new LocationResource(locationDAO))
    }

    public static void main(String[] arguments) throws Exception {
        new LocationApplication().run(arguments)
    }
}

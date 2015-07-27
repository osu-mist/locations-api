package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.resources.SampleResource
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class LocationApplication extends Application<Configuration>{
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {}

    @Override
    public void run(Configuration configuration, Environment environment) {
        environment.jersey().register(new SampleResource())
    }

    public static void main(String[] arguments) throws Exception {
        new LocationApplication().run(arguments)
    }
}

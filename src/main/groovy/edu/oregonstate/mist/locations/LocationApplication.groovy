package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.locations.LocationCommand
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
@groovy.transform.TypeChecked
class LocationApplication extends Application<LocationConfiguration> {
    @Override
    public void initialize(Bootstrap bootstrap) {
        super.initialize(bootstrap)
        bootstrap.addCommand(new LocationCommand(this))
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

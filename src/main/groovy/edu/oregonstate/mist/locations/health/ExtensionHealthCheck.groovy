package edu.oregonstate.mist.locations.health

import com.codahale.metrics.health.HealthCheck.Result

/**
 * Verify that DW can access the Extension datasource
 */
class ExtensionHealthCheck extends LocationHealthCheck {
    ExtensionHealthCheck(Map<String, String> locationConfiguration) {
        super(locationConfiguration)
    }

    @Override
    protected Result check() {
        checkUrl(locationConfiguration.get("extensionUrl"))
    }
}

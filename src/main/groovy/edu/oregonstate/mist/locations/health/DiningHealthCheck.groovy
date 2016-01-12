package edu.oregonstate.mist.locations.health

import com.codahale.metrics.health.HealthCheck.Result

/**
 * Verify that DW can access the Dining datasource
 */
class DiningHealthCheck extends LocationHealthCheck {
    DiningHealthCheck(Map<String, String> locationConfiguration) {
        super(locationConfiguration)
    }

    @Override
    protected Result check() {
        checkUrl(locationConfiguration.get("uhdsUrl"))
    }
}

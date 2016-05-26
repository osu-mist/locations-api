package edu.oregonstate.mist.locations.health

import com.codahale.metrics.health.HealthCheck.Result

/**
 * Verify connection to arcgis
 */
class ArcGisHealthCheck extends LocationHealthCheck {
    ArcGisHealthCheck(Map<String, String> locationConfiguration) {
        super(locationConfiguration)
    }

    @Override
    protected Result check() {
        checkUrl(locationConfiguration.get("uhdsUrl"))
    }
}

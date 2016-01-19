package edu.oregonstate.mist.locations.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

class LocationHealthCheck extends HealthCheck {
    final Map<String, String> locationConfiguration

    LocationHealthCheck(Map<String, String> locationConfiguration) {
        this.locationConfiguration = locationConfiguration
    }

    protected Result check() {
        Result.unhealthy("default health check needs to be overwritten")
    }

    /**
     * Verify that a url is accessible by DW and doesn't return an
     * empty string.
     *
     * @param url
     * @return
     */
    protected Result checkUrl(String url) {
        try {
            String urlText = new URL(url).text

            if (urlText) {
                Result.healthy()
            } else {
                Result.unhealthy("Content of url: (${url}) was empty or null")
            }
        } catch(Exception e) {
            Result.unhealthy(e.message)
        }
    }
}

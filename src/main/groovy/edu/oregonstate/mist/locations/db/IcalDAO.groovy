package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationConfiguration
import edu.oregonstate.mist.locations.LocationUtil

class IcalDAO {
    protected static final ObjectMapper MAPPER = new ObjectMapper()
    LocationUtil locationUtil
    LocationConfiguration configuration

    IcalDAO(LocationConfiguration configuration, LocationUtil locationUtil) {
        this.configuration = configuration
        this.locationUtil = locationUtil
    }

    String getMetadataURL() {
        this.configuration.locationsConfiguration.get("uhdsUrl")
    }

    String getIcalURL() {
        this.configuration.locationsConfiguration.get("icalUrl")
    }

    String getJsonOut() {
        this.configuration.locationsConfiguration.get("jsonOut")
    }
}

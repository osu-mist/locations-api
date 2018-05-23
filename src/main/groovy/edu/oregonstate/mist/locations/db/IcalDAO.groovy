package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationConfiguration
import edu.oregonstate.mist.locations.Cache

class IcalDAO {
    protected static final ObjectMapper MAPPER = new ObjectMapper()
    Cache cache
    LocationConfiguration configuration

    IcalDAO(LocationConfiguration configuration, Cache cache) {
        this.configuration = configuration
        this.cache = cache
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

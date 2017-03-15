package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil

class IcalDAO {
    protected static final ObjectMapper MAPPER = new ObjectMapper()

    String icalURL
    String jsonOut
    LocationUtil locationUtil
    String metadataURL

    IcalDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        metadataURL = locationConfiguration.get("uhdsUrl")
        icalURL = locationConfiguration.get("icalUrl")
        jsonOut = locationConfiguration.get("jsonOut")
        this.locationUtil = locationUtil
    }
}

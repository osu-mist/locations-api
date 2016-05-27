package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ArcGisLocation

class ArcGisDAO {
    /**
     * Url of ArcGIS API. JSON formatted content that includes bldID,
     * bldNam, bldNamAbr, latitude, longitude
     */
    private final String arcGisQueryUrl

    /**
     * File where the arcgis data is downloaded to
     */
    private final String arcGisJsonOut

    /**
     * Helper for caching and getting data from web requests
     */
    private final LocationUtil locationUtil

    public ArcGisDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        arcGisQueryUrl = locationConfiguration.get("arcGisQueryUrl")
        arcGisJsonOut = locationConfiguration.get("arcGisJsonOut")
        this.locationUtil = locationUtil
    }

    /**
     * Retrieves locations data from arcgis
     *
     * @return
     */
    HashMap<String, ArcGisLocation> getArcGisLocations() {
        String gisData = getArcGisLocationList()

        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        def data = [:]
        def features = mapper.readTree(gisData).get("features")

        features.asList().each {
            def arcBuilding = new ArcGisLocation(mapper.readValue(it.get("properties").toString(), Object.class))
            data[arcBuilding.bldNamAbr] = arcBuilding
        }

        data
    }

    private String getArcGisLocationList() throws Exception{
        locationUtil.getDataFromUrlOrCache(arcGisQueryUrl, arcGisJsonOut)
    }
}

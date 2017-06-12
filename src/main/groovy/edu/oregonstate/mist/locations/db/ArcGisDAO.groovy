package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.health.ArcGisHealthCheck

class ArcGisDAO {

    private ObjectMapper mapper = new ObjectMapper()

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
     * Url of ArcGIS API. JSON formatted content that includes bldID,
     * bldNam, bldNamAbr, latitude, longitude
     */
    private final String arcGisGenderInclusiveRRUrl

    /**
     * File where the arcgis data is downloaded to
     */
    private final String arcGisGenderInclusiveRRJsonOut

    /**
     * Helper for caching and getting data from web requests
     */
    private final LocationUtil locationUtil

    public ArcGisDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        arcGisQueryUrl = locationConfiguration.get("arcGisQueryUrl")
        arcGisJsonOut = locationConfiguration.get("arcGisJsonOut")
        arcGisGenderInclusiveRRUrl = locationConfiguration.get("arcGisGenderInclusiveRR")
        arcGisGenderInclusiveRRJsonOut = locationConfiguration.get("arcGisGenderInclusiveJsonOut")
        this.locationUtil = locationUtil
    }

    HashMap<String, ArcGisLocation> getMergedArcGisData() {
        HashMap<String, ArcGisLocation> centroidData = getArcGisData(
                arcGisQueryUrl,
                arcGisJsonOut,
                "properties"
        )
        HashMap<String, ArcGisLocation> genderInclusiveRRData = getArcGisData(
                arcGisGenderInclusiveRRUrl,
                arcGisGenderInclusiveRRJsonOut,
                "attributes"
        )
        genderInclusiveRRData.each {id, value ->
            if (centroidData[id]) {
                centroidData[id].giRestroomCount = value.giRestroomCount
                centroidData[id].giRestroomLimit = value.giRestroomLimit
                centroidData[id].giRestroomLocations = value.giRestroomLocations
            }
        }

        centroidData
    }

    private def getArcGisData(String url, String output, String key) throws Exception{
        String gisData = locationUtil.getDataFromUrlOrCache(url, output)
        def mappedData = mapper.readTree(gisData).get("features")
        def data = [:]

        mappedData.asList().each {
            def arcBuilding = new ArcGisLocation(mapper.readValue(it.get(key).toString(),
                    Object.class))
            String bldNamIdHash = locationUtil.getMD5Hash(arcBuilding.bldID + arcBuilding.bldNam)
            data[bldNamIdHash] = arcBuilding
        }

        data
    }
}

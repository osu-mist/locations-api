package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ArcGisLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArcGisDAO {

    Logger logger = LoggerFactory.getLogger(ArcGisDAO.class)

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

    HashMap<String, ArcGisLocation> getCentroidData() {
        getArcGisData(arcGisQueryUrl, arcGisJsonOut, "properties")
    }

    HashMap<String, ArcGisLocation> getGenderInclusiveRR() {
        getArcGisData(arcGisGenderInclusiveRRUrl, arcGisGenderInclusiveRRJsonOut, "attributes")
    }

    /**
     * Calls method to get JSON data from URL and maps response to an object.
     * Iterates through features of an ARCGIS response and creates a key & value map
     * based on a hash of the building ID and building name.
     */
    private def getArcGisData(String url, String output, String key) throws Exception{
        String gisData = locationUtil.getDataFromUrlOrCache(url, output)
        def mappedData = mapper.readTree(gisData).get("features")
        def data = [:]

        mappedData.asList().each {
            def arcBuilding = new ArcGisLocation(mapper.readValue(it.get(key).toString(),
                    Object.class))
            data[arcBuilding.bldNamAbr] = arcBuilding
        }

        data
    }
}

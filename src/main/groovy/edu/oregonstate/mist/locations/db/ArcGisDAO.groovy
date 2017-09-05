package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArcGisDAO {

    Logger logger = LoggerFactory.getLogger(ArcGisDAO.class)

    private ObjectMapper mapper = new ObjectMapper()

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
        arcGisGenderInclusiveRRUrl = locationConfiguration.get("arcGisGenderInclusiveRR")
        arcGisGenderInclusiveRRJsonOut = locationConfiguration.get("arcGisGenderInclusiveJsonOut")
        this.locationUtil = locationUtil
    }

    HashMap<String, GenderInclusiveRRLocation> getGenderInclusiveRR() {
        getArcGisData()
    }

    /**
     * Calls method to get JSON data from URL and maps response to an object.
     * Iterates through features of an ARCGIS response and creates a key & value map
     * based on a hash of the building ID and building name.
     */
    private def getArcGisData() throws Exception{
        String gisData = locationUtil.getDataFromUrlOrCache(arcGisGenderInclusiveRRUrl,
                arcGisGenderInclusiveRRJsonOut)
        def mappedData = mapper.readTree(gisData).get("features")
        def data = [:]

        mappedData.asList().each {
            def arcBuilding = new GenderInclusiveRRLocation(mapper.readValue(it.get("attributes")
                    .toString(), Object.class))
            data[arcBuilding.bldID] = arcBuilding
        }

        data
    }
}

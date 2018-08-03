package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TypeChecked
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
    private static final String CACHE_FILENAME = "gender-inclusive-rr.json"

    private final int ARC_GIS_THRESHOLD

    /**
     * Helper for caching and getting data from web requests
     */
    private final Cache cache

    public ArcGisDAO(Map<String, String> locationConfiguration, Cache cache) {
        arcGisGenderInclusiveRRUrl = locationConfiguration.get("arcGisGenderInclusiveRR")
        ARC_GIS_THRESHOLD = locationConfiguration.get("arcGisThreshold").toInteger()
        this.cache = cache
    }

    public HashMap<String, GenderInclusiveRRLocation> getGenderInclusiveRR() {
        //@todo we should expect application/json, but arcgis actually returns text/plain. oops
        cache.withDataFromUrlOrCache(arcGisGenderInclusiveRRUrl, CACHE_FILENAME) {
            String gisData -> mapRR(gisData)
        }
    }

    HashMap<String, GenderInclusiveRRLocation> mapRR(String gisData) {
        def mappedData = mapper.readTree(gisData).get("features")
        def data = new HashMap<String, GenderInclusiveRRLocation>()
        LocationUtil.checkThreshold(mappedData.size(),
                ARC_GIS_THRESHOLD, "gender inclusive restrooms")
        mappedData.asList().each {
            def rr = mapper.readValue(it.get("attributes").toString(), GenderInclusiveRRLocation)
            data[rr.bldID] = rr
        }

        data
    }
}

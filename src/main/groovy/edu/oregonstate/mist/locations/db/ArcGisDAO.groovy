package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.Cache
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
    private final String arcGisGenderInclusiveRRJsonOut

    private static final int ARCGIS_THRESHOLD = 30

    /**
     * Helper for caching and getting data from web requests
     */
    private final Cache cache

    public ArcGisDAO(Map<String, String> locationConfiguration, Cache cache) {
        arcGisGenderInclusiveRRUrl = locationConfiguration.get("arcGisGenderInclusiveRR")
        arcGisGenderInclusiveRRJsonOut = locationConfiguration.get("arcGisGenderInclusiveJsonOut")
        this.cache = cache
    }

    public HashMap<String, GenderInclusiveRRLocation> getGenderInclusiveRR() {
        //@todo we should expect application/json, but arcgis actually returns text/plain. oops
        cache.withDataFromUrlOrCache(arcGisGenderInclusiveRRUrl,
                arcGisGenderInclusiveRRJsonOut) {
            String gisData -> mapRR(gisData)
        }
    }

    HashMap<String, GenderInclusiveRRLocation> mapRR(String gisData) {
        def mappedData = mapper.readTree(gisData).get("features")
        def data = new HashMap<String, GenderInclusiveRRLocation>()

        int numFound = mappedData.size()

        if (numFound < ARCGIS_THRESHOLD) {
            throw new DAOException("Found ${numFound} gender inclusive restrooms." +
                    " Not sufficient with threshold of ${ARCGIS_THRESHOLD}")
        }

        mappedData.asList().each {
            def rr = mapper.readValue(it.get("attributes").toString(), GenderInclusiveRRLocation)
            data[rr.bldID] = rr
        }

        data
    }
}

package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.core.CampusMapLocation
import groovy.json.JsonSlurper

class CampusMapDAO {

    private final String campusMapJsonUrl

    private final String campusMapJsonOut

    private final int CAMPUS_MAP_THRESHOLD

    private final Cache cache

    CampusMapDAO(Map<String, String> locationConfiguration, Cache cache) {
        campusMapJsonUrl = locationConfiguration.get("campusMapHttpData")
        campusMapJsonOut = locationConfiguration.get("campusmapJsonOut")
        CAMPUS_MAP_THRESHOLD = locationConfiguration.get("campusMapThreshold").toInteger()
        this.cache = cache
    }

    /**
     * Get hashmap of campus map locations, mapped by ID
     * @return
     */
    public HashMap<String, CampusMapLocation> getCampusMapLocations() {
        List<CampusMapLocation> campusMapLocationList = getCampusMapJson()
        HashMap<String, CampusMapLocation> campusMapLocationHashMap = [:]

        campusMapLocationList.each {
            campusMapLocationHashMap[it.id] = it
        }

        campusMapLocationHashMap
    }

    /**
     * Get List of campus map locations from json via http request
     * @return
     */
    private List<CampusMapLocation> getCampusMapJson() {
        def jsonSlurper = new JsonSlurper()
        cache.withJsonFromUrlOrCache(campusMapJsonUrl, campusMapJsonOut) {
            campusMapData ->
                def locations = jsonSlurper.parseText(campusMapData) as List<CampusMapLocation>
                int numFound = locations.size()
                if (numFound < CAMPUS_MAP_THRESHOLD) {
                    throw new DAOException("Found ${numFound} campus map locations. " +
                            "Not sufficient with threshold of ${CAMPUS_MAP_THRESHOLD}")
                }
                locations
        }
    }
}

package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.core.CampusMapLocation
import groovy.json.JsonSlurper

class CampusMapDAO {

    private final String campusMapJsonUrl

    private final String campusMapJsonOut

    private final Cache cache

    CampusMapDAO(Map<String, String> locationConfiguration, Cache cache) {
        campusMapJsonUrl = locationConfiguration.get("campusMapHttpData")
        campusMapJsonOut = locationConfiguration.get("campusmapJsonOut")
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
        String campusMapData = cache.getJsonFromUrlOrCache(
                campusMapJsonUrl,
                campusMapJsonOut)

        jsonSlurper.parseText(campusMapData) as List<CampusMapLocation>
    }
}

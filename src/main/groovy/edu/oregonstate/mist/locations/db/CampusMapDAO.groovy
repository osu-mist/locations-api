package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.CampusMapLocation
import groovy.json.JsonSlurper
import groovy.transform.PackageScope

class CampusMapDAO {

    private final String campusMapJsonUrl

    private static final String CACHE_FILENAME = "campusmap.json"

    private final int CAMPUS_MAP_THRESHOLD

    private JsonSlurper jsonSlurper = new JsonSlurper()

    private final Cache cache

    CampusMapDAO(Map<String, String> locationConfiguration, Cache cache) {
        campusMapJsonUrl = locationConfiguration.get("campusMapHttpData")
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
    List<CampusMapLocation> getCampusMapJson() {
        cache.withJsonFromUrlOrCache(campusMapJsonUrl, CACHE_FILENAME) {
            String campusMapData -> parseCampusMaps(campusMapData)
        }
    }

    @PackageScope
    List<CampusMapLocation> parseCampusMaps(String campusMapData) {
        def locations = jsonSlurper.parseText(campusMapData) as List<CampusMapLocation>
        LocationUtil.checkThreshold(locations.size(), CAMPUS_MAP_THRESHOLD, "campus map locations")
        locations
    }
}

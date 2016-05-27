package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.mapper.LocationMapper

class LocationDAO {
    private final LocationMapper locationMapper
    private final Map<String, String> locationConfiguration
    private ObjectMapper mapper

    public LocationDAO(Map<String, String> locationConfiguration) {
        mapper = new ObjectMapper()
        this.locationConfiguration = locationConfiguration

        locationMapper = new LocationMapper(
                campusmapUrl: locationConfiguration.get("campusmapUrl"),
                campusmapImageUrl: locationConfiguration.get("campusmapImageUrl"),
                apiEndpointUrl: locationConfiguration.get("apiEndpointUrl")
        )
    }

    /**
     * Retrieves campusmap data from json file.
     *
     * @return List<CampusMapLocation>
     */
    public List<CampusMapLocation> getCampusMapFromJson() {
        try {
            def mapData = getMapJsonFile().getText()
            mapper.readValue(mapData, new TypeReference<List<CampusMapLocation>>() {})
        } catch(Exception e) {
            println e
        }
    }

    /**
     * Takes campusmap data and writes it to json file
     *
     * @param campusMapLocations
     * @return
     */
    public writeMapToJson(List<CampusMapLocation> campusMapLocations) {
        def jsonESInput = getMapJsonFile()
        def jsonStringList = campusMapLocations.collect { mapper.writeValueAsString(it) }

        jsonESInput.write("[" +  jsonStringList.join(",") + "]")
    }

    /**
     * Gets map json filename from configuration and returns file object
     *
     * @return
     */
    private File getMapJsonFile() {
        new File(locationConfiguration.get("campusmapJsonOut"))
    }

    /**
     * Takes arcgis and merges it with campusmap data. Arcgis data overwrites map data. If a
     * building is in the map data, but not in arcgis it is not returned.
     *
     * @param arcGisLocations
     * @param campusMapLocationList
     * @return
     */
    public static ArrayList mergeMapAndArcgis(HashMap<String, ArcGisLocation> arcGisLocations,
                                              List<CampusMapLocation> campusMapLocationList) {
        def mapData = [:]
        campusMapLocationList.each {
            mapData[it.abbrev] = it
        }

        def mergedData = []
        arcGisLocations.each {
            if (mapData[it.key]) {
                mapData[it.key].name = it.value.bldNam
                mapData[it.key].latitude = it.value.latitude
                mapData[it.key].longitude = it.value.longitude

                mergedData += mapData[it.key]
            } else {
                mergedData += it.value
            }
        }

        mergedData
    }

    /**
     * Converts location objects (dining, campusmap, arcgis or extension) to a
     *  resource objects.
     *
     * @param locations
     * @return
     */
    public ResourceObject convert(def location) {
        locationMapper.map(location)
    }
}

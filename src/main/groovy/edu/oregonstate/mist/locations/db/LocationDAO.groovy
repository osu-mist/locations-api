package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.CampusMapLocationDeprecated
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.Geometry
import edu.oregonstate.mist.locations.mapper.LocationMapper
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LocationDAO {

    Logger logger = LoggerFactory.getLogger(LocationDAO.class)

    private final LocationMapper locationMapper
    private ObjectMapper mapper
    private File mapJsonFile
    private File geometriesJsonFile

    public LocationDAO(Map<String, String> locationConfiguration) {
        mapper = new ObjectMapper()
        locationMapper = new LocationMapper(
                campusmapUrl: locationConfiguration.get("campusmapUrl"),
                campusmapImageUrl: locationConfiguration.get("campusmapImageUrl"),
                apiEndpointUrl: locationConfiguration.get("apiEndpointUrl")
        )
        mapJsonFile = new File(locationConfiguration.get("campusmapJsonOut"))
        geometriesJsonFile = new File(locationConfiguration.get("geometries"))
    }

    /**
     * Retrieves ARCGIS geometry data from json file and merges with ARCGIS centroid data.
     *
     * @return HashMap<String, ArcGisLocation>
     */
    public HashMap<String, Geometry> getArcGisGeometries() {
        def jsonSlurper = new JsonSlurper()
        def arcGisGeometries = jsonSlurper.parseText(geometriesJsonFile.getText())
        HashMap<String, Geometry> geometryHashMap = [:]

        arcGisGeometries['features'].each {
            geometryHashMap[it['properties']['BldNamAbr'].toString()] = new Geometry(
                    type: it['geometry']['type'],
                    coordinates: it['geometry']['coordinates'])
        }

        geometryHashMap
    }

    /**
     * Retrieves campusmap data from json file.
     *
     * @return List<CampusMapLocationDeprecated>
     */
    public List<CampusMapLocationDeprecated> getCampusMapFromJson() {
        try {
            def mapData = mapJsonFile.getText()
            mapper.readValue(mapData, new TypeReference<List<CampusMapLocationDeprecated>>() {})
        } catch (FileNotFoundException) {
            null
        }
    }

    /**
     * Takes campusmap data and writes it to json file
     *
     * @param campusMapLocations
     * @return
     */
    public writeMapToJson(List<CampusMapLocationDeprecated> campusMapLocations) {
        def jsonESInput = mapJsonFile
        def jsonStringList = campusMapLocations.collect { mapper.writeValueAsString(it) }

        jsonESInput.write("[" +  jsonStringList.join(",") + "]")
    }

    /**
     * Takes arcgis and merges it with campusmap data. Arcgis data overwrites map data.
     * If a building is in the map data, but not in arcgis it is not returned.
     * Two buildings are considered the same if the abbrev field of the
     * CampusMapLocationDeprecated matches the bldNamAbr field of the ArcGisLocation.
     *
     * @param buildings
     * @param campusMapLocationList
     * @return
     */
    @Deprecated
    public static ArrayList mergeMapAndBuildingsDeprecated(
            HashMap<String, FacilLocation> buildings,
            List<CampusMapLocationDeprecated> campusMapLocationList) {
        def mapData = [:]
        campusMapLocationList.each {
            mapData[it.abbrev] = it
        }

        def mergedData = []
        buildings.each {
            if (mapData[it.key]) {
                mapData[it.key].name = it.value.name
                mapData[it.key].latitude = it.value.latitude
                mapData[it.key].longitude = it.value.longitude
                mapData[it.key].coordinates = it.value.coordinates
                mapData[it.key].coordinatesType = it.value.coordinatesType
                mapData[it.key].giRestroomCount = it.value.giRestroomCount
                mapData[it.key].giRestroomLimit = it.value.giRestroomLimit
                mapData[it.key].giRestroomLocations = it.value.giRestroomLocations

                mergedData += mapData[it.key]
            } else {
                mergedData += it.value
            }
        }

        mergedData
    }

    /**
     * Merge multiple arcGis datasources into FacilLocations
     * @param buildings
     * @param centroids
     * @param genderInclusiveRR
     * @param geometries
     * @return
     */
    public static Map mergeFacilAndArcGis(List<FacilLocation> buildings,
                                          HashMap<String, ArcGisLocation> centroids,
                                          HashMap<String, ArcGisLocation> genderInclusiveRR,
                                          HashMap<String, Geometry> geometries) {
        HashMap<String, FacilLocation> facilLocationHashMap = new HashMap<String, FacilLocation>()

        buildings.each {
            facilLocationHashMap[it.abbreviation] = it
        }

        facilLocationHashMap.each { key, building ->
            if (centroids[key]) {
                facilLocationHashMap[key].latitude = centroids[key].latitude
                facilLocationHashMap[key].longitude = centroids[key].longitude
            }
            if (genderInclusiveRR[key]) {
                facilLocationHashMap[key].giRestroomCount =
                        genderInclusiveRR[key].giRestroomCount
                facilLocationHashMap[key].giRestroomLimit =
                        genderInclusiveRR[key].giRestroomLimit
                facilLocationHashMap[key].giRestroomLocations =
                        genderInclusiveRR[key].giRestroomLocations
            }
            if (geometries[key]) {
                facilLocationHashMap[key].coordinates = geometries[key].coordinates
                facilLocationHashMap[key].coordinatesType =
                        geometries[key].type
            }
        }

        facilLocationHashMap
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

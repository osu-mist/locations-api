package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
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
    public HashMap<String, ArcGisLocation> getArcGisCoordinates() {
        def jsonSlurper = new JsonSlurper()
        def arcJson = jsonSlurper.parseText(geometriesJsonFile.getText())
        HashMap<String, ArcGisLocation> arcHashMap = [:]

        arcJson['features'].each {
            arcHashMap[it['properties']['BldID'].toString()] = new ArcGisLocation(it)
        }

        arcHashMap
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
                                          Map<String, GenderInclusiveRRLocation>
                                                  genderInclusiveRR,
                                          Map<String, ArcGisLocation> geometries) {
        HashMap<String, FacilLocation> facilLocationHashMap = new HashMap<String, FacilLocation>()

        buildings.each {
            facilLocationHashMap[it.bldgID] = it
        }

        facilLocationHashMap.each { key, building ->
            if (genderInclusiveRR[key]) {
                facilLocationHashMap[key].giRestroomCount =
                        genderInclusiveRR[key].giRestroomCount
                facilLocationHashMap[key].giRestroomLimit =
                        genderInclusiveRR[key].giRestroomLimit
                facilLocationHashMap[key].giRestroomLocations =
                        genderInclusiveRR[key].giRestroomLocations
            }
            if (geometries[key]) {
                facilLocationHashMap[key].latitude = geometries[key].latitude
                facilLocationHashMap[key].longitude = geometries[key].longitude
                facilLocationHashMap[key].coordinates = geometries[key].coordinates
                facilLocationHashMap[key].coordinatesType =
                        geometries[key].coordinatesType
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

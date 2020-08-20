package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.AedInventoriesLocation
import edu.oregonstate.mist.locations.core.AdaEntriesLocation
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import edu.oregonstate.mist.locations.core.ParkingLocation
import edu.oregonstate.mist.locations.mapper.LocationMapper
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LocationDAO {

    Logger logger = LoggerFactory.getLogger(LocationDAO.class)

    private final LocationMapper locationMapper
    private ObjectMapper mapper
    private File buildingGeometriesJsonFile
    private File parkingGeometriesJsonFile

    def jsonSlurper = new JsonSlurper()

    public LocationDAO(Map<String, String> locationConfiguration) {
        mapper = new ObjectMapper()
        locationMapper = new LocationMapper(
                apiEndpointUrl: locationConfiguration.get("apiEndpointUrl")
        )
        buildingGeometriesJsonFile = new File(locationConfiguration.get("buildingGeometries"))
        parkingGeometriesJsonFile = new File(locationConfiguration.get("parkingGeometries"))
    }

    /**
     * Retrieves ARCGIS geometry data from json file and merges with ARCGIS centroid data.
     *
     * @return HashMap<String, ArcGisLocation>
     */
    public HashMap<String, ArcGisLocation> getArcGisCoordinates() {
        def arcJson = jsonSlurper.parseText(buildingGeometriesJsonFile.getText())
        HashMap<String, ArcGisLocation> arcHashMap = [:]

        arcJson['features'].each {
            def arcgis = ArcGisLocation.fromJson(it)
            arcHashMap[arcgis.bldID] = ArcGisLocation.fromJson(it)
        }

        arcHashMap
    }

    /**
     * Get parking locations from a static json file which includes their geometries
     * @return
     */
    public List<ParkingLocation> getParkingLocations() {
        def parkingJson = jsonSlurper.parseText(parkingGeometriesJsonFile.getText())
        List<ParkingLocation> parkingLocations = []
        def ignoredParking = []

        Closure<Boolean> isValidField = { def field ->
            field && (field.toString().trim().length() > 0)
        }

        parkingJson['features'].each {
            def properties = it['properties']
            def propID = properties['Prop_ID']
            def parkingZoneGroup = properties['ZoneGroup']

            Boolean isValidParkingZoneGroup = isValidField(parkingZoneGroup) &&
                    (parkingZoneGroup != "Non-Public")

            if (isValidField(propID) && isValidParkingZoneGroup) {
                parkingLocations.add(new ParkingLocation(it))
            } else {
                ignoredParking.add(properties['OBJECTID'])
            }
        }
        logger.warn("These parking lot OBJECTID's were ignored because they" +
                " don't have a valid Prop_ID or ZoneGroup: " + ignoredParking)

        parkingLocations
    }

    /**
     * Merge multiple arcGis datasources into FacilLocations
     * @param buildings
     * @param genderInclusiveRR
     * @param geometries
     * @return
     */
    public static Map<String, FacilLocation> mergeFacilAndArcGis(
            List<FacilLocation> buildings,
            Map<String, GenderInclusiveRRLocation> genderInclusiveRR,
            Map<String, ArcGisLocation> geometries,
            Map<String, AdaEntriesLocation> adaEntries,
            Map<String, AedInventoriesLocation> aedInventories
    ) {
        def facilLocationHashMap = new HashMap<String, FacilLocation>()

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
            if (adaEntries[key]) {
                facilLocationHashMap[key].adaEntries = adaEntries[key].adaEntries
            }

            if (aedInventories[key]) {
                facilLocationHashMap[key].aedInventories = aedInventories[key].aedInventories
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

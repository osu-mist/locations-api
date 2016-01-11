package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.mapper.LocationMapper

class LocationDAO {
    private final LocationMapper locationMapper

    public LocationDAO(Map<String, String> locationConfiguration) {
        locationMapper = new LocationMapper(
                campusmapUrl: locationConfiguration.get("campusmapUrl"),
                campusmapImageUrl: locationConfiguration.get("campusmapImageUrl")
        )
    }

    //@todo: not DRY :(
    public def convertCampusmap(List<CampusMapLocation> campusMapLocations) {
        def resourceObjects = new ArrayList<ResourceObject>()

        campusMapLocations.each {
            resourceObjects.add(locationMapper.map(it))
        }

        resourceObjects
    }

    public def convertDining(List<DiningLocation> diningLocations) {
        def resourceObjects = new ArrayList<ResourceObject>()

        diningLocations.each {
            resourceObjects.add(locationMapper.map(it))
        }

        resourceObjects
    }

    public def convertExtension(List<ExtensionLocation> extensionLocations) {
        def resourceObjects = new ArrayList<ResourceObject>()

        extensionLocations.each {
            resourceObjects.add(locationMapper.map(it))
        }

        resourceObjects
    }
}

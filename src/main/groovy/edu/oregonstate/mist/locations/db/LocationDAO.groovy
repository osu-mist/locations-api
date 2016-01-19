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

    /**
     * Converts the location objects (dining, campusmap or extension) to a
     * list of resource objects.
     *
     * @param locations
     * @return
     */
    public List<ResourceObject> convert(List locations) {
        List<ResourceObject> resourceObjects = new ArrayList<ResourceObject>()
        locations.each {
            resourceObjects.add(locationMapper.map(it))
        }
        resourceObjects
    }
}

package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.CampusMapLocation
import org.junit.Test
import edu.oregonstate.mist.locations.mapper.LocationMapper

@groovy.transform.TypeChecked
class LocationMapperTest {
    private final LocationMapper locationMapper = new LocationMapper(
            campusmapImageUrl: "www.example.com",
            campusmapUrl: "www.examplemap.com",
            apiEndpointUrl: "www.api.example.com"
    )

    @Test
    public void testGenderInclusiveRRLimit() {
        CampusMapLocation campusMapLocation = new CampusMapLocation(
                giRestroomLimit: "This should be true"
        )
        ResourceObject resourceObject = locationMapper.map(campusMapLocation)
        assert resourceObject.attributes['giRestroomLimit'] == true

        campusMapLocation.giRestroomLimit = " "
        resourceObject = locationMapper.map(campusMapLocation)
        assert resourceObject.attributes['giRestroomLimit'] == false

        campusMapLocation.giRestroomLimit = ""
        resourceObject = locationMapper.map(campusMapLocation)
        assert resourceObject.attributes['giRestroomLimit'] == false

        campusMapLocation.giRestroomLimit = null
        resourceObject = locationMapper.map(campusMapLocation)
        assert resourceObject.attributes['giRestroomLimit'] == null
    }
}

package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.mapper.LocationMapper
import org.junit.Test

@groovy.transform.TypeChecked
class LocationMapperTest {
    private final LocationMapper locationMapper = new LocationMapper(
            //campusmapImageUrl: "www.example.com",
            //campusmapUrl: "www.examplemap.com",
            apiEndpointUrl: "www.api.example.com"
    )

    @Test
    public void testGenderInclusiveRRLimit() {
        def loc = new FacilLocation(
                giRestroomLimit: "This should be true"
        )
        ResourceObject resourceObject = locationMapper.map(loc)
        assert resourceObject.attributes['giRestroomLimit'] == true

        loc.giRestroomLimit = " "
        resourceObject = locationMapper.map(loc)
        assert resourceObject.attributes['giRestroomLimit'] == false

        loc.giRestroomLimit = ""
        resourceObject = locationMapper.map(loc)
        assert resourceObject.attributes['giRestroomLimit'] == false

        loc.giRestroomLimit = null
        resourceObject = locationMapper.map(loc)
        assert resourceObject.attributes['giRestroomLimit'] == null
    }
}

package edu.oregonstate.mist.locations.resources

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.GeoLocation
import edu.oregonstate.mist.locations.db.LibraryDAO
import groovy.transform.TypeChecked
import org.junit.Test;

import static org.junit.Assert.*;

@TypeChecked
class MergeUtilTest {
    // CO-942
    @Test
    void testLibraryHours() {
        def libraryDAO = new MockLibraryDAO()
        def mergeUtil = new MergeUtil(libraryDAO, null, null)
        def data = [new ResourceObject(
                id: "d409d908ecc6010a04a3b0387f063145",
                type: "locations",
                attributes: new Attributes(
                        name: "The Valley Library",
                        abbreviation: "VLib",
                        bldgID: "0036",
                        tags: [],
                        openHours: [:],
                        type: "building",
                        summary: "",
                        description: "\r\n\tAccessibility:\r\n\r\n\r\n\tENTRIES: North entry: level to 2nd floor.\r\n\tFLOORS: All floors; elevator; access north section power-assisted doors of 1st through Reserve Book staff area.\r\n",
                        address: "201 SW Waldo Place",
                        city: "CORVALLIS",
                        state: "OR",
                        zip: "97331",
                        thumbnails: [
                                "https://map.oregonstate.edu/sites/map.oregonstate.edu/files/styles/thumbnail/public/locations/valley-library.jpg"
                        ],
                        images: [
                                "https://map.oregonstate.edu/sites/map.oregonstate.edu/files/locations/valley-library.jpg"
                        ],
                        website: "https://map.oregonstate.edu/?id=d409d908ecc6010a04a3b0387f063145",
                        campus: "Corvallis",
                        giRestroomCount: 3,
                        giRestroomLimit: false,
                        giRestroomLocations: "1565, 3565, 5565",
                        synonyms: ["books"],
                        geoLocation: new GeoLocation(lat: 44.565066d, lon: -123.276147d),
                )
        )]
        data = mergeUtil.populate(data)
        assert data[0].id == "d409d908ecc6010a04a3b0387f063145"
        assert data[0].attributes instanceof Attributes
        assert ((Attributes)data[0].attributes).openHours[0] == [new DayOpenHours(
                start: new Date(2010, 10, 02, 7, 0, 0),
                end: new Date(2010, 10, 02, 17, 0, 0))]
    }

    class MockLibraryDAO extends LibraryDAO {
        MockLibraryDAO(){
            super([:], null)
        }

        @Override
        public Map<Integer, List<DayOpenHours>> getLibraryHours() {
            [(0): [new DayOpenHours(
                    start: new Date(2010, 10, 02, 7, 0, 0),
                    end: new Date(2010, 10, 02, 17, 0, 0))]]
        }
    }
}
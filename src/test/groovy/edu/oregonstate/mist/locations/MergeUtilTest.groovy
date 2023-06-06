package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.GeoLocation
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.LibraryDAO
import groovy.mock.interceptor.MockFor
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class MergeUtilTest {
    // CO-942
    @Test
    void testLibraryHours() {
        def libraryDAO = new MockLibraryDAO()
        def mergeUtil = new MergeUtil(libraryDAO, null, null, 0)
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
                        description: "\r\n\tAccessibility:\r\n\r\n\r\n\tENTRIES: North entry: ...",
                        address: "201 SW Waldo Place",
                        city: "CORVALLIS",
                        state: "OR",
                        zip: "97331",
                        thumbnails: [
                                "https://map.oregonstate.edu/sites/map.oregonstate.edu/files/"
                                    + "styles/thumbnail/public/locations/valley-library.jpg"
                        ],
                        images: [
                                "https://map.oregonstate.edu/sites/map.oregonstate.edu/files/"
                                    + "locations/valley-library.jpg"
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
        MockLibraryDAO() {
            super([:], null, null)
        }

        @Override
        public Map<Integer, List<DayOpenHours>> getLibraryHours() {
            [(0): [new DayOpenHours(
                    start: new Date(2010, 10, 02, 7, 0, 0),
                    end: new Date(2010, 10, 02, 17, 0, 0))]]
        }
    }

    def testCampusMapData = ["1": null, "2": null, "3": null]
    def testMergeUtilData = [new ResourceObject(id: "1"), new ResourceObject(id: "2")]
    CampusMapDAO campusMapDAO = new CampusMapDAO(
            ["campusMapHttpData": "test",
             "campusmapJsonOut": "test",
             "campusMapThreshold": "1"],
            new Cache([:]))

    @Rule
    public ExpectedException exception = ExpectedException.none()

    // Ratio is satisfied
    // @Test
    // void testMergeCampusMapData() {
    //     testCampusMapRatio(0.5)
    // }

    // Ratio is not satisfied
    // @Test
    // void testOverRatio() {
    //     exception.expect(Exception.class)
    //     exception.expectMessage("Missing campus maps locations ratio of")
    //     exception.expectMessage("not satisfied")
    //     testCampusMapRatio(0.1)
    // }

    /**
     * Attempts to merge campus map data with a given ratio
     *
     * @param testRatio
     */
    void testCampusMapRatio(float testRatio) {
        def mockCampusMap = new MockFor(CampusMapDAO, true)
        mockCampusMap.demand.CampusMapDAO { campusMapDAO }
        mockCampusMap.demand.getCampusMapLocations { testCampusMapData }
        mockCampusMap.use {
            def campusMapInstance = new CampusMapDAO()
            def mergeUtil = new MergeUtil(null, null, campusMapInstance, testRatio)
            mergeUtil.mergeCampusMapData(testMergeUtilData)
        }
    }
}

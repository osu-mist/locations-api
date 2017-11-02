package edu.oregonstate.mist.locations

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import edu.oregonstate.mist.locations.db.LocationDAO

import org.junit.Test

@groovy.transform.TypeChecked
class LocationDAOTest {
    private ObjectMapper mapper = new ObjectMapper()

    @Test
    public void testMergeFacilAndArcgis_Messy() {
        // Locations with a matching abbrev and BldNamAbr are merged

        def campusmap = [
            new FacilLocation(
                    bldgID: "0032",
                    name: "Facil name",
                    abbreviation: "FOO",
                    address1: "Facil address",
                    address2: "Facil address 2",
                    campus: "Facil campus",
                    city: "Facil city",
                    state: "Facil state",
                    zip: "Facil zip",

                    longitude: "-1",
                    latitude: "1",
                    coordinates: [],
                    coordinatesType: "Facil coordinates",
            ),
        ]

        def arcgis = [
                "0032": new ArcGisLocation(
                        bldID: "0032",
                        bldNam: "Arcgis bldNam",
                        bldNamAbr: "FOO",
                        latitude: "42",
                        longitude: "-42",
                        coordinates: [],
                        coordinatesType: "Arcgis coordinates",
                )
        ]

        def girr = [
                "0032": new GenderInclusiveRRLocation(
                        bldID: "0032",
                        bldNam: "GIRR bldNam",
                        bldNamAbr: "GIRR bldNamAbr",
                        giRestroomCount: 3,
                        giRestroomLimit: "Only for residents!",
                        giRestroomLocations: "110, 210, 310"
                )
        ]

        def expected = [
            "0032": new FacilLocation(
                    bldgID: "0032",
                    name: "Facil name",
                    campus: "Facil campus",
                    abbreviation: "FOO",
                    longitude: "-42",
                    latitude: "42",
                    address1: "Facil address",
                    address2: "Facil address 2",
                    city: "Facil city",
                    state: "Facil state",
                    zip: "Facil zip",

                    coordinates: [],
                    coordinatesType: "Arcgis coordinates",

                    giRestroomCount: 3,
                    giRestroomLimit: "Only for residents!",
                    giRestroomLocations: "110, 210, 310"

            ),
        ]
        def actual = LocationDAO.mergeFacilAndArcGis(campusmap, girr, arcgis)

        assert actual == expected
    }

    @Test
    public void testJsonRoundTrip() {
        // Test that getCampusMapFromJson and writeMapToJson round trip
        /*

        File temp = File.createTempFile("campusmap", ".json")
        temp.deleteOnExit()
        try {
            def dao = new LocationDAO([
                campusmapJsonOut: temp.getPath(), geometries: "test"
            ])

            def campusmap = [
                new CampusMapLocationDeprecated(
                    id: 732,
                    name: "Marketplace West Dining Center",
                    abbrev: "WsDn",
                    longitude: "-123.2835840585",
                    latitude: "44.5639316626",
                    layerId: "",
                    layerNames: "Buildings Map",
                    address: "351 SW 30th Street",
                    adaEntrance: "",
                    shortDescription: "Marketplace West houses seven distinctive restaurant " +
                            "locations combining the cuisine of the world with traditional " +
                            "American favorites.  Marketplace West accepts UHDS Dining Dollars, " +
                            "OSU Card Cash, Visa and MasterCard, checks and cash.",
                    description: "<p>The cuisine of Marketplace West covers many global regions, " +
                            "designed in the style of an open air market. The seven restaurant " +
                            "locations offer fresh Latin, Pacific Rim fare, classic Oregon " +
                            "Natural Beef burgers and hand-dipped shakes, fresh salads and deli " +
                            "sandwiches, pizza, pasta, and coffee.</p><ul><li><a " +
                            "href=\"../../uhds/dining/menus/calabaloos.php\">Calabaloo's Gourmet " +
                            "Burgers™</a><br /></li><li><a " +
                            "href=\"../../uhds/dining/menus/clubhouse_deli.php\">Clubhouse Deli" +
                            "</a><br /></li><li><a href=\"../../uhds/dining/menus/ebgbs.php\">" +
                            "EBGB's</a> (Every Bean's a Good Bean) <br /></li><li>" +
                            "<a href=\"../../uhds/dining/menus/four_corners.php\">Four Corners" +
                            "</a><br /></li><li>" +
                            "<a href=\"../../uhds/dining/menus/ring_of_fire.php\">Ring of Fire" +
                            "</a><br /></li><li>" +
                            "<a href=\"../../uhds/dining/menus/serrano_grill.php\">Serrano Grill™" +
                            "</a><br /></li><li>" +
                            "<a href=\"../../uhds/dining/menus/tomassitos.php\">Tomassito's " +
                            "Italian Cafe</a><br /></li></ul><p>For hours of operation, please " +
                            "visit the <a href=\"../../foodatosu/hours/\">food @ OSU</a> " +
                            "website, and for more general housing information, please visit the " +
                            "<a href=\"../../uhds/\">Housing and Dining Services</a> website.</p>",
                    thumbnail: "wsdn002.jpg",
                    largerImage: "",
                ),
            ]

            // Round-trip
            dao.writeMapToJson(campusmap)
            def roundtrip = dao.getCampusMapFromJson()

            assert campusmap == roundtrip
        } finally {
            temp.delete()
        }
        */
    }
}

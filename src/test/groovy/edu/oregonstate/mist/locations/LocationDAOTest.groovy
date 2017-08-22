package edu.oregonstate.mist.locations

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.CampusMapLocationDeprecated
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.db.LocationDAO

import org.junit.Test

@groovy.transform.TypeChecked
class LocationDAOTest {
    private ObjectMapper mapper = new ObjectMapper()

    @Test
    public void testMergeMapAndArcgis_Simple() {
        // Empty lists merge to an empty list
        assert LocationDAO.mergeMapAndBuildingsDeprecated([:], []) == []

        // Empty arcgis, non-empty campusmap
        assert LocationDAO.mergeMapAndBuildingsDeprecated(
                [:], [new CampusMapLocationDeprecated(id: 101)]) == []

        // Non-empty arcgis, empty campusmap
        def arcgis = new ArcGisLocation(BldNamAbr: "FOO")
        assert arcgis.bldNamAbr != null
        assert LocationDAO.mergeMapAndBuildingsDeprecated(["FOO": arcgis], []) == [arcgis]
    }

    @Test
    public void testMergeMapAndArcgis_TwoShipsPassingInTheNight() {
        // CampusMapLocations which do not match an ArcGisLocation are
        // filtered out

        def campusmap = [
            new CampusMapLocationDeprecated(
                    id: 13,
                    name: "Forrest Observatory",
                    abbrev: "BAR",
                    longitude: "-1",
                    latitude: "1",
                    layerId: "",
                    layerNames: "Buildings Map",
                    address: "address",
                    adaEntrance: "",
                    shortDescription: "shortDescription",
                    description: "description",
                    thumbnail: "thumbnail.png",
                    largerImage: "",
            ),
        ]

        def arcgis = [
            "FOO": new ArcGisLocation(
                BldID: "0061",
                BldNam: "Forrest Observatory",
                BldNamAbr: "FOO",
                Latitude: "42.39561",
                Longitude: "-71.13051",
            ),
        ]

        def expected = [
            new ArcGisLocation(
                BldID: "0061",
                BldNam: "Forrest Observatory",
                BldNamAbr: "FOO",
                Latitude: "42.39561",
                Longitude: "-71.13051",
            ),
        ]

        assert LocationDAO.mergeMapAndBuildingsDeprecated(arcgis, campusmap) == expected
    }

    @Test
    public void testMergeMapAndArcgis_Messy() {
        // Locations with a matching abbrev and BldNamAbr are merged

        def campusmap = [
            new CampusMapLocationDeprecated(
                    id: 13,
                    name: "Campusmap name",
                    abbrev: "FOO",
                    longitude: "-1",
                    latitude: "1",
                    layerId: "",
                    layerNames: "Buildings Map",
                    address: "Campusmap address",
                    adaEntrance: "",
                    shortDescription: "Campusmap shortDescription",
                    description: "Campusmap description",
                    thumbnail: "campusmap.png",
                    largerImage: "",
            ),
        ]

        def arcgis = [
            "FOO": new FacilLocation(
                bldgID: "0032",
                name: "Arcgis bldNam",
                abbreviation: "FOO",
                latitude: "42",
                longitude: "-42",
                giRestroomCount: 3,
                giRestroomLimit: "Only for residents!",
                giRestroomLocations: "110, 210, 310"
            ),
        ]

        def expected = [
            new CampusMapLocationDeprecated(
                    id: 13,
                    name: "Arcgis bldNam",
                    abbrev: "FOO",
                    longitude: "-42",
                    latitude: "42",
                    layerId: "",
                    layerNames: "Buildings Map",
                    address: "Campusmap address",
                    adaEntrance: "",
                    shortDescription: "Campusmap shortDescription",
                    description: "Campusmap description",
                    thumbnail: "campusmap.png",
                    largerImage: "",
                    giRestroomCount: 3,
                    giRestroomLimit: "Only for residents!",
                    giRestroomLocations: "110, 210, 310"

            ),
        ]
        def actual = LocationDAO.mergeMapAndBuildingsDeprecated(arcgis, campusmap)

        assert actual == expected
    }

    @Test
    public void testJsonRoundTrip() {
        // Test that getCampusMapFromJson and writeMapToJson round trip

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
    }

    @Test
    public void testJsonNotFound() {
        // Test that getCampusMapFromJson returns null
        // if the filename does not exist

        def temp = File.createTempFile("campusmap", ".json")
        temp.delete()

        def dao = new LocationDAO([
            campusmapJsonOut: temp.getPath(), geometries: "test"
        ])

        assert dao.getCampusMapFromJson() == null
    }
}

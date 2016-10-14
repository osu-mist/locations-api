package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.db.LocationDAO

import org.junit.Test

class LocationDAOTest {
    @Test
    public void testMergeMapAndArcgis() {
        // Empty lists merge to an empty list
        assert LocationDAO.mergeMapAndArcgis([:], []) == []

        // Empty arcgis

    }

    @Test
    public void testJson() {
        // Test that getCampusMapFromJson and writeMapToJson round trip

        File temp = File.createTempFile("campusmap", ".json")
        temp.deleteOnExit()
        try {

            def dao = new LocationDAO([
                campusmapUrl: 'http://oregonstate.edu/campusmap/',
                campusmapImageUrl: 'http://oregonstate.edu/campusmap/img/metadata/',
                apiEndpointUrl: 'https://api.oregonstate.edu/v1/locations',
                campusmapJsonOut: temp.getPath(),
            ])

            def campusmap = [
                new CampusMapLocation(
                    id: 732,
                    name: "Marketplace West Dining Center",
                    abbrev: "WsDn",
                    longitude: "-123.2835840585",
                    latitude: "44.5639316626",
                    layerId: null,
                    layerNames: "Buildings Map",
                    address: "351 SW 30th Street",
                    adaEntrance: null,
                    shortDescription: "Marketplace West houses seven distinctive restaurant locations combining the cuisine of the world with traditional American favorites.  Marketplace West accepts UHDS Dining Dollars, OSU Card Cash, Visa and MasterCard, checks and cash.",
                    description: "<p>The cuisine of Marketplace West covers many global regions, designed in the style of an open air market.  The seven restaurant locations offer fresh Latin, Pacific Rim fare, classic Oregon Natural Beef burgers and hand-dipped shakes, fresh salads and deli sandwiches, pizza, pasta, and coffee.</p><ul><li><a href=\"../../uhds/dining/menus/calabaloos.php\">Calabaloo's Gourmet Burgers™</a><br /></li><li><a href=\"../../uhds/dining/menus/clubhouse_deli.php\">Clubhouse Deli</a><br /></li><li><a href=\"../../uhds/dining/menus/ebgbs.php\">EBGB's</a> (Every Bean's a Good Bean) <br /></li><li><a href=\"../../uhds/dining/menus/four_corners.php\">Four Corners</a><br /></li><li><a href=\"../../uhds/dining/menus/ring_of_fire.php\">Ring of Fire</a><br /></li><li><a href=\"../../uhds/dining/menus/serrano_grill.php\">Serrano Grill™</a><br /></li><li><a href=\"../../uhds/dining/menus/tomassitos.php\">Tomassito's Italian Cafe</a><br /></li></ul><p>For hours of operation, please visit the <a href=\"../../foodatosu/hours/\">food @ OSU</a> website, and for more general housing information, please visit the <a href=\"../../uhds/\">Housing and Dining Services</a> website.</p>",
                    thumbnail: "wsdn002.jpg",
                    largerImage: null,
                ),
            ]

            // Round-trip
            dao.writeMapToJson(campusmap)
            def actual = dao.getCampusMapFromJson()

            assert actual.equals(campusmap)
        } finally {
            temp.delete()
        }
    }

    @Test
    public void testJsonNotFound() {
        // Test that getCampusMapFromJson returns null if the filename does not
        // exist

        def temp = File.createTempFile("campusmap", ".json")
        temp.delete()

        def dao = new LocationDAO([
            campusmapUrl: 'http://oregonstate.edu/campusmap/',
            campusmapImageUrl: 'http://oregonstate.edu/campusmap/img/metadata/',
            apiEndpointUrl: 'https://api.oregonstate.edu/v1/locations',
            campusmapJsonOut: temp.getPath(),
        ])

        assert dao.getCampusMapFromJson() == null
    }
}

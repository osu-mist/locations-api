package edu.oregonstate.mist.locations

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.AdaEntriesLocation
import edu.oregonstate.mist.locations.core.AedInventoriesLocation
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import edu.oregonstate.mist.locations.db.LocationDAO
import groovy.transform.TypeChecked
import org.junit.Test

@TypeChecked
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
                    giRestroomLocations: "110, 210, 310",

                    adaEntries: [],
                    aedInventories: []
            ),
        ]

        def adaEntries = [
            "0032": new AdaEntriesLocation(
                bldID: "0032",
                lat: "123",
                lon: "45",
                accessible: "Y",
                function: "Entry"
            )
        ]

        def aedInventories = [
            "0032": new AedInventoriesLocation(
                bldg: "32",
                lat: "123",
                lon: "45",
                location: "basement",
                floor: "1",
                make: "fake make",
                model: "fake model",
                serialNo: "1234567",
                departmentOwner: "COE",
                contact: "Harry Potter",
            )
        ]

        def actual = LocationDAO.mergeFacilAndArcGis(
            campusmap, girr, arcgis, adaEntries, aedInventories
        )

        assert actual == expected
    }
}

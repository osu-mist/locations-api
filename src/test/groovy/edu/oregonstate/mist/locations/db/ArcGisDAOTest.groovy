package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.junit.Test

@TypeChecked
class ArcGisDAOTest {
    ArcGisDAO dao = new ArcGisDAO([:], null)

    @Test
    void testRR() {
        def testData =
                '{\n' +
                '   "features":[\n' +
                '       {\n' +
                '           "attributes" : {\n' +
                '               "OBJECTID" : 80, \n' +
                '               "BldNam" : "Withycombe Hall", \n' +
                '               "BldID" : "0075", \n' +
                '               "Notes" : " ", \n' +
                '               "LocaADA" : "0071", \n' +
                '               "LocaADA_Not" : "0123, 0167A, 0307,169", \n' +
                '               "LocaAll" : "0071, 0123, 0167A, 0307, 0169", \n' +
                '               "BldNamAbr" : "With", \n' +
                '               "Limits" : " ", \n' +
                '               "CntADA" : 1, \n' +
                '               "CntADA_Not" : 4, \n' +
                '               "CntAll" : 5\n' +
                '           }, \n' +
                '           "geometry" : \n' +
                '           {\n' +
                '               "x" : 7476856.0816929191, \n' +
                '               "y" : 340760.45964567363\n' +
                '           }\n' +
                '       }\n' +
                '  ]\n' +
                '}\n'
        assert dao.mapRR(testData) == ["0075": new GenderInclusiveRRLocation(
                bldID: "0075",
                bldNam: "Withycombe Hall",
                bldNamAbr: "With",
                giRestroomCount: 5,
                giRestroomLimit: " ",
                giRestroomLocations: "0071, 0123, 0167A, 0307, 0169"
        )]
    }
}


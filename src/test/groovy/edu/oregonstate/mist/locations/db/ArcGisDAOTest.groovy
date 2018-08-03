package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import groovy.transform.TypeChecked
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

@TypeChecked
class ArcGisDAOTest {
    ArcGisDAO dao = new ArcGisDAO(["arcGisThreshold":"1"], null)

    @Rule
    public ExpectedException exception = ExpectedException.none()

    // Threshold is satisfied
    @Test
    void testMapRR() {
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

    // Threshold is not satisfied
    @Test
    void testUnderThreshold() {
        exception.expect(DAOException.class)
        exception.expectMessage("gender inclusive restrooms")
        def testData =
                '{\n' +
                '   "features":[]\n' +
                '}\n'
        dao.mapRR(testData)
    }
}


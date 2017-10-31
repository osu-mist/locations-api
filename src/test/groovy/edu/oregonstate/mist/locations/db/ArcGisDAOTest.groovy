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
        def testData = '{"features":[{"attributes":{"OBJECTID":80,"BldNam":"Withycombe Hall","BldID":"0075","Notes":" ",' +
                '"LocaADA":"0071","LocaADA_Not":"0123, 0167A, 0307,169",' +
                '"LocaAll":"0071, 0123, 0167A, 0307, 0169","BldNamAbr":"With","Limits":" ",' +
                '"CntADA":1,"CntADA_Not":4,"CntAll":5}}]}'
        assert dao.mapRR(testData) == ["0075": new GenderInclusiveRRLocation(
                bldID: "0075", bldNam: "Withycombe Hall", bldNamAbr: "With",
                giRestroomCount: 5, giRestroomLimit: " ",
                giRestroomLocations: "0071, 0123, 0167A, 0307, 0169")
        ]
    }
}


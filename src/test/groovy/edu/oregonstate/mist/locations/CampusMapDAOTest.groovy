package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.DAOException
import org.junit.Test

class CampusMapDAOTest {

    CampusMapDAO campusMapDAO = new CampusMapDAO(["campusMapThreshold": "1"], new Cache([:]))

    // Threshold is satisfied
    @Test
    void testGetCampusMapJson() {
        String testData = """\
            [
                {
                    "id": "test",
                    "name": "test",
                    "abbreviation": "test",
                    "synonyms": [],
                    "address": "test",
                    "description": "test",
                    "descriptionHTML": "test",
                    "images": [],
                    "thumbnail": [],
                    "mapUrl": "test",
                    "Updated date": "test"
                }
            ]
        """.stripIndent()
        campusMapDAO.parseCampusMaps(testData)
    }

    // Threshold is not satisfied
    @Test(expected = DAOException.class)
    void testUnderThreshold() {
        campusMapDAO.parseCampusMaps("[]")
    }
}

package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.DAOException
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class CampusMapDAOTest {

    CampusMapDAO campusMapDAO = new CampusMapDAO(["campusMapThreshold": "1"], new Cache([:]))

    @Rule
    public ExpectedException exception = ExpectedException.none()

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
    @Test
    void testUnderThreshold() {
        exception.expect(DAOException.class)
        exception.expectMessage("campus map locations")
        campusMapDAO.parseCampusMaps("[]")
    }
}

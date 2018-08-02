package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.db.CachedFacilDAO
import edu.oregonstate.mist.locations.db.DAOException
import edu.oregonstate.mist.locations.db.FacilDAO
import groovy.mock.interceptor.StubFor
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class CachedFacilDAOTest {

    @Rule
    public ExpectedException exception = ExpectedException.none()

    // Threshold is satisfied
    @Test
    void testGetBuildings() {
        List<FacilLocation> testData = [new FacilLocation(
                bldgID: "test",
                abbreviation: "test",
                name: "test",
                campus: "test",
                address1: "test",
                address2: "test",
                city: "test",
                state: "test",
                zip: "test"
        )]
        testThreshold(testData)
    }

    // Threshold is not satisfied
    @Test
    void testUnderThreshold() {
        exception.expect(DAOException.class)
        testThreshold([])
    }

    /**
     * Tests CachedFacilDAO.getBuildings with test data
     *
     * @param testData
     */
    void testThreshold(List<FacilLocation> testData) {
        def stubFacilDAO = new StubFor(FacilDAO)
        stubFacilDAO.demand.getBuildings { testData }
        def proxyFacilDAO = stubFacilDAO.proxyInstance()

        def stubCache = new StubFor(Cache)
        stubCache.demand.writeDataToCache { String a, String b -> null }
        stubCache.demand.getCachedData { String a -> "[]" }
        def proxyCache = stubCache.proxyInstance(["cacheDirectory": "proxyDir"])

        CachedFacilDAO cachedFacilDAO = new CachedFacilDAO(proxyFacilDAO, proxyCache, 1)
        cachedFacilDAO.getBuildings()
    }
}

package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.FacilLocation
import org.skife.jdbi.v2.DBI

/**
 * CachedFacilDAO is a wrapper around FacilDAO that adds caching
 */
class CachedFacilDAO implements Closeable {
    private final FacilDAO facilDAO
    private final LocationUtil locationUtil
    CachedFacilDAO(DBI jdbi, LocationUtil locationUtil) {
        // TODO: should we construct FacilDAO here or require that it be passed in?
        this.facilDAO = jdbi.onDemand(FacilDAO.class)
        this.locationUtil = locationUtil
    }

    List<FacilLocation> getBuildings() {
        facilDAO.getBuildings()
    }

    @Override
    void close() {
        facilDAO.close()
    }
}

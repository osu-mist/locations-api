package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.FacilLocation
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.skife.jdbi.v2.exceptions.DBIException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.SQLException

/**
 * CachedFacilDAO is a wrapper around FacilDAO that adds caching.
 */
@TypeChecked
class CachedFacilDAO implements Closeable {
    final static Logger LOGGER = LoggerFactory.getLogger(CachedFacilDAO)
    final ObjectMapper mapper = new ObjectMapper()

    final private static String CACHE_FILENAME = "facil-buildings.json"
    private final FacilDAO facilDAO
    private final Cache cache
    private final int FACIL_LOCATION_THRESHOLD

    CachedFacilDAO(FacilDAO facilDAO, Cache cache, int facilLocationThreshold) {
        // @todo: should we construct FacilDAO here or require that it be passed in?
        // we don't have to worry about connection errors here because onDemand
        // doesn't connect until the first request, which is in getBuildings below
        this.facilDAO = facilDAO
        this.cache = cache
        FACIL_LOCATION_THRESHOLD = facilLocationThreshold
    }

    @Override
    void close() {
        facilDAO.close()
    }

    List<FacilLocation> getBuildings() {
        def buildings
        try {
            buildings = facilDAO.getBuildings()
            LocationUtil.checkThreshold(buildings.size(), FACIL_LOCATION_THRESHOLD, "buildings")
            saveBuildingsToCache(buildings)
        } catch (DAOException | DBIException | SQLException e) {
            //@todo: i think jdbi wraps all SQLExceptions in a DBIException
            LOGGER.error("Got exception while querying facil database", e)
            LOGGER.error("Attempting to fall back on cached data")
            buildings = getBuildingsFromCache()
            LocationUtil.checkThreshold(buildings.size(), FACIL_LOCATION_THRESHOLD, "buildings")
        }
        buildings
    }

    @PackageScope
    void saveBuildingsToCache(List<FacilLocation> buildings) {
        cache.writeDataToCache(CACHE_FILENAME, mapper.writeValueAsString(buildings))
    }

    private List<FacilLocation> getBuildingsFromCache() {
        def cachedData = cache.getCachedData(CACHE_FILENAME)
        def buildings = (List<FacilLocation>)mapper.readValue(cachedData,
                new TypeReference<List<FacilLocation>>() {})
        //@todo: catch exceptions?
        buildings
    }
}

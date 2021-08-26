package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.mapper.FacilMapper
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(FacilMapper)
interface FacilDAO extends Closeable {
    @SqlQuery("""
        SELECT DISTINCT
            LOCATION_BLDG_ID,
            LOCATION_BLDG_NAME,
            LOCATION_BLDG_ABBREVIATION,
            LOCATION_CAMPUS,
            LOCATION_ADDRESS1,
            LOCATION_ADDRESS2,
            LOCATION_CITY,
            LOCATION_STATE,
            LOCATION_ZIP
        FROM
            FACIL_LOCATION
""")
    List<FacilLocation> getBuildings()

    @Override
    void close()
}

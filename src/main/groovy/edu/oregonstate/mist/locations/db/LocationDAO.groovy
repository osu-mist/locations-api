package edu.oregonstate.mist.locations.db

import com.google.common.base.Optional
import edu.oregonstate.mist.locations.core.Location
import edu.oregonstate.mist.locations.mapper.LocationMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(LocationMapper)
public interface LocationDAO extends Closeable {
    @SqlQuery("""
    SELECT
        id,
        name,
        abbrev,
        x(`marker_location`) AS longitude,
        y(`marker_location`) AS latitude,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 5 and lm.location_id = :id and lm.metadata_id = m.id) short_description,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 1 and lm.location_id = :id and lm.metadata_id = m.id) physical_address,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 3 and lm.location_id = :id and lm.metadata_id = m.id) ada_entrance,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 6 and lm.location_id = :id and lm.metadata_id = m.id) thumbnail,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 7 and lm.location_id = :id and lm.metadata_id = m.id) long_description,
        (select metadata from metadata m, locations_metadata lm where metadata_type_id = 8 and lm.location_id = :id and lm.metadata_id = m.id) larger_image
    FROM
        locations
    WHERE
        id = :id;
""")
    Location getLocationById(@Bind("id") Long id)

    void close()
}

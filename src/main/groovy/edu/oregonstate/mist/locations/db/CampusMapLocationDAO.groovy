package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.mapper.CampusMapLocationMapper
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(CampusMapLocationMapper)
public interface CampusMapLocationDAO extends Closeable {

    @SqlQuery("""
    SELECT
        locations.id,
        locations.name AS name,
        locations.abbrev as abbrev,
        x(`marker_location`) AS longitude,
        y(`marker_location`) AS latitude,
        group_concat( l_layer.name ) AS layer_names,
        l_layer.layer_id,
        l_meta2.meta as physical_address,
        l_meta3.meta as ada_entrance,
        l_meta5.meta as short_description,
        l_meta6.meta as thumbnail_image,
        l_meta7.meta as long_description,
        l_meta8.meta as larger_image
    FROM locations
        LEFT JOIN (
            SELECT layers_locations.id, location_id, name, layers.id as layer_id
            FROM layers_locations, layers
            WHERE layers_locations.layer_id = layers.id
        ) AS l_layer ON locations.id = l_layer.location_id
        LEFT JOIN (
            SELECT lm2.location_id, m2.metadata AS meta, m2.metadata_type_id
            FROM metadata m2, locations_metadata lm2
            WHERE m2.id = lm2.metadata_id AND m2.metadata_type_id = 1
        ) AS l_meta2 ON locations.id = l_meta2.location_id
        LEFT JOIN (
            SELECT lm3.location_id, m3.metadata AS meta, m3.metadata_type_id
            FROM metadata m3, locations_metadata lm3
            WHERE m3.id = lm3.metadata_id AND m3.metadata_type_id = 3
        ) AS l_meta3 ON locations.id = l_meta3.location_id
        LEFT JOIN (
            SELECT lm5.location_id, m5.metadata AS meta, m5.metadata_type_id
            FROM metadata m5, locations_metadata lm5
            WHERE m5.id = lm5.metadata_id AND m5.metadata_type_id = 5
        ) AS l_meta5 ON locations.id = l_meta5.location_id
        LEFT JOIN (
            SELECT lm6.location_id, m6.metadata AS meta, m6.metadata_type_id
            FROM metadata m6, locations_metadata lm6
            WHERE m6.id = lm6.metadata_id AND m6.metadata_type_id = 6
        ) AS l_meta6 ON locations.id = l_meta6.location_id
        LEFT JOIN (
            SELECT lm7.location_id, m7.metadata AS meta, m7.metadata_type_id
            FROM metadata m7, locations_metadata lm7
            WHERE m7.id = lm7.metadata_id AND m7.metadata_type_id = 7
        ) AS l_meta7 ON locations.id = l_meta7.location_id
        LEFT JOIN (
            SELECT lm8.location_id, m8.metadata AS meta, m8.metadata_type_id
            FROM metadata m8, locations_metadata lm8
            WHERE m8.id = lm8.metadata_id AND m8.metadata_type_id = 8
        ) AS l_meta8 ON locations.id = l_meta8.location_id
    GROUP BY locations.id
    HAVING layer_id in (1, 24)
""")
    List<CampusMapLocation> getCampusMapLocations()

    @Override
    void close()
}

package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.core.CampusMapLocationDeprecated
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class CampusMapLocationMapper implements ResultSetMapper<CampusMapLocationDeprecated> {
    public CampusMapLocationDeprecated map(
            int i, ResultSet rs, StatementContext sc) throws SQLException {
        new CampusMapLocationDeprecated(
            id: rs.getInt("id"),
            name: rs.getString("name"),
            abbrev: rs.getString("abbrev"),
            longitude: rs.getString("longitude"),
            latitude: rs.getString("latitude"),
            layerNames: rs.getString("layer_names"),
            address: rs.getString("physical_address"),
            adaEntrance: rs.getString("ada_entrance"),
            shortDescription: rs.getString("short_description"),
            description: rs.getString("long_description"),
            thumbnail: rs.getString("thumbnail_image"),
            largerImage: rs.getString("larger_image")
        )

    }

}
package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.core.Location
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class LocationMapper implements ResultSetMapper<Location> {
    public Location map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new Location(
            id: rs.getInt("id"),
            name: rs.getString("name"),
            abbrev: rs.getString("abbrev"),
            shortDescription: rs.getString("short_description"),
            description: rs.getString("long_description"),
            address: rs.getString("physical_address"),
            latitude: rs.getString("latitude"),
            longitude: rs.getString("longitude"),
            adaEntrance: rs.getString("ada_entrance"),
            thumbnail: rs.getString("thumbnail"),
            largerImage: rs.getString("larger_image")
        )

    }

}
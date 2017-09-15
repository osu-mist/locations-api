package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.core.FacilLocation
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class FacilMapper implements ResultSetMapper<FacilLocation> {
    FacilLocation map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new FacilLocation(
                bldgID:         rs.getString("LOCATION_BLDG_ID"),
                abbreviation:   rs.getString("LOCATION_BLDG_ABBREVIATION"),
                name:           rs.getString("LOCATION_BLDG_NAME"),
                campus:         rs.getString("LOCATION_CAMPUS"),
                address1:       rs.getString("LOCATION_ADDRESS1"),
                address2:       rs.getString("LOCATION_ADDRESS2"),
                city:           rs.getString("LOCATION_CITY"),
                state:          rs.getString("LOCATION_STATE"),
                zip:            rs.getString("LOCATION_ZIP")
        )

    }

}
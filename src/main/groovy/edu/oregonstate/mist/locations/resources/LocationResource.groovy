package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.db.CampusMapLocationDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import io.dropwizard.auth.Auth

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
class LocationResource {
    private final CampusMapLocationDAO campusMapLocationDAO
    private final DiningDAO diningDAO

    LocationResource(CampusMapLocationDAO locationDAO, DiningDAO diningDAO) {
        this.campusMapLocationDAO = locationDAO
        this.diningDAO = diningDAO
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    List<CampusMapLocation> getCampusMap(@Auth AuthenticatedUser authenticatedUser) {
        final List<CampusMapLocation> campusMapLocations = campusMapLocationDAO.getCampusMapLocations()

        if (!campusMapLocations) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        campusMapLocations
    }

    @GET
    @Path("dining")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    List<DiningLocation> getDinig(@Auth AuthenticatedUser authenticatedUser) {
        final List<DiningLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        diningLocations
    }
}

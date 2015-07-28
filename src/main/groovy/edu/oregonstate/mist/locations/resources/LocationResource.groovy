package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import com.google.common.base.Optional
import edu.oregonstate.mist.locations.core.Location
import edu.oregonstate.mist.locations.db.LocationDAO
import io.dropwizard.jersey.params.LongParam

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LocationResource {
    private final LocationDAO locationDAO

    //@todo: is this really needed?
    LocationResource(LocationDAO locationDAO) {
        this.locationDAO = locationDAO
    }

    @GET
    @Timed
    @Path('{id: \\d+}')
    Location getById(@PathParam('id') LongParam id) {
        final Location location = locationDAO.getLocationById(id.get())

        if (!location) {
            //@todo - does this also set the correct headers?
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        return location
    }
}

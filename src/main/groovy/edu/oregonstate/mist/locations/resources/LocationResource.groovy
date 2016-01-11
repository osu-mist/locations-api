package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.db.CampusMapLocationDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.locations.jsonapi.ResultObject
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
    private final LocationDAO locationDAO
    private final ExtensionDAO extensionDAO

    LocationResource(CampusMapLocationDAO campusMapLocationDAO, DiningDAO diningDAO, LocationDAO locationDAO,
                     ExtensionDAO extensionDAO) {
        this.campusMapLocationDAO = campusMapLocationDAO
        this.diningDAO = diningDAO
        this.locationDAO = locationDAO
        this.extensionDAO = extensionDAO
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getCampusMap(@Auth AuthenticatedUser authenticatedUser) {
        final List<CampusMapLocation> campusMapLocations = campusMapLocationDAO.getCampusMapLocations()

        if (!campusMapLocations) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        Response.ok(campusMapLocations).build()
    }

    @GET
    @Path("dining")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getDining(@Auth AuthenticatedUser authenticatedUser) {
        final List<DiningLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        Response.ok(diningLocations).build()
    }

    @GET
    @Path("extension")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getExtension(@Auth AuthenticatedUser authenticatedUser) {
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        if (!extensionLocations) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }

        Response.ok(extensionLocations).build()
    }
}

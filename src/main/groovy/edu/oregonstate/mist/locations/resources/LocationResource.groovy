package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.Resource
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
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
class LocationResource extends Resource {
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
            notFound()
        }

        ok(campusMapLocations).build()
    }

    @GET
    @Path("dining")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getDining(@Auth AuthenticatedUser authenticatedUser) {
        final List<DiningLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            notFound()
        }

        ok(diningLocations).build()
    }

    @GET
    @Path("extension")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getExtension(@Auth AuthenticatedUser authenticatedUser) {
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        if (!extensionLocations) {
            notFound()
        }

        ok(extensionLocations).build()
    }

    @GET
    @Path("combined")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response combineSources(@Auth AuthenticatedUser authenticatedUser) {
        def jsonESInput = new File("locations-combined.json")
        jsonESInput.write("") // clear out file

        ResultObject resultObject = new ResultObject()
        final List<CampusMapLocation> campusMapLocations = campusMapLocationDAO.getCampusMapLocations()
        final List<DiningLocation> diningLocations = diningDAO.getDiningLocations()
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        resultObject.data = []
        resultObject.data = locationDAO.convert(campusMapLocations)
        resultObject.data += locationDAO.convert(diningLocations)
        resultObject.data += locationDAO.convert(extensionLocations)

        // @todo: move this somewhere else
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

        resultObject.data.each {
            def indexAction = [index: [_id: it.id]]
            jsonESInput << mapper.writeValueAsString(indexAction) + "\n"
            jsonESInput << mapper.writeValueAsString(it) + "\n"
        }

        //@todo: this would just return an empty data array?
        ok(resultObject).build()
    }
}

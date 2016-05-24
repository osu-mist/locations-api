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
            return notFound().build()
        }

        List<List> locationsList = [ campusMapLocations ]
        ResultObject resultObject = writeJsonAPIToFile("locations-campusmap.json", locationsList)

        ok(resultObject).build()
    }

    @GET
    @Path("dining")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getDining(@Auth AuthenticatedUser authenticatedUser) {
        final List<DiningLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            return notFound().build()
        }

        List<List> locationsList = [ diningLocations ]
        ResultObject resultObject = writeJsonAPIToFile("locations-dining.json", locationsList)

        ok(resultObject).build()
    }

    @GET
    @Path("extension")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getExtension(@Auth AuthenticatedUser authenticatedUser) {
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        if (!extensionLocations) {
            return notFound().build()
        }

        List<List> locationsList = [ extensionLocations ]
        ResultObject resultObject = writeJsonAPIToFile("locations-extension.json", locationsList)

        ok(resultObject).build()
    }

    @GET
    @Path("combined")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response combineSources(@Auth AuthenticatedUser authenticatedUser) {
        List<List> locationsList = []
        locationsList  += campusMapLocationDAO.getCampusMapLocations()
        locationsList  += diningDAO.getDiningLocations()
        locationsList  += extensionDAO.getExtensionLocations()

        ResultObject resultObject = writeJsonAPIToFile("locations-combined.json", locationsList)

        ok(resultObject).build()
    }

    /**
     * Converts the locations list to result objects that can be returned via the browser. It
     * also writes the location object list in a json file to be sent to ES.
     *
     * @param filename
     * @param locationsList
     * @return
     */
    private ResultObject writeJsonAPIToFile(String filename, List<List> locationsList) {
        ResultObject resultObject = new ResultObject()
        def jsonESInput = new File(filename)
        jsonESInput.write("") // clear out file

        resultObject.data = []
        locationsList.each {
            resultObject.data += locationDAO.convert(it)
        }

        // @todo: move this somewhere else
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

        resultObject.data.each {
            def indexAction = [index: [_id: it.id]]
            jsonESInput << mapper.writeValueAsString(indexAction) + "\n"
            jsonESInput << mapper.writeValueAsString(it) + "\n"
        }
        resultObject
    }
}

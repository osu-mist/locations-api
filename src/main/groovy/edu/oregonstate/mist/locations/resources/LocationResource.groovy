package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.db.ArcGisDAO

import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.ExtraDataManager
import edu.oregonstate.mist.locations.db.LibraryDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import edu.oregonstate.mist.api.jsonapi.ResultObject

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
class LocationResource extends Resource {
    private final DiningDAO diningDAO
    private final LocationDAO locationDAO
    private final ExtensionDAO extensionDAO
    private final ArcGisDAO arcGisDAO
    private final ExtraDataDAO extraDataDAO
    private final LibraryDAO libraryDAO
    private ExtraDataManager extraDataManager

    LocationResource(DiningDAO diningDAO, LocationDAO locationDAO, ExtensionDAO extensionDAO,
                     ArcGisDAO arcGisDAO, ExtraDataDAO extraDataDAO,
                     ExtraDataManager extraDataManager, LibraryDAO libraryDAO) {
        this.diningDAO = diningDAO
        this.locationDAO = locationDAO
        this.extensionDAO = extensionDAO
        this.arcGisDAO = arcGisDAO
        this.extraDataDAO = extraDataDAO
        this.extraDataManager = extraDataManager
        this.libraryDAO = libraryDAO
    }

    @GET
    @Path("dining")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getDining() {
        final List<ServiceLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            return notFound().build()
        }

        ResultObject resultObject = writeJsonAPIToFile("locations-dining.json", diningLocations)
        ok(resultObject).build()
    }

    @GET
    @Path("arcgis")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getFacilities() {
        ArrayList mergedData = getArcGisAndMapData()

        if (!mergedData) {
            return notFound().build()
        }

        ResultObject resultObject = writeJsonAPIToFile("locations-arcgis.json", mergedData)
        ok(resultObject).build()
    }

    /**
     * Returns the combined data from the arcgis and campusmap.
     *
     * @return
     */
    private List getArcGisAndMapData() {
        HashMap<String, ArcGisLocation> arcGisLocations = arcGisDAO.getArcGisLocations()
        List<CampusMapLocation> campusMapLocationList = locationDAO.getCampusMapFromJson()
        locationDAO.mergeMapAndArcgis(arcGisLocations, campusMapLocationList)
    }

    @GET
    @Path("extension")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getExtension() {
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        if (!extensionLocations) {
            return notFound().build()
        }

        ResultObject resultObject =
                writeJsonAPIToFile("locations-extension.json", extensionLocations)
        ok(resultObject).build()
    }

    @GET
    @Path("library")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response getLibrary() {
        def data = libraryDAO.getLibraryHours()

        ok(data).build()
    }

    @GET
    @Path("combined")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response combineSources() {
        List<List> locationsList = []
        locationsList  += getArcGisAndMapData()
        locationsList  += extraDataManager.extraData.locations
        locationsList  += diningDAO.getDiningLocations()
        locationsList  += extensionDAO.getExtensionLocations()
        locationsList  += extraDataDAO.getExtraDataLocations( { ! it.tags.contains("services") })

        ResultObject resultObject = writeJsonAPIToFile("locations-combined.json", locationsList)
        ok(resultObject).build()
    }

    @GET
    @Path("services")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    Response services() {
        List<List> locationsList = []
        locationsList  += extraDataDAO.getExtraDataLocations( { it.tags.contains("services") } )

        ResultObject resultObject = writeJsonAPIToFile("services.json", locationsList)
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
    private ResultObject writeJsonAPIToFile(String filename, List locationsList) {
        ResultObject resultObject = new ResultObject()
        def jsonESInput = new File(filename)
        jsonESInput.write("") // clear out file

        resultObject.data = []
        locationsList.each {
            resultObject.data += locationDAO.convert(it)
        }

        MergeUtil mergeUtil = new MergeUtil(resultObject, libraryDAO, extraDataDAO)
        mergeUtil.merge()
        mergeUtil.populate()
        mergeUtil.appendRelationships()

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

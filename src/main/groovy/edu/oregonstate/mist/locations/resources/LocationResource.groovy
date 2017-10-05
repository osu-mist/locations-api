package edu.oregonstate.mist.locations.resources

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.CampusMapLocationDeprecated
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.db.ArcGisDAO
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.ExtraDataManager
import edu.oregonstate.mist.locations.db.FacilDAO
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
    private final Boolean useHttpCampusMap
    private final CampusMapDAO campusMapDAO
    private final FacilDAO facilDAO

    LocationResource(DiningDAO diningDAO, LocationDAO locationDAO, ExtensionDAO extensionDAO,
                     ArcGisDAO arcGisDAO, ExtraDataDAO extraDataDAO,
                     ExtraDataManager extraDataManager, LibraryDAO libraryDAO,
                     Boolean useHttpCampusMap, CampusMapDAO campusMapDAO, FacilDAO facilDAO) {
        this.diningDAO = diningDAO
        this.locationDAO = locationDAO
        this.extensionDAO = extensionDAO
        this.arcGisDAO = arcGisDAO
        this.extraDataDAO = extraDataDAO
        this.extraDataManager = extraDataManager
        this.libraryDAO = libraryDAO
        this.useHttpCampusMap = useHttpCampusMap
        this.campusMapDAO = campusMapDAO
        this.facilDAO = facilDAO
    }

    @GET
    @Path("dining")
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
    @Timed
    Response getFacilities() {
        ArrayList mergedData = getBuildingData()

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
    private List getBuildingData() {
        List<FacilLocation> buildings = facilDAO.getBuildings()
        // Get acrgis gender inclusive restroom data from http request
        HashMap<String, GenderInclusiveRRLocation> genderInclusiveRR =
                arcGisDAO.getGenderInclusiveRR()
        // Get arcgis coordinate data from json file
        HashMap<String, ArcGisLocation> arcGisGeometries = locationDAO.getArcGisCoordinates()

        def buildingAndArcGisMerged = locationDAO.mergeFacilAndArcGis(buildings,
            genderInclusiveRR, arcGisGeometries)

        if (!useHttpCampusMap) {
            List<CampusMapLocationDeprecated> campusMapLocationList =
                    locationDAO.getCampusMapFromJson()
            // Merge the combined arcgis data with campus map data
            locationDAO.mergeMapAndBuildingsDeprecated(
                    buildingAndArcGisMerged, campusMapLocationList)
        } else {
            new ArrayList<FacilLocation>(buildingAndArcGisMerged.values())
        }
    }

    @GET
    @Path("extension")
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
    @Timed
    Response getLibrary() {
        def data = libraryDAO.getLibraryHours()

        ok(data).build()
    }

    @GET
    @Path("combined")
    @Timed
    Response combineSources() {
        List<List> locationsList = []
        locationsList  += getBuildingData()
        locationsList  += extraDataManager.extraData.locations
        locationsList  += diningDAO.getDiningLocations()
        locationsList  += extensionDAO.getExtensionLocations()
        locationsList  += extraDataDAO.getLocations()

        ResultObject resultObject = writeJsonAPIToFile("locations-combined.json", locationsList)
        ok(resultObject).build()
    }

    @GET
    @Path("services")
    @Timed
    Response services() {
        List<List> locationsList = []
        locationsList  += extraDataDAO.getServices()

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
        def jsonESInput = new File(filename)
        jsonESInput.write("") // clear out file

        def data = locationsList.collect { locationDAO.convert(it) }

        MergeUtil mergeUtil = new MergeUtil(
                libraryDAO,
                extraDataDAO,
                campusMapDAO)
        if (useHttpCampusMap) {
            data = mergeUtil.mergeCampusMapData(data)
        }
        if (filename != "services.json") {
            data = mergeUtil.merge(data) // only applies to locations
            data = mergeUtil.populate(data) // only applies to locations for now?
            data = mergeUtil.appendRelationshipsToLocations(data)
        } else {
            data = mergeUtil.appendRelationshipsToServices(data)
        }

        // @todo: move this somewhere else
        ObjectMapper mapper = new ObjectMapper() // can reuse, share globally

        data.each {
            def indexAction = [index: [_id: it.id]]
            jsonESInput << mapper.writeValueAsString(indexAction) + "\n"
            jsonESInput << mapper.writeValueAsString(it) + "\n"
        }

        new ResultObject(data: data)
    }
}

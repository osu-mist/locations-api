package edu.oregonstate.mist.locations

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.LocationConfiguration
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
import edu.oregonstate.mist.locations.resources.MergeUtil
import io.dropwizard.cli.EnvironmentCommand
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import org.apache.http.client.HttpClient
import org.skife.jdbi.v2.DBI

@groovy.transform.TypeChecked
class LocationCommand extends EnvironmentCommand<LocationConfiguration> {
    ObjectMapper mapper = new ObjectMapper()
    private ArcGisDAO arcGisDAO
    private CampusMapDAO campusMapDAO
    private DiningDAO diningDAO
    private ExtensionDAO extensionDAO
    private ExtraDataDAO extraDataDAO
    private FacilDAO facilDAO
    private LibraryDAO libraryDAO
    private LocationDAO locationDAO

    private ExtraDataManager extraDataManager
    private Boolean useHttpCampusMap


    /*
    LocationCommand(DiningDAO diningDAO, LocationDAO locationDAO, ExtensionDAO extensionDAO,
                     ArcGisDAO arcGisDAO, ExtraDataDAO extraDataDAO,
                     ExtraDataManager extraDataManager, LibraryDAO libraryDAO,
                     Boolean useHttpCampusMap, CampusMapDAO campusMapDAO, FacilDAO facilDAO) {
        super("generate", "Generates json files")
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
    */

    LocationCommand(LocationApplication app) {
        super(app, "generate", "Generates json files")
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser)
        // can add arguments here
    }

    @Override
    protected void run(Environment environment,
                       Namespace namespace,
                       LocationConfiguration configuration) throws Exception {

        // the httpclient from DW provides with many metrics and config options
        HttpClient httpClient = new HttpClientBuilder(environment)
                .using(configuration.getHttpClientConfiguration())
                .build("generate-http-client")

        final DBIFactory factory = new DBIFactory()
        final DBI jdbi = factory.build(environment, configuration.getDatabase(), "jdbi")

        def configMap = configuration.locationsConfiguration
        final LocationUtil locationUtil = new LocationUtil(configMap)

        ExtraDataManager extraDataManager = new ExtraDataManager()
        // Managed objects are tied to the lifecycle of the http server.
        // We aren't starting an http server, so we have to start the manager manually.
        //environment.lifecycle().manage(extraDataManager)
        extraDataManager.start()

        arcGisDAO = new ArcGisDAO(configMap, locationUtil)
        campusMapDAO = new CampusMapDAO(configMap, locationUtil)
        diningDAO = new DiningDAO(configuration, locationUtil)
        extensionDAO = new ExtensionDAO(configMap, locationUtil)
        extraDataDAO = new ExtraDataDAO(configuration, locationUtil, extraDataManager)
        facilDAO = jdbi.onDemand(FacilDAO.class)
        libraryDAO = new LibraryDAO(configMap, httpClient)
        locationDAO = new LocationDAO(configMap)

        useHttpCampusMap = Boolean.parseBoolean(
                configuration.locationsConfiguration.get("useHttpCampusMap"))

        System.printf("hi %s\n", configuration.api.endpointUri)
        services()
    }

    void getDining() {
        final List<ServiceLocation> diningLocations = diningDAO.getDiningLocations()

        if (!diningLocations) {
            return
        }

        ResultObject resultObject = writeJsonAPIToFile("locations-dining.json", diningLocations)
    }

    void getFacilities() {
        List mergedData = getBuildingData()

        if (!mergedData) {
            return
        }

        ResultObject resultObject = writeJsonAPIToFile("locations-arcgis.json", mergedData)
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

    void getExtension() {
        final List<ExtensionLocation> extensionLocations = extensionDAO.getExtensionLocations()

        if (!extensionLocations) {
            return
        }

        ResultObject resultObject =
                writeJsonAPIToFile("locations-extension.json", extensionLocations)
    }

    void getLibrary() {
        def data = libraryDAO.getLibraryHours()

    }

    void combineSources() {
        ArrayList<Object> locationsList = []
        locationsList.addAll(getBuildingData())
        locationsList.addAll(extraDataManager.extraData.locations)
        locationsList.addAll(diningDAO.getDiningLocations())
        locationsList.addAll(extensionDAO.getExtensionLocations())
        locationsList.addAll(extraDataDAO.getLocations())

        ResultObject resultObject = writeJsonAPIToFile("locations-combined.json", locationsList)
    }

    void services() {
        ArrayList<Object> locationsList = []
        locationsList.addAll(extraDataDAO.getServices())

        ResultObject resultObject = writeJsonAPIToFile("services.json", locationsList)

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

        ArrayList<ResourceObject> data = []
        locationsList.each {
            data.add(locationDAO.convert(it))
        }

        resultObject.data = data

        MergeUtil mergeUtil = new MergeUtil(
                resultObject,
                libraryDAO,
                extraDataDAO,
                campusMapDAO)
        if (useHttpCampusMap) {
            mergeUtil.mergeCampusMapData()
        }
        if (filename != "services.json") {
            mergeUtil.merge() // only applies to locations
            mergeUtil.populate() // only applies to locations for now?
            mergeUtil.appendRelationshipsToLocations()
        } else {
            mergeUtil.appendRelationshipsToServices()
        }

        data.each {
            def indexAction = [index: [_id: it.id]]
            jsonESInput << mapper.writeValueAsString(indexAction) + "\n"
            jsonESInput << mapper.writeValueAsString(it) + "\n"
        }

        resultObject
    }
}

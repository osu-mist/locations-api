package edu.oregonstate.mist.locations

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.db.ArcGisDAO
import edu.oregonstate.mist.locations.db.CachedFacilDAO
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.ExtensionDAO
import edu.oregonstate.mist.locations.db.ExtraDataManager
import edu.oregonstate.mist.locations.db.LibraryDAO
import edu.oregonstate.mist.locations.db.LocationDAO
import io.dropwizard.cli.EnvironmentCommand
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import org.apache.http.client.HttpClient
import org.skife.jdbi.v2.DBI

@groovy.transform.TypeChecked
class LocationCommand extends EnvironmentCommand<LocationConfiguration> {
    // Note: without this configure line, mapper.writeValue will close the file after writing.
    ObjectMapper mapper = new ObjectMapper()
        .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
    private ArcGisDAO arcGisDAO
    private CampusMapDAO campusMapDAO
    private DiningDAO diningDAO
    private ExtensionDAO extensionDAO
    private ExtraDataDAO extraDataDAO
    private CachedFacilDAO cachedFacilDAO
    private LibraryDAO libraryDAO
    private LocationDAO locationDAO

    private MergeUtil mergeUtil

    private ExtraDataManager extraDataManager

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
        final Cache cache = new Cache(configMap)

        extraDataManager = new ExtraDataManager()
        // Managed objects are tied to the lifecycle of the http server.
        // We aren't starting an http server, so we have to start the manager manually.
        //environment.lifecycle().manage(extraDataManager)
        extraDataManager.start()

        arcGisDAO = new ArcGisDAO(configMap, cache)
        campusMapDAO = new CampusMapDAO(configMap, cache)
        diningDAO = new DiningDAO(configuration, cache)
        extensionDAO = new ExtensionDAO(configMap, cache)
        extraDataDAO = new ExtraDataDAO(configuration, cache, extraDataManager)
        cachedFacilDAO = new CachedFacilDAO(jdbi, cache, configMap)
        libraryDAO = new LibraryDAO(configMap, httpClient, cache)
        locationDAO = new LocationDAO(configMap)

        mergeUtil = new MergeUtil(libraryDAO, extraDataDAO, campusMapDAO)

        writeJsonAPIToFile("services.json", getServices())
        writeJsonAPIToFile("locations-combined.json", getCombinedData())

    }

    /**
     * Returns the combined data from the arcgis and facil sources.
     *
     * @return
     */
    private List getBuildingData() {
        List<FacilLocation> buildings = cachedFacilDAO.getBuildings()
        // Get acrgis gender inclusive restroom data from json file
        def genderInclusiveRR = arcGisDAO.getGenderInclusiveRR()
        // Get arcgis coordinate data from json file
        def arcGisGeometries = locationDAO.getArcGisCoordinates()

        def buildingAndArcGisMerged = locationDAO.mergeFacilAndArcGis(buildings,
            genderInclusiveRR, arcGisGeometries)

        new ArrayList<FacilLocation>(buildingAndArcGisMerged.values())
    }

    List<ResourceObject> getCombinedData() {
        def locationsList = []
        locationsList.addAll(getBuildingData())
        locationsList.addAll(extraDataManager.extraData.locations)
        locationsList.addAll(diningDAO.getDiningLocations())
        locationsList.addAll(extensionDAO.getExtensionLocations())
        locationsList.addAll(extraDataDAO.getLocations())
        locationsList.addAll(locationDAO.getParkingLocations())

        def data = locationsList.collect { locationDAO.convert(it) }

        data = mergeUtil.mergeCampusMapData(data)
        data = mergeUtil.merge(data) // only applies to locations
        data = mergeUtil.populate(data) // only applies to locations for now?
        data = mergeUtil.appendRelationshipsToLocations(data)

        data
    }

    List<ResourceObject> getServices() {
        def serviceList = extraDataDAO.getServices()
        def data = serviceList.collect { locationDAO.convert(it) }

        data = mergeUtil.appendRelationshipsToServices(data)

        data
    }

    /**
     * Writes a list of result objects to a json file, one record per line
     *
     * @param filename
     * @param locationsList
     * @return
     */
    private void writeJsonAPIToFile(String filename, List<ResourceObject> data) {
        def out = new File(filename).newWriter()

        data.each { ResourceObject it ->
            mapper.writeValue(out, it)
            out.newLine()
        }

        out.close()
    }
}

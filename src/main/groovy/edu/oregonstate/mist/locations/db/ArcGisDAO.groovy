package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.AdaEntriesLocation
import edu.oregonstate.mist.locations.core.AdaEntry
import edu.oregonstate.mist.locations.core.AedInventoriesLocation
import edu.oregonstate.mist.locations.core.AedInventory
import edu.oregonstate.mist.locations.core.GenderInclusiveRRLocation
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TypeChecked
class ArcGisDAO {

    Logger logger = LoggerFactory.getLogger(ArcGisDAO.class)

    private ObjectMapper mapper = new ObjectMapper()

    /**
     * Url of ArcGIS API. JSON formatted content that includes bldID,
     * bldNam, bldNamAbr, latitude, longitude
     */
    private final String arcGisGenderInclusiveRRUrl
    private final String arcGisAdaEntriesUrl
    private final String arcGisAedInventoryUrl

    /**
     * File where the arcgis data is downloaded to
     */
    private static final String CACHE_FILENAME = "gender-inclusive-rr.json"

    private final int ARC_GIS_THRESHOLD

    /**
     * Helper for caching and getting data from web requests
     */
    private final Cache cache

    public ArcGisDAO(Map<String, String> locationConfiguration, Cache cache) {
        arcGisGenderInclusiveRRUrl = locationConfiguration.get("arcGisGenderInclusiveRR")
        arcGisAdaEntriesUrl = locationConfiguration.get("arcGisAdaEntries")
        arcGisAedInventoryUrl = locationConfiguration.get("arcGisAedInventory")
        ARC_GIS_THRESHOLD = locationConfiguration.get("arcGisThreshold").toInteger()
        this.cache = cache
    }

    public HashMap<String, GenderInclusiveRRLocation> getGenderInclusiveRR() {
        //@todo we should expect application/json, but arcgis actually returns text/plain. oops
        cache.withDataFromUrlOrCache(arcGisGenderInclusiveRRUrl, CACHE_FILENAME) {
            String gisData -> mapRR(gisData)
        }
    }

    HashMap<String, GenderInclusiveRRLocation> mapRR(String gisData) {
        def mappedData = mapper.readTree(gisData).get("features")
        def data = new HashMap<String, GenderInclusiveRRLocation>()
        LocationUtil.checkThreshold(mappedData.size(),
                ARC_GIS_THRESHOLD, "gender inclusive restrooms")
        mappedData.asList().each {
            def rr = mapper.readValue(it.get("attributes").toString(), GenderInclusiveRRLocation)
            data[rr.bldID] = rr
        }

        data
    }

    HttpURLConnection openHttpUrlConnection(URL url) {
        (HttpURLConnection)url.openConnection()
    }

    String getURL(String url) {
        def conn = openHttpUrlConnection(new URL(url))
        int code = conn.getResponseCode()
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP status code ${code} returned for url ${url}")
        }
        conn.getInputStream().withStream { stream ->
            stream.getText()
        }
    }

    public HashMap<String, AdaEntriesLocation> getAdaEntries() {
        def rawData = getURL(arcGisAdaEntriesUrl)
        def mappedData = mapper.readTree(rawData).get("features")
        def data = new HashMap<String, AdaEntriesLocation>()

        mappedData.asList().each {
            def rr = mapper.readValue(it.get("attributes").toString(), AdaEntriesLocation)
            if (!data.containsKey(rr.bldID)) {
                data[rr.bldID] = rr
            }
            data[rr.bldID].adaEntries.add(
                new AdaEntry(
                    accessible: rr.accessible == "Y",
                    lon: rr.lon as Double,
                    lat: rr.lat as Double,
                    function: rr.function
                )
            )
        }

        data
    }

    public HashMap<String, AedInventoriesLocation> getAedInventories() {
        def rawData = getURL(arcGisAedInventoryUrl)
        def mappedData = mapper.readTree(rawData).get("features")
        def data = new HashMap<String, AedInventoriesLocation>()

        mappedData.asList().each {
            def rr = mapper.readValue(it.get("attributes").toString(), AedInventoriesLocation)

            if (rr.bldg) {
                rr.bldID = rr.bldg.padLeft(4, "0")
                if (!data.containsKey(rr.bldID)) {
                    data[rr.bldID] = rr
                }
                data[rr.bldID].aedInventories.add(
                    new AedInventory(
                        lon: rr.lon as Double,
                        lat: rr.lat as Double,
                        location: rr.location,
                        floor: rr.floor as Integer,
                        make: rr.make,
                        model: rr.model,
                        serialNo: rr.serialNo,
                        departmentOwner: rr.departmentOwner,
                        contact: rr.contact,
                    )
                )
            }
        }

        data
    }
}

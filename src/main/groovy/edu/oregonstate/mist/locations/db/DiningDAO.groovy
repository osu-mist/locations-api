package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.DiningLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The Dining data comes from google calendar
 */
public class DiningDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiningDAO.class)
    private static final ObjectMapper MAPPER = new ObjectMapper()

    /**
     * Url of Dining API. JSON formatted content that includes lat/long,
     * name, calendar id, and today's hours for dining locations.
     */
    private final String uhdsURL

    /**
     * Url format of ical calendar that includes open hours for dining
     */
    private final String icalURL

    /**
     * Filename to store the uhdsURL cache data
     */
    private final String diningJsonOut

    private final LocationUtil locationUtil

    public DiningDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        uhdsURL = locationConfiguration.get("uhdsUrl")
        icalURL = locationConfiguration.get("icalUrl")
        diningJsonOut = locationConfiguration.get("diningJsonOut")
        this.locationUtil = locationUtil
    }

    List<DiningLocation> getDiningLocations() {
        getDiningLocations(uhdsURL, locationUtil)
    }

    List<DiningLocation> getDiningLocations(String uhdsURL, LocationUtil locationUtil) {
        String diningData = getDiningLocationList()

        List<DiningLocation> diners =
                MAPPER.readValue(diningData, new TypeReference<List<DiningLocation>>(){})

        // the json datasource lists the location multiple time if it's open twice a day
        diners.unique(true)

        IcalUtil.getLocationsHours(diners, icalURL, locationUtil)
        //@todo: how to deal with html in title?

        //@todo: need a flag to know if it's the first time in the day that we have flagged

        diners
    }

    /**
     * Gets the list of dining locations from UHDS.
     * Tries to get the data from the web, if it fails
     * it reads it from the cache.
     *
     * @return String json format of dining locations
     */
    private String getDiningLocationList() throws Exception {
        locationUtil.getDataFromUrlOrCache(uhdsURL, diningJsonOut)
    }
}

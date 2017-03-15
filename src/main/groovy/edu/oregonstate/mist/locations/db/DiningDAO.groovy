package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.DiningLocation
import groovy.transform.InheritConstructors

/**
 * The Dining data comes from google calendar
 */
@InheritConstructors
public class DiningDAO extends IcalDAO {
    List<DiningLocation> getDiningLocations() {
        getDiningLocations(locationUtil)
    }

    List<DiningLocation> getDiningLocations(LocationUtil locationUtil) {
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
        locationUtil.getDataFromUrlOrCache(metadataURL, jsonOut)
    }
}

package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ServiceLocation
import groovy.transform.InheritConstructors

/**
 * The Dining data comes from google calendar
 */
@InheritConstructors
public class DiningDAO extends IcalDAO {
    List<ServiceLocation> getDiningLocations() {
        String diningData = getDiningLocationList()

        List<ServiceLocation> diners =
                MAPPER.readValue(diningData, new TypeReference<List<ServiceLocation>>(){})

        // the json datasource lists the location multiple time if it's open twice a day
        diners.unique(true)
        diners.each { it.type = Constants.TYPE_DINING }

        IcalUtil.addLocationHours(diners, icalURL, locationUtil)
        //@todo: how to deal with html in title?

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

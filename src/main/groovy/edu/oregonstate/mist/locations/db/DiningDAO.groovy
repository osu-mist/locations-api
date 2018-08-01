package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ServiceLocation
import groovy.transform.InheritConstructors
import groovy.transform.PackageScope

/**
 * The Dining data comes from google calendar
 */
@InheritConstructors
public class DiningDAO extends IcalDAO {

    private final int DINING_THRESHOLD =
            configuration.locationsConfiguration.get("diningThreshold").toInteger()

    List<ServiceLocation> getDiningLocations() {

        List<ServiceLocation> diners = cache.withJsonFromUrlOrCache(metadataURL, jsonOut) {
            diningData -> mapDiningLocations(diningData)
        }

        if (diners.isEmpty()) {
            throw new DAOException("found zero dining locations")
        }

        // the json datasource lists the location multiple time if it's open twice a day
        diners.unique(true)
        diners.each { it.type = Constants.TYPE_DINING }

        IcalUtil.addLocationHours(diners, icalURL, cache)
        //@todo: how to deal with html in title?

        diners
    }

    @PackageScope
    List<ServiceLocation> mapDiningLocations(String diningData) {
        List<ServiceLocation> locations = MAPPER.readValue(
                diningData, new TypeReference<List<ServiceLocation>>(){}
        )
        LocationUtil.checkThreshold(locations.size(), DINING_THRESHOLD, "dining locations")
        locations
    }

    @Override
    String getJsonOut() {
        this.configuration.locationsConfiguration.get("diningJsonOut")
    }
}

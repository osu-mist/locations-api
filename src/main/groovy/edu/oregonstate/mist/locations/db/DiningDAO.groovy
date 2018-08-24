package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ServiceLocation
import groovy.transform.InheritConstructors
import groovy.transform.PackageScope
import org.apache.http.client.utils.URIBuilder

/**
 * The Dining data comes from google calendar
 */
@InheritConstructors
public class DiningDAO extends IcalDAO {

    private final int DINING_THRESHOLD =
            configuration.locationsConfiguration.get("diningThreshold").toInteger()
    private final String WEEKLY_MENU_URL = configuration.locationsConfiguration.get("weeklyMenuUrl")
    private static final String CACHE_FILENAME = "dining.json"

    List<ServiceLocation> getDiningLocations() {

        List<ServiceLocation> diners = cache.withJsonFromUrlOrCache(metadataURL, CACHE_FILENAME) {
            diningData -> mapDiningLocations(diningData)
        }

        if (diners.isEmpty()) {
            throw new DAOException("found zero dining locations")
        }

        URIBuilder uriBuilder = new URIBuilder(WEEKLY_MENU_URL)

        // the json datasource lists the location multiple time if it's open twice a day
        diners.unique(true)
        diners.each {
            it.type = Constants.TYPE_DINING
            if(it.locId != null) {
                uriBuilder.setParameter("loc", it.locId)
                it.weeklyMenu = uriBuilder.build().toString()
            }
        }

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
}

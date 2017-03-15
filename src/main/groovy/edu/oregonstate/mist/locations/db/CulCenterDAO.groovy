package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ServiceLocation
import groovy.transform.InheritConstructors

/**
 * The Cultural Center data comes from google calendar
 */
@InheritConstructors
public class CulCenterDAO extends IcalDAO {
    /**
     * Filename to store the metadataURL cache data
     */
    private final String culCenterJsonOut

    //@todo: need a way to specify uhdsUrl and jsonOut specific to dao
    //@todo: need to rename dininglocation to a icalLocation??

    List<ServiceLocation> getCulCenterLocations() {
        getCulCenterLocations(locationUtil)
    }

    List<ServiceLocation> getCulCenterLocations(LocationUtil locationUtil) {
        String plainData = getCulCenterLocationList()

        List<ServiceLocation> centers =
                MAPPER.readValue(plainData, new TypeReference<List<ServiceLocation>>(){})

        // the json datasource lists the location multiple time if it's open twice a day
        centers.unique(true)

        //@todo: the method call below modifies the passing object can we change that so that
        // it just returns the needed data???
        IcalUtil.getLocationsHours(centers, icalURL, locationUtil)

        centers
    }

    /**
     * Gets the list of dining locations from UHDS.
     * Tries to get the data from the web, if it fails
     * it reads it from the cache.
     *
     * @return String json format of dining locations
     */
    private String getCulCenterLocationList() throws Exception {
        //@todo: return something hardcoded for now
        """
[

]
"""
    }
}
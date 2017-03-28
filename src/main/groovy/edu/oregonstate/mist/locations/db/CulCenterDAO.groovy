package edu.oregonstate.mist.locations.db

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

    List<ServiceLocation> getCulCenterLocations(Closure filter) {
        List<ServiceLocation> centers = getCulCenterLocationList(filter)

        IcalUtil.addLocationHours(centers, icalURL, locationUtil)
        centers
    }

    /**
     * Gets the list of locations from config file
     *
     */
    private List<ServiceLocation> getCulCenterLocationList(Closure filter) throws Exception {
        configuration.calendars.findAll(filter).collect {
            new ServiceLocation(
                    conceptTitle: it.id,
                    calendarId: it.calendarId,
                    merge: it.merge,
                    parent: it.parent,
                    tags: it.tags,
                    type: it.type
            )
        }
    }
}
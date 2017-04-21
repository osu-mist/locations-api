package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.LocationConfiguration
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ServiceLocation
import groovy.transform.InheritConstructors

/**
 * The Cultural Center and services data comes from google calendar. It interacts with the
 * ExtraDataManager to get the metadata.
 */
@InheritConstructors
public class ExtraDataDAO extends IcalDAO {
    ExtraDataManager extraDataManager

    ExtraDataDAO(LocationConfiguration configuration, LocationUtil locationUtil,
                 ExtraDataManager extraDataManager) {
        super(configuration, locationUtil)
        this.extraDataManager = extraDataManager
    }

    List<ServiceLocation> getLocations() {
        getExtraDataLocations( { !it.tags.contains("services") } )
    }

    List<ServiceLocation> getServices() {
        getExtraDataLocations( { it.tags.contains("services") } )
    }

    private List<ServiceLocation> getExtraDataLocations(Closure filter) {
        List<ServiceLocation> centers = getServiceLocationList(filter)

        IcalUtil.addLocationHours(centers, icalURL, locationUtil)
        centers
    }

    /**
     * Gets the list of locations from extra-data file
     *
     */
    private List<ServiceLocation> getServiceLocationList(Closure filter) throws Exception {
        extraDataManager.extraData.calendars.findAll(filter)?.collect {
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

    /**
     * Returns extra data locations defined as tags: services without hours populated.
     *
     * @return
     */
    public List<ServiceLocation> getLazyServices() {
          getServiceLocationList { it.tags.contains("services") }
    }
}
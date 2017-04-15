package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.oregonstate.mist.locations.Constants

class ServiceAttributes {
    String name
    List<String> tags = []
    Map<Integer, List<DayOpenHours>> openHours = new HashMap<Integer, List<DayOpenHours>>()
    String type // used for searching. values: building, dining.

    /**
     * Used to calculate relationships between objects
     */
    String parent

    /**
     * Used so that we can map the relationships back to the jsonapi object of the parent
     * location.
     */
    String locationId

    /**
     * Used to calculate if the object holds only hours / metadata that needs to be merged
     */
    @JsonIgnore
    Boolean merge = false

    String getLocationId() {
        if (parent) {
            BaseType.calculateId(Constants.TYPE_BUILDING, parent)
        }
    }
}

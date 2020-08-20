package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.oregonstate.mist.locations.Constants
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
/**
 * FacilLocation represents a building from the FACIL_LOCATION database table
 */
class FacilLocation extends BaseType {
    String type = Constants.TYPE_BUILDING

    String bldgID
    String abbreviation
    String name
    String campus
    String address1
    String address2
    String city
    String state
    String zip

    // Fields needed to merge FacilLocation and arcGisLocation
    String latitude
    String longitude
    def coordinates
    String coordinatesType
    Integer giRestroomCount
    String giRestroomLimit
    String giRestroomLocations
    Boolean adaEntriesAccessable
    List<AdaEntry> adaEntries = []
    List<AedInventory> aedInventories = []

    @JsonIgnore
    Integer getGiRestroomCount() {
        giRestroomCount ?: 0
    }

    @JsonIgnore
    String getGiRestroomLocations() {
        giRestroomLocations?.trim()
    }

    @JsonIgnore
    @Override
    protected String getIdField() {
        bldgID
    }

    @JsonIgnore
    String getAddress() {
        address2 ? address1 + "\n" + address2 : address1
    }

    @JsonIgnore
    String getPrettyCampus() {
        if (!campus) {
            null
        } else if (campus.equalsIgnoreCase("CASCADESCAMPUS")) {
            Constants.CAMPUS_CASCADES
        } else if (campus.equalsIgnoreCase("OSUCORVALLIS")) {
            Constants.CAMPUS_CORVALLIS
        } else if (campus.equalsIgnoreCase("HMSC")) {
            Constants.CAMPUS_HMSC
        } else {
            Constants.CAMPUS_OTHER
        }
    }

    // Note: FacilLocation is seralized to json as part of the caching infrastructure.
    // If you add any more accessor methods above, remember to mark them as
    // @JsonIgnore or else deserialization will fail
}

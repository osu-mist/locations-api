package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

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

    @Override
    protected String getIdField() {
        abbreviation ?: name
    }

    String getAddress() {
        address2 ? address1 + "\n" + address2 : address1
    }

    String getPrettyCampus() {
        if (campus.equalsIgnoreCase("CASCADESCAMPUS")) {
            "Cascades"
        } else if (campus.equalsIgnoreCase("OSUCORVALLIS")) {
            "Corvallis"
        } else if (campus.equalsIgnoreCase("HMSC")) {
            "HMSC"
        } else {
            "Other"
        }
    }
}

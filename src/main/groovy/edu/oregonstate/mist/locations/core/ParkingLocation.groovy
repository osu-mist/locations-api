package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

class ParkingLocation extends BaseType {
    String type = Constants.TYPE_PARKING

    String description
    String propID
    String parkingZoneGroup
    def coordinates
    String coordinatesType

    @Override
    protected String getIdField() {
        propID
    }

    ParkingLocation(def arcGisMap) {
        this.description = arcGisMap['properties']['AiM_Desc']
        this.propID = arcGisMap['properties']['Prop_ID']
        this.parkingZoneGroup = arcGisMap['properties']['ZoneGroup']
        this.coordinates = arcGisMap['geometry']['coordinates']
        this.coordinatesType = arcGisMap['geometry']['type']
    }
}

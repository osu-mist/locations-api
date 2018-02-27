package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

class ParkingLocation extends BaseType {
    String type = Constants.TYPE_PARKING

    String description
    String propID
    String parkingZoneGroup
    def coordinates
    String coordinatesType
    String latitude
    String longitude

    @Override
    protected String getIdField() {
        propID + parkingZoneGroup
    }

    ParkingLocation(def arcGisMap) {
        this.description = arcGisMap['properties']['AiM_Desc']
        this.propID = arcGisMap['properties']['Prop_ID']
        this.parkingZoneGroup = arcGisMap['properties']['ZoneGroup']
        this.latitude = arcGisMap['properties']['Cent_Lat']
        this.longitude = arcGisMap['properties']['Cent_Lon']

        if (arcGisMap['geometry']) {
            this.coordinates = arcGisMap['geometry']['coordinates']
            this.coordinatesType = arcGisMap['geometry']['type']
        }
    }
}

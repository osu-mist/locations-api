package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

class ArcGisLocation {
    String type = Constants.TYPE_BUILDING

    String bldID
    String bldNam
    String bldNamAbr
    String latitude
    String longitude
    def coordinates
    String coordinatesType

    ArcGisLocation(def arcGisMap) {
        this.bldID = arcGisMap['properties']['BldID']
        this.bldNam = arcGisMap['properties']['BldNam']
        this.bldNamAbr = arcGisMap['properties']['BldNamAbr']
        this.latitude = arcGisMap['properties']['Cent_Lat']
        this.longitude = arcGisMap['properties']['Cent_Lon']
        this.coordinates = arcGisMap['geometry']['coordinates']
        this.coordinatesType = arcGisMap['geometry']['type']
    }
}

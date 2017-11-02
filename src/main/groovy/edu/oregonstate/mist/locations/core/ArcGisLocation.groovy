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

    static ArcGisLocation fromJson(def arcGisMap) {
        def obj = new ArcGisLocation()
        obj.bldID = arcGisMap['properties']['BldID']
        obj.bldNam = arcGisMap['properties']['BldNam']
        obj.bldNamAbr = arcGisMap['properties']['BldNamAbr']
        obj.latitude = arcGisMap['properties']['Cent_Lat']
        obj.longitude = arcGisMap['properties']['Cent_Lon']
        obj.coordinates = arcGisMap['geometry']['coordinates']
        obj.coordinatesType = arcGisMap['geometry']['type']
        return obj
    }
}

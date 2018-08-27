package edu.oregonstate.mist.locations.core

@groovy.transform.EqualsAndHashCode
class Attributes extends ServiceAttributes {
    String abbreviation
    GeoLocation geoLocation
    Geometry geometry
    String summary
    String description
    String descriptionHTML
    String address
    String city
    String state
    String zip
    String county
    String telephone
    String fax
    List<String> thumbnails = []
    List<String> images = []
    List<String> departments =[]
    String website
    Integer sqft
    String calendar
    String campus
    Integer giRestroomCount
    Boolean giRestroomLimit
    String giRestroomLocations
    List<String> synonyms = []
    String bldgID
    String parkingZoneGroup
    String propID
    Integer adaParkingSpaceCount
    Integer motorcycleParkingSpaceCount
    Integer evParkingSpaceCount
}

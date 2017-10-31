package edu.oregonstate.mist.locations.core

@groovy.transform.EqualsAndHashCode
class Attributes extends ServiceAttributes {
    String abbreviation
    GeoLocation geoLocation
    Geometry geometry
    String summary
    String description
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

    int getHashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0)
        result = 31 * result + (summary != null ? summary.hashCode() : 0)
        result = 31 * result + (website != null ? website.hashCode() : 0)
        result = 31 * result + (address != null ? address.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (telephone != null ? telephone.hashCode() : 0)
        result = 31 * result + (bldgID != null ? bldgID.hashCode() : 0)
        result = 31 * result + (parkingZoneGroup != null ? parkingZoneGroup.hashCode() : 0)
        result = 31 * result + (propID != null ? propID.hashCode() : 0)

        Math.abs(result)
    }
}

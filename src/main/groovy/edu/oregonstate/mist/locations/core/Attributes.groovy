package edu.oregonstate.mist.locations.core

@groovy.transform.EqualsAndHashCode
class Attributes {
    String name
    String abbreviation
    GeoLocation geoLocation
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
    List<String> departments
    String website
    Integer sqft
    String calendar
    String campus
    String type // used for searching. values: building, dining.
    Map<Integer, List<DayOpenHours>> openHours = new HashMap<Integer, List<DayOpenHours>>()
    int getHashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0)
        result = 31 * result + (website != null ? website.hashCode() : 0)
        result = 31 * result + (address != null ? address.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (telephone != null ? telephone.hashCode() : 0)

        Math.abs(result)
    }
}

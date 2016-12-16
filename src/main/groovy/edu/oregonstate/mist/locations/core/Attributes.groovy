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
    List<String> thumbnails
    List<String> images
    List<String> departments
    String website
    Integer sqft
    String calendar
    String campus
    String type // used for searching. values: building, dining.
    Map<Integer, List<DayOpenHours>> openHours = new HashMap<Integer, List<DayOpenHours>>()
}

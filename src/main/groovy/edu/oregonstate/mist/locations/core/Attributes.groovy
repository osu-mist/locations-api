package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnore

@groovy.transform.EqualsAndHashCode
class Attributes extends ServiceAttributes {
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
}

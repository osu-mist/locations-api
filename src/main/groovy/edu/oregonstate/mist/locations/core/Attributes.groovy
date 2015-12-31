package edu.oregonstate.mist.locations.core

class Attributes {
    String name
    String abbreviation
    String latitude
    String longitude
    String summary
    String description
    String address
    String city
    String state
    String zip
    String telephone
    String fax
    List<String> thumbnails
    List<String> images
    List<String> departments
    String website
    Integer sqft
    String calendar
    String campus
    HashMap<Integer, List<DayOpenHours>> openHours = new HashMap<Integer, List<DayOpenHours>>()
}

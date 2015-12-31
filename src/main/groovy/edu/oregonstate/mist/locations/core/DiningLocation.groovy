package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty

class DiningLocation implements Comparable {
    @JsonProperty("concept_title")
    String conceptTitle
    String zone
    @JsonProperty("calendar_id")
    String calendarId
    @JsonProperty("concept_coord")
    String conceptCoord //"latitude, longitude"
    String start
    String end
    HashMap<Integer, List<DayOpenHours>> openHours = new HashMap<Integer, List<DayOpenHours>>()

    private final Integer LATITUDE_INDEX = 0
    private final Integer LONGITUDE_INDEX = 1

    public String getLatitude() {
        getToken(LATITUDE_INDEX)
    }

    public String getLongitude() {
        getToken(LONGITUDE_INDEX)
    }

    private void getToken(Integer index) {
        if (!conceptCoord) {
            return
        }
        conceptCoord.tokenize(",").get(index).trim()
    }

    @Override
    int compareTo(Object o) {
        calendarId <=> o.calendarId
    }

    @Override
    public String toString() {
        "DiningLocation{" +
                "conceptTitle='" + conceptTitle + '\'' +
                ", zone='" + zone + '\'' +
                ", calendarId='" + calendarId + '\'' +
                ", conceptCoord='" + conceptCoord + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}'
    }
}
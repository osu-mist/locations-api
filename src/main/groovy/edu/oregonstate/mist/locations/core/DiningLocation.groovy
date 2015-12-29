package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty

class DiningLocation implements Comparable {
    @JsonProperty("concept_title")
    String conceptTitle
    String zone
    @JsonProperty("calendar_id")
    String calendarId
    @JsonProperty("concept_coord")
    String conceptCoord
    String start
    String end
    HashMap<Integer, List<OpenHours>> weekOpenHours = new HashMap<Integer, List<OpenHours>>()

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
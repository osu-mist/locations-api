package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.locations.Constants

class ServiceLocation extends BaseType implements Comparable {
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
    List<String> tags = []
    String parent
    Boolean merge = false
    String type = Constants.TYPE_OTHER

    private final Integer LATITUDE_INDEX = 0
    private final Integer LONGITUDE_INDEX = 1

    public String getLatitude() {
        getCoordToken(LATITUDE_INDEX)
    }

    public String getLongitude() {
        getCoordToken(LONGITUDE_INDEX)
    }

    private String getCoordToken(Integer index) {
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
    protected String getIdField() {
        conceptTitle
    }
}

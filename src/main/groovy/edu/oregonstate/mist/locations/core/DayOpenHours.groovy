package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnore

class DayOpenHours {
    Date start
    Date end
    /**
     * Property used to identify events in a series.
     *
     * An event can be overwritten by a new event by using the same UID with a
     * different sequence
     *
     */
    @JsonIgnore
    String uid
    @JsonIgnore
    Integer sequence

    @Override
    public String toString() {
        "OpenHours{" +
                "start=" + start +
                ", end=" + end +
                ", uid='" + uid + '\'' +
                ", sequence=" + sequence +
                '}'
    }
}

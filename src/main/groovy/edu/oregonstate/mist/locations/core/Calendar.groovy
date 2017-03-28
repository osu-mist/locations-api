package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.NotEmpty

class Calendar {
    @NotEmpty
    String id
    @NotEmpty
    String calendarId
    Boolean merge = false
    List<String> tags
    String parent
    String type
}

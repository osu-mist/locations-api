package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represent the data that the library API returns
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class LibraryHours {
    String open
    String close

    @JsonProperty("string_date")
    String stringDate

    @JsonProperty("sortable_date")
    String sortableDate

    @JsonProperty("formatted_hours")
    String formattedHours

    @JsonProperty("open_all_day")
    Boolean openAllDay

    @JsonProperty("closes_at_night")
    Boolean closesAtNight
}

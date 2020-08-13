package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
class AdaEntriesLocation {
    @JsonProperty("BldID")
    String bldID
    @JsonProperty("Lat")
    String lat
    @JsonProperty("Lon")
    String lon
    @JsonProperty("Accessable")
    String accessible
    List<AdaEntry> adaEntries = []
}

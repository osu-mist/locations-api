package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
class GenderInclusiveRRLocation {
    @JsonProperty("BldID")
    String bldID
    @JsonProperty("BldNam")
    String bldNam
    @JsonProperty("BldNamAbr")
    String bldNamAbr
    @JsonProperty("CntAll")
    Integer giRestroomCount
    @JsonProperty("Limits")
    String giRestroomLimit
    @JsonProperty("LocaAll")
    String giRestroomLocations
}

package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty

@groovy.transform.EqualsAndHashCode
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

    GenderInclusiveRRLocation(def arcGisMap) {
        this.bldID = arcGisMap.BldID
        this.bldNam = arcGisMap.BldNam
        this.bldNamAbr = arcGisMap.BldNamAbr
        this.giRestroomCount = arcGisMap.CntAll
        this.giRestroomLimit = arcGisMap.Limits
        this.giRestroomLocations = arcGisMap.LocaAll
    }
}

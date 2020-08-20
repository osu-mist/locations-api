package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
class AedInventoriesLocation {
    @JsonProperty("Bldg")
    String bldg
    String bldID
    @JsonProperty("Latitude")
    String lat
    @JsonProperty("Longitude")
    String lon
    @JsonProperty("PLACENAME")
    String location
    @JsonProperty("Floor")
    String floor
    @JsonProperty("Make")
    String make
    @JsonProperty("Model")
    String model
    @JsonProperty("SerialNo")
    String serialNo
    @JsonProperty("DeptOwner")
    String departmentOwner
    @JsonProperty("Contact")
    String contact
    List<AedInventory> aedInventories = []
}

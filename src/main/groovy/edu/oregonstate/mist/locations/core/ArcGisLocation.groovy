package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.locations.Constants

@groovy.transform.EqualsAndHashCode
class ArcGisLocation extends BaseType {
    String type = Constants.TYPE_BUILDING

    @JsonProperty("BldID")
    String bldID
    @JsonProperty("BldNam")
    String bldNam
    @JsonProperty("BldNamAbr")
    String bldNamAbr
    @JsonProperty("Latitude")
    String latitude
    @JsonProperty("Longitude")
    String longitude
    def coordinates
    String coordinatesType

    /**
     * Jackson wasn't cooperating to convert BldProperty to bldProperty
     * since we were using object casting + mapper.readValue.
     *
     * @param arcGisMap
     */
    ArcGisLocation(def arcGisMap) {
        this.bldID = arcGisMap.BldID
        this.bldNam = arcGisMap.BldNam
        this.bldNamAbr = arcGisMap.BldNamAbr
        this.latitude = arcGisMap.Latitude
        this.longitude = arcGisMap.Longitude
    }

    @Override
    protected String getIdField() {
        bldNamAbr ?: bldNam
    }
}

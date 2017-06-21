package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

@groovy.transform.EqualsAndHashCode
class CampusMapLocation extends BaseType {
    String type = Constants.TYPE_BUILDING

    Integer id
    String name
    String abbrev
    String longitude
    String latitude
    String layerId
    String layerNames
    String address
    String adaEntrance
    String shortDescription
    String description
    String thumbnail
    String largerImage
    def coordinates
    String coordinatesType
    Integer giRestroomCount
    String giRestroomLimit
    String giRestroomLocations

    @Override
    protected String getIdField() {
        abbrev ?: name
    }
}

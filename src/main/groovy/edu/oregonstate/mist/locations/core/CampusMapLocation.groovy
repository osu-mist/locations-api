package edu.oregonstate.mist.locations.core

@groovy.transform.EqualsAndHashCode
class CampusMapLocation extends BaseType {
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

    @Override
    protected String getIdField() {
        abbrev ?: name
    }
}

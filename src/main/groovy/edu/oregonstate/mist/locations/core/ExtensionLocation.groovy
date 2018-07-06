package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants

@groovy.transform.EqualsAndHashCode
class ExtensionLocation extends BaseType {
    private static final int LATITUDE_INDEX = 1
    private static final int LONGITUDE_INDEX = 0

    String geoLocation
    String groupName
    String streetAddress
    String city
    String state
    String zipCode
    String fax
    String tel
    String guid
    String county
    String locationUrl

    public String getLatitude() {
        getCoordToken(LATITUDE_INDEX)
    }

    public String getLongitude() {
        getCoordToken(LONGITUDE_INDEX)
    }

    private String getCoordToken(Integer index) {
        def matcher = geoLocation =~ Constants.VALID_LAT_LONG
        if (matcher.count) {
            matcher[index][0]
        }
    }

    @Override
    protected String getIdField() {
        guid
    }
}

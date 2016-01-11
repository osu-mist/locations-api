package edu.oregonstate.mist.locations.core

class ExtensionLocation {
    private static final String GEO_LOCATION_REGEX = "-?\\d+(\\.\\d+)?"
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
        def matcher = geoLocation =~ GEO_LOCATION_REGEX
        if (matcher.count) {
            matcher[index][0]
        }
    }
}

package edu.oregonstate.mist.locations

class Constants {
    // Valid values for Attributes.type
    public static final String TYPE_BUILDING = "building"
    public static final String TYPE_DINING = "dining"
    public static final String TYPE_OTHER = "other"
    public static final String TYPE_SERVICES = "services"
    public static final String TYPE_PARKING = "parking"

    // Locations frontend endpoint names,
    // used in constructing URLs
    public static final String SERVICES = "services"
    public static final String LOCATIONS = "locations"

    // Valid values for Attributes.campus
    public static final String CAMPUS_CORVALLIS = "Corvallis"
    public static final String CAMPUS_CASCADES = "Cascades"
    public static final String CAMPUS_HMSC = "HMSC"
    public static final String CAMPUS_EXTENSION = "Extension"
    public static final String CAMPUS_OTHER = "Other"

    // Regexp which matches a valid latitude or longitude value
    public static final String VALID_LAT_LONG = "-?\\d+(\\.\\d+)?"
}

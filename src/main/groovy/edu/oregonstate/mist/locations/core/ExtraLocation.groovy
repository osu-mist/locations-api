package edu.oregonstate.mist.locations.core

class ExtraLocation extends BaseType {
    String name
    String abbreviation
    String longitude
    String latitude
    String campus
    String type
    List<String> tags = []

    @Override
    protected String getIdField() {
        abbreviation ?: name
    }
}

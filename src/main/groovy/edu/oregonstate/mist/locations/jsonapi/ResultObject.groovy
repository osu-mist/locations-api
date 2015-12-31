package edu.oregonstate.mist.locations.jsonapi

class ResultObject {
    // optional field
    HashMap<String, String> links // @todo: this would be setup by the responding DW (current url contains filtering and other options + pagination)

    //required field
    def data
}

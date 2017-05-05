package edu.oregonstate.mist.locations.core

class Geometry {
    Double[][][] coordinates

    String getType() {
        if (coordinates.length == 1) {
            return "Polygon"
        } else if (coordinates.length > 1) {
            return "MultiPolygon"
        }
    }
}

package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil

abstract class BaseType {
    String type = Constants.TYPE_OTHER

    String calculateId() {
        LocationUtil.getMD5Hash(type + getIdField())
    }

    protected String getIdField() {
        throw new Exception("Failed to define id field on subclass.")
    }
}

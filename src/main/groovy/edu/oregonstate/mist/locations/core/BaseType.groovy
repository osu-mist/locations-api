package edu.oregonstate.mist.locations.core

import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseType {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseType.class)

    String type = Constants.TYPE_OTHER

    String calculateId() {
        calculateId(getType(), getIdField())
    }

    static String calculateId(String type, String id) {
        if (LOGGER.isDebugEnabled()) {
            type + id
        }  else {
            LocationUtil.getMD5Hash(type + id)
        }
    }

    protected String getIdField() {
        throw new Exception("Failed to define id field on subclass.")
    }
}

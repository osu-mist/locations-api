package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.db.DAOException

import java.security.MessageDigest

class LocationUtil {
    final Map<String, String> locationConfiguration

    LocationUtil(Map<String, String> locationConfiguration) {
        this.locationConfiguration = locationConfiguration
    }

    /**
     * Calculates MD5 Hash of a string
     *
     * @param content
     * @return
     */
    public static String getMD5Hash(String content) {
        MessageDigest.getInstance("MD5").digest(content.bytes).encodeHex().toString()
    }

    /**
     * Throws exception if a threshold is not satisfied
     *
     * @param numFound
     * @param threshold
     * @param type String detailing location types used in exception message
     */
    static void checkThreshold(int numFound, int threshold, String type) {
        if (numFound < threshold) {
            throw new DAOException("Found ${numFound} ${type}." +
                    " Not sufficient with threshold of ${threshold}")
        }
    }
}

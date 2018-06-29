package edu.oregonstate.mist.locations

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
}

package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ExtensionLocation
import groovy.transform.PackageScope

class ExtensionDAO {
    /**
     * D6 extension view that returns all locations via xml
     */
    private final String extensionUrl

    private static final String CACHE_FILENAME = "extension-locations.xml"

    private final int EXTENSION_THRESHOLD

    private final Cache cache

    ExtensionDAO(Map<String, String> locationConfiguration, Cache cache) {
        extensionUrl = locationConfiguration.get("extensionUrl")
        EXTENSION_THRESHOLD = locationConfiguration.get("extensionThreshold").toInteger()
        this.cache = cache
    }

    /**
     * Returns a list of extension locations from their D6 site
     *
     * @return
     */
    public List<ExtensionLocation> getExtensionLocations() {
        cache.withDataFromUrlOrCache(extensionUrl, CACHE_FILENAME) { extensionXML ->
            parseExtensionData(extensionXML)
        }
    }

    @PackageScope
    List<ExtensionLocation> parseExtensionData(String extensionXML) {
        def response = new XmlSlurper().parseText(extensionXML)
        LocationUtil.checkThreshold(response.item.size(),
                EXTENSION_THRESHOLD, "extension locations")
        response.item.collect {
            new ExtensionLocation(
                    geoLocation: it.GeoLocation,
                    groupName: it.GroupName,
                    streetAddress: it.StreetAddress,
                    city: it.City,
                    zipCode: it.ZIPCode,
                    fax: it.fax,
                    tel: it.tel,
                    guid: it.GUID,
                    county: it.county,
                    locationUrl: it.location_url
            )
        }
    }
}

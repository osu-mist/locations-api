package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.core.ExtensionLocation

class ExtensionDAO {
    /**
     * D6 extension view that returns all locations via xml
     */
    private final String extensionUrl

    private final String extensionXmlOut

    private final int EXTENSION_THRESHOLD

    private final Cache cache

    ExtensionDAO(Map<String, String> locationConfiguration, Cache cache) {
        extensionUrl = locationConfiguration.get("extensionUrl")
        extensionXmlOut = locationConfiguration.get("extensionXmlOut")
        EXTENSION_THRESHOLD = locationConfiguration.get("extensionThreshold").toInteger()
        this.cache = cache
    }

    /**
     * Returns a list of extension locations from their D6 site
     *
     * @return
     */
    public List<ExtensionLocation> getExtensionLocations() {
        cache.withDataFromUrlOrCache(extensionUrl, extensionXmlOut) { extensionXML ->
            parseExtensionData(extensionXML)
        }
    }

    private List<ExtensionLocation> parseExtensionData(String extensionXML) {
        def response = new XmlSlurper().parseText(extensionXML)
        int numFound = response.item.size()
        if (numFound < EXTENSION_THRESHOLD) {
            throw new DAOException("Found ${numFound} extension locations." +
                    " Not sufficient with threshold of ${EXTENSION_THRESHOLD}")
        }
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

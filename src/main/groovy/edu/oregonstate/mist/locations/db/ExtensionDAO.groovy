package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.core.ExtensionLocation

class ExtensionDAO {
    /**
     * D6 extension view that returns all locations via xml
     */
    private final String extensionUrl

    private final String extensionXmlOut

    private final Cache cache

    ExtensionDAO(Map<String, String> locationConfiguration, Cache cache) {
        extensionUrl = locationConfiguration.get("extensionUrl")
        extensionXmlOut = locationConfiguration.get("extensionXmlOut")
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

    static private List<ExtensionLocation> parseExtensionData(String extensionXML) {
        def node = new XmlSlurper().parseText(extensionXML)
        if (node.group.isEmpty()) {
            throw new DAOException("found zero extension locations")
        }
        node.group.collect {
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

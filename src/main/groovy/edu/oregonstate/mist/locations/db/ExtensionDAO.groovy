package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ExtensionLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExtensionDAO {
    /**
     * D6 extension view that returns all locations via xml
     */
    private final String extensionUrl

    private final String extensionXmlOut

    private final LocationUtil locationUtil

    ExtensionDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        extensionUrl = locationConfiguration.get("extensionUrl")
        extensionXmlOut = locationConfiguration.get("extensionXmlOut")
        this.locationUtil = locationUtil
    }

    /**
     * Returns a list of extension locations from their D6 site
     *
     * @return
     */
    public List<ExtensionLocation> getExtensionLocations() {
        String extensionXML = getExtensionData()
        def node = new XmlSlurper().parseText(extensionXML)
        List<ExtensionLocation> extensionLocations = new ArrayList<>()

        node.group.each {
            extensionLocations += new ExtensionLocation(
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

        extensionLocations
    }

    private String getExtensionData() {
        // Temporary fix for CO-1122: always pull from the cache, not the url
        //locationUtil.getDataFromUrlOrCache(extensionUrl, extensionXmlOut)
        locationUtil.getCachedData(extensionXmlOut)
    }
}

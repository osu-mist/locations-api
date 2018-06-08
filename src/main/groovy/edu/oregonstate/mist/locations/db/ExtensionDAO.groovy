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
        def response = new XmlSlurper().parseText(extensionXML)
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

    private String getExtensionData() {
        locationUtil.getDataFromUrlOrCache(extensionUrl, extensionXmlOut)
    }
}

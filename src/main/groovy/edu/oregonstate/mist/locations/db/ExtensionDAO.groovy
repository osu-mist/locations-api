package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.ExtensionLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExtensionDAO {
    /**
     * D6 extension view that returns all locations via xml
     */
    private final String extensionUrl

    ExtensionDAO(Map<String, String> locationConfiguration) {
        extensionUrl = locationConfiguration.get("extensionUrl")
    }

    /**
     * Returns a list of extension locations from their D6 site
     *
     * @return
     */
    public List<ExtensionLocation> getExtensionLocations() {
        String extensionXML = new URL(extensionUrl).text
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
}

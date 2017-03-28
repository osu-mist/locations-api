package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.core.GeoLocation
import edu.oregonstate.mist.locations.jsonapi.ResourceObject

import java.nio.charset.StandardCharsets

class LocationMapper  {
    String campusmapUrl
    String campusmapImageUrl
    String apiEndpointUrl

    public ResourceObject map(CampusMapLocation campusMapLocation) {
        Attributes attributes = new Attributes(
            name: campusMapLocation.name,
            abbreviation: campusMapLocation.abbrev,
            geoLocation: createGeoLocation(campusMapLocation.latitude,
                                            campusMapLocation.longitude),
            address: campusMapLocation.address,
            summary: campusMapLocation.shortDescription,
            description: campusMapLocation.description,
            thumbnails: [getImageUrl(campusMapLocation.thumbnail)] - null,
            images: [getImageUrl(campusMapLocation.largerImage)] - null,
            type: Constants.TYPE_BUILDING
        )

        // Some attribute fields are calculated based on campusmap information
        setCalculatedFields(attributes, campusMapLocation)

        def id = LocationUtil.getMD5Hash(Constants.CAMPUSMAP + campusMapLocation.id)
        buildResourceObject(id, attributes)
    }

    public ResourceObject map(ServiceLocation serviceLocation) {
        //@todo: move it somewhere else? call it something else?
        def summary = serviceLocation.zone ? "Zone: ${serviceLocation.zone}" : ''

        Attributes attributes = new Attributes(
            name: serviceLocation.conceptTitle,
            geoLocation: createGeoLocation(serviceLocation.latitude,
                                            serviceLocation.longitude),
            summary: summary,
            type: serviceLocation.type,
            campus: Constants.CAMPUS_CORVALLIS,
            openHours: serviceLocation.openHours,
            merge: serviceLocation.merge,
            tags: serviceLocation.tags,
            parent: serviceLocation.parent
        )

        //@todo: the value of dining below needs to change to: dining, cultural and recsports
        def id = LocationUtil.getMD5Hash(Constants.DINING + serviceLocation.calendarId)
        buildResourceObject(id, attributes)
    }

    public ResourceObject map(ExtensionLocation extensionLocation) {
        Attributes attributes = new Attributes(
            name: extensionLocation.groupName,
            geoLocation: createGeoLocation(extensionLocation.latitude,
                                           extensionLocation.longitude),
            address: extensionLocation.streetAddress,
            city: extensionLocation.city,
            state: extensionLocation.state,
            zip: extensionLocation.zipCode,
            telephone: extensionLocation.tel,
            fax: extensionLocation.fax,
            county: extensionLocation.county,
            website: extensionLocation.locationUrl,
            type: Constants.TYPE_BUILDING,
            campus: Constants.CAMPUS_EXTENSION,
        )

        def id = LocationUtil.getMD5Hash(Constants.EXTENSION + extensionLocation.guid)
        buildResourceObject(id, attributes)
    }

    public ResourceObject map(ArcGisLocation arcGisLocation) {
        Attributes attributes = new Attributes(
                name: arcGisLocation.bldNam,
                abbreviation: arcGisLocation.bldNamAbr,
                geoLocation: createGeoLocation(arcGisLocation.latitude,
                                               arcGisLocation.longitude),
                type: Constants.TYPE_BUILDING,
                campus: Constants.CAMPUS_CORVALLIS,
        )

        def id = LocationUtil.getMD5Hash(Constants.ARCGIS + arcGisLocation.bldID)
        buildResourceObject(id, attributes)
    }

    private String getCampusmapWebsite(Integer id) {
        campusmapUrl + id
    }

    private String getImageUrl(String image) {
        if (!image) {
            return null
        }

        campusmapImageUrl + URLEncoder.encode(image, StandardCharsets.UTF_8.toString())
    }

    /**
     * Sets the state, city, campus and website for the campusmap locations. The zip is not
     * set since some campusmap buildings have the 97330 or 97331 zip code. All campusmap
     * locations are from Corvallis and the campusmap url is well known
     *
     * @param attributes
     * @param campusMapLocation
     */
    private void setCalculatedFields(Attributes attributes, CampusMapLocation campusMapLocation) {
        attributes.state = "OR"
        attributes.city = "Corvallis"
        attributes.campus = Constants.CAMPUS_CORVALLIS
        attributes.website = getCampusmapWebsite(campusMapLocation.id)
    }

    private void setLinks(ResourceObject resourceObject) {
        resourceObject.links = ['self': apiEndpointUrl + "/" + resourceObject.id]
    }

    /**
     * Builds the ResourceObject and sets the links.self attribute
     *
     * @param id
     * @param attributes
     */
    private ResourceObject buildResourceObject(String id, Attributes attributes) {
        def resourceObject = new ResourceObject(id: id, type: "locations", attributes: attributes)
        setLinks(resourceObject)
        resourceObject
    }

    /**
     *  Create a GeoLocation object for Attributes
     * @param latitude
     * @param longitude
     * @return
     */
    private static GeoLocation createGeoLocation(String latitude, String longitude) {
        if (latitude != null && longitude != null && latitude.isDouble() && longitude.isDouble()) {
            return new GeoLocation(
                    lat: latitude as Double,
                    lon: longitude as Double
            )
        }
    }
}
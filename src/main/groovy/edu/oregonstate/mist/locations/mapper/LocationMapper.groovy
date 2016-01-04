package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.jsonapi.ResourceObject

import java.nio.charset.StandardCharsets

class LocationMapper  {
    public static final String CAMPUSMAP = "campusmap"
    public static final String DINING = "dining"

    String campusmapUrl
    String campusmapImageUrl

    public ResourceObject map(CampusMapLocation campusMapLocation) {
        Attributes attributes = new Attributes(
            name: campusMapLocation.name,
            abbreviation: campusMapLocation.abbrev,
            latitude: campusMapLocation.latitude,
            longitude: campusMapLocation.longitude,
            address: campusMapLocation.address,
            summary: campusMapLocation.shortDescription,
            description: campusMapLocation.description,
            thumbnails: [getImageUrl(campusMapLocation.thumbnail)],
            images: [getImageUrl(campusMapLocation.largerImage)]
        )

        // Some attribute fields are calculated based on campusmap information
        setCalculatedFields(attributes, campusMapLocation)

        def id = LocationUtil.getMD5Hash(CAMPUSMAP + campusMapLocation.id)
        new ResourceObject(id: id, type: "locations", attributes: attributes)
    }

    public ResourceObject map(DiningLocation diningLocation) {
        Attributes attributes = new Attributes(
            name: diningLocation.conceptTitle,
            latitude: diningLocation.latitude,
            longitude: diningLocation.longitude,
            summary: "Zone: ${diningLocation.zone}", //@todo: move it somewhere else? call it something else?
            openHours: diningLocation.openHours
        )

        def id = LocationUtil.getMD5Hash(DINING + diningLocation.calendarId)
        new ResourceObject(id: id, type: "locations", attributes: attributes)
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
        attributes.campus = "corvallis"
        attributes.website = getCampusmapWebsite(campusMapLocation.id)
    }
}
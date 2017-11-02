package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.core.ArcGisLocation
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.ExtraLocation
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.Geometry
import edu.oregonstate.mist.locations.core.ServiceAttributes
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.core.GeoLocation
import edu.oregonstate.mist.api.jsonapi.ResourceObject

import java.nio.charset.StandardCharsets

class LocationMapper  {
    String campusmapUrl
    String campusmapImageUrl
    String apiEndpointUrl

    public ResourceObject map(ServiceLocation serviceLocation) {
        // The ServiceLocation class is used for multiple types of data
        def attributes
        if(isService(serviceLocation)) {
            attributes = new ServiceAttributes(
                    name: serviceLocation.conceptTitle,
                    type: serviceLocation.type,
                    openHours: serviceLocation.openHours,
                    merge: serviceLocation.merge,
                    tags: serviceLocation.tags,
                    parent: serviceLocation.parent
            )

        } else {
            def summary = serviceLocation.zone ? "Zone: ${serviceLocation.zone}" : ''
            attributes = new Attributes(name: serviceLocation.conceptTitle,
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
        }

        buildResourceObject(serviceLocation.calculateId(), attributes)
    }

    private static boolean isService(def serviceLocation) {
        serviceLocation.type == Constants.TYPE_SERVICES
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

        buildResourceObject(extensionLocation.calculateId(), attributes)
    }

    public ResourceObject map(ArcGisLocation arcGisLocation) {
        Attributes attributes = new Attributes(
                name: arcGisLocation.bldNam,
                abbreviation: arcGisLocation.bldNamAbr,
                geoLocation: createGeoLocation(arcGisLocation.latitude,
                                               arcGisLocation.longitude),
                geometry: new Geometry(
                    coordinates: arcGisLocation.coordinates,
                    type: arcGisLocation.coordinatesType),
                type: Constants.TYPE_BUILDING,
                campus: Constants.CAMPUS_CORVALLIS,
                giRestroomCount: arcGisLocation.giRestroomCount,
                giRestroomLimit: getGiRestroomLimit(arcGisLocation.giRestroomLimit),
                giRestroomLocations: arcGisLocation.giRestroomLocations
        )

        buildResourceObject(arcGisLocation.calculateId(), attributes)
    }

    public ResourceObject map(ExtraLocation extraLocation) {
        Attributes attributes = new Attributes(
                name: extraLocation.name,
                //@todo: maybe we should leave the abbreviation blank?
                bldgID: extraLocation.bldgID,
                geoLocation: createGeoLocation(extraLocation.latitude,
                        extraLocation.longitude),
                type: extraLocation.type,
                campus: extraLocation.campus,
        )

        buildResourceObject(extraLocation.calculateId(), attributes)
    }

    ResourceObject map(FacilLocation facilLocation) {
        Attributes attributes = new Attributes(
                name: facilLocation.name,
                abbreviation: facilLocation.abbreviation,
                geoLocation: createGeoLocation(facilLocation.latitude,
                        facilLocation.longitude),
                geometry: new Geometry(
                        coordinates: facilLocation.coordinates,
                        type: facilLocation.coordinatesType),
                type: Constants.TYPE_BUILDING,
                campus: facilLocation.getPrettyCampus(),
                address: facilLocation.address,
                city: facilLocation.city,
                state: facilLocation.state,
                zip: facilLocation.zip,
                giRestroomCount: facilLocation.giRestroomCount,
                giRestroomLimit: getGiRestroomLimit(facilLocation.giRestroomLimit),
                giRestroomLocations: facilLocation.giRestroomLocations,
                bldgID: facilLocation.bldgID
        )

        buildResourceObject(facilLocation.calculateId(), attributes)
    }

    private void setLinks(ResourceObject resourceObject) {
        String resource = isService(resourceObject.attributes) ? Constants.SERVICES :
                Constants.LOCATIONS

        String selfUrl = "$apiEndpointUrl$resource/${resourceObject.id}"
        resourceObject.links = ['self': selfUrl]
    }

    /**
     * Builds the ResourceObject and sets the links.self attribute
     *
     * @param id
     * @param attributes
     */
    private ResourceObject buildResourceObject(String id, def attributes) {
        def type = isService(attributes) ? Constants.TYPE_SERVICES : "locations"
        def resourceObject = new ResourceObject(id: id, type: type, attributes: attributes)

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

    /**
     * Return boolean giRestroomLimit based on contents of string
     */
    private Boolean getGiRestroomLimit(String restroomLimitString) {
        if (restroomLimitString == null) {
            return null
        }

        restroomLimitString?.trim()
    }
}
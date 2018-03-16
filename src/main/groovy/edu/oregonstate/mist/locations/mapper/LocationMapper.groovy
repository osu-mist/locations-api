package edu.oregonstate.mist.locations.mapper

import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.ExtraLocation
import edu.oregonstate.mist.locations.core.FacilLocation
import edu.oregonstate.mist.locations.core.Geometry
import edu.oregonstate.mist.locations.core.ParkingLocation
import edu.oregonstate.mist.locations.core.ServiceAttributes
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.core.GeoLocation
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

@TypeChecked
class LocationMapper {
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
            String summary = serviceLocation.zone ? "Zone: ${serviceLocation.zone}" : ''
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

    @TypeChecked(TypeCheckingMode.SKIP)
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

    public ResourceObject map(ParkingLocation parkingLocation) {
        Attributes attributes = new Attributes(
                name: parkingLocation.description,
                parkingZoneGroup: parkingLocation.parkingZoneGroup,
                geometry: new Geometry(
                        coordinates: parkingLocation.coordinates,
                        type: parkingLocation.coordinatesType
                ),
                type: parkingLocation.type,
                campus: Constants.CAMPUS_CORVALLIS,
                propID: parkingLocation.propID,
                adaParkingSpaceCount : parkingLocation.adaParkingSpaceCount,
                motorcycleParkingSpaceCount : parkingLocation.motorcycleParkingSpaceCount,
                evParkingSpaceCount : parkingLocation.evParkingSpaceCount,
                geoLocation: createGeoLocation(parkingLocation.latitude, parkingLocation.longitude)
        )

        buildResourceObject(parkingLocation.calculateId(), attributes)
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
        def links = ['self': selfUrl]
        resourceObject.links = links
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
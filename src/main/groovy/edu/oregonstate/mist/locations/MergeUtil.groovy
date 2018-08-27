package edu.oregonstate.mist.locations

import edu.oregonstate.mist.api.jsonapi.ResourceIdentifierObject
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.LibraryDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MergeUtil {
    LibraryDAO libraryDAO
    ExtraDataDAO extraDataDAO
    CampusMapDAO campusMapDAO
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeUtil.class)

    // Ratio of how many campus maps locations are allowed to be missing
    private final float MISSING_LOCATIONS_RATIO

    MergeUtil(LibraryDAO libraryDAO,
              ExtraDataDAO extraDataDAO,
              CampusMapDAO campusMapDAO,
              float missingLocationsRatio) {
        this.libraryDAO = libraryDAO
        this.extraDataDAO = extraDataDAO
        this.campusMapDAO = campusMapDAO
        MISSING_LOCATIONS_RATIO = missingLocationsRatio
    }

    /**
     * Iterates over the data and merges the hours from the merge:true extra-data.yaml
     * file into the original resourceObject that comes from arcgis or campusmap.
     *
     * @param data list of objects to merge
     */
    List<ResourceObject> merge(List<ResourceObject> data) {
        def dataToMerge = getMergeData(data)

        dataToMerge.each { it ->
            // Here we are trying to find the original location (not defined in extra-data.yaml)
            // the hours come from the calendar object defined in extra-data.yaml
            def orig = data.find { primeObject ->
                primeObject.attributes.bldgID == it.attributes.name &&
                        !primeObject.attributes.merge
            }

            if (orig) {
                orig.attributes.openHours = it?.attributes?.openHours

                LOGGER.debug(orig.attributes.name?.toString() + " original tags = " +
                        orig.attributes?.tags?.toString())
                LOGGER.debug("new tags = " + it?.attributes?.tags)

                orig.attributes.tags += it?.attributes?.tags
            } else {
                LOGGER.debug("Original location was NULL data to merge " + orig)
            }
        }

        data - dataToMerge
    }

    /**
     * Returns the data (originally from extra-data.yaml) where the merge attribute was set
     * to true.
     *
     * @param data list of data
     * @return data
     */
    private List<ResourceObject> getMergeData(List<ResourceObject> data) {
        data.findAll {
            it.attributes.merge
        }
    }

    /**
     * Iterates over the services in extra-data.yaml and appends the services to the locations'
     * resultObject
     *
     * @param data list of objects to merge
     * @return data
     */
    public List<ResourceObject> appendRelationshipsToLocations(List<ResourceObject> data) {
        extraDataDAO.getLazyServices().each {
            data.findAll( { resourceObject ->
                resourceObject.attributes.bldgID == it.parent && !it.merge
            }).each { resourceObject ->
                LOGGER.debug("resource about to enter in relationship: " +
                        resourceObject.attributes.bldgID)

                if (!resourceObject?.relationships?.services) {
                     resourceObject.relationships = [
                             "services": [
                                     "data": []
                             ]
                     ]
                }

                String id = it.calculateId()
                if (LOGGER.debugEnabled) {
                    id += " " + it.conceptTitle
                }

                resourceObject.relationships.services.data <<
                        new ResourceIdentifierObject(id: id, type: it.type)
            }
        }
        data
    }

    /**
     * Appends relationships to the services resource object.
     *
     * @param data list of objects to merge
     * @return data
     */
    static public List<ResourceObject> appendRelationshipsToServices(List<ResourceObject> data) {
        data.each { resourceObject ->
            def id = resourceObject.attributes.locationId
            LOGGER.debug("services resource about to enter in relationship: $id")

            resourceObject.relationships = [
                    "locations": [
                            "data": [
                                    new ResourceIdentifierObject(id: id, type: Constants.LOCATIONS)
                            ]
                    ]
            ]
        }
    }

    /**
     * Populates library hours
     * @param data
     * @return data
     */
    List<ResourceObject> populate(List<ResourceObject> data) {
        data.each {
            // Note: 0036 is the valley library
            if (it?.attributes?.bldgID == '0036') {
                it.attributes.setOpenHours(libraryDAO.getLibraryHours())
            }
        }
    }

    /**
     * Merge campus map data into data
     *
     * @param data list of resource objects
     * @return data
     */
    List<ResourceObject> mergeCampusMapData(List<ResourceObject> data) {
        HashMap<String, CampusMapLocation> campusMapData =
                campusMapDAO.getCampusMapLocations()

        data.each {
            if (campusMapData[it.id]) {
                it.attributes.address = campusMapData[it.id].address
                it.attributes.description = campusMapData[it.id].description
                it.attributes.descriptionHTML = campusMapData[it.id].descriptionHTML
                it.attributes.images = campusMapData[it.id].images
                it.attributes.thumbnails = campusMapData[it.id].thumbnail
                it.attributes.website = campusMapData[it.id].mapUrl
                it.attributes.synonyms = campusMapData[it.id].synonyms
            }
        }

        def missingLocations = campusMapData.keySet() - data.collect { it.id }
        float ratioMissing = missingLocations.size() / campusMapData.size()
        LOGGER.info("Ratio of missing campus maps locations: ${ratioMissing}")
        if (ratioMissing > MISSING_LOCATIONS_RATIO) {
            throw new Exception("Missing campus maps locations ratio of " +
                    "${MISSING_LOCATIONS_RATIO} not satisfied")
        }
        data
    }
}

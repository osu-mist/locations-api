package edu.oregonstate.mist.locations.resources

import edu.oregonstate.mist.api.jsonapi.ResourceIdentifierObject
import edu.oregonstate.mist.locations.Constants
import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.core.CampusMapLocation
import edu.oregonstate.mist.locations.db.CampusMapDAO
import edu.oregonstate.mist.locations.db.ExtraDataDAO
import edu.oregonstate.mist.locations.db.LibraryDAO
import edu.oregonstate.mist.api.jsonapi.ResultObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MergeUtil {
    ResultObject resultObject
    LibraryDAO libraryDAO
    ExtraDataDAO extraDataDAO
    CampusMapDAO campusMapDAO
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeUtil.class)

    MergeUtil(ResultObject resultObject,
              LibraryDAO libraryDAO,
              ExtraDataDAO extraDataDAO,
              CampusMapDAO campusMapDAO) {
        this.resultObject = resultObject
        this.libraryDAO = libraryDAO
        this.extraDataDAO = extraDataDAO
        this.campusMapDAO = campusMapDAO
    }

    /**
     * Iterates over the resultObject and merges the hours from the merge:true extra-data.yaml
     * file into the original resourceObject that comes from arcgis or campusmap.
     */
    void merge() {
        def dataToMerge = getMergeData()

        dataToMerge.each { it ->
            // Here we are trying to find the original location (not defined in extra-data.yaml)
            // the hours come from the calendar object defined in extra-data.yaml
            def foo = resultObject.data.find { primeObject ->
                primeObject.attributes.abbreviation == it.attributes.name &&
                        !primeObject.attributes.merge
            }

            if (foo) {
                foo?.attributes?.openHours = it?.attributes?.openHours

                LOGGER.debug(foo?.attributes?.name?.toString() + " original tags = " +
                        foo?.attributes?.tags?.toString())
                LOGGER.debug("new tags = " + it?.attributes?.tags)

                foo?.attributes?.tags += it?.attributes?.tags
            } else {
                LOGGER.debug("Original location was NULL data to merge " + foo)

            }
        }

        resultObject.data = resultObject.data - dataToMerge
    }

    /**
     * Returns the data (originally from extra-data.yaml) where the merge attribute was set
     * to true.
     *
     * @return
     */
    private ArrayList getMergeData() {
        resultObject.data.findAll {
            it.attributes.merge
        }
    }

    /**
     * Iterates over the services in extra-data.yaml and appends the services to the locations'
     * resultObject
     */
    public void appendRelationshipsToLocations() {
        extraDataDAO.getLazyServices().each {
            resultObject.data.findAll( { resourceObject ->
                resourceObject.attributes.abbreviation == it.parent && !it.merge
            }).each { resourceObject ->
                LOGGER.debug("resource about to enter in relationship: " +
                        resourceObject.attributes.abbreviation)

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
    }

    /**
     * Appends relationships to the services resource object.
     */
    public void appendRelationshipsToServices() {
        resultObject.data.each { resourceObject ->
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
     * Can populate resource objects' attributes by dynamically calling the
     * method populateABBREVIATION
     */
    void populate() {
        String methodPrefix = "populate"
        String methodName
        resultObject.data.each {
            methodName = methodPrefix + it?.attributes?.abbreviation?.capitalize()
            if (this.metaClass.respondsTo(this, methodName)) {
                this."$methodName"(it.attributes)
            }
        }
    }

    /**
     * Add library hours
     *
     * @param attributes
     */
    void populateVLib(Attributes attributes) {
        attributes.setOpenHours(libraryDAO.getLibraryHours())
    }

    /**
     * Merge campus map data into resultObject
     */
    void mergeCampusMapData() {
        HashMap<String, CampusMapLocation> campusMapData =
                campusMapDAO.getCampusMapLocations()

        resultObject.data.each {
            if (campusMapData[it.id]) {
                it.attributes.address = campusMapData[it.id].address
                it.attributes.description = campusMapData[it.id].description
                it.attributes.images = campusMapData[it.id].images
                it.attributes.thumbnails = campusMapData[it.id].thumbnail
                it.attributes.website = campusMapData[it.id].mapUrl
                it.attributes.synonyms = campusMapData[it.id].synonyms
            }
        }

    }
}

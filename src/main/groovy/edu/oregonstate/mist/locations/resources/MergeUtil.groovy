package edu.oregonstate.mist.locations.resources

import edu.oregonstate.mist.locations.core.Attributes
import edu.oregonstate.mist.locations.db.LibraryDAO
import edu.oregonstate.mist.locations.jsonapi.ResultObject

class MergeUtil {

    ResultObject resultObject
    LibraryDAO libraryDAO

    MergeUtil(ResultObject resultObject, LibraryDAO libraryDAO) {
        this.resultObject = resultObject
        this.libraryDAO = libraryDAO
    }

    void merge() {
        def dataToMerge = getMergeData()

        dataToMerge.each { it ->
            // Here we are trying to find the original location (not defined in configuration.yaml)
            // the hours come from the calendar object defined in configuration.yaml
            def foo = resultObject.data.find { primeObject ->
                primeObject.attributes.abbreviation == it.attributes.name &&
                        !primeObject.attributes.merge
            }

            if (foo) {
                foo?.attributes?.openHours = it?.attributes?.openHours

                // debugging information to be removed
                println foo?.attributes?.name + " original tags = " + foo?.attributes?.tags
                println "new tags = " + it?.attributes?.tags

                foo?.attributes?.tags += it?.attributes?.tags
            } else {
                println "jose debug: NULL DATA TO MERGE " + foo
            }
        }

        resultObject.data = resultObject.data - dataToMerge
    }

    private ArrayList getMergeData() {
        resultObject.data.findAll {
            it.attributes.merge
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
}

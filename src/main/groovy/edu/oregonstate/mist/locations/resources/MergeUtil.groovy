package edu.oregonstate.mist.locations.resources

import edu.oregonstate.mist.locations.jsonapi.ResultObject

class MergeUtil {

    ResultObject resultObject

    MergeUtil(ResultObject resultObject) {
        this.resultObject = resultObject
    }

    void merge() {
        def dataToMerge = getMergeData()

        dataToMerge.each { it ->
            def foo = resultObject.data.find { primeObject ->
                primeObject.attributes.abbreviation == it.attributes.name &&
                        primeObject.attributes.merge
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
}

package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.locations.core.ExtraData
import edu.oregonstate.mist.locations.core.ExtraLocation
import io.dropwizard.lifecycle.Managed

import org.yaml.snakeyaml.*
import org.yaml.snakeyaml.constructor.Constructor

class ExtraDataManager implements Managed {
    public static final String FILE_NAME = 'extra-data.yaml'

    ExtraData extraData = new ExtraData()

    /**
     * Loads FILE_NAME.
     * DW App will halt during startup if FileNotFoundException is thrown.
     *
     * @throws FileNotFoundException
     */

    @Override
    public void start() throws FileNotFoundException {
        String text = new File('./' + FILE_NAME)?.text
        if (!text) {
            throw new FileNotFoundException("couldn't load $FILE_NAME")
        }

        println "cedenoj + " + text
        println "cedenoj size = ${text.size()}"

        Constructor c = new Constructor(ExtraData.class)
        TypeDescription t = new TypeDescription(ExtraData.class)
        t.putListPropertyType('locations', ExtraLocation.class)
        c.addTypeDescription(t)

        Yaml yml = new Yaml(c)
        extraData = (ExtraData) yml.load(text)

        extraData?.locations?.each {
            println it
        }
    }

    @Override
    public void stop() throws Exception {
    }

}

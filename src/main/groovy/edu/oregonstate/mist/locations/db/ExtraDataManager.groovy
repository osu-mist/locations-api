package edu.oregonstate.mist.locations.db

import edu.oregonstate.mist.locations.core.ExtraData
import edu.oregonstate.mist.locations.core.ExtraLocation
import io.dropwizard.lifecycle.Managed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.*
import org.yaml.snakeyaml.constructor.Constructor

class ExtraDataManager implements Managed {
    public static final String FILE_NAME = 'extra-data.yaml'
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtraDataManager.class)

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

        LOGGER.debug("extra-data file loaded with: " + text)

        Constructor c = new Constructor(ExtraData.class)
        TypeDescription t = new TypeDescription(ExtraData.class)
        t.putListPropertyType('locations', ExtraLocation.class)
        c.addTypeDescription(t)

        Yaml yml = new Yaml(c)
        extraData = (ExtraData) yml.load(text)

        extraData?.locations?.each {
            LOGGER.debug("loaded location from extra data file: " + it)
        }
    }

    @Override
    public void stop() throws Exception {
    }

}

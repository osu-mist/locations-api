package edu.oregonstate.mist.locations.core

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test

class FacilLocationTest extends GroovyTestCase {
    /**
     * Verify that FacilLocation can be sucessfully serialized/unserialized to json.
     * This is because CachedFacilDAO relies on being able to serialize a list of
     * FacilLocations in order to cache them.
     */
    @Test
    void testSerializeUnserialize() {
        ObjectMapper mapper = new ObjectMapper()

        def original = new FacilLocation()
        def serialized = mapper.writeValueAsString(original)
        def unserialized = mapper.readValue(serialized, FacilLocation)
        assert original == unserialized
    }
}

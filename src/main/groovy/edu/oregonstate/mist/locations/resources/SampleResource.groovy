package edu.oregonstate.mist.locations.resources

import edu.oregonstate.mist.locations.core.Sample
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path('/')
class SampleResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respond() {
        Sample object = new Sample()
        return object.message
    }
}
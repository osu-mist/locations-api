package edu.oregonstate.mist.locations.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import edu.oregonstate.mist.locations.db.LibraryDAO

/**
 * Verify connection to library's api
 */
class LibraryHealthCheck extends HealthCheck {
    private final LibraryDAO libraryDAO

    LibraryHealthCheck(LibraryDAO libraryDAO) {
        this.libraryDAO = libraryDAO
    }

    @Override
    protected Result check() {
        def failMessage = "There were no library hours for the entire week. Check the api"
        def libraryHours = libraryDAO.getLibraryHours()

        libraryHours == null || libraryHours.isEmpty() ?
                Result.unhealthy(failMessage) : Result.healthy()
    }
}

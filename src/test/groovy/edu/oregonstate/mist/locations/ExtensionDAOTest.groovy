package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.core.ExtensionLocation
import edu.oregonstate.mist.locations.db.ExtensionDAO
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test

class ExtensionDAOTest {
    private static ExtensionDAO extensionDAO
    private static LocationUtil locationUtil
    private static List<ExtensionLocation> extensionLocations

    @ClassRule
    public static final DropwizardAppRule<LocationConfiguration> APPLICATION =
            new DropwizardAppRule<LocationConfiguration>(
                    LocationApplication.class,
                    new File("configuration.yaml").absolutePath)

    @BeforeClass
    public static void setUpClass() {
        locationUtil = new LocationUtil(APPLICATION.configuration.locationsConfiguration)
        extensionDAO = new ExtensionDAO(APPLICATION.configuration.locationsConfiguration,
                locationUtil)
        extensionLocations = extensionDAO.getExtensionLocations()
    }

    @Test
    public void testGetLocations() {
        assert extensionLocations.size() != 0

        extensionLocations.each { extensionLocation ->
            assert extensionLocation.county != null
            assert extensionLocation.zipCode != null
            assert extensionLocation.fax != null
            assert extensionLocation.locationUrl != null

            assert extensionLocation.latitude.matches(LocationUtil.VALID_LAT_LONG)
            assert extensionLocation.longitude.matches(LocationUtil.VALID_LAT_LONG)
        }
    }
}

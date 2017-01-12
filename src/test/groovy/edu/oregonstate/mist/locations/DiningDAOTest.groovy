package edu.oregonstate.mist.locations

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.db.DiningDAO
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DiningDAOTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiningDAOTest.class)

    private static DiningDAO diningDAO
    private static LocationUtil locationUtil
    private static List<DiningLocation> diningLocations
    private static def VALID_DAY_RANGE = 1..7
    private static int MINIMUM_NUMBER_OF_VALID_DAYS = 3
    private static int MAXIMUM_NUMBER_OF_INVALID_LOCATIONS = 2

    @ClassRule
    public static final DropwizardAppRule<LocationConfiguration> APPLICATION =
            new DropwizardAppRule<LocationConfiguration>(
                    LocationApplication.class,
                    new File("configuration.yaml").absolutePath)

    @BeforeClass
    public static void setUpClass() {
        locationUtil = new LocationUtil(APPLICATION.configuration.locationsConfiguration)
        diningDAO = new DiningDAO(APPLICATION.configuration.locationsConfiguration, locationUtil)
        diningLocations = diningDAO.getDiningLocations()
    }

    @Test
    public void testGetDiningLocations() {
        assert !diningLocations.isEmpty()
        int invalidDiningCount = 0

        diningLocations.each {
            if (isValidDining(it)) {
                if (!hasValidOpenHours(it)) {
                    invalidDiningCount++
                }
            } else {
                invalidDiningCount++
            }
        }

        LOGGER.info("invalid dining locations: ${invalidDiningCount}")
        assert invalidDiningCount <= MAXIMUM_NUMBER_OF_INVALID_LOCATIONS
    }

    @Test
    public void testOpenHoursUTC() {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        assert !diningLocations.isEmpty()

        int testDiningLocationIndex = diningLocations.findIndexOf { hasValidOpenHours(it) }
        DiningLocation diningLocation = diningLocations.get(testDiningLocationIndex)

        println diningLocation

        LOGGER.info("diningLocation: ${diningLocation.conceptTitle}")
        List<DayOpenHours> dayOpenHours = diningLocation?.openHours?.find { it.value } ?.value

        JsonNode node = mapper.valueToTree(dayOpenHours.get(0))

        String startJSONText = node.get("start").asText()
        String endJSONText = node.get("end").asText()

        assert endJSONText.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}[T][0-9]{2}:[0-9]{2}:[0-9]{2}[Z]")
        assert startJSONText.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}[T][0-9]{2}:[0-9]{2}:[0-9]{2}[Z]")
    }

    private static boolean isValidDining(DiningLocation diningLocation) {
        diningLocation.with {
            conceptTitle && zone && calendarId && latitude && longitude &&
             latitude.matches(LocationUtil.VALID_LAT_LONG) && longitude.matches(LocationUtil.VALID_LAT_LONG)
        }
    }

    /**
     * Checks the validity of the openHours in a dining location. Along the properties it
     * verifies: openHours is not empty, days in openHours is within range, start & end are not
     * empty and that the number of invalid days for a dining location is within range.
     *
     * @param diningLocation
     * @return
     */
    private static boolean hasValidOpenHours(DiningLocation diningLocation) {
        boolean validDayIndex = false
        boolean emptyOpenHours = diningLocation?.openHours?.isEmpty()
        int invalidDays = 0

        diningLocation?.openHours?.each { day, dayOpenHoursList ->
            validDayIndex = VALID_DAY_RANGE.contains(day)
            int invalidTimeSlotCount = dayOpenHoursList?.findAll { !it.start || !it.end } ?.size()

            if (!dayOpenHoursList || invalidTimeSlotCount) {
                invalidDays++
            }
        }

        LOGGER.error("${diningLocation.conceptTitle} - invalidDays: ${invalidDays} - emptyOpenHours: ${emptyOpenHours}")
        !emptyOpenHours && (7 - invalidDays) >= MINIMUM_NUMBER_OF_VALID_DAYS
    }
}

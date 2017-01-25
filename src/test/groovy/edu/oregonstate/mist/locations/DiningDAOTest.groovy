package edu.oregonstate.mist.locations

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.db.DiningDAO
import io.dropwizard.testing.junit.DropwizardAppRule
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
            if (!isValidDining(it) || !hasValidOpenHours(it)) {
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
        LOGGER.info("diningLocation: ${diningLocation.conceptTitle}")

        // Find a day that has open hours
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

        LOGGER.info("${diningLocation.conceptTitle} - invalidDays: ${invalidDays} - emptyOpenHours: ${emptyOpenHours}")
        !emptyOpenHours && (7 - invalidDays) >= MINIMUM_NUMBER_OF_VALID_DAYS
    }

    @Test
    public void testFilterEvents() {
        // Parse test calendar
        def stream = this.class.getResourceAsStream('javaii.ics')
        if (stream == null) {
            throw new Exception("couldn't open test file javaii.ics")
        }
        CalendarBuilder builder = new CalendarBuilder()
        Calendar calendar = builder.build(stream)


        // Test that modified recurrence events correctly override
        // the base event (see ECSPCS-311)

        // Filter events by day
        def tz = DateTimeZone.forID("America/Los_Angeles")
        def day = new DateTime(2017, 1, 6, 0, 0, tz) // Midnight, Jan 6th, PST
        List events = diningDAO.getEventsForDay(calendar, day)

        println(events)

        // Expected result?
        assert events == [
            new DayOpenHours(
                start: diningDAO.combineEventHours(day, new DateTime(
                        new net.fortuna.ical4j.model.DateTime("20170106T073000"))),
                end:   diningDAO.combineEventHours(day, new DateTime(
                        new net.fortuna.ical4j.model.DateTime("20170106T150000"))),
                uid: "jvspu68dcau21vdtpj49li6d1o@google.com",
                sequence: 0,
                recurrenceId: "20170106T073000",
                lastModified: new net.fortuna.ical4j.model.DateTime("20161129T231154Z"),
            )
        ]

        // Test that an event that ends on midnight does not leak into
        // the next day (see ECSPCS-311)

        // Filter events by day
        day = new DateTime(2017, 1, 12, 0, 0, tz) // Midnight, Jan 12th, PST
        events = diningDAO.getEventsForDay(calendar, day)

        println(events)

        // Expected result?
        assert events.size() == 1
        assert events == [
            new DayOpenHours(
                start: diningDAO.combineEventHours(day, new DateTime(
                        new net.fortuna.ical4j.model.DateTime("20160929T073000"))),
                end:   diningDAO.combineEventHours(day, new DateTime(
                        new net.fortuna.ical4j.model.DateTime("20160929T230000"))),
                uid:   "ruq45d78ag0km1m83i5b5flaoc@google.com",
                sequence: 0,
                recurrenceId: null,
                lastModified: new net.fortuna.ical4j.model.DateTime("20160913T191159Z"),
            )
        ]


    }
}

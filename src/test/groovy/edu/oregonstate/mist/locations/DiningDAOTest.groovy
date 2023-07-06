package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.ServiceLocation
import edu.oregonstate.mist.locations.db.DAOException
import edu.oregonstate.mist.locations.db.DiningDAO
import edu.oregonstate.mist.locations.db.IcalUtil
import groovy.mock.interceptor.MockFor
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

class DiningDAOTest {

    final String weeklyMenuUrl = "https://test.url/weeklyMenu/index"

    LocationConfiguration locationConfiguration = new LocationConfiguration(
            locationsConfiguration: ["diningThreshold": "1", "weeklyMenuUrl": weeklyMenuUrl]
    )
    DiningDAO diningDAO = new DiningDAO(locationConfiguration, new Cache([:]))

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
        List events = IcalUtil.getEventsForDay(calendar, day)

        println(events)

        // Expected result?
        // assert events == [
        //     new DayOpenHours(
        //         start: new net.fortuna.ical4j.model.DateTime("20170106T073000"),
        //         end:  new net.fortuna.ical4j.model.DateTime("20170106T150000"),
        //         uid: "jvspu68dcau21vdtpj49li6d1o@google.com",
        //         sequence: 0,
        //         recurrenceId: "20170106T073000",
        //         lastModified: new net.fortuna.ical4j.model.DateTime("20161129T231154Z"),
        //     )
        // ]

        // Test that an event that ends on midnight does not leak into
        // the next day (see ECSPCS-311)

        // Filter events by day
        day = new DateTime(2017, 1, 12, 0, 0, tz) // Midnight, Jan 12th, PST
        events = IcalUtil.getEventsForDay(calendar, day)

        println(events)

        // Expected result?
        assert events.size() == 1
        // assert events == [
        //     new DayOpenHours(
        //         start: new net.fortuna.ical4j.model.DateTime("20170112T073000"),
        //         end: new net.fortuna.ical4j.model.DateTime("20170112T230000"),
        //         uid:   "ruq45d78ag0km1m83i5b5flaoc@google.com",
        //         sequence: 0,
        //         recurrenceId: null,
        //         lastModified: new net.fortuna.ical4j.model.DateTime("20160913T191159Z"),
        //     )
        // ]
    }

    @Rule
    public ExpectedException exception = ExpectedException.none()

    // Threshold is satisfied
    @Test
    void testMapDiningLocations() {
        String testData = """\
            [
                {
                    "concept_title": "Test",
                    "zone": "Test",
                    "calendar_id": "Test",
                    "concept_coord": "Test, Test",
                    "start": "Test",
                    "end": "Test"
                }
            ]
        """.stripIndent()
        diningDAO.mapDiningLocations(testData)
    }

    // Threshold is not satisfied
    @Test
    void testUnderThreshold() {
        exception.expect(DAOException.class)
        exception.expectMessage("dining locations")
        String testData = "[]"
        diningDAO.mapDiningLocations(testData)
    }

    // Temporary folder used in mocking a Cache
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    // Test that weeklyMenuUrl is constructed properly
    @Test
    void testValidWeeklyMenu() {
        String locId = "123"
        assert getWeeklyMenuLink(locId) == "${weeklyMenuUrl}?loc=${locId}"
    }

    // Test that weeklyMenuUrl is null when locId is null
    @Test
    void testNullWeeklyMenu() {
        assert getWeeklyMenuLink(null) == null
    }

    /**
     * Gets the value of the weeklyMenu field of a test dining location with a given locId
     *
     * @param locId
     */
    String getWeeklyMenuLink(String locId) {
        List<ServiceLocation> testData = [new ServiceLocation(
                conceptTitle: "test",
                zone: "test",
                calendarId: "test",
                conceptCoord: "test",
                locId: locId,
                "start": "test",
                "end": "test"
        )]

        def mockCache = new MockFor(Cache)
        mockCache.demand.withJsonFromUrlOrCache { String url, String filename, Closure closure ->
            testData
        }
        Cache proxyCache = mockCache.proxyInstance(["cacheDirectory": folder.toString()])
        IcalUtil.metaClass.static.addLocationHours = {
            List<ServiceLocation> diners, String icalURLTemplate, Cache cache -> testData
        }
        DiningDAO dao = new DiningDAO(locationConfiguration, proxyCache)
        List<ServiceLocation> locations = dao.getDiningLocations()
        assert locations.size() == 1
        locations[0].weeklyMenu
    }
}

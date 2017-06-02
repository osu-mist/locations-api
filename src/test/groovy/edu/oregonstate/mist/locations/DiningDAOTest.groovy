package edu.oregonstate.mist.locations

import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.db.IcalUtil
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

class DiningDAOTest {
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
        assert events == [
            new DayOpenHours(
                start: new net.fortuna.ical4j.model.DateTime("20170106T073000"),
                end:  new net.fortuna.ical4j.model.DateTime("20170106T150000"),
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
        events = IcalUtil.getEventsForDay(calendar, day)

        println(events)

        // Expected result?
        assert events.size() == 1
        assert events == [
            new DayOpenHours(
                start: new net.fortuna.ical4j.model.DateTime("20170112T073000"),
                end: new net.fortuna.ical4j.model.DateTime("20170112T230000"),
                uid:   "ruq45d78ag0km1m83i5b5flaoc@google.com",
                sequence: 0,
                recurrenceId: null,
                lastModified: new net.fortuna.ical4j.model.DateTime("20160913T191159Z"),
            )
        ]
    }
}

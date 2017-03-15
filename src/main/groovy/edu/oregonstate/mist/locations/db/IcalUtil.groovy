package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.DiningLocation
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.filter.Filter
import net.fortuna.ical4j.filter.PeriodRule
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.PeriodList
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IcalUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(IcalUtil.class)
    private static final ObjectMapper MAPPER = new ObjectMapper()

    public static List<DiningLocation> getLocationsHours(List<DiningLocation> diners,
                                                  String icalURLTemplate,
                                                         LocationUtil locationUtil) {
        diners.each {
            def icalURL = icalURLTemplate.replace("calendar-id", "${it.calendarId}")
            def icalFileName = it.calendarId + ".ics"
            LOGGER.debug(icalURL)

            String icalData = getIcalData(icalURL, icalFileName, locationUtil)
            it.openHours = parseDiningICAL(icalData)
        }
    }

    private static String getIcalData(String icalURL, String icalFileName,
                                      LocationUtil locationUtil) {
        // @todo: what if it's not new, but the open hours in calendar are different for next week?
        locationUtil.getDataFromUrlOrCache(icalURL, icalFileName)
    }

    public static HashMap<Integer, List<DayOpenHours>> parseDiningICAL(String icalData) {
        // setup ical4j calendar and parse it
        CalendarBuilder builder = new CalendarBuilder()
        def stream = new ByteArrayInputStream(icalData.getBytes())
        Calendar calendar = builder.build(stream)

        // setup jodatime varaibles
        Map<Integer, List<DayOpenHours>> weekOpenHours = new HashMap<Integer, List<DayOpenHours>>()
        DateTime today = new DateTime()

        // iterate over a week to find out next 7 days of open hours
        (0..6).each { days ->
            def singleDay = today.plusDays(days)
            def events = getEventsForDay(calendar, singleDay)
            weekOpenHours.put(singleDay.dayOfWeek, events)
        }

        weekOpenHours
    }

    /**
     * GetEventsForDay filters the events in a Calendar to those on a given
     * day. This method is public for testing purposes only.
     */
    public static List<DayOpenHours> getEventsForDay(Calendar calendar, DateTime singleDay) {
        def dayOpenHoursList = new ArrayList<DayOpenHours>()

        // filter out so that only events for the current day are retrieved
        singleDay = singleDay.withTimeAtStartOfDay().plusSeconds(1)
        def ical4jToday = new net.fortuna.ical4j.model.DateTime(singleDay.toDate())
        Period period = new Period(ical4jToday, new Dur(0, 23, 59, 59))
        Filter filter = new Filter(new PeriodRule(period))
        List eventsToday = filter.filter(calendar.getComponents(Component.VEVENT))

        eventsToday.each { event ->
            PeriodList periodList = event.calculateRecurrenceSet(period)
            def dtStart = periodList.getAt(0).getRangeStart()
            def dtEnd = periodList.getAt(0).getRangeEnd()
            def sequence = event.getSequence()
            def uid = event.getUid()
            def recurrenceId = event.getRecurrenceId()
            def lastModified = event.getLastModified()

            // Json annotation in POGO handles utc storage
            DayOpenHours eventHours = new DayOpenHours(
                start: dtStart,
                end: dtEnd,
                uid: uid.value,
                sequence: sequence?.sequenceNo,
                recurrenceId: recurrenceId?.value,
                lastModified: lastModified?.date,
            )

            // Add event to the list, or not, depending on whether it conflicts
            // with the UID of another event

            def existingUIDEventKey = dayOpenHoursList.findIndexOf { openHour ->
                openHour.uid == uid.value
            }

            if (existingUIDEventKey != -1) {
                DayOpenHours existingEventHours = dayOpenHoursList.get(existingUIDEventKey)
                if (supersedesEvent(eventHours, existingEventHours)) {
                    // removing previous event since new event overwrites it
                    dayOpenHoursList.remove(existingUIDEventKey)
                    dayOpenHoursList.add(eventHours)
                } else {
                    // existing event in the dayOpenHours list takes precedence;
                    // do nothing
                }
            } else {
                dayOpenHoursList.add(eventHours)
            }
        }

        dayOpenHoursList
    }

    /**
     * Reports whether event x should supersede event y with the same uid.
     */
    private static boolean supersedesEvent(DayOpenHours x, DayOpenHours y) {
        // Sanity check: the two events must have the same uid
        if (x.uid != y.uid) {
            LOGGER.log("attempted to check whether two events with different uids conflict")
            return false
        }

        // Prefer an instance of a recurring event over the
        // definition of the recurring event
        // (that is, prefer an event with a RECURRENCE-ID over one without)
        if (x.recurrenceId != null && y.recurrenceId == null) {
            return true
        }
        if (x.recurrenceId == null && y.recurrenceId != null) {
            return false
        }

        // Prefer an event with a higher sequence number
        if (x.sequence != y.sequence) {
            return x.sequence > y.sequence
        }

        // Last resort: prefer the event that has been modified most recently
        !x.lastModified.before(y.lastModified)
    }
}

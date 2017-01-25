package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.core.DayOpenHours
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.filter.Filter
import net.fortuna.ical4j.filter.PeriodRule
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.Property
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The Dining data comes from google calendar
 */
public class DiningDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiningDAO.class)
    private static final ObjectMapper MAPPER = new ObjectMapper()

    /**
     * Url of Dining API. JSON formatted content that includes lat/long,
     * name, calendar id, and today's hours for dining locations.
     */
    private final String uhdsURL

    /**
     * Url format of ical calendar that includes open hours for dining
     */
    private final String icalURL

    /**
     * Filename to store the uhdsURL cache data
     */
    private final String diningJsonOut

    private final LocationUtil locationUtil

    public DiningDAO(Map<String, String> locationConfiguration, LocationUtil locationUtil) {
        uhdsURL = locationConfiguration.get("uhdsUrl")
        icalURL = locationConfiguration.get("icalUrl")
        diningJsonOut = locationConfiguration.get("diningJsonOut")
        this.locationUtil = locationUtil
    }

    List<DiningLocation> getDiningLocations() {
        String diningData = getDiningLocationList()

        List<DiningLocation> diners = MAPPER.readValue(diningData, new TypeReference<List<DiningLocation>>(){})
        diners.unique(true) // the json datasource lists the location multiple time if it's open twice a day

        diners.each {
            def icalURL = icalURL.replace("calendar-id", "${it.calendarId}")
            def icalFileName = it.calendarId + ".ics"
            LOGGER.debug(icalURL)

            String icalData = getIcalData(icalURL, icalFileName)
            it.openHours = parseDiningICAL(icalData)
        }
        //@todo: how to deal with html in title?

        //@todo: need a flag to know if it's the first time in the day that we have flagged

        diners
    }

    private String getIcalData(String icalURL, String icalFileName) {
        // @todo: what if it's not new, but the open hours in the calendar are different for next week?
        locationUtil.getDataFromUrlOrCache(icalURL, icalFileName)
    }

    /**
     * Gets the list of dining locations from UHDS.
     * Tries to get the data from the web, if it fails
     * it reads it from the cache.
     *
     * @return String json format of dining locations
     */
    private String getDiningLocationList() throws Exception {
        locationUtil.getDataFromUrlOrCache(uhdsURL, diningJsonOut)
    }

    private HashMap<Integer, List<DayOpenHours>> parseDiningICAL(String icalData) {
        // setup ical4j calendar and parse it
        CalendarBuilder builder = new CalendarBuilder()
        def stream = new ByteArrayInputStream(icalData.getBytes())
        Calendar calendar = builder.build(stream)

        // setup jodatime varaibles
        Map<Integer, List<DayOpenHours>> weekOpenHours = new HashMap<Integer, List<DayOpenHours>>()
        DateTime today = new DateTime()

//        iterate over a week to find out next 7 days of open hours
                (0..6).each { days ->
                    def currentDay = today.plusDays(days)
                    def events = getEventsForDay(calendar, currentDay)
                    weekOpenHours.put(currentDay.dayOfWeek, events)
                }

        weekOpenHours
    }

    /**
     * GetEventsForDay filters the events in a Calendar to those on a given
     * day. This method is public for testing purposes only.
     */
    public static List<DayOpenHours> getEventsForDay(Calendar calendar, DateTime currentDay) {
        def dayOpenHoursList = new ArrayList<DayOpenHours>()

        // filter out so that only events for the current day are retrieved
        currentDay = currentDay.withTimeAtStartOfDay().plusSeconds(1)
        def ical4jToday = new net.fortuna.ical4j.model.DateTime(currentDay.toDate())
        Period period = new Period(ical4jToday, new Dur(0, 23, 59, 59))
        Filter filter = new Filter(new PeriodRule(period))
        List eventsToday = filter.filter(calendar.getComponents(Component.VEVENT))

        eventsToday.each { event ->

            def dtStart = new DateTime(event.getStartDate().date)
            def dtEnd = new DateTime(event.getEndDate().date)
            def sequence = event.getSequence()
            def uid = event.getUid()
            def recurrenceId = event.getRecurrenceId()
            def lastModified = event.getLastModified()

            // Json annotation in POGO handles utc storage
            DayOpenHours eventHours = new DayOpenHours(
                start: combineEventHours(currentDay, dtStart),
                end: combineEventHours(currentDay, dtEnd),
                uid: uid.value,
                sequence: sequence?.sequenceNo,
                recurrenceId: recurrenceId?.value,
                lastModified: lastModified.date,
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
        return !x.lastModified.before(y.lastModified)
    }

    /**
     * Use date for current week and the time from the ical to generate the up-to-date eventHour
     * @param currDate
     * @param icalHour
     * @return eventHour
     */
    public static Date combineEventHours(DateTime currDate, DateTime icalHour) {
        DateTime eventHour = new DateTime(currDate.year, currDate.monthOfYear, currDate.dayOfMonth,
                icalHour.getHourOfDay(), icalHour.getMinuteOfHour(), icalHour.getSecondOfMinute(), icalHour.getZone())

        // change 00:00 to 23:59:59
        if (eventHour.getHourOfDay() == 0 && eventHour.getMinuteOfHour() == 0) {
            eventHour = eventHour.minusSeconds(1)
        }

        eventHour.toDate()
    }
}

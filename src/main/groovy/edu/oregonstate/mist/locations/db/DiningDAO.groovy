package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.LocationUtil
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.core.DayOpenHours
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.filter.Filter
import net.fortuna.ical4j.filter.PeriodRule
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.property.ExDate
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The Dining data comes from google calendar
 */
public class DiningDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiningDAO.class)

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

        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        List<DiningLocation> diners = mapper.readValue(diningData, new TypeReference<List<DiningLocation>>(){})
        diners.unique(true) // the json datasource lists the location multiple time if it's open twice a day

        diners.each {
            def icalURL = icalURL.replace("calendar-id", "${it.calendarId}")
            def icalFileName = it.calendarId + ".ics"
            LOGGER.debug(icalURL)

            String icalData = getIcalData(icalURL, icalFileName)
            it.openHours = parseDiningICAL(icalData)
        }
        //@todo: how to deal with html in title?

        //@todo: need a flag to know if it's the first time in the day taht we have flagged

        diners
    }

    private String getIcalData(String icalURL, String icalFileName) {
        // @todo: what if it's not new, but the open hours in the calendar are different for next week?
        locationUtil.getDataFromUrlOrCache(icalURL, icalFileName)
    }

    /**
     * Gets the list of dining locations from UHDS. Tries to get the data from the web, if it fails
     * it reads it from the cache.
     *
     * @return String json format of dining locations
     */
    private String getDiningLocationList() throws Exception{
        locationUtil.getDataFromUrlOrCache(uhdsURL, diningJsonOut)
    }

    private HashMap<Integer, List<DayOpenHours>> parseDiningICAL(String icalData) {
        // setup ical4j calendar and parse it
        CalendarBuilder builder = new CalendarBuilder()
        def stream = new ByteArrayInputStream(icalData.getBytes())
        net.fortuna.ical4j.model.Calendar calendar = builder.build(stream)

        // setup jodatime varaibles
        Map<Integer, List<DayOpenHours>> weekOpenHours = new HashMap<Integer, List<DayOpenHours>>()
        DateTime today = new DateTime().withTimeAtStartOfDay()

        (0..6).each { // iterate over a week to find out next 7 days of open hours
            def singleDay = today.plusDays(it)
            def dayOpenHoursList = new ArrayList<DayOpenHours>()

            // filter out so that only events for the current day are retrieved
            def ical4jToday = new net.fortuna.ical4j.model.DateTime(singleDay.toDate())
            Period period = new Period(ical4jToday, new Dur(1, 0, 0, 0))
            Filter filter = new Filter(new PeriodRule(period))
            List eventsToday = filter.filter(calendar.getComponents(Component.VEVENT))

            eventsToday.each { // put break right here
                addEventToToday(it, dayOpenHoursList)
            } // iterate over today's events

            weekOpenHours.put(singleDay.dayOfWeek, dayOpenHoursList)
        } // iterate over weekday
        weekOpenHours
    }

    private static void addEventToToday(def event, ArrayList<DayOpenHours> dayOpenHoursList) {
        def dtStart = event.getStartDate()
        def dtEnd = event.getEndDate()
        def sequence = event.getSequence()
        def uid = event.getUid()
        def recurrenceId = event.getRecurrenceId()
        def lastModified = event.getLastModified()

        // Json annotation in POGO handles utc storage
        DayOpenHours eventHours = new DayOpenHours(
                start: dtStart.date,
                end: dtEnd.date,
                uid: uid.value,
                sequence: sequence?.sequenceNo,
                recurrenceId: recurrenceId?.value,
                lastModified: lastModified.date,
        )

        def existingUIDEventKey = dayOpenHoursList.findIndexOf { openHour ->
            openHour.uid == uid.value
        }

        if (existingUIDEventKey != -1) {
            DayOpenHours existingEventHours = dayOpenHoursList.get(existingUIDEventKey)
            if (supersedesEvent(eventHours, existingEventHours)) {
                // removing previous event since new event overwrites it
                dayOpenHoursList.remove(existingUIDEventKey)
            } else {
                // existing event in the dayOpenHours list takes precedence
                return
            }
        }

        dayOpenHoursList.add(eventHours)
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
}

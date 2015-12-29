package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.core.DiningLocation
import edu.oregonstate.mist.locations.core.OpenHours
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

import java.security.MessageDigest

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

    /**
     * Working directory where the cache and downloaded / generated files are stored.
     */
    private final String cacheDirectory

    public DiningDAO(Map<String, String> locationConfiguration) {
        uhdsURL = locationConfiguration.get("uhdsUrl")
        icalURL = locationConfiguration.get("icalUrl")
        diningJsonOut = locationConfiguration.get("diningJsonOut")
        cacheDirectory = locationConfiguration.get("cacheDirectory")
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
            it.weekOpenHours = parseDiningICAL(icalData)
        }
        //@todo: how to deal with html in title?

        //@todo: need a flag to know if it's the first time in the day taht we have flagged
        //@todo: need a cache or data directory where all the files should be stored
        //@todo: return only YYYY-MM-DDTHH:MMZ (utc with up to minute for the most accuracy)

        diners
    }

    private String getIcalData(String icalURL, String icalFileName) {
        // @todo: what if it's not new, but the open hours in the calendar are different for next week?
        getDataFromUrlOrCache(icalURL, icalFileName)
    }

    /**
     * Gets the list of dining locations from UHDS. Tries to get the data from the web, if it fails
     * it reads it from the cache.
     *
     * @return String json format of dining locations
     */
    private String getDiningLocationList() throws Exception{
        getDataFromUrlOrCache(uhdsURL, diningJsonOut)
    }

    private String getDataFromUrlOrCache(String URL, String cachedFile) throws Exception {
        def data
        def filePath = cacheDirectory + "/" + cachedFile
        try {
            data = new URL(URL).getText()
            if (data && isDataSourceNew(cachedFile, data)) {
                LOGGER.info("New content found for: ${URL}")
                createCacheDirectory()

                new File(filePath).write(data)
            } else {
                LOGGER.info("No new content for: ${URL}")
            }
        } catch (Exception e) {
            LOGGER.error("Ran into an exception grabbing the URL data", e)
            data = new File(filePath).getText()
        }

        data
    }

    /**
     * Create cache directory if needed.
     */
    private void createCacheDirectory() {
        // Create a File object representing cache directory
        def directory = new File(cacheDirectory)

        // If it doesn't exist
        if( !directory.exists() ) {
            LOGGER.info("Creating cache directory: ${cacheDirectory}")
            directory.mkdirs()
        }
    }

    private HashMap<Integer, List<OpenHours>> parseDiningICAL(String icalData) {
        // setup ical4j calendar and parse it
        CalendarBuilder builder = new CalendarBuilder()
        def stream = new ByteArrayInputStream(icalData.getBytes())
        net.fortuna.ical4j.model.Calendar calendar = builder.build(stream)

        // setup jodatime varaibles
        Map<Integer, List<OpenHours>> weekOpenHours = new HashMap<Integer, List<OpenHours>>()
        DateTime today = new DateTime().withTimeAtStartOfDay()

        (0..6).each { // iterate over a week to find out next 7 days of open hours
            def singleDay = today.plusDays(it)
            def dayOpenHours = new ArrayList<OpenHours>()

            // filter out so that only events for the current day are retrieved
            def ical4jToday = new net.fortuna.ical4j.model.DateTime(singleDay.toDate())
            Period period = new Period(ical4jToday, new Dur(1, 0, 0, 0))
            Filter filter = new Filter(new PeriodRule(period))
            List eventsToday = filter.filter(calendar.getComponents(Component.VEVENT))

            eventsToday.each { // put break right here
                if (!isEventExcluded(it)) { // today was excluded from event recursive rule
                    addEventToToday(it, dayOpenHours)
                }
            } // iterate over today's events

            weekOpenHours.put(singleDay.dayOfWeek, dayOpenHours)
        } // iterate over weekday
        weekOpenHours
    }

    /**
     * Checks to see if the event's recurrence was excluded using EXDATE.
     *
     * @param it
     * @return
     */
    private boolean isEventExcluded(def it) {
        PropertyList exDates = it.getProperties(Property.EXDATE)
        exDates.each { ex ->
            ((ExDate) ex).dates.each { oneExDate ->
                if (new DateTime(oneExDate).toLocalDate().equals(new LocalDate())) {
                    return true
                }
            }
        }

        false
    }

    private void addEventToToday(def it, ArrayList<OpenHours> dayOpenHours) {
        def dtStart = it.getProperties().getProperty(Property.DTSTART)
        def dtEnd = it.getProperties().getProperty(Property.DTEND)
        def sequence = it.getProperties().getProperty(Property.SEQUENCE)
        def uid = it.getProperties().getProperty(Property.UID)

        //@todo: Store start/end in utc
        OpenHours eventHours = new OpenHours([start: dtStart.date, end: dtEnd.date, sequence: sequence.sequenceNo,
                                             uid  : uid.value])

        def existingUIDEventKey = dayOpenHours.findIndexOf { openHour ->
            openHour.uid == uid.value
        }

        if (existingUIDEventKey != -1) {
            if (dayOpenHours.get(existingUIDEventKey).sequence > eventHours.sequence) {
                return // existing event in the dayOpenHours takes precedence
            } else {
                dayOpenHours.remove(existingUIDEventKey) // removing previous event since new event overwrites it
            }
        }

        dayOpenHours.add(eventHours)
    }

    private boolean isDataSourceNew(String filename, String recentData) {
        def file = new File(cacheDirectory + "/" + filename)
        if (!file.exists()) {
            return true
        }

        String fileContent = file?.getText()
        if (!fileContent) {
            return true
        }

        getMD5Hash(fileContent) == getMD5Hash(recentData)
    }

    /**
     * Calculates MD5 Hash of a string
     *
     * @param content
     * @return
     */
    private String getMD5Hash(String content) {
        MessageDigest.getInstance("MD5").digest(content.bytes).encodeHex().toString()
    }
}

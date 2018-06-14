package edu.oregonstate.mist.locations.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.locations.Cache
import edu.oregonstate.mist.locations.core.DayOpenHours
import edu.oregonstate.mist.locations.core.LibraryHours
import groovy.transform.TypeChecked
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.apache.http.client.methods.HttpPost
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TypeChecked
class LibraryDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryDAO)

    public static final String LIBRARY_PATH = "library.json"
    private final String libraryUrl
    private final CloseableHttpClient httpClient
    private final Cache cache

    public static final String DATE_FORMAT = "yyyy-MM-dd"
    public static final String DATETIME_FORMAT = "yyyy-MM-dd hh:mma"

    private ObjectMapper mapper = new ObjectMapper()

    LibraryDAO(Map<String, String> locationConfiguration,
               CloseableHttpClient httpClient,
               Cache cache) {
        libraryUrl = locationConfiguration.get("libraryUrl")
        this.httpClient = httpClient
        this.cache = cache
    }

    /**
     * Returns the day of the week with a list of open hours for the library
     *
     * @return
     */
    public Map<Integer, List<DayOpenHours>> getLibraryHours() {
        buildLibraryHours(new DateTime().withTimeAtStartOfDay(), 7)
    }

    /**
     *
     * @param startDate          String startDate in the form of YYYY-MM-DD
     * @return
     */
    private Map<Integer, List<DayOpenHours>> buildLibraryHours(DateTime startDate, int numDays) {
        Map<String, LibraryHours> data = getLibraryData(startDate, numDays)
        // Attributes requires a list of DayOpenHours
        Map<Integer, List<DayOpenHours>> openHours = new HashMap<>()
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATETIME_FORMAT)
                .withZone(DateTimeZone.forID("America/Los_Angeles"))

        data?.each { _, it ->
            // Sometimes the data has space padding :(
            def openDate = it.sortableDate.trim()
            def open = it.open.trim()
            def close = it.close.trim()

            DateTime start = formatter.parseDateTime(openDate + " " + open)
            DateTime end = formatter.parseDateTime(openDate + " " + close)

            if (!it.closesAtNight) {
                end = end.withTime(23, 59, 59, 999)
            }

            Integer index = Days.daysBetween(startDate, start).getDays() + 1
            openHours.put(index, [
                    new DayOpenHours(start: start.toDate(), end: end.toDate())
            ])
        }

        openHours
    }

    private Map<String, LibraryHours> getLibraryData(DateTime startDate, int numDays) {
        String parameters = getDatesParameter(startDate, numDays)

        def entityString
        try {
            entityString = doPostRequest(libraryUrl, parameters)

            def data = mapLibraryHours(entityString)

            if (data.isEmpty()) {
                throw new DAOException("found no library hours")
            }

            cache.writeDataToCache(LIBRARY_PATH, entityString)

            return data
        } catch (Exception e) {
            LOGGER.error("Error getting library json data from url ${libraryUrl}", e)
            LOGGER.info("Attempting to fall back on cached data")
            entityString = cache.getCachedData(LIBRARY_PATH)
            return mapLibraryHours(entityString)
        }
    }

    private Map<String,LibraryHours> mapLibraryHours(String entityString) {
        (Map<String,LibraryHours>)this.mapper.readValue(entityString,
                new TypeReference<HashMap<String, LibraryHours>>() {})
    }

    private String doPostRequest(String url, String body) {
        HttpPost post = new HttpPost(url)
        post.setHeader("Content-Type", "application/json")
        post.setHeader("Accept", "application/vnd.kiosks.v1")
        post.setEntity(new StringEntity(body))

        CloseableHttpResponse response = null
        try {
            response = httpClient.execute(post)
            int code = response.getStatusLine().getStatusCode()
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP status code ${code} returned for url ${url}")
            }
            HttpEntity entity = response.getEntity()
            try {
                return EntityUtils.toString(entity)
            } finally {
                EntityUtils.consume(entity)
            }
        } finally {
            response?.close()
        }
    }

    /**
     * Provides POST parameters to submit to the library api
     *
     * @param numDays
     * @param startDate
     * @return
     */
    private static String getDatesParameter(DateTime startDate, int numDays) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(DATE_FORMAT)
        def dates = []
        numDays.times {
            def singleDay = startDate.plusDays(it)
            dates.add(dtfOut.print(singleDay))
        }
        '{"dates":["' + dates.join('", "') + '"]}'
    }
}

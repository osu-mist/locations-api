package edu.oregonstate.mist.locations

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.MediaType
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class Cache {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class)

    /**
     * Working directory where the cache and downloaded / generated files are stored.
     */
    private final String cacheDirectory

    Cache(Map<String, String> locationConfiguration) {
        cacheDirectory = locationConfiguration.get("cacheDirectory")
    }

    /**
     * Runs a closure using data from either the url or the cache
     *
     * The new data is only written to cache if the closure completes successfully.
     * Note that the closure may be called twice -- if it fails on the url data,
     * it will be re-run using the cached data.
     *
     * If you would like to more carefully control what gets written to cache,
     * and how fallback is implemented, use the getCachedData and writeDataToCache methods.
     *
     * Example usage:
     *
     *      def parsedData = cache.withDataFromUrlOrCache(url, path) { data ->
     *          parse(data)
     *      }
     *
     * @param url  the url to fetch
     * @param cacheFilename  filename in the cache directory to read/write cached data from
     * @param closure  block to be executed
     * @returns  the value returned by the closure
     */
    public <T> T withDataFromUrlOrCache(String url, String cacheFilename, Closure closure) {
        withDataFromUrlOrCache(url, cacheFilename, MediaType.WILDCARD_TYPE, closure)
    }

    /**
     * Like withDataFromUrlOrCache, but checks that the returned content type is application/json.
     * It does not actually try to parse the data to check if it is valid JSON.
     */
    public <T> T withJsonFromUrlOrCache(String url, String cacheFilename, Closure closure) {
        withDataFromUrlOrCache(url, cacheFilename, MediaType.APPLICATION_JSON_TYPE, closure)
    }

    /**
     * Like withDataFromUrlOrCache, but checks that the returned content type is compatible
     * with the expected media type.
     */
    public <T> T withDataFromUrlOrCache(
            String url,
            String cacheFilename,
            MediaType expectedMediaType,
            Closure closure
    ) {
        def file = getFile(cacheFilename)
        String data
        try {
            // First, try to use data fetched from the url

            data = getURL(url, expectedMediaType)
            T returnValue = closure(data)

            // If successful, save to the cache
            if (data && isDataSourceNew(file, data)) {
                LOGGER.info("New content found for: ${url}")
                createCacheDirectory()
                file.write(data)
            } else {
                LOGGER.info("No new content for: ${url}")
            }

            return returnValue
        } catch (Exception e) {
            if (data == null) {
                LOGGER.error("Ran into an exception grabbing the url data", e)
            } else {
                LOGGER.error("Ran into an exception processing the data from url ${url}", e)
            }
            LOGGER.info("Attempting to fall back on cached data")

            // Attempt to fall back on cached data
            withCachedFile(file, closure)
        }
    }

    static private <T> T withCachedFile(File file, Closure closure) {
        String data
        try {
            // @todo: catch the IOException if the file doesn't exist and raise NotCachedError
            // or something
            data = file.getText()
        } catch (Exception e) {
            LOGGER.error("Ran into an exception reading the cache", e)
            throw e
        }
        try {
            return closure(data)
        } catch (Exception e) {
            LOGGER.error("Ran into an exception processing the cached data (${file})", e)
            throw e
        }
    }

    static private String getURL(String url, MediaType expectedMediaType) {
        def conn = (HttpURLConnection) new URL(url).openConnection()
        // @todo: set read timeout?
        // @todo: verify content type
        int code = conn.getResponseCode()
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP status code ${code} returned for url ${url}")
        }
        String contentType = conn.getContentType()
        MediaType mediaType = MediaType.valueOf(contentType)
        if (!mediaType.isCompatible(expectedMediaType)) {
            throw new IOException("Incompatible content type from url ${url}: " +
                    "got ${contentType}, want ${expectedMediaType}")
        }
        conn.getInputStream().withStream { stream ->
            stream.getText()
        }
    }

    /**
     * Tries to get the data from the url. If fetching from the url fails, it retrieves the
     * data from the cache directory.
     *
     * @param url
     * @param cachedFile
     * @return
     * @throws Exception
     */
    @Deprecated
    public String getDataFromUrlOrCache(String url, String cachedFile) {
        // @todo: this should probably be implemented as a context manager
        // e.g. withDataFromUrlOrCache(url,cachepath) { data -> ... }
        // the new data is only written to cache if the closure completes successfully

        def file = getFile(cachedFile)
        try {
            def data = getURL(url, MediaType.WILDCARD_TYPE)

            if (data && isDataSourceNew(file, data)) {
                LOGGER.info("New content found for: ${url}")
                createCacheDirectory()
                file.write(data)
            } else {
                LOGGER.info("No new content for: ${url}")
            }
            return data
        } catch (Exception e) {
            LOGGER.error("Ran into an exception grabbing the url data", e)
            LOGGER.info("Attempting to fall back on cached data")
            // @todo: catch the IOException if the file doesn't exist and raise NotCachedError
            // or something
            return file.getText()
        }
    }

    /**
     * Returns data stored in the cache
     * @param cachedFile path within the cache directory
     * @throws IOException  if the file does not exist
     * @return cached data
     */
    public String getCachedData(String cachedFile) {
        def file = getFile(cachedFile)
        LOGGER.info(file.toString())
        // @todo: catch the IOException if the file doesn't exist
        //        and raise NotCachedError or something
        def data = file.getText()
        data
    }

    /**
     * Writes data to the cache
     * @param cachedFile path within the cache directory
     * @param data data to write
     */
    public void writeDataToCache(String cachedFile, String data) {
        createCacheDirectory()
        def file = getFile(cachedFile)
        if (data && isDataSourceNew(file, data)) {
            LOGGER.info("New content found for ${cachedFile}")
            createCacheDirectory()

            file.write(data)
        } else {
            LOGGER.info("No new content for ${cachedFile}")
        }
    }

    /**
     * Returns a File within the cache directory
     *
     * @param fileName path relative to cacheDirectory
     * @return
     */
    private File getFile(String fileName) {
        if (fileName == null) {
            throw new Exception("fileName must not be null")
        }

        String filePath = cacheDirectory + "/" + fileName

        // Check that cached file is within the current api directory
        Path child = Paths.get(filePath).toAbsolutePath()
        Path parent = Paths.get(cacheDirectory).toAbsolutePath()
        if (!child.startsWith(parent)) {
            throw new Exception("Cache directory is outside of api directory")
        }

        new File(filePath)
    }

    /**
     * Create cache directory if needed.
     */
    private void createCacheDirectory() {
        // Create a File object representing cache directory
        def directory = new File(cacheDirectory)

        // If it doesn't exist
        if (!directory.exists()) {
            LOGGER.info("Creating cache directory: ${cacheDirectory}")
            directory.mkdirs()
        }
    }

    /**
     * Checks whether the datasource is new or not by comparing the contents
     *
     * @param filename
     * @param recentData
     * @return
     */
    private Boolean isDataSourceNew(File file, String recentData) {
        if (!file.exists()) {
            return true
        }

        String fileContent = file?.getText()
        if (!fileContent) {
            return true
        }

        fileContent != recentData
    }
}
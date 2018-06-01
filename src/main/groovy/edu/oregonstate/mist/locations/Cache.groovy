package edu.oregonstate.mist.locations

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    public <T> T withDataFromUrlOrCache(String url, String cacheFilename, Closure closure) {
        def file = getFile(cacheFilename)
        String data
        try {
            def conn = (HttpURLConnection) new URL(url).openConnection()
            // @todo: set read timeout?
            // @todo: verify content type
            int code = conn.getResponseCode()
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP status code ${code} returned for url ${url}")
            }

            data = conn.getInputStream().withStream { stream ->
                stream.getText()
            }
        } catch (Exception e) {
            LOGGER.error("Ran into an exception grabbing the url data", e)
            // @todo: catch the IOException if the file doesn't exist and raise NotCachedError
            // or something
            data = file.getText()
        }

        T returnValue = closure(data)

        if (data && isDataSourceNew(file, data)) {
            LOGGER.info("New content found for: ${url}")
            createCacheDirectory()
            file.write(data)
        } else {
            LOGGER.info("No new content for: ${url}")
        }

        return returnValue
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
    public String getDataFromUrlOrCache(String url, String cachedFile) {
        // @todo: this should probably be implemented as a context manager
        // e.g. withDataFromUrlOrCache(url,cachepath) { data -> ... }
        // the new data is only written to cache if the closure completes successfully

        def file = getFile(cachedFile)
        try {
            def conn = (HttpURLConnection)new URL(url).openConnection()
            // @todo: set read timeout?
            // @todo: verify content type
            int code = conn.getResponseCode()
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP status code ${code} returned for url ${url}")
            }

            def data = conn.getInputStream().withStream{ stream ->
                stream.getText()
            }

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
            // @todo: catch the IOException if the file doesn't exist and raise NotCachedError
            // or something
            return file.getText()
        }
    }

    /**
     * Tries to get json data from the url. If fetching from the url fails, it retrieves the
     * data from the cache directory. This method checks that the returned content type is
     * application/json, but it does not actually try to parse the data.
     *
     * @param url
     * @param cachedFile
     * @return
     * @throws Exception
     */
    public String getJsonFromUrlOrCache(String url, String cachedFile) {
        // @todo: check content type
        getDataFromUrlOrCache(url,cachedFile)
    }

    /**
     * Tries to get xml data from the url. If fetching from the url fails, it retrieves the
     * data from the cache directory. This method checks that the returned content type is
     * application/xml, but it does not actually try to parse the data.
     *
     * @param url
     * @param cachedFile
     * @return
     * @throws Exception
     */
    public String getXmlFromUrlOrCache(String url, String cachedFile) {
        // @todo: check content type
        getDataFromUrlOrCache(url,cachedFile)
    }

    /**
     * Returns data stored in the cache
     * @param cachedFile    path within the cache directory
     * @throws IOException  if the file does not exist
     * @return cached data
     */
    public String getCachedData(String cachedFile) {
        def file = getFile(cachedFile)
        LOGGER.info(file.toString())
        // @todo: catch the IOException if the file doesn't exist and raise NotCachedError
        // or something
        def data = file.getText()
        data
    }

    /**
     * Writes data to the cache
     * @param cachedFile path within the cache directory
     * @param data       data to write
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
        if( !directory.exists() ) {
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
package edu.oregonstate.mist.locations

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.MessageDigest

class LocationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationUtil.class)

    final Map<String, String> locationConfiguration

    /**
     * Working directory where the cache and downloaded / generated files are stored.
     */
    private final String cacheDirectory

    LocationUtil(Map<String, String> locationConfiguration) {
        this.locationConfiguration = locationConfiguration
        cacheDirectory = locationConfiguration.get("cacheDirectory")
    }

    /**
     * Calculates MD5 Hash of a string
     *
     * @param content
     * @return
     */
    public static String getMD5Hash(String content) {
        MessageDigest.getInstance("MD5").digest(content.bytes).encodeHex().toString()
    }

    /**
     * Tries to get the data from the URL. If fetching from the url fails, it retrieves the
     * data from the cache directory
     *
     * @param URL
     * @param cachedFile
     * @return
     * @throws Exception
     */
    public String getDataFromUrlOrCache(String URL, String cachedFile) throws Exception {
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

    /**
     * Checks whether the datasource is new or not by comparing md5 hash
     *
     * @param filename
     * @param recentData
     * @return
     */
    private boolean isDataSourceNew(String filename, String recentData) {
        def file = new File(cacheDirectory + "/" + filename)
        if (!file.exists()) {
            return true
        }

        String fileContent = file?.getText()
        if (!fileContent) {
            return true
        }

        getMD5Hash(fileContent) != getMD5Hash(recentData)
    }
}

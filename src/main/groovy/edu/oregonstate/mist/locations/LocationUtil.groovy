package edu.oregonstate.mist.locations

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

class LocationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationUtil.class)

    public static final String VALID_LAT_LONG = "-?\\d+(\\.\\d+)?"

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
     * Tries to get the data from the url. If fetching from the url fails, it retrieves the
     * data from the cache directory
     *
     * @param url
     * @param cachedFile
     * @return
     * @throws Exception
     */
    public String getDataFromUrlOrCache(String url, String cachedFile) {
        def data
        def filePath = getFilePath(cachedFile)

        try {
            // Check that cached file is within the current api directory
            Path child = Paths.get(filePath).toAbsolutePath()
            def parent = Paths.get(cacheDirectory).toAbsolutePath()
            if (!child.startsWith(parent)) {
                throw new Exception("Cache directory is outside of api directory")
            }

            data = new URL(url).getText()
            if (data && isDataSourceNew(cachedFile, data)) {
                LOGGER.info("New content found for: ${url}")
                createCacheDirectory()

                new File(filePath).write(data)
            } else {
                LOGGER.info("No new content for: ${url}")
            }
        } catch (Exception e) {
            LOGGER.error("Ran into an exception grabbing the url data", e)
            data = new File(filePath).getText()
        }

        data
    }

    /**
     * Returns path to file within cacheDirectory
     *
     * @param fileName
     * @return
     */
    private String getFilePath(String fileName) {
        cacheDirectory + "/" + fileName
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
        def file = new File(getFilePath(filename))
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

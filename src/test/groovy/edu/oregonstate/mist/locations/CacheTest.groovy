package edu.oregonstate.mist.locations

import groovy.test.GroovyAssert
import javax.ws.rs.core.MediaType
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class CacheTest {
    private Cache cache
    private final String CACHE_DIRECTORY = "test-cache"
    private final String CACHE_FILE = "test1.json"
    private final String IGNORED_URL = "https://this-is-ignored/"

    abstract class MockCache extends Cache {
        MockCache(Map x) { super(x) }

        @Override
        String getURL(String url, MediaType expectedMediaType) {
                getURLData()
        }

        abstract String getURLData()
    }

    @Before
    void setup() {
        File file = new File("${CACHE_DIRECTORY}/${CACHE_FILE}")
        file.getParentFile().mkdirs()
        file.createNewFile()
        cache = new Cache([cacheDirectory: CACHE_DIRECTORY])
    }

    @After
    void teardown() {
        FileUtils.deleteDirectory(new File(CACHE_DIRECTORY))
    }

    @Test
    void testWithDataFromUrlOrCache() {
        // the first request is cached and subsequent responses pull from it

        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            Integer n = 0
            @Override
            String getURL(String url, MediaType expectedMediaType) {
                if (n == 0) {
                    n += 1
                    return '{"data": 42}'
                } else {
                    throw new IOException("simulated http request failure")
                }
            }
        }

        String ret1 = cache.withDataFromUrlOrCache(IGNORED_URL, CACHE_FILE) { it }
        String ret2 = cache.withDataFromUrlOrCache(IGNORED_URL, CACHE_FILE) { it }
        assert ret1 == ret2
    }

    @Test
    void testWithDataFromUrlOrCache_success() {
        // if the http fetch succeeds we don't hit the cache

        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            String getURL(String url, MediaType expectedMediaType) {
                "{}"
            }
        }

        int n = 0
        Integer ret = cache.withDataFromUrlOrCache(IGNORED_URL, CACHE_FILE) {
            n += 1
            42
        }
        assert ret == 42

        if (n != 1) {
            if (n == 0) {
                Assert.fail("closure not called")
            } else {
                Assert.fail("closure called more than once")
            }
        }
    }

    @Test
    void testWithDataFromUrlOrCache_fail() {
        // if the http fetch fails we retry

        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            String getURL(String url, MediaType expectedMediaType) {
                throw new IOException("simulated http request failure")
            }
        }

        int n = 0
        Integer ret = cache.withDataFromUrlOrCache(IGNORED_URL, CACHE_FILE) {
            n += 1
            try {
                cache.getURL(IGNORED_URL, null)
                41
            } catch (IOException e) {
                n += 1
                42
            }
        }
        assert ret == 42

        if (n != 2) {
            if (n == 0) {
                Assert.fail("closure not called")
            } else if (n == 1) {
                Assert.fail("closure only called once")
            } else {
                Assert.fail("closure called more than once")
            }
        }
    }

    @Test
    void testGetUrl() {
        // getURL succeeds
        HttpURLConnection conn = Mockito.mock(HttpURLConnection)
        Mockito.when(conn.getResponseCode()).thenReturn(200)
        Mockito.when(conn.getContentType()).thenReturn("text/plain; charset=UTF-8")
        Mockito.when(conn.getInputStream()).thenReturn(new ByteArrayInputStream("42".bytes))
    }

    @Test
    void testGetUrl_statusFailure() {
        HttpURLConnection conn = Mockito.mock(HttpURLConnection)
        Mockito.when(conn.getResponseCode()).thenReturn(404)
        Mockito.when(conn.getContentType()).thenReturn("text/plain; charset=UTF-8")
        Mockito.when(conn.getInputStream()).thenReturn(new ByteArrayInputStream("42".bytes))

        // getURL fails if the connection is not 200
        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            HttpURLConnection openHttpUrlConnection(URL _) {
                conn
            }
        }
        GroovyAssert.shouldFail(IOException) {
            cache.getURL(IGNORED_URL, MediaType.WILDCARD_TYPE)
        }
    }

    @Test
    void testGetUrl_contentTypeFailure() {
        HttpURLConnection conn = Mockito.mock(HttpURLConnection)
        Mockito.when(conn.getResponseCode()).thenReturn(200)
        Mockito.when(conn.getContentType()).thenReturn("application/json")
        Mockito.when(conn.getInputStream()).thenReturn(new ByteArrayInputStream("42".bytes))
        // getURL fails if the connection is not 200
        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            HttpURLConnection openHttpUrlConnection(URL _) {
                conn
            }
        }

        GroovyAssert.shouldFail(IOException) {
            cache.getURL(IGNORED_URL, MediaType.APPLICATION_XML_TYPE)
        }
    }
}

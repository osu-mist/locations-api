package edu.oregonstate.mist.locations

import groovy.test.GroovyAssert

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import javax.ws.rs.core.MediaType


class CacheTest {
    private Cache cache
    private final String CACHE_DIRECTORY = "test-cache"

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
        cache = new Cache([cacheDirectory: CACHE_DIRECTORY])
    }

    @After
    void teardown() {
        //FileUtils.deleteDirectory(CACHE_DIRECTORY)
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

        String ret1 = cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") { it }
        String ret2 = cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") { it }
        assert ret1 == ret2
    }

    @Test
    void testWithDataFromUrlOrCache_success() {
        // if the http fetch succeeeds we don't hit the cache

        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            String getURL(String url, MediaType expectedMediaType) {
                return "{}"
            }
        }

        int n = 0
        Integer ret = cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") {
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
    void testWithDataFromUrlOrCache_fail1() {
        // if the http fetch fails we retry

        Cache cache = new Cache([cacheDirectory: CACHE_DIRECTORY]) {
            @Override
            String getURL(String url, MediaType expectedMediaType) {
                throw new IOException("simulated http request failure")
            }
            @Override
            String getFile(File x) {
                return "{}"
            }
        }

        int n = 0
        Integer ret = cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") {
            n += 1
            42
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
    void testWithDataFromUrlOrCache_() {
        // if the closure fails we retry
        cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") {
            throw new RuntimeException("ruh-roh")
        }

        // if both fail we explode
        cache.withDataFromUrlOrCache("https://this-is-ignored/", "test1.json") {
            throw new RuntimeException("ruh-roh")
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
        cache.getURL("http://this-is-ignored", MediaType.WILDCARD_TYPE)
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
            cache.getURL("http://this-is-ignored", MediaType.APPLICATION_XML_TYPE)
        }
    }

    @Test
    void testWithJsonFromUrlOrCache() {
        // checks content-type
    }


    @Test
    void testGetCachedData() {

    }

    @Test
    void testWriteDataToCache() {

    }
}

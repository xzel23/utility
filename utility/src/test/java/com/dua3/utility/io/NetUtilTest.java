package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class NetUtilTest {

    @Test
    void testReadContent() throws Exception {
        final URLConnection urlConnectionMock = Mockito.mock(URLConnection.class);
        final String expected = "test content";
        InputStream is = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
        Mockito.when(urlConnectionMock.getInputStream()).thenReturn(is);

        URLStreamHandler mockStreamHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return urlConnectionMock;
            }
        };

        URL url = new URL("http://test", "test.com", 80, "", mockStreamHandler);
        String content = NetUtil.readContent(url, StandardCharsets.UTF_8);
        assertEquals(expected, content);
    }

    @Test
    void testSameURL() throws MalformedURLException {
        assertTrue(NetUtil.sameURL(null, null));

        URL url1 = new URL("http://test1");
        URL url2 = new URL("http://test2");
        assertFalse(NetUtil.sameURL(url1, url2));

        url1 = new URL("http://test");
        url2 = new URL("http://test");
        assertTrue(NetUtil.sameURL(url1, url2));
    }

    @Test
    void testVoidURL() {
        URL voidUrl = NetUtil.voidURL();
        assertNotNull(voidUrl);
        assertEquals("null", voidUrl.getProtocol());
    }
}
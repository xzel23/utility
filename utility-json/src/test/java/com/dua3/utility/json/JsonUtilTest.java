package com.dua3.utility.json;

import com.dua3.utility.io.IOUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

class JsonUtilTest {

    private final URL resourceUrl = getClass().getResource("test.json");
    private final Path resourcePath = IOUtil.toPath(resourceUrl);

    @Test
    void testReadUrl() throws IOException {
        JsonUtil.read(resourceUrl);
    }

    @Test
    void testReadPath() throws IOException {
        JsonUtil.read(resourcePath);
    }
}

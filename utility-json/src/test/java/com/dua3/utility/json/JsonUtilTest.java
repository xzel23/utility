package com.dua3.utility.json;

import com.dua3.utility.io.IOUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    private final URL resourceUrl = getClass().getResource("test.json");
    private final Path resourcePath = IOUtil.toPath(resourceUrl);

    @org.junit.jupiter.api.Test
    void testReadUrl() throws IOException {
        JsonUtil.read(resourceUrl);
    }

    @org.junit.jupiter.api.Test
    void testReadPath() throws IOException {
        JsonUtil.read(resourcePath);
    }
}

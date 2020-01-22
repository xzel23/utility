// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Test the FileSystemView class.
 */
public class IOUtilTest {

    /**
     * Test getting extension for a Path.
     */
    @Test
    public void testGetExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "txt";
        String actual = IOUtil.getExtension(path);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtensionStringArg() {
        assertEquals("txt", IOUtil.getExtension("test.txt"));
        assertEquals("txt", IOUtil.getExtension("folder/subfolder/test.txt"));
        assertEquals("txt", IOUtil.getExtension("folder/subfolder/test.txt/"));
        assertEquals("txt", IOUtil.getExtension("./folder/subfolder/test.txt"));
        assertEquals("txt", IOUtil.getExtension("./folder/subfolder/test.txt/"));

        assertEquals("", IOUtil.getExtension("test"));
        assertEquals("", IOUtil.getExtension("folder/subfolder/test"));
        assertEquals("", IOUtil.getExtension("folder/subfolder/test/"));
        assertEquals("", IOUtil.getExtension("./folder/subfolder/test"));
        assertEquals("", IOUtil.getExtension("./folder/subfolder/test/"));
        assertEquals("", IOUtil.getExtension("/test"));
        assertEquals("", IOUtil.getExtension("./test"));
        assertEquals("", IOUtil.getExtension("../test"));
    }

    @Test
    public void testStripExtension() {
        assertEquals("test", IOUtil.stripExtension("test.txt"));
        assertEquals("folder/subfolder/test", IOUtil.stripExtension("folder/subfolder/test.txt"));
        assertEquals("folder/subfolder/test", IOUtil.stripExtension("folder/subfolder/test.txt/"));
        assertEquals("./folder/subfolder/test", IOUtil.stripExtension("./folder/subfolder/test.txt"));
        assertEquals("./folder/subfolder/test", IOUtil.stripExtension("./folder/subfolder/test.txt/"));

        assertEquals("test", IOUtil.stripExtension("test"));
        assertEquals("folder/subfolder/test", IOUtil.stripExtension("folder/subfolder/test"));
        assertEquals("folder/subfolder/test/", IOUtil.stripExtension("folder/subfolder/test/"));
        assertEquals("./folder/subfolder/test", IOUtil.stripExtension("./folder/subfolder/test"));
        assertEquals("./folder/subfolder/test/", IOUtil.stripExtension("./folder/subfolder/test/"));
        assertEquals("/test", IOUtil.stripExtension("/test"));
        assertEquals("./test", IOUtil.stripExtension("./test"));
        assertEquals("../test", IOUtil.stripExtension("../test"));
    }
}

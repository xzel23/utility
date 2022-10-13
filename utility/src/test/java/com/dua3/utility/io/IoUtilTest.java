// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.io.IoUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the FileSystemView class.
 */
public class IoUtilTest {

    /**
     * Test getting extension for a Path.
     */
    @Test
    public void testGetExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "txt";
        String actual = IoUtil.getExtension(path);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtensionStringArg() {
        assertEquals("txt", IoUtil.getExtension("test.txt"));
        assertEquals("txt", IoUtil.getExtension("folder/subfolder/test.txt"));
        assertEquals("txt", IoUtil.getExtension("folder/subfolder/test.txt/"));
        assertEquals("txt", IoUtil.getExtension("./folder/subfolder/test.txt"));
        assertEquals("txt", IoUtil.getExtension("./folder/subfolder/test.txt/"));

        assertEquals("def", IoUtil.getExtension("test.txt.def"));

        assertEquals("", IoUtil.getExtension("test"));
        assertEquals("", IoUtil.getExtension("folder/subfolder/test"));
        assertEquals("", IoUtil.getExtension("folder/subfolder/test/"));
        assertEquals("", IoUtil.getExtension("./folder/subfolder/test"));
        assertEquals("", IoUtil.getExtension("./folder/subfolder/test/"));
        assertEquals("", IoUtil.getExtension("/test"));
        assertEquals("", IoUtil.getExtension("./test"));
        assertEquals("", IoUtil.getExtension("../test"));
    }

    @Test
    public void testStripExtension() {
        assertEquals("test", IoUtil.stripExtension("test.txt"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test.txt"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test.txt/"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test.txt"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test.txt/"));

        assertEquals("test.txt", IoUtil.stripExtension("test.txt.def"));

        assertEquals("test", IoUtil.stripExtension("test"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test"));
        assertEquals("folder/subfolder/test/", IoUtil.stripExtension("folder/subfolder/test/"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test"));
        assertEquals("./folder/subfolder/test/", IoUtil.stripExtension("./folder/subfolder/test/"));
        assertEquals("/test", IoUtil.stripExtension("/test"));
        assertEquals("./test", IoUtil.stripExtension("./test"));
        assertEquals("../test", IoUtil.stripExtension("../test"));
    }

    @Test
    public void testReplaceExtension() {
        assertEquals("test.xyz", IoUtil.replaceExtension("test.txt", "xyz"));
        assertEquals("folder/subfolder/test.xyz", IoUtil.replaceExtension("folder/subfolder/test.txt", "xyz"));
        assertEquals("folder/subfolder/test.xyz/", IoUtil.replaceExtension("folder/subfolder/test.txt/", "xyz"));
        assertEquals("./folder/subfolder/test.xyz", IoUtil.replaceExtension("./folder/subfolder/test.txt", "xyz"));
        assertEquals("./folder/subfolder/test.xyz/", IoUtil.replaceExtension("./folder/subfolder/test.txt/", "xyz"));

        assertEquals("test.txt.abc", IoUtil.replaceExtension("test.txt.def", "abc"));

        assertEquals("test.xyz", IoUtil.replaceExtension("test", "xyz"));
        assertEquals("folder/subfolder/test.xyz", IoUtil.replaceExtension("folder/subfolder/test", "xyz"));
        assertEquals("folder/subfolder/test.xyz/", IoUtil.replaceExtension("folder/subfolder/test/", "xyz"));
        assertEquals("./folder/subfolder/test.xyz", IoUtil.replaceExtension("./folder/subfolder/test", "xyz"));
        assertEquals("./folder/subfolder/test.xyz/", IoUtil.replaceExtension("./folder/subfolder/test/", "xyz"));
        assertEquals("/test.xyz", IoUtil.replaceExtension("/test", "xyz"));
        assertEquals("./test.xyz", IoUtil.replaceExtension("./test", "xyz"));
        assertEquals("../test.xyz", IoUtil.replaceExtension("../test", "xyz"));
    }
}

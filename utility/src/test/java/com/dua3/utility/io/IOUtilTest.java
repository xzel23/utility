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
    public void testStripExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "text";
        String actual = IOUtil.stripExtension(String.valueOf(path.getFileName()));
        assertEquals(expected, actual);
    }

}

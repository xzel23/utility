package com.dua3.utility.test.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.dua3.utility.io.IOUtil;

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

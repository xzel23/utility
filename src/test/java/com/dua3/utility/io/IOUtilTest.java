package com.dua3.utility.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testStripExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "text";
        String actual = IOUtil.stripExtension(String.valueOf(path.getFileName()));
        Assert.assertEquals(expected, actual);
    }

}

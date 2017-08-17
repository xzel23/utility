package com.dua3.utility.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the FileSystemView class.
 */
public class IOUtilTest {
    
    @Test
    public void testGetExtension() throws IOException {
        Path path = Paths.get("folder", "text.txt");
        String expected = "txt";
        String actual = IOUtil.getExtension(path);
        Assert.assertEquals(expected, actual);
    }
    
}

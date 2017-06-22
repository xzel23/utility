package com.dua3.utility.io;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class FileSystemViewTest {

    @Test
    public void testClass() throws IOException {
        // at least when run from within eclipse (and probalbly other IDEs as well) *.class is loaded from the file system
        testClassHelper(getClass());
    }

    @Test
    public void testJdkClass() throws IOException {
        // java.lang.String should be loaded from rt.jar, so this tests the jar functionality
        testClassHelper(java.lang.String.class);
    }

    public void testClassHelper(Class<?> clazz) throws IOException {
        try (FileSystemView fsv = FileSystemView.create(clazz)) {
            assertNotNull(fsv);

            String pathToClassFile = clazz.getSimpleName()+".class";
            Path path = fsv.resolve(pathToClassFile);

            assertTrue(Files.exists(path));
            assertTrue(Files.size(path)>0);
        }
    }

}

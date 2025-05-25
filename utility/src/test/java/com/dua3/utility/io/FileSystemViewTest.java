// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the FileSystemView class.
 */
class FileSystemViewTest {

    private static void testClassHelper(Class<?> clazz) throws IOException {
        try (FileSystemView fsv = FileSystemView.forClass(clazz)) {
            assertNotNull(fsv);

            String pathToClassFile = clazz.getSimpleName() + ".class";
            Path path = fsv.resolve(pathToClassFile);

            assertTrue(Files.exists(path));
            assertTrue(Files.size(path) > 0);
        }
    }

    /**
     * Construct a FileSystemView for classpath resources and try to access a class
     * file.
     * In this test, a user supplied class is used which at least when run from
     * eclipse
     * is loaded from a class file on the file system.
     *
     * @throws IOException if resource could not be loaded
     */
    @Test
    void testClass() throws IOException {
        // at least when run from within eclipse (and probably other IDEs as well)
        // *.class is loaded from the file
        // system
        testClassHelper(getClass());
    }

    /**
     * Construct a FileSystemView for classpath resources and try to access a class
     * file.
     * In this test, a JDK class is used which is loaded from within a jar file.
     *
     * @throws IOException if resource could not be loaded
     */
    @Test
    void testJarClass() throws IOException {
        // org.junit.Assert should be loaded from rt.jar, so this tests the jar
        // functionality
        testClassHelper(org.junit.jupiter.api.Test.class);
    }

}

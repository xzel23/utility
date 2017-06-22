package com.dua3.utility.io;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class FileSystemViewTest {

    @Test
    public void test() throws IOException {
        Class<?> clazz = getClass();
        try (FileSystemView fsv = FileSystemView.create(clazz)) {
            assertNotNull(fsv);

            String pathToClassFile = clazz.getSimpleName()+".class";
            Path path = fsv.resolve(pathToClassFile);

            assertTrue(Files.exists(path));
            assertTrue(Files.size(path)>0);
        }
    }

}

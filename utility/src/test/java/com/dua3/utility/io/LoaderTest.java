package com.dua3.utility.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.SAME_THREAD)
class LoaderTest {

    @BeforeEach
    void setUp() {
        LoaderTestFirstProvider.reset();
        LoaderTestSecondProvider.reset();
    }

    @Test
    void loadUsesFirstMatchingSpiImplementation(@TempDir Path tempDir) throws IOException {
        Path input = tempDir.resolve("input.bin");
        Files.write(input, LoaderTestSecondProvider.magicBytes());

        Object[] options = {"opt", 17};
        URI uri = input.toUri();

        String result = Loader.load(String.class, uri, options);

        assertEquals("second-loader-result", result);
        assertEquals(0, LoaderTestFirstProvider.loadCalls);
        assertEquals(1, LoaderTestSecondProvider.loadCalls);
        assertEquals(LoaderTestSecondProvider.expectedMagic(), LoaderTestSecondProvider.lastMagic);
        assertSame(options, LoaderTestSecondProvider.lastOptions);
    }

    @Test
    void loadThrowsIfNoMatchingImplementationExists(@TempDir Path tempDir) throws IOException {
        Path input = tempDir.resolve("input.bin");
        Files.write(input, LoaderTestSecondProvider.magicBytes());

        IOException ex = assertThrows(IOException.class, () -> Loader.load(Integer.class, input.toUri(), new Object[0]));
        assertEquals("no Loader implementation supports java.lang.Integer", ex.getMessage());
    }

    @Test
    void loadUsesCallerSuppliedPayloadWithoutChangingItsUri() throws IOException {
        URI uri = URI.create("https://objects.example.test/documents/input.bin");
        try (Payload payload = Payload.fromInputStream(uri, new ByteArrayInputStream(LoaderTestSecondProvider.magicBytes()))) {
            String result = Loader.load(String.class, payload, "opt");

            assertEquals("second-loader-result", result);
            assertEquals(uri, LoaderTestSecondProvider.lastUri);
            assertEquals(1, LoaderTestSecondProvider.loadCalls);
        }
    }
}

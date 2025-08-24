package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CompressedBytesTest {

    /**
     * Tests the `toByteArray` method of the `CompressedBytes` class.
     * The `toByteArray` method decompresses a compressed byte array and returns the original data as a byte array.
     */
    @Test
    void testToByteArrayWithSimpleByteArray() {
        // Initialize some test data
        byte[] originalData = "Hello, World!".getBytes(StandardCharsets.UTF_8);

        // Compress the data
        CompressedBytes compressedBytes = CompressedBytes.compress(originalData);

        // Decompress using toByteArray
        byte[] result = compressedBytes.toByteArray();

        // Validate the decompressed data matches the original
        assertArrayEquals(originalData, result, "Decompressed data should match the original data");
    }

    @Test
    void testToByteArrayWithEmptyByteArray() {
        // Initialize empty byte array
        byte[] originalData = new byte[0];

        // Compress the data
        CompressedBytes compressedBytes = CompressedBytes.compress(originalData);

        // Decompress using toByteArray
        byte[] result = compressedBytes.toByteArray();

        // Validate the decompressed data matches the original
        assertArrayEquals(originalData, result, "Decompressed data should match the original empty array");
    }

    @Test
    void testToByteArrayWithLargeByteArray() {
        // Generate a large byte array
        byte[] originalData = new byte[10_000];
        Arrays.fill(originalData, (byte) 7); // Fill array with a specific value

        // Compress the data
        CompressedBytes compressedBytes = CompressedBytes.compress(originalData);

        // Decompress using toByteArray
        byte[] result = compressedBytes.toByteArray();

        // Validate the decompressed data matches the original
        assertArrayEquals(originalData, result, "Decompressed data should match the original large array");
    }

    @Test
    void testToByteArrayWithInputStream() {
        // Initialize some input data
        byte[] originalData = "Stream Data Example".getBytes(StandardCharsets.UTF_8);
        InputStream originalStream = new ByteArrayInputStream(originalData);

        // Compress the InputStream
        CompressedBytes compressedBytes = CompressedBytes.compress(originalStream);

        // Decompress using toByteArray
        byte[] result = compressedBytes.toByteArray();

        // Validate the decompressed data matches the original
        assertArrayEquals(originalData, result, "Decompressed data should match the original input stream content");
    }

    @Test
    void testLoadCompressedData() {
        // Initialize some test data
        byte[] originalData = "Test data for loading".getBytes(StandardCharsets.UTF_8);

        // Compress the data
        CompressedBytes original = CompressedBytes.compress(originalData);

        // Get the compressed data
        byte[] compressedData = original.getCompressedData();

        // Load the compressed data
        CompressedBytes loaded = CompressedBytes.loadCompressedData(compressedData);

        // Verify the loaded data matches the original
        assertArrayEquals(originalData, loaded.toByteArray(), "Loaded data should match the original data");
    }

    @Test
    void testLoadCompressedDataFromInputStream() throws IOException {
        // Initialize some test data
        byte[] originalData = "Test data for loading from stream".getBytes(StandardCharsets.UTF_8);

        // Compress the data
        CompressedBytes original = CompressedBytes.compress(originalData);

        // Get the compressed data and create an input stream
        byte[] compressedData = original.getCompressedData();
        InputStream inputStream = new ByteArrayInputStream(compressedData);

        // Load the compressed data from the input stream
        CompressedBytes loaded = CompressedBytes.loadCompressedData(inputStream);

        // Verify the loaded data matches the original
        assertArrayEquals(originalData, loaded.toByteArray(), "Loaded data from stream should match the original data");
    }

    @Test
    void testLoadCompressedDataWithEmptyData() {
        // Test with empty data
        byte[] emptyData = new byte[0];

        // Loading empty data should throw an exception
        assertThrows(IllegalArgumentException.class, () -> CompressedBytes.loadCompressedData(emptyData));
    }

    @Test
    void testIsCompressed() {
        // Initialize some test data that will be compressed
        byte[] compressibleData = new byte[1000];
        Arrays.fill(compressibleData, (byte) 0); // Highly compressible data

        // Initialize some test data that won't be compressed efficiently
        Random random = new Random();
        byte[] randomData = new byte[100];
        for (int i = 0; i < randomData.length; i++) {
            randomData[i] = (byte) random.nextInt(256);
        }

        // Compress the data
        CompressedBytes compressed = CompressedBytes.compress(compressibleData);
        CompressedBytes possiblyUncompressed = CompressedBytes.compress(randomData);

        // Test isCompressed
        assertTrue(compressed.isCompressed(), "Compressible data should be stored compressed");

        assertArrayEquals(compressibleData, compressed.toByteArray(), "Compressible data should be restored correctly");
        assertArrayEquals(randomData, possiblyUncompressed.toByteArray(), "Compressible data should be restored correctly");
    }

    @Test
    void testInputStream() throws IOException {
        // Initialize some test data
        byte[] originalData = "Test data for input stream".getBytes(StandardCharsets.UTF_8);

        // Compress the data
        CompressedBytes compressedBytes = CompressedBytes.compress(originalData);

        // Get input stream and read all bytes
        try (InputStream inputStream = compressedBytes.inputStream()) {
            byte[] result = inputStream.readAllBytes();

            // Verify the data read from the input stream matches the original
            assertArrayEquals(originalData, result, "Data from input stream should match the original data");
        }
    }

    @Test
    void testGetCompressedData() {
        // Initialize some test data
        byte[] originalData = "Test data for getting compressed data".getBytes(StandardCharsets.UTF_8);

        // Compress the data
        CompressedBytes compressedBytes = CompressedBytes.compress(originalData);

        // Get the compressed data
        byte[] compressedData = compressedBytes.getCompressedData();

        // Verify the compressed data is not null and has some content
        assertNotNull(compressedData, "Compressed data should not be null");
        assertTrue(compressedData.length > 0, "Compressed data should have some content");

        // Verify the first byte indicates compression status
        assertTrue(compressedData[0] == 0 || compressedData[0] == 1,
                "First byte should be 0 (uncompressed) or 1 (compressed)");
    }
}

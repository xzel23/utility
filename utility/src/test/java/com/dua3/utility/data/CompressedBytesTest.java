package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
}
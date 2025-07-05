// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilTest {

    @Test
    void testGetRandom() {
        // Get the SecureRandom instance
        SecureRandom random = RandomUtil.getRandom();

        // Verify it's not null
        assertNotNull(random);

        // Verify that calling getRandom() multiple times returns the same instance
        SecureRandom random2 = RandomUtil.getRandom();
        assertSame(random, random2, "Multiple calls to getRandom() should return the same instance");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 16, 32, 64, 128})
    void testGenerateRandomBytes(int length) {
        // Generate random bytes
        byte[] randomBytes = RandomUtil.generateRandomBytes(length);

        // Verify the length
        assertEquals(length, randomBytes.length);

        // For non-zero length, verify that multiple calls generate different bytes
        if (length > 0) {
            byte[] randomBytes2 = RandomUtil.generateRandomBytes(length);
            assertFalse(Arrays.equals(randomBytes, randomBytes2),
                    "Multiple calls to generateRandomBytes() should generate different bytes");
        }
    }

    @Test
    void testRandomBytesDistribution() {
        // This test checks that the random bytes have a reasonable distribution
        // by generating a large number of bytes and checking that all possible byte values appear

        // Skip this test for very slow or resource-constrained environments
        int length = 10000; // Generate 10,000 bytes
        byte[] randomBytes = RandomUtil.generateRandomBytes(length);

        // Count occurrences of each byte value
        Set<Byte> uniqueValues = new HashSet<>();
        for (byte b : randomBytes) {
            uniqueValues.add(b);
        }

        // With 10,000 bytes, we expect to see most of the 256 possible byte values
        // A reasonable lower bound might be 200 unique values
        assertTrue(uniqueValues.size() > 200,
                "Expected at least 200 unique byte values in 10,000 random bytes, but got " + uniqueValues.size());
    }
}
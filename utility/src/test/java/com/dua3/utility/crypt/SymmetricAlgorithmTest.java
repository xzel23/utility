package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.InvalidAlgorithmParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SymmetricAlgorithm} enum.
 */
class SymmetricAlgorithmTest {

    @Test
    void testGetTransformation() {
        assertEquals("AES/GCM/NoPadding", SymmetricAlgorithm.AES.getTransformation());
    }

    @Test
    void testGetKeyAlgorithm() {
        assertEquals("AES", SymmetricAlgorithm.AES.getKeyAlgorithm());
    }

    @Test
    void testGetDefaultKeySize() {
        assertEquals(256, SymmetricAlgorithm.AES.getDefaultKeySize());
    }

    @Test
    void testGetIvLength() {
        assertEquals(12, SymmetricAlgorithm.AES.getIvLength());
    }

    @Test
    void testRequiresIv() {
        assertTrue(SymmetricAlgorithm.AES.requiresIv());
    }

    @Test
    void testIsAuthenticated() {
        assertTrue(SymmetricAlgorithm.AES.isAuthenticated());
    }

    @Test
    void testValidateKeySizeValid() throws InvalidAlgorithmParameterException {
        // These should not throw exceptions
        SymmetricAlgorithm.AES.validateKeySize(128);
        SymmetricAlgorithm.AES.validateKeySize(192);
        SymmetricAlgorithm.AES.validateKeySize(256);
    }

    @ParameterizedTest
    @ValueSource(ints = {64, 96, 127, 129, 191, 193, 255, 257, 384})
    void testValidateKeySizeInvalid(int keySize) {
        assertThrows(InvalidAlgorithmParameterException.class, () -> 
            SymmetricAlgorithm.AES.validateKeySize(keySize)
        );
    }

    @Test
    void testAlgorithm() {
        assertEquals("AES", SymmetricAlgorithm.AES.algorithm());
    }

    @ParameterizedTest
    @EnumSource(SymmetricAlgorithm.class)
    void testEnumValues(SymmetricAlgorithm algorithm) {
        // This test ensures that all enum values are properly defined
        assertNotNull(algorithm.getTransformation());
        assertNotNull(algorithm.getKeyAlgorithm());
        assertTrue(algorithm.getDefaultKeySize() > 0);
        assertTrue(algorithm.getIvLength() >= 0);
        assertNotNull(algorithm.algorithm());
    }
}
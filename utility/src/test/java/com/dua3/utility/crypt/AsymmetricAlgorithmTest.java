package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link AsymmetricAlgorithm} enum.
 */
class AsymmetricAlgorithmTest {

    @Test
    void testAlgorithm() {
        assertEquals("RSA", AsymmetricAlgorithm.RSA.algorithm());
        assertEquals("EC", AsymmetricAlgorithm.EC.algorithm());
        assertEquals("DSA", AsymmetricAlgorithm.DSA.algorithm());
    }

    @Test
    void testKeyFactoryAlgorithm() {
        assertEquals("RSA", AsymmetricAlgorithm.RSA.keyFactoryAlgorithm());
        assertEquals("EC", AsymmetricAlgorithm.EC.keyFactoryAlgorithm());
        assertEquals("DSA", AsymmetricAlgorithm.DSA.keyFactoryAlgorithm());
    }

    @Test
    void testGetSignatureAlgorithm() {
        assertEquals(Optional.of("SHA256withRSA"), AsymmetricAlgorithm.RSA.getSignatureAlgorithm());
        assertEquals(Optional.of("SHA256withECDSA"), AsymmetricAlgorithm.EC.getSignatureAlgorithm());
        assertEquals(Optional.of("SHA256withDSA"), AsymmetricAlgorithm.DSA.getSignatureAlgorithm());
    }

    @Test
    void testGetTransformation() {
        assertEquals(Optional.of("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"), AsymmetricAlgorithm.RSA.getTransformation());
        assertEquals(Optional.of("ECIES"), AsymmetricAlgorithm.EC.getTransformation());
        assertEquals(Optional.empty(), AsymmetricAlgorithm.DSA.getTransformation());
    }

    @Test
    void testIsEncryptionSupported() {
        assertTrue(AsymmetricAlgorithm.RSA.isEncryptionSupported());
        assertTrue(AsymmetricAlgorithm.EC.isEncryptionSupported());
        assertFalse(AsymmetricAlgorithm.DSA.isEncryptionSupported());
    }

    @ParameterizedTest
    @EnumSource(AsymmetricAlgorithm.class)
    void testEnumValues(AsymmetricAlgorithm algorithm) {
        // This test ensures that all enum values are properly defined
        assertNotNull(algorithm.algorithm());
        assertNotNull(algorithm.keyFactoryAlgorithm());
    }
}
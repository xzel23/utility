package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;

import java.security.Provider;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BouncyCastleTest {

    @Test
    void testIsAvailable() {
        assertTrue(BouncyCastle.isAvailable());
    }

    @Test
    void testEnsureAvailable() {
        assertDoesNotThrow(BouncyCastle::ensureAvailable);
    }

    @Test
    void testEnsureProvider() {
        Provider provider = BouncyCastle.ensureProvider();
        assertNotNull(provider);
        assertEquals("BC", provider.getName());
    }

    @Test
    void testGetProvider() {
        Optional<Provider> providerOpt = BouncyCastle.getProvider();
        assertTrue(providerOpt.isPresent());
        assertEquals("BC", providerOpt.get().getName());
    }
}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class CryptUtilTest {

    private static final int[] KEY_LENGTHS = {128, 192, 256};

    private static final String[] MESSAGES = {
            "",
            "secret message",
            System.getProperties().toString()
    };

    @Test
    void testTextEncryption() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            System.out.format("Testing encryption with key length %d bits%n", keyLength);

            byte[] key = CryptUtil.generateKey(keyLength);
            System.out.format("key = %s%n", HexFormat.of().formatHex(key));

            for (String message : MESSAGES) {
                System.out.format("message length = %d%n", message.length());

                String encrypted = CryptUtil.encrypt(key, message);
                String decrypted = CryptUtil.decrypt(key, encrypted);

                System.out.format("cipher  length = %d%n", encrypted.length());

                assertEquals(message, decrypted);
            }
        }
    }

    @Test
    void testCharArrayEncryption() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            byte[] key = CryptUtil.generateKey(keyLength);

            for (String message : MESSAGES) {
                char[] messageChars = message.toCharArray();
                String encrypted = CryptUtil.encrypt(key, messageChars);
                String decrypted = CryptUtil.decrypt(key, encrypted);

                assertEquals(message, decrypted);

                // Verify the char array was not modified
                assertEquals(message, new String(messageChars));
            }
        }
    }

    @Test
    void testDecryptToChars() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            byte[] key = CryptUtil.generateKey(keyLength);

            for (String message : MESSAGES) {
                String encrypted = CryptUtil.encrypt(key, message);
                char[] decryptedChars = CryptUtil.decryptToChars(key, encrypted);

                assertEquals(message, new String(decryptedChars));

                // Clean up sensitive data
                Arrays.fill(decryptedChars, '\0');
            }
        }
    }

    @Test
    void testByteArrayEncryption() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            byte[] key = CryptUtil.generateKey(keyLength);

            for (String message : MESSAGES) {
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                byte[] encrypted = CryptUtil.encrypt(key, messageBytes);
                byte[] decrypted = CryptUtil.decrypt(key, encrypted);

                assertEquals(message, new String(decrypted, StandardCharsets.UTF_8));

                // Verify the original byte array was not modified
                assertArrayEquals(messageBytes, message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 24, 32})
    void testGenerateSalt(int length) {
        byte[] salt = CryptUtil.generateSalt(length);

        assertEquals(length, salt.length);

        // Generate another salt and verify it's different (extremely unlikely to be the same)
        byte[] anotherSalt = CryptUtil.generateSalt(length);
        assertFalse(Arrays.equals(salt, anotherSalt));
    }

    @ParameterizedTest
    @ValueSource(ints = {128, 192, 256})
    void testGenerateKey(int bits) {
        byte[] key = CryptUtil.generateKey(bits);

        assertEquals(bits / 8, key.length);

        // Generate another key and verify it's different (extremely unlikely to be the same)
        byte[] anotherKey = CryptUtil.generateKey(bits);
        assertFalse(Arrays.equals(key, anotherKey));
    }

    @Test
    void testDeriveKeyWithSalt() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        byte[] salt = CryptUtil.generateSalt(16);

        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            byte[] key = CryptUtil.deriveKey(passphrase.clone(), salt, 10000, keyBits);

            assertEquals(keyBits / 8, key.length);

            // Derive the key again with the same parameters and verify it's the same
            byte[] sameKey = CryptUtil.deriveKey(passphrase.clone(), salt, 10000, keyBits);
            assertArrayEquals(key, sameKey);

            // Derive with different salt and verify it's different
            byte[] differentSalt = CryptUtil.generateSalt(16);
            byte[] differentKey = CryptUtil.deriveKey(passphrase.clone(), differentSalt, 10000, keyBits);
            assertFalse(Arrays.equals(key, differentKey));
        }
    }

    @Test
    void testDeriveKeyWithContext() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        String context = "user:testuser";

        byte[] key = CryptUtil.deriveKey(passphrase.clone(), context);

        // Default key size is 256 bits = 32 bytes
        assertEquals(32, key.length);

        // Derive the key again with the same parameters and verify it's the same
        byte[] sameKey = CryptUtil.deriveKey(passphrase.clone(), context);
        assertArrayEquals(key, sameKey);

        // Derive with different context and verify it's different
        String differentContext = "user:otheruser";
        byte[] differentKey = CryptUtil.deriveKey(passphrase.clone(), differentContext);
        assertFalse(Arrays.equals(key, differentKey));
    }

    @Test
    void testEndToEndWithDerivedKey() throws GeneralSecurityException {
        char[] passphrase = "secure-passphrase".toCharArray();
        String context = "app:test";

        byte[] key = CryptUtil.deriveKey(passphrase.clone(), context);

        String message = "This is a secret message";
        String encrypted = CryptUtil.encrypt(key, message);
        String decrypted = CryptUtil.decrypt(key, encrypted);

        assertEquals(message, decrypted);
    }
}

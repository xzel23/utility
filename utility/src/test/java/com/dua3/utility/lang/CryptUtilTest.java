// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.SecretKey;

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

    @Test
    void testDeriveSecretKeyWithSalt() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        byte[] salt = CryptUtil.generateSalt(16);

        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey key = CryptUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits);

            // Verify key length
            assertEquals(keyBits / 8, key.getEncoded().length);

            // Derive the key again with the same parameters and verify it's the same
            SecretKey sameKey = CryptUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits);
            assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

            // Derive with different salt and verify it's different
            byte[] differentSalt = CryptUtil.generateSalt(16);
            SecretKey differentKey = CryptUtil.deriveSecretKey(passphrase.clone(), differentSalt, 10000, keyBits);
            assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));

            // Test encryption/decryption with the derived key
            String message = "Test message for derived key";
            String encrypted = CryptUtil.encrypt(key, message);
            String decrypted = CryptUtil.decrypt(key, encrypted);
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testDeriveSecretKeyWithContext() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        String context = "user:testuser";

        SecretKey key = CryptUtil.deriveSecretKey(passphrase.clone(), context);

        // Default key size is 256 bits = 32 bytes
        assertEquals(32, key.getEncoded().length);

        // Derive the key again with the same parameters and verify it's the same
        SecretKey sameKey = CryptUtil.deriveSecretKey(passphrase.clone(), context);
        assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

        // Derive with different context and verify it's different
        String differentContext = "user:otheruser";
        SecretKey differentKey = CryptUtil.deriveSecretKey(passphrase.clone(), differentContext);
        assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));

        // Test encryption/decryption with the derived key
        String message = "Test message for context-derived key";
        String encrypted = CryptUtil.encrypt(key, message);
        String decrypted = CryptUtil.decrypt(key, encrypted);
        assertEquals(message, decrypted);
    }

    @Test
    void testToSecretKey() throws GeneralSecurityException {
        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            byte[] keyBytes = CryptUtil.generateKey(keyBits);

            SecretKey secretKey = CryptUtil.toSecretKey(keyBytes);

            // Verify key length
            assertEquals(keyBits / 8, secretKey.getEncoded().length);

            // Verify the key material is the same
            assertArrayEquals(keyBytes, secretKey.getEncoded());

            // Test encryption/decryption with the key
            String message = "Test message for SecretKey";
            String encrypted = CryptUtil.encrypt(secretKey, message);
            String decrypted = CryptUtil.decrypt(secretKey, encrypted);
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testGenerateSecretKey() throws GeneralSecurityException {
        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey key = CryptUtil.generateSecretKey(keyBits);

            // Verify key length
            assertEquals(keyBits / 8, key.getEncoded().length);

            // Generate another key and verify it's different
            SecretKey anotherKey = CryptUtil.generateSecretKey(keyBits);
            assertFalse(Arrays.equals(key.getEncoded(), anotherKey.getEncoded()));

            // Test encryption/decryption with the generated key
            String message = "Test message for generated SecretKey";
            String encrypted = CryptUtil.encrypt(key, message);
            String decrypted = CryptUtil.decrypt(key, encrypted);
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testKeyConversionMethodsDefault() throws GeneralSecurityException {
        // Generate a key pair for testing with default algorithm (RSA)
        KeyPair keyPair = CryptUtil.generateRSAKeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();
        PrivateKey originalPrivateKey = keyPair.getPrivate();

        // Test toPublicKey and toPrivateKey with default algorithm
        byte[] publicKeyBytes = originalPublicKey.getEncoded();
        byte[] privateKeyBytes = originalPrivateKey.getEncoded();

        PublicKey convertedPublicKey = CryptUtil.toPublicKey(publicKeyBytes);
        PrivateKey convertedPrivateKey = CryptUtil.toPrivateKey(privateKeyBytes);

        // Verify the converted keys match the originals
        assertArrayEquals(originalPublicKey.getEncoded(), convertedPublicKey.getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedPrivateKey.getEncoded());

        // Test toKeyPair with default algorithm
        KeyPair convertedKeyPair = CryptUtil.toKeyPair(publicKeyBytes, privateKeyBytes);
        assertArrayEquals(originalPublicKey.getEncoded(), convertedKeyPair.getPublic().getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedKeyPair.getPrivate().getEncoded());
    }

    @ParameterizedTest
    @EnumSource(CryptUtil.AsymmetricAlgorithm.class)
    void testKeyConversionMethodsWithAlgorithm(CryptUtil.AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        // Generate appropriate key pair based on algorithm
        KeyPair keyPair = generateKeyPairForAlgorithm(algorithm);
        PublicKey originalPublicKey = keyPair.getPublic();
        PrivateKey originalPrivateKey = keyPair.getPrivate();

        // Test toPublicKey and toPrivateKey with explicit algorithm
        byte[] publicKeyBytes = originalPublicKey.getEncoded();
        byte[] privateKeyBytes = originalPrivateKey.getEncoded();

        PublicKey convertedPublicKeyWithAlg = CryptUtil.toPublicKey(publicKeyBytes, algorithm);
        PrivateKey convertedPrivateKeyWithAlg = CryptUtil.toPrivateKey(privateKeyBytes, algorithm);

        // Verify the converted keys match the originals
        assertArrayEquals(originalPublicKey.getEncoded(), convertedPublicKeyWithAlg.getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedPrivateKeyWithAlg.getEncoded());

        // Test toKeyPair with explicit algorithm
        KeyPair convertedKeyPairWithAlg = CryptUtil.toKeyPair(publicKeyBytes, privateKeyBytes, algorithm);
        assertArrayEquals(originalPublicKey.getEncoded(), convertedKeyPairWithAlg.getPublic().getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedKeyPairWithAlg.getPrivate().getEncoded());
    }

    private KeyPair generateKeyPairForAlgorithm(CryptUtil.AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        switch (algorithm) {
            case RSA:
                return CryptUtil.generateRSAKeyPair();
            case EC:
            case ECIES:
                return CryptUtil.generateECKeyPair("secp256r1");
            case DSA:
                return CryptUtil.generateKeyPair(algorithm, 2048);
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }


    @Test
    void testAsymmetricEncryption() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = CryptUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array encryption/decryption
        for (String message : MESSAGES) {
            if (message.isEmpty()) {
                continue; // Skip empty message as it might not be supported by RSA
            }

            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            // Skip if data is too large for RSA encryption
            if (data.length > 100) {
                continue;
            }

            byte[] encrypted = CryptUtil.encryptAsymmetric(publicKey, data);
            byte[] decrypted = CryptUtil.decryptAsymmetric(privateKey, encrypted);

            assertArrayEquals(data, decrypted);
        }

        // Test string encryption/decryption
        String message = "Short test message";
        String encrypted = CryptUtil.encryptAsymmetric(publicKey, message);
        String decrypted = CryptUtil.decryptAsymmetric(privateKey, encrypted);

        assertEquals(message, decrypted);

        // Test char array encryption/decryption
        char[] messageChars = message.toCharArray();
        String encryptedChars = CryptUtil.encryptAsymmetric(publicKey, messageChars);
        char[] decryptedChars = CryptUtil.decryptAsymmetricToChars(privateKey, encryptedChars);

        assertEquals(message, new String(decryptedChars));
    }

    @Test
    void testSigningAndVerification() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = CryptUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array signing/verification
        for (String message : MESSAGES) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            byte[] signature = CryptUtil.sign(privateKey, data);
            boolean verified = CryptUtil.verify(publicKey, data, signature);

            assertTrue(verified);

            // Verify that modifying the data invalidates the signature
            if (data.length > 0) {
                byte[] modifiedData = Arrays.copyOf(data, data.length);
                modifiedData[0] = (byte) (modifiedData[0] + 1);
                boolean verifiedModified = CryptUtil.verify(publicKey, modifiedData, signature);
                assertFalse(verifiedModified);
            }
        }

        // Test string signing/verification
        String message = "Test message for signing";
        String signature = CryptUtil.sign(privateKey, message);
        boolean verified = CryptUtil.verify(publicKey, message, signature);

        assertTrue(verified);

        // Test char array signing/verification
        char[] messageChars = message.toCharArray();
        String signatureChars = CryptUtil.sign(privateKey, messageChars);
        boolean verifiedChars = CryptUtil.verify(publicKey, messageChars, signatureChars);

        assertTrue(verifiedChars);
    }

    @Test
    void testHybridEncryption() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = CryptUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array hybrid encryption/decryption
        for (String message : MESSAGES) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = CryptUtil.encryptHybrid(publicKey, data);
            byte[] decrypted = CryptUtil.decryptHybrid(privateKey, encrypted);

            assertArrayEquals(data, decrypted);
        }

        // Test string hybrid encryption/decryption
        String message = "Test message for hybrid encryption";
        String encrypted = CryptUtil.encryptHybrid(publicKey, message);
        String decrypted = CryptUtil.decryptHybrid(privateKey, encrypted);

        assertEquals(message, decrypted);

        // Test char array hybrid encryption/decryption
        char[] messageChars = message.toCharArray();
        String encryptedChars = CryptUtil.encryptHybrid(publicKey, messageChars);
        char[] decryptedChars = CryptUtil.decryptHybridToChars(privateKey, encryptedChars);

        assertEquals(message, new String(decryptedChars));
    }

    @Test
    void testGenerateKeyPair() throws GeneralSecurityException {
        // Test RSA key pair generation
        KeyPair rsaKeyPair = CryptUtil.generateRSAKeyPair();
        assertNotNull(rsaKeyPair);
        assertEquals(CryptUtil.AsymmetricAlgorithm.RSA.name(), rsaKeyPair.getPublic().getAlgorithm());
        assertEquals(CryptUtil.AsymmetricAlgorithm.RSA.name(), rsaKeyPair.getPrivate().getAlgorithm());

        // Test custom algorithm and key size
        KeyPair customKeyPair = CryptUtil.generateKeyPair(CryptUtil.AsymmetricAlgorithm.RSA, 2048);
        assertNotNull(customKeyPair);
        assertEquals(CryptUtil.AsymmetricAlgorithm.RSA.name(), customKeyPair.getPublic().getAlgorithm());
        assertEquals(CryptUtil.AsymmetricAlgorithm.RSA.name(), customKeyPair.getPrivate().getAlgorithm());

        // Test EC key pair generation
        KeyPair ecKeyPair = CryptUtil.generateECKeyPair("secp256r1");
        assertNotNull(ecKeyPair);
        assertEquals("EC", ecKeyPair.getPublic().getAlgorithm());
        assertEquals("EC", ecKeyPair.getPrivate().getAlgorithm());
    }
}

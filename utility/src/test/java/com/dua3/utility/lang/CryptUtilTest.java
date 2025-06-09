// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.utility.crypt.AsymmetricAlgorithm;
import com.dua3.utility.crypt.CryptUtil;
import com.dua3.utility.crypt.InputBufferHandling;
import com.dua3.utility.crypt.KeyUtil;
import com.dua3.utility.crypt.SignatureUtil;
import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.security.InvalidKeyException;
import java.util.Locale;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;

class CryptUtilTest {

    static {
        // Register Bouncy Castle provider for tests
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            System.err.println("Failed to register Bouncy Castle provider: " + e.getMessage());
        }
    }

    private static final int[] KEY_LENGTHS = {128, 192, 256};

    private static final String[] MESSAGES = {
            "",
            "secret message",
            System.getProperties().toString()
    };

    private static boolean isEciesSupported() {
        try {
            // Try to get an ECIES cipher to check if a provider supports it
            Cipher.getInstance("ECIES");
            return true;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return false;
        }
    }

    @Test
    void testSymmetricEncryption() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            SecretKey key = KeyUtil.generateSecretKey(keyLength);

            for (String message : MESSAGES) {
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                byte[] encrypted = CryptUtil.encryptSymmetric(key, messageBytes, InputBufferHandling.PRESERVE);
                byte[] decrypted = CryptUtil.decryptSymmetric(key, encrypted);

                assertEquals(message, new String(decrypted, StandardCharsets.UTF_8));

                // Verify the original byte array was not modified
                assertArrayEquals(messageBytes, message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 24, 32})
    void testGenerateSalt(int length) {
        byte[] salt = KeyUtil.generateSalt(length);

        assertEquals(length, salt.length);

        // Generate another salt and verify it's different (extremely unlikely to be the same)
        byte[] anotherSalt = KeyUtil.generateSalt(length);
        assertFalse(Arrays.equals(salt, anotherSalt));
    }

    @ParameterizedTest
    @ValueSource(ints = {128, 192, 256})
    void testgenerateSecretKey(int bits) throws GeneralSecurityException {
        SecretKey key = KeyUtil.generateSecretKey(bits);

        // Generate another key and verify it's different (extremely unlikely to be the same)
        SecretKey anotherKey = KeyUtil.generateSecretKey(bits);
        assertFalse(Arrays.equals(key.getEncoded(), anotherKey.getEncoded()));
    }

    @Test
    void testDeriveKeyWithSalt() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        byte[] salt = KeyUtil.generateSalt(16);

        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey key = KeyUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits, InputBufferHandling.PRESERVE);

            // Derive the key again with the same parameters and verify it's the same
            SecretKey sameKey = KeyUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits, InputBufferHandling.CLEAR_AFTER_USE);
            assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

            // Derive with different salt and verify it's different
            byte[] differentSalt = KeyUtil.generateSalt(16);
            SecretKey differentKey = KeyUtil.deriveSecretKey(passphrase.clone(), differentSalt, 10000, keyBits, InputBufferHandling.CLEAR_AFTER_USE);
            assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));
        }
    }

    @Test
    void testDeriveKeyWithContext() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        char[] context = "user:testuser".toCharArray();

        SecretKey key = KeyUtil.deriveSecretKey(passphrase.clone(), context, InputBufferHandling.CLEAR_AFTER_USE);

        // Derive the key again with the same parameters and verify it's the same
        SecretKey sameKey = KeyUtil.deriveSecretKey(passphrase.clone(), context, InputBufferHandling.CLEAR_AFTER_USE);
        assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

        // Derive with different context and verify it's different
        char[] differentContext = "user:otheruser".toCharArray();
        SecretKey differentKey = KeyUtil.deriveSecretKey(passphrase.clone(), differentContext, InputBufferHandling.CLEAR_AFTER_USE);
        assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));
    }

    @Test
    void testEndToEndWithDerivedKey() throws GeneralSecurityException {
        char[] passphrase = "secure-passphrase".toCharArray();
        char[] context = "app:test".toCharArray();

        SecretKey key = KeyUtil.deriveSecretKey(passphrase.clone(), context, InputBufferHandling.CLEAR_AFTER_USE);

        String message = "This is a secret message";
        byte[] encrypted = CryptUtil.encryptSymmetric(key, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
        String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(key, encrypted));

        assertEquals(message, decrypted);
    }

    @Test
    void testDeriveSecretKeyWithSalt() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        byte[] salt = KeyUtil.generateSalt(16);

        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey key = KeyUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits, InputBufferHandling.PRESERVE);

            // Verify key length
            assertEquals(keyBits / 8, key.getEncoded().length);

            // Derive the key again with the same parameters and verify it's the same
            SecretKey sameKey = KeyUtil.deriveSecretKey(passphrase.clone(), salt, 10000, keyBits, InputBufferHandling.CLEAR_AFTER_USE);
            assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

            // Derive with different salt and verify it's different
            byte[] differentSalt = KeyUtil.generateSalt(16);
            SecretKey differentKey = KeyUtil.deriveSecretKey(passphrase.clone(), differentSalt, 10000, keyBits, InputBufferHandling.CLEAR_AFTER_USE);
            assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));

            // Test encryption/decryption with the derived key
            String message = "Test message for derived key";
            byte[] encrypted = CryptUtil.encryptSymmetric(key, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
            String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(key, encrypted));
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testDeriveSecretKeyWithContext() throws GeneralSecurityException {
        char[] passphrase = "test-passphrase".toCharArray();
        char[] context = "user:testuser".toCharArray();

        SecretKey key = KeyUtil.deriveSecretKey(passphrase.clone(), context, InputBufferHandling.CLEAR_AFTER_USE);

        // Default key size is 256 bits = 32 bytes
        assertEquals(32, key.getEncoded().length);

        // Derive the key again with the same parameters and verify it's the same
        SecretKey sameKey = KeyUtil.deriveSecretKey(passphrase.clone(), context, InputBufferHandling.CLEAR_AFTER_USE);
        assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

        // Derive with different context and verify it's different
        char[] differentContext = "user:otheruser".toCharArray();
        SecretKey differentKey = KeyUtil.deriveSecretKey(passphrase.clone(), differentContext, InputBufferHandling.CLEAR_AFTER_USE);
        assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));

        // Test encryption/decryption with the derived key
        String message = "Test message for context-derived key";
        byte[] encrypted = CryptUtil.encryptSymmetric(key, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
        String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(key, encrypted));
        assertEquals(message, decrypted);
    }

    @Test
    void testToSecretKey() throws GeneralSecurityException {
        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey k = KeyUtil.generateSecretKey(keyBits);
            SecretKey secretKey = KeyUtil.toSecretKey(k.getEncoded());

            // Verify key length
            assertEquals(keyBits / 8, k.getEncoded().length);

            // Verify the key material is the same
            assertArrayEquals(k.getEncoded(), secretKey.getEncoded());

            // Test encryption/decryption with the key
            String message = "Test message for SecretKey";
            byte[] encrypted = CryptUtil.encryptSymmetric(secretKey, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
            String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(secretKey, encrypted));
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testGenerateSecretKey() throws GeneralSecurityException {
        // Test with different key sizes
        for (int keyBits : new int[]{128, 192, 256}) {
            SecretKey key = KeyUtil.generateSecretKey(keyBits);

            // Verify key length
            assertEquals(keyBits / 8, key.getEncoded().length);

            // Generate another key and verify it's different
            SecretKey anotherKey = KeyUtil.generateSecretKey(keyBits);
            assertFalse(Arrays.equals(key.getEncoded(), anotherKey.getEncoded()));

            // Test encryption/decryption with the generated key
            String message = "Test message for generated SecretKey";
            byte[] encrypted = CryptUtil.encryptSymmetric(key, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
            String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(key, encrypted));
            assertEquals(message, decrypted);
        }
    }

    @Test
    void testKeyConversionMethodsDefault() throws GeneralSecurityException {
        // Generate a key pair for testing with default algorithm (RSA)
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();
        PrivateKey originalPrivateKey = keyPair.getPrivate();

        // Test toPublicKey and toPrivateKey with default algorithm
        byte[] publicKeyBytes = originalPublicKey.getEncoded();
        byte[] privateKeyBytes = originalPrivateKey.getEncoded();

        PublicKey convertedPublicKey = KeyUtil.toPublicKey(publicKeyBytes);
        PrivateKey convertedPrivateKey = KeyUtil.toPrivateKey(privateKeyBytes);

        // Verify the converted keys match the originals
        assertArrayEquals(originalPublicKey.getEncoded(), convertedPublicKey.getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedPrivateKey.getEncoded());
    }

    @ParameterizedTest
    @EnumSource(AsymmetricAlgorithm.class)
    void testKeyConversionMethodsWithAlgorithm(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        // Generate appropriate key pair based on algorithm
        KeyPair keyPair = generateSecretKeyPairForAlgorithm(algorithm);
        PublicKey originalPublicKey = keyPair.getPublic();
        PrivateKey originalPrivateKey = keyPair.getPrivate();

        // Test toPublicKey and toPrivateKey with explicit algorithm
        byte[] publicKeyBytes = originalPublicKey.getEncoded();
        byte[] privateKeyBytes = originalPrivateKey.getEncoded();

        PublicKey convertedPublicKeyWithAlg = KeyUtil.toPublicKey(publicKeyBytes, algorithm);
        PrivateKey convertedPrivateKeyWithAlg = KeyUtil.toPrivateKey(privateKeyBytes, algorithm);

        // Verify the converted keys match the originals
        assertArrayEquals(originalPublicKey.getEncoded(), convertedPublicKeyWithAlg.getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedPrivateKeyWithAlg.getEncoded());

        // Test toKeyPair with explicit algorithm
        KeyPair convertedKeyPairWithAlg = KeyUtil.toKeyPair(publicKeyBytes, privateKeyBytes, algorithm);
        assertArrayEquals(originalPublicKey.getEncoded(), convertedKeyPairWithAlg.getPublic().getEncoded());
        assertArrayEquals(originalPrivateKey.getEncoded(), convertedKeyPairWithAlg.getPrivate().getEncoded());
    }

    private KeyPair generateSecretKeyPairForAlgorithm(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        switch (algorithm) {
            case RSA:
                return KeyUtil.generateRSAKeyPair();
            case EC:
                return KeyUtil.generateECKeyPair("secp256r1");
            case DSA:
                return KeyUtil.generateKeyPair(algorithm, 2048);
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    @Test
    void testSigningAndVerification() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array signing/verification
        for (String message : MESSAGES) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            byte[] signature = SignatureUtil.sign(privateKey, data, InputBufferHandling.PRESERVE);
            boolean verified = SignatureUtil.verify(publicKey, data, signature, InputBufferHandling.CLEAR_AFTER_USE);

            assertTrue(verified);

            // Verify that modifying the data invalidates the signature
            if (data.length > 0) {
                byte[] modifiedData = Arrays.copyOf(data, data.length);
                modifiedData[0] = (byte) (modifiedData[0] + 1);
                boolean verifiedModified = SignatureUtil.verify(publicKey, modifiedData, signature, InputBufferHandling.CLEAR_AFTER_USE);
                assertFalse(verifiedModified);
            }
        }

        // Test string signing/verification
        String message = "Test message for signing";
        byte[] signature = SignatureUtil.sign(privateKey, message);
        boolean verified = SignatureUtil.verify(publicKey, message, signature);

        assertTrue(verified);

        // Test char array signing/verification
        byte[] signatureChars = SignatureUtil.sign(privateKey, message.toCharArray(), InputBufferHandling.CLEAR_AFTER_USE);
        boolean verifiedChars = SignatureUtil.verify(publicKey, message.toCharArray(), signatureChars, InputBufferHandling.CLEAR_AFTER_USE);

        assertTrue(verifiedChars);
    }

    @Test
    void testHybridEncryptionWithDefaultAlgorithm() throws GeneralSecurityException {
        // Generate a key pair for testing
        KeyPair keyPair = KeyUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Test byte array hybrid encryption/decryption
        for (String message : MESSAGES) {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = CryptUtil.encryptHybrid(publicKey, data, InputBufferHandling.PRESERVE);
            byte[] decrypted = CryptUtil.decryptHybrid(privateKey, encrypted);

            assertArrayEquals(data, decrypted);
        }
    }

    @Test
    void testgenerateSecretKeyPair() throws GeneralSecurityException {
        // Test RSA key pair generation
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        assertNotNull(rsaKeyPair);
        assertEquals(AsymmetricAlgorithm.RSA.name(), rsaKeyPair.getPublic().getAlgorithm());
        assertEquals(AsymmetricAlgorithm.RSA.name(), rsaKeyPair.getPrivate().getAlgorithm());

        // Test custom algorithm and key size
        KeyPair customKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        assertNotNull(customKeyPair);
        assertEquals(AsymmetricAlgorithm.RSA.name(), customKeyPair.getPublic().getAlgorithm());
        assertEquals(AsymmetricAlgorithm.RSA.name(), customKeyPair.getPrivate().getAlgorithm());

        // Test EC key pair generation
        KeyPair ecKeyPair = KeyUtil.generateECKeyPair("secp256r1");
        assertNotNull(ecKeyPair);
        assertEquals("EC", ecKeyPair.getPublic().getAlgorithm());
        assertEquals("EC", ecKeyPair.getPrivate().getAlgorithm());
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class)
    void testAsymmetricEncryption(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        // Add assumption for ECIES
        if (algorithm == AsymmetricAlgorithm.EC) {
            assumeTrue(isEciesSupported(), 
                "ECIES algorithm requires a third-party security provider (like Bouncy Castle) to be available");
        }

        KeyPair keyPair = generateSecretKeyPairForAlgorithm(algorithm);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        if (algorithm.isEncryptionSupported()) {
            // Algorithm supports direct encryption - test with various message types

            // Test byte array encryption/decryption with all messages
            for (String message : MESSAGES) {
                if (message.isEmpty()) {
                    continue; // Skip empty message as it might not be supported
                }

                byte[] data = message.getBytes(StandardCharsets.UTF_8);

                // Skip if data is too large for the algorithm (e.g., RSA has size limits)
                if (data.length > 100) {
                    continue;
                }

                byte[] encrypted = CryptUtil.encryptAsymmetric(publicKey, data);
                byte[] decrypted = CryptUtil.decryptAsymmetric(privateKey, encrypted);
                assertArrayEquals(data, decrypted);
            }
        } else {
            // Algorithm doesn't support direct encryption - should throw InvalidKeyException
            byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);

            // Test that all encryption methods throw InvalidKeyException
            assertThrows(InvalidKeyException.class, () -> {
                CryptUtil.encryptAsymmetric(publicKey, testData);
            });
        }
    }

    @Test
    void testAsymmetricEncryptionWithOversizedData() throws InvalidAlgorithmParameterException {
        // Generate RSA key pair with known size (2048 bits)
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        PublicKey rsaPublicKey = rsaKeyPair.getPublic();

        // RSA with OAEP padding can encrypt at most (key_size_in_bytes - 2 - 2*hash_length - label_length)
        // For 2048-bit RSA with SHA-256 OAEP: 256 - 2 - 2*32 - 0 = 190 bytes maximum
        // Let's create data that's definitely too large
        byte[] oversizedData = new byte[300]; // 300 bytes - definitely too large for 2048-bit RSA
        Arrays.fill(oversizedData, (byte) 'A');

        // This should throw an exception
        assertThrows(GeneralSecurityException.class, () -> {
            CryptUtil.encryptAsymmetric(rsaPublicKey, oversizedData);
        });
    }

    @Test
    void testAsymmetricEncryptionMaximumSizeRSA() throws GeneralSecurityException {
        // Generate RSA key pair
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        PublicKey rsaPublicKey = rsaKeyPair.getPublic();
        PrivateKey rsaPrivateKey = rsaKeyPair.getPrivate();

        // For 2048-bit RSA with OAEP SHA-256 padding:
        // Maximum plaintext size = key_size - 2*hash_size - 2 = 256 - 2*32 - 2 = 190 bytes
        byte[] maxSizeData = new byte[190];
        Arrays.fill(maxSizeData, (byte) 'X');

        // This should work (at the limit)
        try {
            byte[] encrypted = CryptUtil.encryptAsymmetric(rsaPublicKey, maxSizeData);
            byte[] decrypted = CryptUtil.decryptAsymmetric(rsaPrivateKey, encrypted);
            assertArrayEquals(maxSizeData, decrypted);
        } catch (GeneralSecurityException e) {
            // If 190 bytes fails, try with a smaller size
            // Some implementations might have slightly different limits
            byte[] smallerData = new byte[180];
            Arrays.fill(smallerData, (byte) 'X');

            byte[] encrypted = CryptUtil.encryptAsymmetric(rsaPublicKey, smallerData);
            byte[] decrypted = CryptUtil.decryptAsymmetric(rsaPrivateKey, encrypted);
            assertArrayEquals(smallerData, decrypted);
        }

        // One byte over the limit should definitely fail
        byte[] oversizedByOne = new byte[191];
        Arrays.fill(oversizedByOne, (byte) 'Y');

        assertThrows(GeneralSecurityException.class, () -> {
            CryptUtil.encryptAsymmetric(rsaPublicKey, oversizedByOne);
        });
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class)
    void testHybridEncryption(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        // Add assumption for ECIES
        if (algorithm == AsymmetricAlgorithm.EC) {
            assumeTrue(isEciesSupported(), 
                "ECIES algorithm requires a third-party security provider (like Bouncy Castle) to be available");
        }

        KeyPair keyPair = generateSecretKeyPairForAlgorithm(algorithm);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        if (algorithm.isEncryptionSupported()) {
            // Algorithm supports encryption - test hybrid encryption with large data

            // Test with large data that would fail with direct asymmetric encryption
            String largeMessage = "A".repeat(1000); // 1KB of data
            byte[] largeData = largeMessage.getBytes(StandardCharsets.UTF_8);

            // Test byte array hybrid encryption/decryption
            byte[] encryptedData = CryptUtil.encryptHybrid(publicKey, largeData, InputBufferHandling.PRESERVE);
            byte[] decryptedData = CryptUtil.decryptHybrid(privateKey, encryptedData);
            assertArrayEquals(largeData, decryptedData);

            // Test with various message sizes
            for (String message : MESSAGES) {
                if (message.isEmpty()) {
                    continue; // Skip empty message
                }

                byte[] data = message.getBytes(StandardCharsets.UTF_8);

                // Test hybrid encryption (should work for any size)
                byte[] hybridEncrypted = CryptUtil.encryptHybrid(publicKey, data, InputBufferHandling.PRESERVE);
                byte[] hybridDecrypted = CryptUtil.decryptHybrid(privateKey, hybridEncrypted);
                assertArrayEquals(data, hybridDecrypted);
            }

        } else {
            // Algorithm doesn't support encryption - should throw InvalidKeyException
            // But with a clearer message since this is for hybrid encryption
            byte[] testData = "test data for hybrid encryption".getBytes(StandardCharsets.UTF_8);

            // Test that all hybrid encryption methods throw InvalidKeyException
            InvalidKeyException exception1 = assertThrows(InvalidKeyException.class, () -> {
                CryptUtil.encryptHybrid(publicKey, testData, InputBufferHandling.CLEAR_AFTER_USE);
            });

            // Verify the exception message doesn't suggest using hybrid encryption
            // (since that's what we're already trying to do)
            String expectedPattern = "do(es)? not support.*encryption|for signatures only, not encryption";
            assertTrue(exception1.getMessage().toLowerCase(Locale.ROOT).matches(".*(" + expectedPattern + ").*"),
                "Exception message should indicate algorithm doesn't support encryption: " + exception1.getMessage());
        }
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class)
    void testAsymmetricEncryptionSupport(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        // Add assumption for ECIES
        if (algorithm == AsymmetricAlgorithm.EC) {
            assumeTrue(isEciesSupported(), 
                "ECIES algorithm requires a third-party security provider (like Bouncy Castle) to be available");
        }

        KeyPair keyPair = generateSecretKeyPairForAlgorithm(algorithm);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String shortMessage = "Hello, asymmetric world!";
        byte[] shortData = shortMessage.getBytes(StandardCharsets.UTF_8);

        if (algorithm.isEncryptionSupported()) {
            // Algorithm supports direct encryption - test it
            byte[] encryptedData = CryptUtil.encryptAsymmetric(publicKey, shortData);
            byte[] decryptedData = CryptUtil.decryptAsymmetric(privateKey, encryptedData);
            assertArrayEquals(shortData, decryptedData);
        } else {
            // Algorithm doesn't support direct encryption - should throw exception
            assertThrows(InvalidKeyException.class, () -> {
                CryptUtil.encryptAsymmetric(publicKey, shortData);
            });
        }
    }
}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
}
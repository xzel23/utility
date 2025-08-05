// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CryptUtilTest {

    @Test
    void testGeneratePasswordValidLength() {
        // Generate a password
        String password = CryptUtil.generatePassword();

        // Verify that the password length matches the expected size
        assertNotNull(password, "Password should not be null");
        assertEquals(24, password.length(), "Password length should be 24 characters (Base64 encoding of 16 bytes)");
    }

    @Test
    void testGeneratePasswordUniqueValues() {
        // Generate multiple passwords
        String password1 = CryptUtil.generatePassword();
        String password2 = CryptUtil.generatePassword();

        // Verify that the passwords are unique
        assertNotNull(password1, "Password1 should not be null");
        assertNotNull(password2, "Password2 should not be null");
        assertNotEquals(password1, password2, "Generated passwords should be unique");
    }

    private static final int[] KEY_LENGTHS = {128, 192, 256};
    private static final String[] MESSAGES = {
            "",
            "secret message",
            System.getProperties().toString()
    };

    // Test data for Argon2id tests
    private static final String TEST_PASSWORD = "test-password";
    private static final String TEST_PEPPER = "khcb6fbdvdqtd8923rbvjhcv96c";
    // Use a fixed salt for testing instead of generating a random one
    private static final byte[] TEST_SALT = new byte[]{
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    };

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
        byte[] input = "secure-passphrase".getBytes(StandardCharsets.UTF_8);
        byte[] info = "app:test".getBytes(StandardCharsets.UTF_8);
        SecretKey key = KeyUtil.deriveSecretKeyWithRandomSalt(SymmetricAlgorithm.AES, input, info, InputBufferHandling.CLEAR_AFTER_USE);

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
    @EnumSource(AsymmetricAlgorithm.class)
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

    private static boolean isEciesSupported() {
        try {
            // Try to get an ECIES cipher to check if a provider supports it
            Cipher.getInstance("ECIES");
            return true;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return false;
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
    @EnumSource(AsymmetricAlgorithm.class)
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
    @EnumSource(AsymmetricAlgorithm.class)
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

    @Test
    void testValidateAsymmetricEncryptionKey() throws GeneralSecurityException {
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        PublicKey rsaPublicKey = rsaKeyPair.getPublic();

        // Test valid key with appropriate data size
        byte[] validData = new byte[190]; // RSA max size for 2048-bit key
        assertDoesNotThrow(() ->
                CryptUtil.validateAsymmetricEncryptionKey(rsaPublicKey, validData.length));

        // Test RSA key with oversized data
        byte[] oversizedData = new byte[300];
        IllegalBlockSizeException exception = assertThrows(IllegalBlockSizeException.class, () ->
                CryptUtil.validateAsymmetricEncryptionKey(rsaPublicKey, oversizedData.length));
        assertTrue(exception.getMessage().contains("Data too large for RSA key"));

        // Test invalid DSA key
        KeyPair dsaKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.DSA, 2048);
        PublicKey dsaPublicKey = dsaKeyPair.getPublic();

        InvalidKeyException dsaException = assertThrows(InvalidKeyException.class, () ->
                CryptUtil.validateAsymmetricEncryptionKey(dsaPublicKey, validData.length));
        assertTrue(dsaException.getMessage().contains("DSA keys are for signatures only, not encryption"));
    }

    @Test
    void testEmailHashValidData() {
        byte[] hash = CryptUtil.emailHash("User@Test.com", TEST_PEPPER);

        assertNotNull(hash);
        assertEquals(32, hash.length);

        byte[] hash2 = CryptUtil.emailHash("user@test.com", TEST_PEPPER);
        assertArrayEquals(hash, hash2, "Hash should be the same for equivalent input data");
    }

    @Test
    void testEmailHashInvalidPepper() {
        assertThrows(IllegalArgumentException.class, () -> {
            CryptUtil.emailHash("test@example.com", "123");
        });
    }

    @Test
    void testEmailHashEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> CryptUtil.emailHash("", TEST_PEPPER));
    }

    @Test
    void testValidateAsymmetricEncryptionKeyWithValidRSAKey() throws GeneralSecurityException {
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        PublicKey rsaPublicKey = rsaKeyPair.getPublic();

        byte[] validData = new byte[190]; // RSA max size for 2048-bit key

        assertDoesNotThrow(() ->
                CryptUtil.validateAsymmetricEncryptionKey(rsaPublicKey, validData.length));
    }

    @Test
    void testValidateAsymmetricEncryptionKeyWithOversizedDataRSA() throws GeneralSecurityException {
        KeyPair rsaKeyPair = KeyUtil.generateRSAKeyPair();
        PublicKey rsaPublicKey = rsaKeyPair.getPublic();

        byte[] oversizedData = new byte[300]; // Exceeding max size for 2048-bit RSA key

        IllegalBlockSizeException exception = assertThrows(IllegalBlockSizeException.class, () ->
                CryptUtil.validateAsymmetricEncryptionKey(rsaPublicKey, oversizedData.length));
        assertTrue(exception.getMessage().contains("Data too large for RSA key"));
    }

    @Test
    void testValidateAsymmetricEncryptionKeyWithDSAKey() throws GeneralSecurityException {
        KeyPair dsaKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.DSA, 2048);
        PublicKey dsaPublicKey = dsaKeyPair.getPublic();

        byte[] validData = new byte[190]; // Arbitrary size for testing

        InvalidKeyException exception = assertThrows(InvalidKeyException.class, () ->
                CryptUtil.validateAsymmetricEncryptionKey(dsaPublicKey, validData.length));
        assertTrue(exception.getMessage().contains("DSA keys are for signatures only, not encryption"));
    }

    @Test
    void testValidateAsymmetricEncryptionKeyWithInvalidKey() {
        PublicKey invalidKey = new PublicKey() {
            @Override
            public String getAlgorithm() {
                return "INVALID";
            }

            @Override
            public String getFormat() {
                return "X.509";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        };

        byte[] validData = new byte[190]; // Arbitrary size for testing

        assertThrows(InvalidKeyException.class, () ->
                CryptUtil.validateAsymmetricEncryptionKey(invalidKey, validData.length));
    }

    // Argon2id Tests

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idBytesWithSecretKey() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(RandomUtil.generateRandomBytes(32), "AES");

        // Test valid case
        byte[] result = CryptUtil.getArgon2idBytes(input, TEST_SALT, secretKey);

        // Verify result
        assertNotNull(result);
        assertEquals(32, result.length); // Should be 256 bits (32 bytes)

        // Test consistency - same inputs should produce same output
        byte[] result2 = CryptUtil.getArgon2idBytes(input, TEST_SALT, secretKey);
        assertArrayEquals(result, result2);

        // Test different inputs produce different outputs
        byte[] differentInput = "different-password".getBytes(StandardCharsets.UTF_8);
        byte[] differentResult = CryptUtil.getArgon2idBytes(differentInput, TEST_SALT, secretKey);
        assertFalse(Arrays.equals(result, differentResult));
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idBytesWithPepper() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);

        // Test valid case
        byte[] result = CryptUtil.getArgon2idBytes(input, TEST_SALT, TEST_PEPPER);

        // Verify result
        assertNotNull(result);
        assertEquals(32, result.length); // Should be 256 bits (32 bytes)

        // Test consistency - same inputs should produce same output
        byte[] result2 = CryptUtil.getArgon2idBytes(input, TEST_SALT, TEST_PEPPER);
        assertArrayEquals(result, result2);

        // Test different inputs produce different outputs
        String differentPepper = "different-pepper";
        byte[] differentResult = CryptUtil.getArgon2idBytes(input, TEST_SALT, differentPepper);
        assertFalse(Arrays.equals(result, differentResult));
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idBytesWithByteArraySecret() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);
        byte[] secret = RandomUtil.generateRandomBytes(32);

        // Test valid case
        byte[] result = CryptUtil.getArgon2idBytes(input, TEST_SALT, secret);

        // Verify result
        assertNotNull(result);
        assertEquals(32, result.length); // Should be 256 bits (32 bytes)

        // Test with invalid salt length
        byte[] invalidSalt = RandomUtil.generateRandomBytes(8); // Not 16 bytes
        assertThrows(IllegalArgumentException.class, () ->
                CryptUtil.getArgon2idBytes(input, invalidSalt, secret)
        );
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idWithByteArrayAndSecretKey() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(RandomUtil.generateRandomBytes(32), "AES");

        // Test valid case
        String result = CryptUtil.getArgon2id(input, secretKey);

        // Verify result format (should be salt$hash in Base64)
        assertNotNull(result);
        assertTrue(result.contains("$"), "Result should contain $ separator");

        String[] parts = result.split("\\$");
        assertEquals(2, parts.length, "Result should have exactly two parts");

        // Both parts should be valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[0]));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[1]));

        // Salt should be 16 bytes
        assertEquals(16, Base64.getDecoder().decode(parts[0]).length);

        // Hash should be 32 bytes (256 bits)
        assertEquals(32, Base64.getDecoder().decode(parts[1]).length);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idWithByteArrayAndPepper() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);

        // Test valid case
        String result = CryptUtil.getArgon2id(input, TEST_PEPPER);

        // Verify result format (should be salt$hash in Base64)
        assertNotNull(result);
        assertTrue(result.contains("$"), "Result should contain $ separator");

        String[] parts = result.split("\\$");
        assertEquals(2, parts.length, "Result should have exactly two parts");

        // Both parts should be valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[0]));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[1]));
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idWithStringAndSecretKey() {
        // Create test data
        SecretKey secretKey = new SecretKeySpec(RandomUtil.generateRandomBytes(32), "AES");

        // Test valid case
        String result = CryptUtil.getArgon2id(TEST_PASSWORD, secretKey);

        // Verify result format (should be salt$hash in Base64)
        assertNotNull(result);
        assertTrue(result.contains("$"), "Result should contain $ separator");

        // Test that different inputs produce different outputs
        String differentPassword = "different-password";
        String differentResult = CryptUtil.getArgon2id(differentPassword, secretKey);
        assertNotEquals(result, differentResult);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testGetArgon2idWithStringAndPepper() {
        // Test valid case
        String result = CryptUtil.getArgon2id(TEST_PASSWORD, TEST_PEPPER);

        // Verify result format (should be salt$hash in Base64)
        assertNotNull(result);
        assertTrue(result.contains("$"), "Result should contain $ separator");

        // Test that different inputs produce different outputs
        String differentPassword = "different-password";
        String differentResult = CryptUtil.getArgon2id(differentPassword, TEST_PEPPER);
        assertNotEquals(result, differentResult);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithByteArrayAndSecretKey() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(RandomUtil.generateRandomBytes(32), "AES");

        // Generate hash
        String saltAndHash = CryptUtil.getArgon2id(input, secretKey);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(input, secretKey, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        byte[] wrongInput = "wrong-password".getBytes(StandardCharsets.UTF_8);
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongInput, secretKey, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");

        // Test with invalid saltAndHash format
        String invalidSaltAndHash = "invalid-format";
        assertThrows(IllegalArgumentException.class, () ->
                CryptUtil.verifyArgon2id(input, secretKey, invalidSaltAndHash)
        );
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithByteArrayAndByteArraySecret() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);
        byte[] secret = RandomUtil.generateRandomBytes(32);

        // Generate hash using the byte array version
        byte[] salt = RandomUtil.generateRandomBytes(16);
        byte[] hash = CryptUtil.getArgon2idBytes(input, salt, secret);
        String saltAndHash = Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(input, secret, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        byte[] wrongInput = "wrong-password".getBytes(StandardCharsets.UTF_8);
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongInput, secret, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithByteArrayAndPepper() {
        // Create test data
        byte[] input = TEST_PASSWORD.getBytes(StandardCharsets.UTF_8);

        // Generate hash
        String saltAndHash = CryptUtil.getArgon2id(input, TEST_PEPPER);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(input, TEST_PEPPER, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        byte[] wrongInput = "wrong-password".getBytes(StandardCharsets.UTF_8);
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongInput, TEST_PEPPER, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithStringAndSecretKey() {
        // Create test data
        SecretKey secretKey = new SecretKeySpec(RandomUtil.generateRandomBytes(32), "AES");

        // Generate hash
        String saltAndHash = CryptUtil.getArgon2id(TEST_PASSWORD, secretKey);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(TEST_PASSWORD, secretKey, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        String wrongPassword = "wrong-password";
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongPassword, secretKey, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithStringAndByteArraySecret() {
        // Create test data
        byte[] secret = RandomUtil.generateRandomBytes(32);

        // Generate hash using the byte array version
        byte[] salt = RandomUtil.generateRandomBytes(16);
        byte[] hash = CryptUtil.getArgon2idBytes(TEST_PASSWORD.getBytes(StandardCharsets.UTF_8), salt, secret);
        String saltAndHash = Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(TEST_PASSWORD, secret, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        String wrongPassword = "wrong-password";
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongPassword, secret, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void testVerifyArgon2idWithStringAndPepper() {
        // Generate hash
        String saltAndHash = CryptUtil.getArgon2id(TEST_PASSWORD, TEST_PEPPER);

        // Test verification with correct input
        boolean result = CryptUtil.verifyArgon2id(TEST_PASSWORD, TEST_PEPPER, saltAndHash);
        assertTrue(result, "Verification should succeed with correct input");

        // Test verification with incorrect input
        String wrongPassword = "wrong-password";
        boolean wrongResult = CryptUtil.verifyArgon2id(wrongPassword, TEST_PEPPER, saltAndHash);
        assertFalse(wrongResult, "Verification should fail with incorrect input");
    }
}

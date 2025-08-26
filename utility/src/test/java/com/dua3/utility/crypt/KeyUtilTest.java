// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
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
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class KeyUtilTest {

    private static String toPem(String header, byte[] data) {
        String base64 = java.util.Base64.getEncoder().encodeToString(data);
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ").append(header).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            int end = Math.min(i + 64, base64.length());
            sb.append(base64, i, end).append('\n');
        }
        sb.append("-----END ").append(header).append("-----\n");
        return sb.toString();
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
    void testDeriveSecretKeyWithSalt() throws GeneralSecurityException {
        byte[] passphrase = "secure-passphrase".getBytes(StandardCharsets.UTF_8);
        byte[] info = "app:test".getBytes(StandardCharsets.UTF_8);
        byte[] salt = KeyUtil.generateSalt(16);

        // derive key
        SecretKey key = KeyUtil.deriveSecretKey(SymmetricAlgorithm.AES, passphrase.clone(), salt, info, InputBufferHandling.PRESERVE);

        // Verify key length
        assertEquals(256 / 8, key.getEncoded().length);

        // Derive the key again with the same parameters and verify it's the same
        SecretKey sameKey = KeyUtil.deriveSecretKey(SymmetricAlgorithm.AES, passphrase.clone(), salt, info, InputBufferHandling.PRESERVE);
        assertArrayEquals(key.getEncoded(), sameKey.getEncoded());

        // Derive with different salt and verify it's different
        byte[] differentSalt = KeyUtil.generateSalt(16);
        SecretKey differentKey = KeyUtil.deriveSecretKey(SymmetricAlgorithm.AES, passphrase.clone(), differentSalt, info, InputBufferHandling.PRESERVE);
        assertFalse(Arrays.equals(key.getEncoded(), differentKey.getEncoded()));

        // Test encryption/decryption with the derived key
        String message = "Test message for derived key";
        byte[] encrypted = CryptUtil.encryptSymmetric(key, TextUtil.toByteArray(message), InputBufferHandling.CLEAR_AFTER_USE);
        String decrypted = TextUtil.decodeToString(CryptUtil.decryptSymmetric(key, encrypted));
        assertEquals(message, decrypted);
    }

    @Test
    void testLoadPublicAndPrivateKeyFromGenericPem_RSA() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        String pemPub = toPem("PUBLIC KEY", kp.getPublic().getEncoded());
        String pemPriv = toPem("PRIVATE KEY", kp.getPrivate().getEncoded());

        PublicKey loadedPub = KeyUtil.loadPublicKeyFromPem(pemPub);
        PrivateKey loadedPriv = KeyUtil.loadPrivateKeyFromPem(pemPriv);

        assertArrayEquals(kp.getPublic().getEncoded(), loadedPub.getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedPriv.getEncoded());
    }

    @Test
    void testLoadPublicAndPrivateKeyFromAlgorithmSpecificPem_RSA() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        // Use RSA-specific headers but still PKCS#8/X.509 content
        String pemPub = toPem("RSA PUBLIC KEY", kp.getPublic().getEncoded());
        String pemPriv = toPem("RSA PRIVATE KEY", kp.getPrivate().getEncoded());

        PublicKey loadedPub = KeyUtil.loadPublicKeyFromPem(pemPub);
        PrivateKey loadedPriv = KeyUtil.loadPrivateKeyFromPem(pemPriv);

        assertArrayEquals(kp.getPublic().getEncoded(), loadedPub.getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedPriv.getEncoded());
    }

    @Test
    void testLoadPublicAndPrivateKeyFromGenericPem_EC() throws Exception {
        KeyPair kp = KeyUtil.generateECKeyPair("secp256r1");
        String pemPub = toPem("PUBLIC KEY", kp.getPublic().getEncoded());
        String pemPriv = toPem("PRIVATE KEY", kp.getPrivate().getEncoded());

        PublicKey loadedPub = KeyUtil.loadPublicKeyFromPem(pemPub);
        PrivateKey loadedPriv = KeyUtil.loadPrivateKeyFromPem(pemPriv);

        assertArrayEquals(kp.getPublic().getEncoded(), loadedPub.getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedPriv.getEncoded());
    }

    @Test
    void testLoadPublicAndPrivateKeyFromAlgorithmSpecificPem_EC() throws Exception {
        KeyPair kp = KeyUtil.generateECKeyPair("secp256r1");
        String pemPub = toPem("EC PUBLIC KEY", kp.getPublic().getEncoded());
        String pemPriv = toPem("EC PRIVATE KEY", kp.getPrivate().getEncoded());

        PublicKey loadedPub = KeyUtil.loadPublicKeyFromPem(pemPub);
        PrivateKey loadedPriv = KeyUtil.loadPrivateKeyFromPem(pemPriv);

        assertArrayEquals(kp.getPublic().getEncoded(), loadedPub.getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedPriv.getEncoded());
    }

    @Test
    void testLoadKeyFromPem_withWhitespaceAndWindowsNewlines() throws Exception {
        KeyPair kp = KeyUtil.generateRSAKeyPair();
        String pemPub = toPem("PUBLIC KEY", kp.getPublic().getEncoded()).replace("\n", "\r\n");
        String pemPriv = ("  \n" + toPem("PRIVATE KEY", kp.getPrivate().getEncoded()) + "\n  ").replace("\n", "\r\n");

        PublicKey loadedPub = KeyUtil.loadPublicKeyFromPem(pemPub);
        PrivateKey loadedPriv = KeyUtil.loadPrivateKeyFromPem(pemPriv);

        assertArrayEquals(kp.getPublic().getEncoded(), loadedPub.getEncoded());
        assertArrayEquals(kp.getPrivate().getEncoded(), loadedPriv.getEncoded());
    }

    @Test
    void testLoadPublicKeyFromPem_invalidBase64() {
        String pem = "-----BEGIN PUBLIC KEY-----\ninvalid$$base64==\n-----END PUBLIC KEY-----\n";
        assertThrows(java.security.spec.InvalidKeySpecException.class, () -> KeyUtil.loadPublicKeyFromPem(pem));
    }

    @Test
    void testLoadPrivateKeyFromPem_invalidBase64() {
        String pem = "-----BEGIN PRIVATE KEY-----\ninvalid$$base64==\n-----END PRIVATE KEY-----\n";
        assertThrows(java.security.spec.InvalidKeySpecException.class, () -> KeyUtil.loadPrivateKeyFromPem(pem));
    }

    @Test
    void testLoadKeyFromPem_missingHeader() {
        String pem = "no headers here\n";
        assertThrows(java.security.spec.InvalidKeySpecException.class, () -> KeyUtil.loadPublicKeyFromPem(pem));
        assertThrows(java.security.spec.InvalidKeySpecException.class, () -> KeyUtil.loadPrivateKeyFromPem(pem));
    }

}

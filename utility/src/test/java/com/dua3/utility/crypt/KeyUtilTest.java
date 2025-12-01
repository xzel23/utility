// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyUtilTest {

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
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
        KeyPair keyPair = generatePrivatePairForAlgorithm(algorithm);
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

    private KeyPair generatePrivatePairForAlgorithm(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        return switch (algorithm) {
            case RSA -> KeyUtil.generateRSAKeyPair();
            case EC -> KeyUtil.generateECKeyPair("secp256r1");
            case DSA -> KeyUtil.generateKeyPair(algorithm, 2048);
        };
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
    void testToPem_InvalidKey() {
        // Test with an unsupported key type
        Key unsupportedKey = new SecretKeySpec(new byte[16], "AES"); // SecretKey not supported for PEM
        assertThrows(IllegalStateException.class, () -> KeyUtil.toPem(unsupportedKey));
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = "RSA")
        // EC does not work without encryption
    void testAppendPemAndLoadPrivateKeyRoundTrip_Unencrypted(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        StringBuilder sb = new StringBuilder();
        KeyUtil.appendPem(kp.getPrivate(), sb);
        String pem = sb.toString();

        PrivateKey loaded = PemData.parse(pem).asPrivateKey();
        if (algorithm == AsymmetricAlgorithm.EC) {
            // Comparing getEncoded() for EC keys is unreliable for encrypted PKCS#8 round-trips
            ECPrivateKey ecOrig = (ECPrivateKey) kp.getPrivate();
            ECPrivateKey ecLoaded = (ECPrivateKey) loaded;
            assertEquals(ecOrig.getS(), ecLoaded.getS());
            assertEquals(ecOrig.getParams().getCurve(), ecLoaded.getParams().getCurve());
        } else {
            assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getEncoded());
        }
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = {"RSA", "EC"})
    void testAppendPemAndLoadPrivateKeyRoundTrip_Encrypted(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        StringBuilder sb = new StringBuilder();
        char[] password = "changeit".toCharArray();
        KeyUtil.appendPem(kp.getPrivate(), password.clone(), sb);
        String pem = sb.toString();

        PrivateKey loaded = PemData.parse(pem).asPrivateKey(password.clone());
        if (algorithm == AsymmetricAlgorithm.EC) {
            // Comparing getEncoded() for EC keys is unreliable for encrypted PKCS#8 round-trips
            ECPrivateKey ecOrig = (ECPrivateKey) kp.getPrivate();
            ECPrivateKey ecLoaded = (ECPrivateKey) loaded;
            assertEquals(ecOrig.getS(), ecLoaded.getS());
            assertEquals(ecOrig.getParams().getCurve(), ecLoaded.getParams().getCurve());
        } else {
            assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getEncoded());
        }
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = "RSA")
        // EC does not work without encryption
    void testAppendPemAndLoadKeyPairRoundTrip_Unencrypted(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        StringBuilder sb = new StringBuilder();
        KeyUtil.appendPem(kp, sb);
        String pem = sb.toString();

        KeyPair loaded = PemData.parse(pem).asKeyPair();
        if (algorithm == AsymmetricAlgorithm.EC) {
            // Comparing getEncoded() for EC keys is unreliable for encrypted PKCS#8 round-trips
            ECPrivateKey ecOrig = (ECPrivateKey) kp.getPrivate();
            ECPrivateKey ecLoaded = (ECPrivateKey) loaded.getPrivate();
            assertEquals(ecOrig.getS(), ecLoaded.getS());
            assertEquals(ecOrig.getParams().getCurve(), ecLoaded.getParams().getCurve());
            ECPublicKey ecOrig2 = (ECPublicKey) kp.getPublic();
            ECPublicKey ecLoaded2 = (ECPublicKey) loaded.getPublic();
            assertEquals(ecOrig2.getW(), ecLoaded2.getW());
            assertEquals(ecOrig2.getParams().getCurve(), ecLoaded2.getParams().getCurve());
        } else {
            assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getPrivate().getEncoded());
            assertArrayEquals(kp.getPublic().getEncoded(), loaded.getPublic().getEncoded());
        }
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = {"RSA", "EC"})
    void testAppendPemAndLoadKeyPairRoundTrip_Encrypted(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        StringBuilder sb = new StringBuilder();
        char[] password = "changeit".toCharArray();
        KeyUtil.appendPem(kp, password.clone(), sb);
        String pem = sb.toString();

        KeyPair loaded = PemData.parse(pem).asKeyPair(password.clone());
        if (algorithm == AsymmetricAlgorithm.EC) {
            // Comparing getEncoded() for EC keys is unreliable for encrypted PKCS#8 round-trips
            ECPrivateKey ecOrig = (ECPrivateKey) kp.getPrivate();
            ECPrivateKey ecLoaded = (ECPrivateKey) loaded.getPrivate();
            assertEquals(ecOrig.getS(), ecLoaded.getS());
            assertEquals(ecOrig.getParams().getCurve(), ecLoaded.getParams().getCurve());
            ECPublicKey ecOrig2 = (ECPublicKey) kp.getPublic();
            ECPublicKey ecLoaded2 = (ECPublicKey) loaded.getPublic();
            assertEquals(ecOrig2.getW(), ecLoaded2.getW());
            assertEquals(ecOrig2.getParams().getCurve(), ecLoaded2.getParams().getCurve());
        } else {
            assertArrayEquals(kp.getPrivate().getEncoded(), loaded.getPrivate().getEncoded());
            assertArrayEquals(kp.getPublic().getEncoded(), loaded.getPublic().getEncoded());
        }
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = {"RSA", "EC"})
    void testDerRoundTrip_PublicKey(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        PublicKey original = kp.getPublic();

        byte[] der = KeyUtil.toDer(original);
        Key parsed = KeyUtil.parseDer(der);

        assertTrue(parsed instanceof PublicKey, "Parsed key should be a PublicKey");
        assertArrayEquals(original.getEncoded(), ((PublicKey) parsed).getEncoded());
    }

    @ParameterizedTest
    @EnumSource(value = AsymmetricAlgorithm.class, names = {"RSA", "EC"})
    void testDerRoundTrip_PrivateKey(AsymmetricAlgorithm algorithm) throws Exception {
        KeyPair kp = generatePrivatePairForAlgorithm(algorithm);
        PrivateKey original = kp.getPrivate();

        byte[] der = KeyUtil.toDer(original);
        Key parsed = KeyUtil.parseDer(der);

        assertInstanceOf(PrivateKey.class, parsed, "Parsed key should be a PrivateKey");
        assertArrayEquals(original.getEncoded(), ((PrivateKey) parsed).getEncoded());
    }
}

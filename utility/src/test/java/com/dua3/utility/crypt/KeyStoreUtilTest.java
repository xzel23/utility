// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStoreUtilTest {

    @BeforeAll
    static void setUp() {
        // Ensure BouncyCastle is loaded and registered before tests are run
        BouncyCastle.ensureAvailable();
    }

    private static final char[] PASSWORD = "test-password".toCharArray();
    private static final String SECRET_KEY_ALIAS = "test-secret-key";
    private static final String KEY_PAIR_ALIAS = "test-key-pair";
    private static final String CERTIFICATE_ALIAS = "test-certificate";

    private static char[] password() {
        return PASSWORD.clone();
    }

    @ParameterizedTest
    @EnumSource(KeyStoreType.class)
    void testCreateKeyStoreWithType(KeyStoreType type) throws GeneralSecurityException {
        // Create a KeyStore with specified type
        KeyStore keyStore = KeyStoreUtil.createKeyStore(type, password());

        // Verify the KeyStore was created
        assertNotNull(keyStore);
        assertFalse(keyStore.aliases().hasMoreElements(), "New KeyStore should be empty");
    }

    @Test
    void testCreateKeyStoreEmptyPassword() throws GeneralSecurityException {
        // Create KeyStore with empty password
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, new char[0]);

        // Verify KeyStore is created successfully
        assertNotNull(keyStore);
        assertFalse(keyStore.aliases().hasMoreElements(), "New KeyStore should be empty");
    }

    @Test
    void testStoreAndLoadSecretKey() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a secret key
        SecretKey originalKey = KeyUtil.generateSecretKey(256);

        // Store the secret key
        KeyStoreUtil.storeSecretKey(keyStore, SECRET_KEY_ALIAS, originalKey, password());

        // Verify the key was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, SECRET_KEY_ALIAS));

        // Load the secret key
        SecretKey loadedKey = KeyStoreUtil.loadSecretKey(keyStore, SECRET_KEY_ALIAS, password());

        // Verify the loaded key matches the original
        assertNotNull(loadedKey);
        assertArrayEquals(originalKey.getEncoded(), loadedKey.getEncoded());
    }

    @Test
    void testStoreAndLoadKeyPair() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a key pair
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 1, false);

        // Store the key pair
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, cert, password());

        // Verify the key was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, KEY_PAIR_ALIAS));

        // Load the private key and certificate
        var loadedPrivateKey = KeyStoreUtil.loadPrivateKey(keyStore, KEY_PAIR_ALIAS, password());
        var loadedCert = KeyStoreUtil.loadCertificate(keyStore, KEY_PAIR_ALIAS);

        // Verify the loaded keys match the originals
        assertNotNull(loadedPrivateKey);
        assertNotNull(loadedCert);
        assertArrayEquals(keyPair.getPrivate().getEncoded(), loadedPrivateKey.getEncoded());
        assertArrayEquals(cert[0].getEncoded(), loadedCert.getEncoded());
    }

    @Test
    void testStoreCertificate() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a self-signed certificate
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 10, false);

        // Store the certificate
        KeyStoreUtil.storeCertificate(keyStore, CERTIFICATE_ALIAS, cert[0]);

        // Verify certificate was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, CERTIFICATE_ALIAS));

        // Load the certificate
        var loadedCert = KeyStoreUtil.loadCertificate(keyStore, CERTIFICATE_ALIAS);

        // Verify the loaded certificate matches the original
        assertNotNull(loadedCert);
        assertArrayEquals(cert[0].getEncoded(), loadedCert.getEncoded());
    }

    @Test
    void testListAndDeleteEntries() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Add various entries
        SecretKey secretKey = KeyUtil.generateSecretKey(256);
        KeyStoreUtil.storeSecretKey(keyStore, SECRET_KEY_ALIAS, secretKey, password());

        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 7, false);
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, cert, password());

        KeyStoreUtil.storeCertificate(keyStore, CERTIFICATE_ALIAS, cert[0]);

        // List entries and verify
        var aliases = KeyStoreUtil.listAliases(keyStore);
        assertTrue(aliases.contains(SECRET_KEY_ALIAS));
        assertTrue(aliases.contains(KEY_PAIR_ALIAS));
        assertTrue(aliases.contains(CERTIFICATE_ALIAS));

        // Delete entries
        KeyStoreUtil.deleteKey(keyStore, SECRET_KEY_ALIAS);
        KeyStoreUtil.deleteKey(keyStore, KEY_PAIR_ALIAS);
        KeyStoreUtil.deleteKey(keyStore, CERTIFICATE_ALIAS);

        // Verify entries were deleted
        aliases = KeyStoreUtil.listAliases(keyStore);
        assertTrue(aliases.isEmpty());
    }

    @Test
    void testSaveAndLoadKeyStore(@TempDir Path tempDir) throws Exception {
        // Create a KeyStore
        KeyStore originalKeyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate and store a secret key
        SecretKey secretKey = KeyUtil.generateSecretKey(256);
        KeyStoreUtil.storeSecretKey(originalKeyStore, SECRET_KEY_ALIAS, secretKey, password());

        // Save the KeyStore to a file
        Path keystoreFile = tempDir.resolve("keystore.jks");
        KeyStoreUtil.saveKeyStoreToFile(originalKeyStore, keystoreFile, password());

        // Load the KeyStore from the file
        KeyStore loadedKeyStore = KeyStoreUtil.loadKeyStoreFromFile(keystoreFile, password());

        // Verify the loaded KeyStore contains the expected entries
        assertTrue(KeyStoreUtil.containsKey(loadedKeyStore, SECRET_KEY_ALIAS));

        // Load the secret key from the loaded KeyStore
        SecretKey loadedKey = KeyStoreUtil.loadSecretKey(loadedKeyStore, SECRET_KEY_ALIAS, password());

        // Verify the loaded key matches the original
        assertNotNull(loadedKey);
        assertArrayEquals(secretKey.getEncoded(), loadedKey.getEncoded());
    }

    @Test
    void testLoadPublicKey() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a key pair
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 1, false);

        // Store the key pair
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, cert, password());

        // Load the public key
        PublicKey loadedPublicKey = KeyStoreUtil.loadPublicKey(keyStore, KEY_PAIR_ALIAS);

        // Verify the loaded public key matches the original
        assertNotNull(loadedPublicKey);
        assertArrayEquals(keyPair.getPublic().getEncoded(), loadedPublicKey.getEncoded());
    }

    @Test
    void testLoadKeyPair() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a key pair
        KeyPair originalKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(originalKeyPair, "CN=Test", 1, false);

        // Store the key pair
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, originalKeyPair, cert, password());

        // Load the key pair
        KeyPair loadedKeyPair = KeyStoreUtil.loadKeyPair(keyStore, KEY_PAIR_ALIAS, password());

        // Verify the loaded key pair matches the original
        assertNotNull(loadedKeyPair);
        assertNotNull(loadedKeyPair.getPrivate());
        assertNotNull(loadedKeyPair.getPublic());
        assertArrayEquals(originalKeyPair.getPrivate().getEncoded(), loadedKeyPair.getPrivate().getEncoded());
        assertArrayEquals(originalKeyPair.getPublic().getEncoded(), loadedKeyPair.getPublic().getEncoded());
    }

    @Test
    void testLoadCertificateChain() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate a key pair and certificate chain
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] originalCertChain = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 1, false);

        // Store the key pair with certificate chain
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, originalCertChain, password());

        // Load the certificate chain
        Certificate[] loadedCertChain = KeyStoreUtil.loadCertificateChain(keyStore, KEY_PAIR_ALIAS);

        // Verify the loaded certificate chain matches the original
        assertNotNull(loadedCertChain);
        assertEquals(originalCertChain.length, loadedCertChain.length);

        // Verify each certificate in the chain
        for (int i = 0; i < originalCertChain.length; i++) {
            assertArrayEquals(originalCertChain[i].getEncoded(), loadedCertChain[i].getEncoded());
        }
    }

    @Test
    void testGenerateAndStoreSecretKey() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate and store a secret key
        String testAlias = "test-generated-secret-key";
        SymmetricAlgorithm algorithm = SymmetricAlgorithm.AES;
        int keySize = 256;

        KeyStoreUtil.generateAndStoreSecretKey(keyStore, testAlias, algorithm, keySize, password());

        // Verify the key was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, testAlias));

        // Load the secret key
        SecretKey loadedKey = KeyStoreUtil.loadSecretKey(keyStore, testAlias, password());

        // Verify the loaded key
        assertNotNull(loadedKey);
        assertEquals(algorithm.getKeyAlgorithm(), loadedKey.getAlgorithm());

        // For AES-256, the key length should be 32 bytes (256 bits)
        assertEquals(keySize / 8, loadedKey.getEncoded().length);
    }

    @Test
    void testGenerateAndStoreKeyPairWithX509Certificate() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Parameters for key pair generation
        String testAlias = "test-generated-key-pair";
        AsymmetricAlgorithm algorithm = AsymmetricAlgorithm.RSA;
        int keySize = 2048;
        String subject = "CN=Test, O=TestOrg, C=US";
        int validityDays = 365;

        // Generate and store key pair with X509 certificate
        KeyStoreUtil.generateAndStoreKeyPairWithX509Certificate(
                keyStore, testAlias, algorithm, keySize, subject, validityDays, password());

        // Verify the key pair was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, testAlias));

        // Load the key pair
        KeyPair loadedKeyPair = KeyStoreUtil.loadKeyPair(keyStore, testAlias, password());

        // Verify the loaded key pair
        assertNotNull(loadedKeyPair);
        assertNotNull(loadedKeyPair.getPrivate());
        assertNotNull(loadedKeyPair.getPublic());

        // Load the certificate chain
        Certificate[] certChain = KeyStoreUtil.loadCertificateChain(keyStore, testAlias);

        // Verify the certificate chain
        assertNotNull(certChain);
        assertTrue(certChain.length > 0);

        // Verify the public key in the certificate matches the one in the key pair
        assertEquals(loadedKeyPair.getPublic(), certChain[0].getPublicKey());
    }
}

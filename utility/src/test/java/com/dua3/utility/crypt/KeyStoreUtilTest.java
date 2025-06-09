// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStoreUtilTest {

    static {
        // Register Bouncy Castle provider for tests
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            System.err.println("Failed to register Bouncy Castle provider: " + e.getMessage());
        }
    }

    private static final char[] PASSWORD = "test-password".toCharArray();
    private static final String SECRET_KEY_ALIAS = "test-secret-key";
    private static final String KEY_PAIR_ALIAS = "test-key-pair";
    private static final String CERTIFICATE_ALIAS = "test-certificate";

    @Test
    void testCreateKeyStore() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Verify the KeyStore was created
        assertNotNull(keyStore);
        assertTrue(keyStore.aliases().hasMoreElements() == false, "New KeyStore should be empty");
    }

    @Test
    void testCreateKeyStoreWithType() throws GeneralSecurityException {
        // Create a KeyStore with specified type
        KeyStore keyStore = KeyStoreUtil.createKeyStore("PKCS12", PASSWORD);

        // Verify the KeyStore was created
        assertNotNull(keyStore);
        assertTrue(keyStore.aliases().hasMoreElements() == false, "New KeyStore should be empty");
    }

    @Test
    void testCreateKeyStoreWithInvalidType() {
        assertThrows(KeyStoreException.class, () -> {
            KeyStoreUtil.createKeyStore("INVALID", PASSWORD);
        });
    }

    @Test
    void testCreateKeyStoreEmptyPassword() throws GeneralSecurityException {
        // Create KeyStore with empty password
        KeyStore keyStore = KeyStoreUtil.createKeyStore("PKCS12", new char[0]);

        // Verify KeyStore is created successfully
        assertNotNull(keyStore);
        assertTrue(keyStore.aliases().hasMoreElements() == false, "New KeyStore should be empty");
    }

    @Test
    void testStoreAndLoadSecretKey() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Generate a secret key
        SecretKey originalKey = KeyUtil.generateSecretKey(256);

        // Store the secret key
        KeyStoreUtil.storeSecretKey(keyStore, SECRET_KEY_ALIAS, originalKey, PASSWORD);

        // Verify the key was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, SECRET_KEY_ALIAS));

        // Load the secret key
        SecretKey loadedKey = KeyStoreUtil.loadSecretKey(keyStore, SECRET_KEY_ALIAS, PASSWORD);

        // Verify the loaded key matches the original
        assertNotNull(loadedKey);
        assertArrayEquals(originalKey.getEncoded(), loadedKey.getEncoded());
    }

    @Test
    void testStoreAndLoadKeyPair() throws GeneralSecurityException {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Generate a key pair
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 1);

        // Store the key pair
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, cert, PASSWORD);

        // Verify the key was stored
        assertTrue(KeyStoreUtil.containsKey(keyStore, KEY_PAIR_ALIAS));

        // Load the private key and certificate
        var loadedPrivateKey = KeyStoreUtil.loadPrivateKey(keyStore, KEY_PAIR_ALIAS, PASSWORD);
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
        KeyStore keyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Generate a self-signed certificate
        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 10);

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
        KeyStore keyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Add various entries
        SecretKey secretKey = KeyUtil.generateSecretKey(256);
        KeyStoreUtil.storeSecretKey(keyStore, SECRET_KEY_ALIAS, secretKey, PASSWORD);

        KeyPair keyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] cert = CertificateUtil.createSelfSignedX509Certificate(keyPair, "CN=Test", 7);
        KeyStoreUtil.storeKeyPair(keyStore, KEY_PAIR_ALIAS, keyPair, cert, PASSWORD);

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
        KeyStore originalKeyStore = KeyStoreUtil.createKeyStore(PASSWORD);

        // Generate and store a secret key
        SecretKey secretKey = KeyUtil.generateSecretKey(256);
        KeyStoreUtil.storeSecretKey(originalKeyStore, SECRET_KEY_ALIAS, secretKey, PASSWORD);

        // Save the KeyStore to a file
        Path keystoreFile = tempDir.resolve("keystore.jks");
        KeyStoreUtil.saveKeyStoreToFile(originalKeyStore, keystoreFile, PASSWORD);

        // Load the KeyStore from the file
        KeyStore loadedKeyStore = KeyStoreUtil.loadKeyStoreFromFile(keystoreFile, PASSWORD);

        // Verify the loaded KeyStore contains the expected entries
        assertTrue(KeyStoreUtil.containsKey(loadedKeyStore, SECRET_KEY_ALIAS));

        // Load the secret key from the loaded KeyStore
        SecretKey loadedKey = KeyStoreUtil.loadSecretKey(loadedKeyStore, SECRET_KEY_ALIAS, PASSWORD);

        // Verify the loaded key matches the original
        assertNotNull(loadedKey);
        assertArrayEquals(secretKey.getEncoded(), loadedKey.getEncoded());
    }
}

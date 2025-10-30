// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import com.dua3.utility.data.DataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStoreUtilTest {

    private static final Logger LOG = LogManager.getLogger(KeyStoreUtilTest.class);

    @TempDir
    Path tempDir;

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
        if (!type.isExportOnly()) {
            // Create a KeyStore with specified type
            KeyStore keyStore = KeyStoreUtil.createKeyStore(type, password());

            // Verify the KeyStore was created
            assertNotNull(keyStore);
            assertFalse(keyStore.aliases().hasMoreElements(), "New KeyStore should be empty");
        }
    }

    @ParameterizedTest
    @EnumSource(KeyStoreType.class)
    void testGetKeyStoreType(KeyStoreType type) throws GeneralSecurityException {
        if (!type.isExportOnly()) {
            // Create a KeyStore with specified type
            KeyStore keyStore = KeyStoreUtil.createKeyStore(type, password());

            // assert the type can be extracted correctly
            assertEquals(type, KeyStoreUtil.getKeyStoreType(keyStore));
        }
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

    @ParameterizedTest
    @ValueSource(strings = {"JCEKS", "PKCS12", "ZIP"}) // JCS does ot support storing secret keys
    void testSaveAndLoadKeyStore(String targetTypeName) throws Exception {
        KeyStoreType targetType = KeyStoreType.valueOf(targetTypeName);

        // Create a KeyStore
        KeyStore originalKeyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Generate and store a secret key
        SecretKey secretKey = KeyUtil.generateSecretKey(256);
        KeyStoreUtil.storeSecretKey(originalKeyStore, SECRET_KEY_ALIAS, secretKey, password());

        // Save the KeyStore to a file (use PKSC#12 because JKS doe
        Path keystoreFile = tempDir.resolve("keystore." + targetType.getExtension());
        KeyStoreUtil.saveKeyStoreToFile(originalKeyStore, keystoreFile, password());

        if (!targetType.isExportOnly()) {
            // Load the KeyStore from the file
            KeyStore loadedKeyStore = KeyStoreUtil.loadKeyStore(keystoreFile, password());

            // Verify the loaded KeyStore contains the expected entries
            assertTrue(KeyStoreUtil.containsKey(loadedKeyStore, SECRET_KEY_ALIAS));

            // Load the secret key from the loaded KeyStore
            SecretKey loadedKey = KeyStoreUtil.loadSecretKey(loadedKeyStore, SECRET_KEY_ALIAS, password());

            // Verify the loaded key matches the original
            assertNotNull(loadedKey);
            assertArrayEquals(secretKey.getEncoded(), loadedKey.getEncoded());
        }
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

    @Test
    void testCertificateChainPropagationThroughKeyStore() throws Exception {
        // Create a KeyStore
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Create root certificate
        KeyPair rootKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        String rootSubject = "CN=Root,O=Test Organization,C=US";
        Certificate[] rootCertChain = CertificateUtil.createSelfSignedX509Certificate(
                rootKeyPair, rootSubject, 730, true);

        // Store root in keystore
        KeyStoreUtil.storeKeyPair(keyStore, "root", rootKeyPair, rootCertChain, password());

        // Save and reload after root
        keyStore = saveAndReloadKeyStore(keyStore);

        // Verify root chain has 1 certificate
        Certificate[] storedRootChain = KeyStoreUtil.loadCertificateChain(keyStore, "root");
        assertEquals(1, storedRootChain.length, "Root chain should contain 1 certificate");

        // Create and store certificate 1 signed by root
        createAndStoreSignedCertificate(keyStore, "root", "cert-1", 2);

        // Save and reload after cert-1
        keyStore = saveAndReloadKeyStore(keyStore);

        // Verify cert-1 still has 2 certificates after reload
        Certificate[] cert1Chain = KeyStoreUtil.loadCertificateChain(keyStore, "cert-1");
        assertEquals(2, cert1Chain.length, "Cert-1 chain should contain 2 certificates after save/reload");

        // Create and store certificate 2 signed by certificate 1
        createAndStoreSignedCertificate(keyStore, "cert-1", "cert-2", 3);

        // Save and reload after cert-2
        keyStore = saveAndReloadKeyStore(keyStore);

        // Verify cert-2 still has 3 certificates after reload
        Certificate[] cert2Chain = KeyStoreUtil.loadCertificateChain(keyStore, "cert-2");
        assertEquals(3, cert2Chain.length, "Cert-2 chain should contain 3 certificates after save/reload");

        // Create and store certificate 3 signed by certificate 2
        createAndStoreSignedCertificate(keyStore, "cert-2", "cert-3", 4);

        // Save and reload after cert-3
        keyStore = saveAndReloadKeyStore(keyStore);

        // Verify cert-3 still has 4 certificates after reload
        Certificate[] cert3Chain = KeyStoreUtil.loadCertificateChain(keyStore, "cert-3");
        assertEquals(4, cert3Chain.length, "Cert-3 chain should contain 4 certificates after save/reload");
    }

    /**
     * Helper method that saves the KeyStore to a temporary file and loads it back.
     * This tests the full round-trip through disk persistence.
     *
     * @param keyStore the KeyStore to save and reload
     * @return a new KeyStore instance loaded from disk
     */
    private KeyStore saveAndReloadKeyStore(KeyStore keyStore) throws Exception {
        // Debug: print chain lengths before save
        LOG.debug("=== BEFORE SAVE ===");
        Enumeration<String> aliasesBefore = keyStore.aliases();
        while (aliasesBefore.hasMoreElements()) {
            String alias = aliasesBefore.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                Certificate[] chain = keyStore.getCertificateChain(alias);
                LOG.debug("Alias '{}' has {} certificates in chain", alias, chain == null ? 0 : chain.length);
            }
        }

        // Save to temporary file
        Path keystoreFile = tempDir.resolve("test-keystore.p12");
        KeyStoreUtil.saveKeyStoreToFile(keyStore, keystoreFile, password());

        LOG.debug("KeyStore saved to {}", keystoreFile);

        // Load it back
        KeyStore reloadedKeyStore = KeyStoreUtil.loadKeyStore(keystoreFile, password());

        // Debug: print chain lengths after load
        LOG.debug("=== AFTER LOAD ===");
        Enumeration<String> aliasesAfter = reloadedKeyStore.aliases();
        while (aliasesAfter.hasMoreElements()) {
            String alias = aliasesAfter.nextElement();
            if (reloadedKeyStore.isKeyEntry(alias)) {
                Certificate[] chain = reloadedKeyStore.getCertificateChain(alias);
                LOG.debug("Alias '{}' has {} certificates in chain", alias, chain == null ? 0 : chain.length);
            }
        }

        LOG.debug("KeyStore reloaded from {}", keystoreFile);

        return reloadedKeyStore;
    }

    @Test
    void testGetCaAliases_EmptyKeystore() throws Exception {
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());
        var caAliases = KeyStoreUtil.getCaAliases(keyStore);
        assertNotNull(caAliases);
        assertTrue(caAliases.isEmpty(), "Empty keystore should yield no CA aliases");
    }

    @Test
    void testGetCaAliases_WithCertificateEntries() throws Exception {
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Create a CA certificate and a non-CA certificate and store as certificate entries
        KeyPair caKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] caCertChain = CertificateUtil.createSelfSignedX509Certificate(
                caKeyPair, "CN=Test CA,O=Org,C=US", 365, true);
        KeyStoreUtil.storeCertificate(keyStore, "ca-cert", caCertChain[0]);

        KeyPair endEntityKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] endEntityCertChain = CertificateUtil.createSelfSignedX509Certificate(
                endEntityKeyPair, "CN=End Entity,O=Org,C=US", 365, false);
        KeyStoreUtil.storeCertificate(keyStore, "end-entity-cert", endEntityCertChain[0]);

        var caAliases = KeyStoreUtil.getCaAliases(keyStore);
        assertTrue(caAliases.contains("ca-cert"), "CA certificate alias should be returned");
        assertFalse(caAliases.contains("end-entity-cert"), "Non-CA certificate alias should not be returned");
        assertEquals(1, caAliases.size(), "Exactly one CA alias expected");
    }

    @Test
    void testGetCaAliases_WithKeyEntries() throws Exception {
        KeyStore keyStore = KeyStoreUtil.createKeyStore(KeyStoreType.PKCS12, password());

        // Store a key entry with a CA certificate (self-signed CA)
        KeyPair caKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] caCertChain = CertificateUtil.createSelfSignedX509Certificate(
                caKeyPair, "CN=Key CA,O=Org,C=US", 365, true);
        KeyStoreUtil.storeKeyPair(keyStore, "ca-key", caKeyPair, caCertChain, password());

        // Store a key entry with a non-CA certificate (end-entity)
        KeyPair eeKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        Certificate[] eeCertChain = CertificateUtil.createSelfSignedX509Certificate(
                eeKeyPair, "CN=Key EE,O=Org,C=US", 365, false);
        KeyStoreUtil.storeKeyPair(keyStore, "ee-key", eeKeyPair, eeCertChain, password());

        var caAliases = KeyStoreUtil.getCaAliases(keyStore);
        assertTrue(caAliases.contains("ca-key"), "Key alias with CA leaf cert should be returned");
        assertFalse(caAliases.contains("ee-key"), "Key alias with non-CA leaf cert should not be returned");
        assertEquals(1, caAliases.size(), "Exactly one CA alias expected");
    }

    /**
     * Helper method that:
     * 1. Retrieves the parent key and certificate chain from the keystore
     * 2. Creates a new certificate signed by the parent
     * 3. Stores the new certificate in the keystore
     * 4. Verifies the stored certificate chain has the expected length
     *
     * @param keyStore the KeyStore to work with
     * @param parentAlias the alias of the parent certificate
     * @param newAlias the alias for the new certificate
     * @param expectedChainLength the expected length of the certificate chain after storage
     */
    private void createAndStoreSignedCertificate(
            KeyStore keyStore,
            String parentAlias,
            String newAlias,
            int expectedChainLength) throws GeneralSecurityException {

        // Retrieve parent private key and certificate chain from keystore
        PrivateKey parentPrivateKey = KeyStoreUtil.loadPrivateKey(keyStore, parentAlias, password());
        X509Certificate[] parentChain = DataUtil.convert(
                KeyStoreUtil.loadCertificateChain(keyStore, parentAlias),
                X509Certificate[].class
        );

        LOG.debug("Parent '{}' chain length: {}", parentAlias, parentChain.length);

        // Create new key pair
        KeyPair newKeyPair = KeyUtil.generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
        String newSubject = "CN=" + newAlias + ",O=Test Organization,C=US";

        // Create certificate signed by parent
        X509Certificate[] newCertChain = CertificateUtil.createX509Certificate(
                newKeyPair,
                newSubject,
                365,
                true,
                parentPrivateKey,
                parentChain);

        // Verify the chain returned from creation has the expected length
        assertEquals(expectedChainLength, newCertChain.length,
                "Certificate chain for '" + newAlias + "' should contain " + expectedChainLength + " certificates after creation");

        LOG.debug("New certificate '{}' chain length after creation: {}", newAlias, newCertChain.length);

        // Store the new certificate in the keystore
        KeyStoreUtil.storeKeyPair(keyStore, newAlias, newKeyPair, newCertChain, password());

        // Read back the certificate chain from the keystore
        Certificate[] storedChain = KeyStoreUtil.loadCertificateChain(keyStore, newAlias);

        LOG.debug("Certificate '{}' chain length after storage and retrieval: {}", newAlias, storedChain.length);

        // Verify the stored chain has the expected length
        assertEquals(expectedChainLength, storedChain.length,
                "Certificate chain for '" + newAlias + "' should contain " + expectedChainLength + " certificates after storage");

        // Verify the chain structure
        X509Certificate storedCert = (X509Certificate) storedChain[0];
        assertEquals("CN=" + newAlias + ",O=Test Organization,C=US",
                storedCert.getSubjectX500Principal().getName(),
                "First certificate should be the new certificate");

        // Verify the certificate chain is valid
        assertDoesNotThrow(() -> CertificateUtil.verifyCertificateChain((X509Certificate[]) storedChain),
                "Certificate chain for '" + newAlias + "' should be valid");
    }
}

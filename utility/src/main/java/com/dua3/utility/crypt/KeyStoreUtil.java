package com.dua3.utility.crypt;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for KeyStore operations.
 */
public final class KeyStoreUtil {

    private static final String KEYSTORE_TYPE_DEFAULT = "PKCS12";

    /**
     * Utility class private constructor.
     */
    private KeyStoreUtil() {
        // nothing to do
    }

    /**
     * Creates a new empty KeyStore of the specified type.
     *
     * @param type the KeyStore type ("PKCS12", "JKS", etc.)
     * @param password the password to protect the KeyStore
     * @return a new empty KeyStore
     * @throws GeneralSecurityException if KeyStore creation fails
     */
    public static KeyStore createKeyStore(String type, char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to create KeyStore", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Creates a new empty KeyStore using the default type (PKCS12).
     *
     * @param password the password to protect the KeyStore
     * @return a new empty KeyStore
     * @throws GeneralSecurityException if KeyStore creation fails
     */
    public static KeyStore createKeyStore(char[] password) throws GeneralSecurityException {
        return createKeyStore(KEYSTORE_TYPE_DEFAULT, password);
    }

    /**
     * Loads a KeyStore from an InputStream.
     *
     * @param inputStream the InputStream containing the KeyStore data
     * @param password the password to decrypt the KeyStore
     * @return the loaded KeyStore
     * @throws GeneralSecurityException if KeyStore loading fails
     */
    public static KeyStore loadKeyStore(InputStream inputStream, char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE_DEFAULT);
            keyStore.load(inputStream, password);
            return keyStore;
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to load KeyStore", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Loads a KeyStore from a file.
     *
     * @param keystoreFile the path to the KeyStore file
     * @param password the password to decrypt the KeyStore
     * @return the loaded KeyStore
     * @throws GeneralSecurityException if KeyStore loading fails
     * @throws IOException if file I/O fails
     */
    public static KeyStore loadKeyStoreFromFile(Path keystoreFile, char[] password) throws GeneralSecurityException, IOException {
        try (InputStream inputStream = Files.newInputStream(keystoreFile)) {
            return loadKeyStore(inputStream, password);
        }
    }

    /**
     * Saves a KeyStore to an OutputStream.
     *
     * @param keyStore the KeyStore to save
     * @param outputStream the OutputStream to write to
     * @param password the password to encrypt the KeyStore
     * @throws GeneralSecurityException if KeyStore saving fails
     */
    public static void saveKeyStore(KeyStore keyStore, OutputStream outputStream, char[] password) throws GeneralSecurityException {
        try {
            keyStore.store(outputStream, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new GeneralSecurityException("Failed to save KeyStore", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Saves a KeyStore to a file.
     *
     * @param keyStore the KeyStore to save
     * @param keystoreFile the path to save the KeyStore to
     * @param password the password to encrypt the KeyStore
     * @throws GeneralSecurityException if KeyStore saving fails
     * @throws IOException if file I/O fails
     */
    public static void saveKeyStoreToFile(KeyStore keyStore, Path keystoreFile, char[] password) throws GeneralSecurityException, IOException {
        try (OutputStream outputStream = Files.newOutputStream(keystoreFile)) {
            saveKeyStore(keyStore, outputStream, password);
        }
    }

    /**
     * Stores a SecretKey in the KeyStore.
     *
     * @param keyStore the KeyStore to store the key in
     * @param alias the alias for the key
     * @param key the SecretKey to store
     * @param password the password to protect the key
     * @throws GeneralSecurityException if storing the key fails
     */
    public static void storeSecretKey(KeyStore keyStore, String alias, SecretKey key, char[] password) throws GeneralSecurityException {
        try {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
            KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(password);
            keyStore.setEntry(alias, entry, protection);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Stores a KeyPair with a certificate chain in the KeyStore.
     *
     * @param keyStore the KeyStore to store the key pair in
     * @param alias the alias for the key pair
     * @param keyPair the KeyPair to store
     * @param certificateChain the certificate chain for the public key
     * @param password the password to protect the private key
     * @throws GeneralSecurityException if storing the key pair fails
     */
    public static void storeKeyPair(KeyStore keyStore, String alias, KeyPair keyPair, Certificate[] certificateChain, char[] password) throws GeneralSecurityException {
        try {
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, certificateChain);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Loads a SecretKey from the KeyStore.
     *
     * @param keyStore the KeyStore to load the key from
     * @param alias the alias of the key
     * @param password the password to decrypt the key
     * @return the loaded SecretKey
     * @throws GeneralSecurityException if loading the key fails or key is not found
     */
    public static SecretKey loadSecretKey(KeyStore keyStore, String alias, char[] password) throws GeneralSecurityException {
        try {
            KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(password);
            KeyStore.Entry entry = keyStore.getEntry(alias, protection);

            if (entry == null) {
                throw new GeneralSecurityException("Key not found with alias: " + alias);
            }

            if (!(entry instanceof KeyStore.SecretKeyEntry secretKeyEntry)) {
                throw new GeneralSecurityException("Entry is not a SecretKey: " + alias);
            }

            return secretKeyEntry.getSecretKey();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Loads a PrivateKey from the KeyStore.
     *
     * @param keyStore the KeyStore to load the key from
     * @param alias the alias of the key
     * @param password the password to decrypt the key
     * @return the loaded PrivateKey
     * @throws GeneralSecurityException if loading the key fails or key is not found
     */
    public static PrivateKey loadPrivateKey(KeyStore keyStore, String alias, char[] password) throws GeneralSecurityException {
        try {
            Key key = keyStore.getKey(alias, password);

            if (key == null) {
                throw new GeneralSecurityException("Private key not found with alias: " + alias);
            }

            if (!(key instanceof PrivateKey privateKey)) {
                throw new GeneralSecurityException("Entry is not a PrivateKey: " + alias);
            }

            return privateKey;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Loads a PublicKey from the KeyStore (from the certificate).
     *
     * @param keyStore the KeyStore to load the key from
     * @param alias the alias of the key
     * @return the loaded PublicKey
     * @throws GeneralSecurityException if loading the key fails or key is not found
     */
    public static PublicKey loadPublicKey(KeyStore keyStore, String alias) throws GeneralSecurityException {
        Certificate certificate = keyStore.getCertificate(alias);

        if (certificate == null) {
            throw new GeneralSecurityException("Certificate not found with alias: " + alias);
        }

        return certificate.getPublicKey();
    }

    /**
     * Loads a KeyPair from the KeyStore.
     *
     * @param keyStore the KeyStore to load the key pair from
     * @param alias the alias of the key pair
     * @param password the password to decrypt the private key
     * @return the loaded KeyPair
     * @throws GeneralSecurityException if loading the key pair fails or keys are not found
     */
    public static KeyPair loadKeyPair(KeyStore keyStore, String alias, char[] password) throws GeneralSecurityException {
        PrivateKey privateKey = loadPrivateKey(keyStore, alias, password);
        PublicKey publicKey = loadPublicKey(keyStore, alias);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Stores a certificate in the KeyStore.
     *
     * @param keyStore the KeyStore to store the certificate in
     * @param alias the alias for the certificate
     * @param certificate the certificate to store
     * @throws GeneralSecurityException if storing the certificate fails
     */
    public static void storeCertificate(KeyStore keyStore, String alias, Certificate certificate) throws GeneralSecurityException {
        keyStore.setCertificateEntry(alias, certificate);
    }

    /**
     * Loads a certificate from the KeyStore.
     *
     * @param keyStore the KeyStore to load the certificate from
     * @param alias the alias of the certificate
     * @return the loaded certificate
     * @throws GeneralSecurityException if loading the certificate fails or certificate is not found
     */
    public static Certificate loadCertificate(KeyStore keyStore, String alias) throws GeneralSecurityException {
        Certificate certificate = keyStore.getCertificate(alias);

        if (certificate == null) {
            throw new GeneralSecurityException("Certificate not found with alias: " + alias);
        }

        return certificate;
    }

    /**
     * Loads a certificate chain from the KeyStore.
     *
     * @param keyStore the KeyStore to load the certificate chain from
     * @param alias the alias of the certificate chain
     * @return the loaded certificate chain
     * @throws GeneralSecurityException if loading the certificate chain fails or chain is not found
     */
    public static Certificate[] loadCertificateChain(KeyStore keyStore, String alias) throws GeneralSecurityException {
        Certificate[] chain = keyStore.getCertificateChain(alias);

        if (chain == null) {
            throw new GeneralSecurityException("Certificate chain not found with alias: " + alias);
        }

        return chain;
    }

    /**
     * Checks if the KeyStore contains an entry with the specified alias.
     *
     * @param keyStore the KeyStore to check
     * @param alias the alias to check for
     * @return true if the KeyStore contains the alias, false otherwise
     * @throws GeneralSecurityException if checking the KeyStore fails
     */
    public static boolean containsKey(KeyStore keyStore, String alias) throws GeneralSecurityException {
        return keyStore.containsAlias(alias);
    }

    /**
     * Deletes an entry from the KeyStore.
     *
     * @param keyStore the KeyStore to delete the entry from
     * @param alias the alias of the entry to delete
     * @throws GeneralSecurityException if deleting the entry fails
     */
    public static void deleteKey(KeyStore keyStore, String alias) throws GeneralSecurityException {
        if (!keyStore.containsAlias(alias)) {
            throw new GeneralSecurityException("Alias not found: " + alias);
        }
        keyStore.deleteEntry(alias);
    }

    /**
     * Lists all aliases in the KeyStore.
     *
     * @param keyStore the KeyStore to list aliases from
     * @return a set of all aliases in the KeyStore
     * @throws GeneralSecurityException if listing aliases fails
     */
    public static Set<String> listAliases(KeyStore keyStore) throws GeneralSecurityException {
        Set<String> aliases = new HashSet<>();
        Enumeration<String> aliasEnum = keyStore.aliases();
        while (aliasEnum.hasMoreElements()) {
            aliases.add(aliasEnum.nextElement());
        }
        return aliases;
    }

    /**
     * Generates a secret key and stores it in the KeyStore.
     *
     * @param keyStore the KeyStore to store the secret key in
     * @param alias the alias for the secret key
     * @param algorithm the symmetric algorithm
     * @param keySize the key size in bits
     * @param password the password to protect the key
     * @throws GeneralSecurityException if key generation or storage fails
     */
    public static void generateAndStoreSecretKey(KeyStore keyStore, String alias, SymmetricAlgorithm algorithm, int keySize, char[] password) throws GeneralSecurityException {
        SecretKey secretKey = KeyUtil.generateSecretKey(keySize, algorithm);
        storeSecretKey(keyStore, alias, secretKey, password);
    }

    /**
     * Generates a key pair and stores it in the KeyStore with a proper self-signed X.509 certificate.
     * This method creates a standards-compliant X.509 certificate using available security providers.
     *
     * @param keyStore the KeyStore to store the key pair in
     * @param alias the alias for the key pair
     * @param algorithm the asymmetric algorithm
     * @param keySize the key size in bits
     * @param password the password to protect the private key
     * @param subject the certificate subject (e.g., "CN=MyApp, O=MyOrg, C=US")
     * @param validityDays number of days the certificate should be valid
     * @throws GeneralSecurityException if key generation or storage fails
     * @throws UnsupportedOperationException if no suitable provider for X.509 certificate generation is available
     */
    public static void generateAndStoreKeyPairWithX509Certificate(KeyStore keyStore, String alias, AsymmetricAlgorithm algorithm, int keySize, char[] password, String subject, int validityDays) throws GeneralSecurityException {
        KeyPair keyPair = KeyUtil.generateKeyPair(algorithm, keySize);
        Certificate[] certificateChain = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays);
        storeKeyPair(keyStore, alias, keyPair, certificateChain, password);
    }
}
package com.dua3.utility.crypt;

import com.dua3.utility.io.IoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for KeyStore operations.
 */
public final class KeyStoreUtil {
    private static final Logger LOG = LogManager.getLogger(KeyStoreUtil.class);

    static {
        // make sure BouncyCastle is loaded
        try {
            BouncyCastle.ensureAvailable();
            LOG.debug("BouncyCastle successfully loaded");
        } catch (RuntimeException e) {
            LOG.error("Failed to load BouncyCastle", e);
        }
    }

    /**
     * Utility class private constructor.
     */
    private KeyStoreUtil() { /* utility class */ }

    /**
     * Creates and initializes a new KeyStore of the specified type, secured with the provided password.
     * The InputBufferHandling parameter determines how the password buffer is handled after use.
     *
     * @param type the type of KeyStore to be created, specified as a KeyStoreType
     * @param password the password used to protect the KeyStore, provided as a character array
     * @return a newly created KeyStore instance of the specified type
     * @throws GeneralSecurityException if there is an issue initializing the KeyStore
     */
    public static KeyStore createKeyStore(KeyStoreType type, char[] password) throws GeneralSecurityException {
        return createKeyStore(type.name(), password);
    }

    /**
     * Creates and initializes a new KeyStore instance with the specified type and password.
     *
     * @param type the type of KeyStore to be created, such as "JKS" or "PKCS12"
     * @param password the password used to secure the KeyStore; it will be cleared if inputBufferHandling is not PRESERVE
     * @return a newly created and initialized KeyStore instance
     * @throws GeneralSecurityException if there is an error creating or initializing the KeyStore
     */
    private static KeyStore createKeyStore(String type, char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(null, password);
            LOG.debug("Created new KeyStore of type {}", type);
            return keyStore;
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to create KeyStore", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Loads a {@link KeyStore} from an InputStream.
     *
     * @param type the type of the {@link KeyStore}
     * @param inputStream the InputStream containing the KeyStore data
     * @param password the password to decrypt the KeyStore
     * @return the loaded KeyStore
     * @throws GeneralSecurityException if KeyStore loading fails
     */
    public static KeyStore loadKeyStore(KeyStoreType type, InputStream inputStream, char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(type.name());
            keyStore.load(inputStream, password);
            LOG.debug("Loaded KeyStore of type {}", type);
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
    public static KeyStore loadKeyStore(Path keystoreFile, char[] password) throws GeneralSecurityException, IOException {
        try {
            KeyStoreType type = KeyStoreType.forExtension(IoUtil.getExtension(keystoreFile));
            try (InputStream inputStream = Files.newInputStream(keystoreFile)) {
                return loadKeyStore(type, inputStream, password);
            }
        } finally {
            Arrays.fill(password, '\0');
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
            LOG.debug("KeyStore data written");
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
        try {
            try (OutputStream outputStream = Files.newOutputStream(keystoreFile)) {
                saveKeyStore(keyStore, outputStream, password);
                LOG.debug("KeyStore written to file {}", keystoreFile);
            }
        } finally {
            Arrays.fill(password, '\0');
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
            KeyStore.Entry entry = new KeyStore.SecretKeyEntry(key);
            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password);
            keyStore.setEntry(alias, entry, protection);
            LOG.debug("Stored secret key with alias {}", alias);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Stores a KeyPair with a certificate chain in the KeyStore.
     *
     * @param keyStore            the KeyStore to store the key pair in
     * @param alias               the alias for the key pair
     * @param keyPair             the KeyPair to store
     * @param certificateChain    the certificate chain for the public key
     * @param password            the password to protect the private key
     * @throws GeneralSecurityException if storing the key pair fails
     */
    public static void storeKeyPair(KeyStore keyStore, String alias, KeyPair keyPair, Certificate[] certificateChain, char[] password) throws GeneralSecurityException {
        try {
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, certificateChain);
            LOG.debug("Stored key pair with alias {}", alias);
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
            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password);
            KeyStore.Entry entry = keyStore.getEntry(alias, protection);

            if (entry == null) {
                throw new GeneralSecurityException("Key not found with alias: " + alias);
            }

            if (!(entry instanceof KeyStore.SecretKeyEntry secretKeyEntry)) {
                throw new GeneralSecurityException("Entry is not a SecretKey: " + alias);
            }

            LOG.debug("Loaded secret key with alias {}", alias);

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

            LOG.debug("Loaded private key with alias {}", alias);

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
        return loadCertificate(keyStore, alias).getPublicKey();
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
        PrivateKey privateKey = loadPrivateKey(keyStore, alias, password); // also clears password
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
        LOG.debug("Stored certificate with alias {}", alias);
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

        LOG.debug("Loaded certificate with alias {}", alias);

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

        LOG.debug("Loaded certificate chain containing {} entries with alias {}", chain.length, alias);

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
        LOG.debug("Deleted key with alias {}", alias);
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
        try {
            SecretKey secretKey = KeyUtil.generateSecretKey(keySize, algorithm);
            storeSecretKey(keyStore, alias, secretKey, password);
            LOG.debug("Stored new secret key with alias {}", alias);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Generates a key pair and stores it in the KeyStore with a proper self-signed X.509 certificate.
     * This method creates a standards-compliant X.509 certificate using available security providers.
     *
     * @param keyStore            the KeyStore to store the key pair in
     * @param alias               the alias for the key pair
     * @param algorithm           the asymmetric algorithm
     * @param keySize             the key size in bits
     * @param subject             the certificate subject (e.g., "CN=MyApp, O=MyOrg, C=US")
     * @param validityDays        number of days the certificate should be valid
     * @param password            the password to protect the private key
     * @throws GeneralSecurityException      if key generation or storage fails
     * @throws UnsupportedOperationException if no suitable provider for X.509 certificate generation is available
     */
    public static void generateAndStoreKeyPairWithX509Certificate(KeyStore keyStore, String alias, AsymmetricAlgorithm algorithm, int keySize, String subject, int validityDays, char[] password) throws GeneralSecurityException {
        KeyPair keyPair = KeyUtil.generateKeyPair(algorithm, keySize);
        Certificate[] certificateChain = CertificateUtil.createSelfSignedX509Certificate(keyPair, subject, validityDays, false);
        storeKeyPair(keyStore, alias, keyPair, certificateChain, password);
        LOG.debug("Stored new key pair with alias {}", alias);
    }

    /**
     * Retrieves a list of aliases from the provided KeyStore that represent
     * Certificate Authority (CA) certificates. Certificates are identified as CA certificates
     * based on the key usage extension, specifically checking if the keyCertSign bit is set.
     *
     * @param ks the KeyStore from which to extract the aliases. It must be properly initialized with certificates and keys.
     * @return a list of aliases that are associated with CA certificates within the provided KeyStore.
     *         If no such aliases are found, an empty list is returned.
     * @throws KeyStoreException when an error occurs accessing the kestore data
     */
    public static List<String> getCaAliases(KeyStore ks)throws KeyStoreException {
        List<String> aliases = new ArrayList<>();
        ks.aliases().asIterator().forEachRemaining(alias -> {
            LOG.debug("Processing alias: {}", alias);
            try {
                // Process both certificate entries and key entries
                boolean isCertEntry = ks.isCertificateEntry(alias);
                boolean isKeyEntry = ks.isKeyEntry(alias);

                if (isCertEntry) {
                    // Get certificate information
                    LOG.trace("Alias belongs to certificate entry: {}", alias);
                    java.security.cert.Certificate cert = ks.getCertificate(alias);

                    addCertIfX509(alias, cert, aliases);
                } else if (isKeyEntry) {
                    // Get certificate chain for key entry
                    LOG.debug("[DEBUG_LOG] Processing key entry: {}", alias);

                    Certificate[] certChain = ks.getCertificateChain(alias);

                    if (certChain != null && certChain.length > 0) {
                        addCertIfX509(alias, certChain[0], aliases);
                    }
                } else {
                    LOG.debug("Alias {} is neither a certificate entry nor a key entry", alias);
                }
            } catch (KeyStoreException | RuntimeException  e) {
                // Skip this alias if there's an error
                LOG.warn("Error processing alias: {}", alias, e);
            }
        });

        return aliases;
    }

    private static void addCertIfX509(String alias, Certificate cert, List<String> aliases) {
        if (cert instanceof X509Certificate x509Cert) {
            // Check if this is a CA certificate - Key usage bit 5 is for keyCertSign
            boolean[] keyUsage = x509Cert.getKeyUsage();
            boolean isCA = keyUsage != null && keyUsage.length > 5 && keyUsage[5];

            if (isCA) {
                LOG.debug(" Adding alias for CA certificate {}", alias);
                aliases.add(alias);
            } else {
                LOG.trace("Not a CA certificate {}", alias);
            }
        } else {
            LOG.debug("Certificate {} is not an X509Certificate", alias);
        }
    }
}
package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.jspecify.annotations.Nullable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for cryptographic key operations.
 */
public final class KeyUtil {
    private static final Logger LOG = LogManager.getLogger(KeyUtil.class);

    private static final String PUBLIC_KEY = "PUBLIC KEY";
    private static final String PRIVATE_KEY = "PRIVATE KEY";
    private static final String SECRET_KEY = "SECRET KEY";

    /**
     * Utility class private constructor.
     */
    private KeyUtil() { /* utility class */ }

    /**
     * Generate random salt for key derivation.
     *
     * @param length salt length in bytes (recommended: 16 or 32)
     * @return random salt
     */
    public static byte[] generateSalt(int length) {
        return RandomUtil.generateRandomBytes(length);
    }

    /**
     * Validates a symmetric key for use with the specified algorithm.
     *
     * @param key       the key to validate
     * @param algorithm the algorithm the key will be used with
     * @throws GeneralSecurityException if the key is not compatible or key validation fails
     */
    public static void validateSymmetricKey(Key key, SymmetricAlgorithm algorithm) throws GeneralSecurityException {
        if (!algorithm.getKeyAlgorithm().equals(key.getAlgorithm())) {
            throw new InvalidKeyException(
                    String.format("Key algorithm mismatch. Expected: %s, got: %s",
                            algorithm.getKeyAlgorithm(), key.getAlgorithm()));
        }

        if (key.getEncoded() != null) {
            algorithm.validateKeySize(key.getEncoded().length * 8);
        }
    }

    /**
     * Create a SecretKey from a byte array using the default algorithm.
     *
     * @param keyBytes the key bytes (must be 128, 192, or 256 bits for AES)
     * @return SecretKey instance
     * @throws InvalidAlgorithmParameterException if the bytes could not be converted to a secret key
     */
    public static SecretKey toSecretKey(byte[] keyBytes) throws InvalidAlgorithmParameterException {
        return toSecretKey(keyBytes, SymmetricAlgorithm.AES);
    }

    /**
     * Converts the provided byte array into a {@link SecretKey} using the specified symmetric algorithm.
     *
     * @param keyBytes  the byte array representing the key material
     * @param algorithm the symmetric algorithm used to validate the key size and determine the key algorithm
     * @return a {@link SecretKey} generated from the provided key material and algorithm
     * @throws InvalidAlgorithmParameterException if the key size is invalid for the specified algorithm
     */
    public static SecretKey toSecretKey(byte[] keyBytes, SymmetricAlgorithm algorithm) throws InvalidAlgorithmParameterException {
        algorithm.validateKeySize(keyBytes.length * 8);
        return new SecretKeySpec(keyBytes, algorithm.getKeyAlgorithm());
    }

    /**
     * Generate SecretKey using the default algorithm.
     *
     * @param bits the number of bits (128, 192, or 256 for AES)
     * @return the generated SecretKey
     * @throws GeneralSecurityException if key generation fails
     */
    public static SecretKey generateSecretKey(int bits) throws GeneralSecurityException {
        return generateSecretKey(bits, SymmetricAlgorithm.AES);
    }

    /**
     * Generate SecretKey for the specified algorithm.
     *
     * @param bits      the number of bits
     * @param algorithm the symmetric algorithm
     * @return the generated SecretKey
     * @throws GeneralSecurityException if key generation fails
     */
    public static SecretKey generateSecretKey(int bits, SymmetricAlgorithm algorithm) throws GeneralSecurityException {
        algorithm.validateKeySize(bits);

        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm.getKeyAlgorithm());
        keyGen.init(bits, RandomUtil.getRandom());
        return keyGen.generateKey();
    }

    /**
     * Converts a byte array representing a private key into a {@code PrivateKey} object
     * using the default key generation algorithm.
     *
     * @param bytes the byte array containing the encoded private key
     * @return the generated {@code PrivateKey} object
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PrivateKey toPrivateKey(byte[] bytes) throws GeneralSecurityException {
        return toPrivateKey(bytes, AsymmetricAlgorithm.RSA);
    }

    /**
     * Converts a given byte array into a {@code PrivateKey} instance using the specified algorithm.
     *
     * @param bytes     the private key in encoded byte format
     * @param algorithm the asymmetric algorithm
     * @return a {@code PrivateKey} created from the provided byte array and algorithm
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PrivateKey toPrivateKey(byte[] bytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm.keyFactoryAlgorithm());
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Converts a byte array into a {@code PublicKey} using the default key generation algorithm.
     *
     * @param bytes the byte array representing the public key
     * @return the corresponding {@code PublicKey} instance
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PublicKey toPublicKey(byte[] bytes) throws GeneralSecurityException {
        return toPublicKey(bytes, AsymmetricAlgorithm.RSA);
    }

    /**
     * Converts the provided byte array into a {@code PublicKey} instance using the specified algorithm.
     *
     * @param bytes     the byte array containing the key data
     * @param algorithm the asymmetric algorithm
     * @return the generated {@code PublicKey}
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PublicKey toPublicKey(byte[] bytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm.keyFactoryAlgorithm());
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Creates a {@code KeyPair} using the given public and private key byte arrays.
     *
     * @param publicKeyBytes  the byte array representation of the public key
     * @param privateKeyBytes the byte array representation of the private key
     * @param algorithm       the asymmetric algorithm
     * @return a {@code KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        PublicKey publicKey = toPublicKey(publicKeyBytes, algorithm);
        PrivateKey privateKey = toPrivateKey(privateKeyBytes, algorithm);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Generate an Elliptic Curve key pair using a named curve.
     * <p>
     * Supported standard curves:
     * <ul>
     *   <li>"secp256r1" (P-256) - 256-bit curve</li>
     *   <li>"secp384r1" (P-384) - 384-bit curve</li>
     *   <li>"secp521r1" (P-521) - 521-bit curve</li>
     * </ul>
     *
     * @param curveName the name of the elliptic curve ("secp256r1", "secp384r1", "secp521r1")
     * @return the generated EC key pair
     * @throws GeneralSecurityException if key generation fails or the curve is not supported
     */
    public static KeyPair generateECKeyPair(String curveName) throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
        keyGen.initialize(ecSpec, RandomUtil.getRandom());
        return keyGen.generateKeyPair();
    }

    /**
     * Generate an RSA key pair with default key size (2048 bits).
     *
     * @return the generated RSA key pair
     * @throws InvalidAlgorithmParameterException if the key generation fails
     */
    public static KeyPair generateRSAKeyPair() throws InvalidAlgorithmParameterException {
        return generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
    }

    /**
     * Generate an asymmetric key pair.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize   the key size in bits
     * @return the generated key pair
     * @throws InvalidAlgorithmParameterException if key size validation fails or key generation fails
     */
    public static KeyPair generateKeyPair(AsymmetricAlgorithm algorithm, int keySize) throws InvalidAlgorithmParameterException {
        validateAsymmetricKeySize(algorithm, keySize);

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm.keyFactoryAlgorithm());
            keyGen.initialize(keySize, RandomUtil.getRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Validates the size of an asymmetric key based on the specified algorithm.
     * Ensures that the key size meets the minimum or required standards for security purposes.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize   the size of the key in bits, which will be checked against the requirements
     *                  specified for the given algorithm.
     *                  For RSA and DSA, the minimum is 2048 bits.
     *                  For EC, only specific sizes such as 256 (P-256), 384 (P-384), or 521 (P-521) bits are allowed.
     *                  If the key size does not satisfy the constraints for the selected algorithm,
     *                  an exception will be thrown.
     * @throws InvalidAlgorithmParameterException if the key size does not satisfy the constraints
     *                                            for the selected algorithm
     */
    public static void validateAsymmetricKeySize(AsymmetricAlgorithm algorithm, int keySize) throws InvalidAlgorithmParameterException {
        switch (algorithm) {
            case RSA, DSA -> {
                if (keySize < 2048) {
                    throw new InvalidAlgorithmParameterException(algorithm + " key size must be at least 2048 bits, but was: " + keySize);
                }
            }
            case EC -> {
                if (keySize != 256 && keySize != 384 && keySize != 521) {
                    throw new InvalidAlgorithmParameterException("EC key size must be 256, 384, or 521 bits, but was: " + keySize);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + algorithm);
        }
    }

    /**
     * Derives a secret key for the specified symmetric algorithm using a randomly generated salt.
     *
     * @param algorithm           the symmetric algorithm for which the key is to be derived
     * @param input               the input data used for key derivation
     * @param info                optional context-specific information used in the key derivation process
     * @param inputBufferHandling the handling mechanism for input buffers during key derivation
     * @return the derived secret key
     */
    public static SecretKey deriveSecretKeyWithRandomSalt(SymmetricAlgorithm algorithm, byte[] input, byte @Nullable [] info, InputBufferHandling inputBufferHandling) {
        byte[] salt = RandomUtil.generateRandomBytes(32); // 256-bit salt
        return deriveSecretKey(algorithm, salt, input, info, inputBufferHandling);
    }

    /**
     * Derives a cryptographic key using the specified symmetric algorithm, salt, input data, and additional info.
     * The method utilizes the HKDF (HMAC-based Extract-and-Expand Key Derivation Function) with SHA-256 to
     * securely derive a key.
     *
     * @param algorithm           the symmetric algorithm for which the key is being derived; it determines the key size
     *                            and the key algorithm (e.g., AES)
     * @param salt                a non-secret random value used to ensure uniqueness of derived keys; must be at least 16 bytes
     * @param input               the input keying material (IKM) used as a source of entropy for key derivation
     * @param info                optional context and application-specific information used for domain separation during
     *                            key derivation
     * @param inputBufferHandling the handling mechanism for input buffers during key derivation
     * @return a {@link SecretKey} instance containing the derived key that is compatible with the specified algorithm
     * @throws IllegalArgumentException if the provided salt is shorter than 16 bytes
     */
    public static SecretKey deriveSecretKey(SymmetricAlgorithm algorithm, byte[] salt, byte[] input, byte @Nullable [] info, InputBufferHandling inputBufferHandling) {
        try {
            // Validate salt size
            if (salt.length < 16) {
                throw new IllegalArgumentException("Salt must be at least 16 bytes for security");
            }

            // Create HKDF with SHA-256
            HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

            // Initialize HKDF with info parameter for domain separation
            hkdf.init(new HKDFParameters(input, salt, info));

            // Generate key bytes
            byte[] keyBytes = new byte[algorithm.getDefaultKeySize() / 8];
            hkdf.generateBytes(keyBytes, 0, keyBytes.length);

            // Create SecretKey from derived bytes
            return new SecretKeySpec(keyBytes, algorithm.getKeyAlgorithm());
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(input, (byte) 0);
                Arrays.fill(salt, (byte) 0);
                if (info != null) {
                    Arrays.fill(info, (byte) 0);
                }
            }
        }
    }

    /**
     * Loads a public key from a PEM formatted string.
     *
     * @param pem the PEM formatted string containing the public key.
     * @return the {@code PublicKey} object representing the loaded public key.
     * @throws InvalidKeySpecException if the specified key specification is invalid.
     * @throws NoSuchAlgorithmException if the algorithm is not available in the environment
     */
    public static PublicKey loadPublicKeyFromPem(String pem) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String algorithm = extractKeyAlgorithmFromPemHeader(pem, PUBLIC_KEY);
        String clean = PATTERN_CLEAN_PEM.matcher(pem).replaceAll("");
        byte[] bytes = decodeKeyDataBase64(clean);

        // For standard "PUBLIC KEY" format, let Java determine the algorithm from the key data
        if ("PUBLIC".equals(algorithm)) {
            for (AsymmetricAlgorithm alg : AsymmetricAlgorithm.values()) {
                try {
                    KeyFactory kf = KeyFactory.getInstance(alg.keyFactoryAlgorithm());
                    return kf.generatePublic(new X509EncodedKeySpec(bytes));
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    LOG.debug("Unable to load public key from PEM data using algorithm '{}', trying next: {}", alg, e.getMessage());
                }
            }
            // If all fail, throw with the last exception
            throw new InvalidKeySpecException("Unable to determine key algorithm from PEM data");
        }

        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePublic(new X509EncodedKeySpec(bytes));
    }

    private static byte[] decodeKeyDataBase64(String clean) throws InvalidKeySpecException {
        try {
            return Base64.getDecoder().decode(clean);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    private static final Pattern PATTERN_CLEAN_PEM = Pattern.compile("-----[^-]*-----|\\s");

    /**
     * Loads an unencrypted private key from a PEM-formatted PKCS#8 string without encryption.
     *
     * @param pem the PEM-formatted private key string, including the header and footer
     * @return the PrivateKey object reconstructed from the provided PEM string
     * @throws InvalidKeySpecException  if the provided PEM content cannot be converted to a valid private key
     * @throws NoSuchAlgorithmException if the algorithm is not available in the environment
     */
    public static PrivateKey loadPrivateKeyFromPem(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return loadPrivateKeyFromPem(pem, "".toCharArray());
    }

    /**
     * Loads a private key from a PEM-formatted PKCS#8 string.
     * Supports multiple algorithms (RSA, EC, DSA) and optional encryption.
     *
     * @param pem      the PEM-formatted private key string, including the header and footer
     * @param password the password for decryption, or empty array for unencrypted keys
     * @return the PrivateKey object reconstructed from the provided PEM string
     * @throws InvalidKeySpecException  if the provided PEM content cannot be converted to a valid private key
     * @throws NoSuchAlgorithmException if the algorithm is not available in the environment
     */
    public static PrivateKey loadPrivateKeyFromPem(String pem, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = extractKeyAlgorithmFromPemHeader(pem, PRIVATE_KEY);
        String clean = PATTERN_CLEAN_PEM.matcher(pem).replaceAll("");
        byte[] bytes = decodeKeyDataBase64(clean);

        // Handle encrypted keys
        if (password.length > 0 && pem.contains("ENCRYPTED")) {
            try {
                // Extract salt (first 16 bytes) and encrypted data
                byte[] salt = Arrays.copyOfRange(bytes, 0, 16);
                byte[] encryptedData = Arrays.copyOfRange(bytes, 16, bytes.length);

                // Derive decryption key from password
                SecretKey derivedKey = deriveSecretKey(
                        SymmetricAlgorithm.AES,
                        salt,
                        new String(password).getBytes(StandardCharsets.UTF_8),
                        null,
                        InputBufferHandling.PRESERVE
                );

                // Decrypt the key data
                bytes = CryptUtil.decryptSymmetric(
                        SymmetricAlgorithm.AES,
                        derivedKey,
                        encryptedData
                );
            } catch (GeneralSecurityException e) {
                throw new InvalidKeySpecException("Failed to decrypt private key", e);
            } finally {
                // Clean up sensitive data
                Arrays.fill(password, '\0');
            }
        }

        // For standard "PRIVATE KEY" format, let Java determine the algorithm from the key data
        if ("PRIVATE".equals(algorithm)) {
            // Try RSA first (most common), then EC, then DSA
            for (AsymmetricAlgorithm alg : AsymmetricAlgorithm.values()) {
                try {
                    KeyFactory kf = KeyFactory.getInstance(alg.keyFactoryAlgorithm());
                    return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
                } catch (InvalidKeySpecException e) {
                    LOG.debug("Unable to load private key from PEM data using {} algorithm '{}', trying next: {}",
                            alg.getClass().getSimpleName(), alg.keyFactoryAlgorithm(), e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    LOG.debug("Unable to load private key from PEM data - {} algorithm '{}' not available, trying next: {}",
                            alg.getClass().getSimpleName(), alg.keyFactoryAlgorithm(), e.getMessage());
                }
            }
            // If all fail, throw with the last exception
            throw new InvalidKeySpecException("Unable to determine key algorithm from PEM data");
        }

        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    /**
     * Loads an unencrypted {@link SecretKey} from a PEM-formatted string.
     *
     * @param pem the PEM-formatted string containing the encoded key
     * @return the {@link SecretKey} object derived from the PEM data
     * @throws InvalidKeySpecException if the key specification is invalid
     */
    public static SecretKey loadSecretKeyFromPem(String pem) throws InvalidKeySpecException {
        return loadSecretKeyFromPem(pem, "".toCharArray());
    }

    /**
     * Loads a secret key from a PEM-formatted string.
     * This method supports reading and decrypting PEM-formatted secret keys,
     * optionally handling encrypted keys protected with a password.
     *
     * @param pem The PEM-formatted secret key string. It may include an encryption header if the key is encrypted.
     * @param password The password used to decrypt the key if it is encrypted. Pass an empty array if no password is used.
     *                 The password array will be cleared internally after usage for security purposes.
     * @return A {@link SecretKey} constructed from the provided PEM data.
     * @throws InvalidKeySpecException If the key data is invalid, decryption fails, or key generation is unsuccessful.
     */
    public static SecretKey loadSecretKeyFromPem(String pem, char[] password) throws InvalidKeySpecException {
        String algorithm = extractKeyAlgorithmFromPemHeader(pem, SECRET_KEY);
        String clean = PATTERN_CLEAN_PEM.matcher(pem).replaceAll("");
        byte[] bytes = decodeKeyDataBase64(clean);

        // Handle encrypted keys
        if (password.length > 0 && pem.contains("ENCRYPTED")) {
            try {
                // Extract salt (first 16 bytes) and encrypted data
                byte[] salt = Arrays.copyOfRange(bytes, 0, 16);
                byte[] encryptedData = Arrays.copyOfRange(bytes, 16, bytes.length);

                // Derive decryption key from password
                SecretKey derivedKey = deriveSecretKey(
                        SymmetricAlgorithm.AES,
                        salt,
                        new String(password).getBytes(StandardCharsets.UTF_8),
                        null,
                        InputBufferHandling.PRESERVE
                );

                // Decrypt the key data
                bytes = CryptUtil.decryptSymmetric(
                        SymmetricAlgorithm.AES,
                        derivedKey,
                        encryptedData
                );
            } catch (GeneralSecurityException e) {
                throw new InvalidKeySpecException("Failed to decrypt secret key", e);
            } finally {
                // Clean up sensitive data
                Arrays.fill(password, '\0');
            }
        }

        // For standard "SECRET KEY" format, use AES as default algorithm
        if ("SECRET".equals(algorithm)) {
            algorithm = SymmetricAlgorithm.AES.getKeyAlgorithm();
        }

        // Create SecretKey from the decoded bytes
        try {
            return new SecretKeySpec(bytes, algorithm);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException("Invalid key data", e);
        }
    }

    /**
     * Extracts the algorithm from a PEM header using regex pattern matching.
     * <p>
     * This method returns values like "RSA", "EC", "DSA" (key algorithms)
     * or "PUBLIC", "PRIVATE", "SECRET" (when no algorithm is specified).
     *
     * @param pem the PEM formatted string
     * @param keyType the type of key ("PUBLIC KEY" or "PRIVATE KEY")
     * @return the algorithm name for KeyFactory, or the keyType if no specific algorithm found
     * @throws InvalidKeySpecException if the PEM format is invalid or missing required headers
     */
    private static String extractKeyAlgorithmFromPemHeader(String pem, String keyType) throws InvalidKeySpecException {
        // Add size limit for security
        if (pem.length() > 100_000) { // 100KB limit
            throw new InvalidKeySpecException("PEM data exceeds maximum allowed size");
        }

        String pattern = "-----BEGIN\\s+(ENCRYPTED\\s+)?(?:(?<algorithm>\\w+)\\s+)?" + Pattern.quote(keyType) + "-----";
        Pattern pemPattern = Pattern.compile(pattern);
        Matcher matcher = pemPattern.matcher(pem);

        if (matcher.find()) {
            String algorithm = matcher.group("algorithm");
            return algorithm != null ? algorithm : keyType.split("\\s+")[0]; // Return "PUBLIC" or "PRIVATE" for standard format
        }

        // Fallback - shouldn't happen with valid PEM
        throw new InvalidKeySpecException("Invalid PEM format: missing required header");
    }

    /**
     * Converts the given cryptographic key to its PEM (Privacy-Enhanced Mail) format.
     *
     * @param key the cryptographic key to be converted into PEM format
     * @return the PEM-encoded string representation of the key
     * @throws UncheckedIOException if an unexpected I/O exception occurs
     */
    public static String toPem(Key key) {
        try {
            StringBuilder sb = new StringBuilder();
            appendPem(key, sb);
            return sb.toString();
        } catch (IOException e) {
            // should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Converts the given cryptographic key to an encrypted PEM (Privacy-Enhanced Mail) format.
     *
     * @param key the cryptographic key to be converted into PEM format
     * @param password the password protecting the key
     * @return the PEM-encoded string representation of the key
     * @throws UncheckedIOException if an unexpected I/O exception occurs
     */
    public static String toPem(Key key, char[] password) {
        try {
            StringBuilder sb = new StringBuilder();
            appendPem(key, password, sb);
            return sb.toString();
        } catch (IOException e) {
            // should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Appends a PEM (Privacy Enhanced Mail) format representation of the specified cryptographic key
     * to the provided {@link Appendable}.
     * <p>
     * The method determines the key type (private or public), extracts its binary encoding, converts
     * it to a Base64-encoded string, and wraps it in the appropriate PEM format with headers and footers.
     *
     * @param key the cryptographic key to be formatted into PEM; must be either a {@link PrivateKey} or {@link PublicKey}.
     * @param app the target {@link Appendable} to which the PEM representation will be appended.
     * @throws IOException if an I/O error occurs while writing to the {@link Appendable}.
     * @throws IllegalStateException if the key type is not supported.
     * @throws IllegalArgumentException if the provided key cannot be encoded.
     */
    public static void appendPem(Key key, Appendable app) throws IOException {
        // Determine key type and algorithm prefix
        String type = switch (key) {
            case PrivateKey pk -> PRIVATE_KEY;
            case PublicKey pk -> PUBLIC_KEY;
            default -> throw new IllegalStateException("Unsupported key type: " + key.getClass().getName());
        };
        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            throw new IllegalArgumentException("Key cannot be encoded");
        }

        // Convert to Base64 with line breaks every 64 characters
        CharSequence base64 = TextUtil.asCharSequence(TextUtil.base64EncodeToChars(encoded));

        // Write PEM format
        app.append("-----BEGIN ").append(type).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            app.append(base64, i, Math.min(i + 64, base64.length()));
            app.append('\n');
        }
        app.append("-----END ").append(type).append("-----\n");
    }

    /**
     * Appends the PEM-encoded representation of the given key into the provided Appendable instance.
     * If a password is provided, the key will be encrypted using the given password.
     *
     * @param key      the cryptographic key to be encoded
     * @param password the password used to encrypt the key. If null, the key will not be encrypted
     * @param app      the Appendable instance that will receive the PEM-encoded key
     * @throws IOException if an I/O error occurs while appending the data
     */
    public static void appendPem(Key key, char[] password, Appendable app) throws IOException {
        if (password.length == 0) {
            appendPem(key, app);
            return;
        }

        // Determine key type and algorithm prefix
        String type = switch (key) {
            case PrivateKey pk -> "ENCRYPTED " + PRIVATE_KEY;
            case PublicKey pk -> PUBLIC_KEY; // Public keys are not typically encrypted
            case SecretKey sk -> "ENCRYPTED " + SECRET_KEY;
            default -> throw new IllegalStateException("Unsupported key type: " + key.getClass().getName());
        };

        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            throw new IllegalArgumentException("Key cannot be encoded");
        }

        // For public keys or when no password is provided, use standard PEM format
        if (key instanceof PublicKey) {
            appendPem(key, app);
            return;
        }

        // Generate a random salt for encryption
        byte[] salt = generateSalt(16);

        // Derive encryption key from password
        SecretKey derivedKey = deriveSecretKey(
                SymmetricAlgorithm.AES,
                salt,
                new String(password).getBytes(StandardCharsets.UTF_8),
                null,
                InputBufferHandling.PRESERVE
        );

        try {
            // Encrypt the key data
            byte[] encryptedData = CryptUtil.encryptSymmetric(
                    SymmetricAlgorithm.AES,
                    derivedKey,
                    encoded,
                    InputBufferHandling.PRESERVE
            );

            // Combine salt and encrypted data
            byte[] combined = new byte[salt.length + encryptedData.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(encryptedData, 0, combined, salt.length, encryptedData.length);

            // Convert to Base64 with line breaks every 64 characters
            CharSequence base64 = TextUtil.asCharSequence(TextUtil.base64EncodeToChars(combined));

            // Write PEM format
            app.append("-----BEGIN ").append(type).append("-----\n");
            for (int i = 0; i < base64.length(); i += 64) {
                app.append(base64, i, Math.min(i + 64, base64.length()));
                app.append('\n');
            }
            app.append("-----END ").append(type).append("-----\n");
        } catch (GeneralSecurityException e) {
            // this should not happen
            throw new IllegalStateException("encryption of then PEM data failed", e);
        } finally {
            // Clean up sensitive data
            Arrays.fill(password, '\0');
            Arrays.fill(encoded, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }
    /**
     * Parses a DER-encoded key and returns the corresponding key object, either a public or private key,
     * based on the provided byte array.
     *
     * @param bytes the byte array containing the DER-encoded key
     * @return the parsed key object, either a {@link PublicKey} or {@link PrivateKey}
     * @throws GeneralSecurityException if there is an issue with generating the key (e.g., unsupported algorithm)
     * @throws IOException if the byte array cannot be parsed as a valid ASN.1 structure
     */
    public static Key parseDer(byte[] bytes) throws GeneralSecurityException, IOException {
        if (!(ASN1Primitive.fromByteArray(bytes) instanceof ASN1Sequence asn1Sequence)) {
            throw new InvalidKeyException("Invalid DER: expected ASN.1 sequence");
        }

        if (asn1Sequence.size() == 2 && asn1Sequence.getObjectAt(1) instanceof DERBitString) {
            // Public key
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(asn1Sequence);
            AsymmetricAlgorithm algorithm = getAlgorithmFromOid(spki.getAlgorithm().getAlgorithm().getId());
            return KeyFactory
                    .getInstance(algorithm.keyFactoryAlgorithm())
                    .generatePublic(new X509EncodedKeySpec(bytes));
        } else if (asn1Sequence.size() >= 3 && asn1Sequence.getObjectAt(0) instanceof ASN1Integer
                && asn1Sequence.getObjectAt(1) instanceof ASN1Sequence
                && asn1Sequence.getObjectAt(2) instanceof ASN1OctetString) {
            // Private key
            PrivateKeyInfo pki = PrivateKeyInfo.getInstance(asn1Sequence);
            AsymmetricAlgorithm algorithm = getAlgorithmFromOid(pki.getPrivateKeyAlgorithm().getAlgorithm().getId());
            return KeyFactory
                    .getInstance(algorithm.keyFactoryAlgorithm())
                    .generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } else {
            throw new InvalidKeyException("Unknown DER key format");
        }
    }

    /**
     * Retrieves the corresponding asymmetric algorithm based on the provided OID.
     *
     * @param oid the object identifier (OID) that specifies the asymmetric algorithm
     * @return the {@link AsymmetricAlgorithm} associated with the specified OID
     * @throws InvalidKeyException if the provided OID does not correspond to a known algorithm
     */
    private static AsymmetricAlgorithm getAlgorithmFromOid(String oid) throws InvalidKeyException {
        return switch (oid) {
            case "1.2.840.113549.1.1.1" -> AsymmetricAlgorithm.RSA;
            case "1.2.840.10045.2.1" -> AsymmetricAlgorithm.EC;
            case "1.2.840.10040.4.1" -> AsymmetricAlgorithm.DSA;
            default -> throw new InvalidKeyException("Unknown public key algorithm OID: " + oid);
        };
    }

    /**
     * Converts a given {@link Key} to its encoded form using DER (Distinguished Encoding Rules).
     * Supports X.509 encoding for public keys, PKCS#8 encoding for private keys,
     * and raw byte encoding for symmetric keys.
     *
     * @param key the key to be converted to DER encoding. This can be an instance of
     *            {@link PublicKey}, {@link PrivateKey}, or {@link SecretKey}.
     * @return the DER-encoded byte array representation of the specified key.
     * @throws GeneralSecurityException if the key type is unsupported or an encoding error occurs.
     */
    public static byte[] toDer(Key key) throws GeneralSecurityException {
        return switch (key) {
            // X.509 DER encoding for public keys
            case PublicKey publicKey -> new X509EncodedKeySpec(publicKey.getEncoded()).getEncoded();
            // PKCS#8 DER encoding for private keys
            case PrivateKey privateKey -> new PKCS8EncodedKeySpec(privateKey.getEncoded()).getEncoded();
            // raw bytes for symmetric keys
            case SecretKey secretKey -> secretKey.getEncoded();
            default -> throw new InvalidKeyException("Unsupported key type: " + key.getClass());
        };
    }
}
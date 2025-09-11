package com.dua3.utility.crypt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.jspecify.annotations.Nullable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for cryptographic key operations.
 */
public final class KeyUtil {
    private static final Logger LOG = LogManager.getLogger(KeyUtil.class);

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
                Arrays.fill(info, (byte) 0);
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
        String algorithm = extractAlgorithmFromPemHeader(pem, "PUBLIC KEY");
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
            return java.util.Base64.getDecoder().decode(clean);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    private static final Pattern PATTERN_CLEAN_PEM = Pattern.compile("-----[^-]*-----|\\s");

    /**
     * Loads a private key from a PEM-formatted PKCS#8 string.
     * Supports multiple algorithms (RSA, EC, DSA) by attempting to parse with each.
     *
     * @param pem the PEM-formatted private key string, including the header and footer
     * @return the PrivateKey object reconstructed from the provided PEM string
     * @throws InvalidKeySpecException if the provided PEM content cannot be converted to a valid private key
     * @throws NoSuchAlgorithmException if the algorithm is not available in the environment
     */
    public static PrivateKey loadPrivateKeyFromPem(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = extractAlgorithmFromPemHeader(pem, "PRIVATE KEY");
        String clean = PATTERN_CLEAN_PEM.matcher(pem).replaceAll("");
        byte[] bytes = decodeKeyDataBase64(clean);

        // For standard "PRIVATE KEY" format, let Java determine the algorithm from the key data
        if ("PRIVATE".equals(algorithm)) {
            // Try RSA first (most common), then EC, then DSA
            for (AsymmetricAlgorithm alg : AsymmetricAlgorithm.values()) {
                try {
                    KeyFactory kf = KeyFactory.getInstance(alg.keyFactoryAlgorithm());
                    return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
                } catch (InvalidKeySpecException e) {
                    LOG.debug("Unable to load private key from PEM data using {} algorithm '{}', trying next: {}", alg.getClass().getSimpleName(), alg.keyFactoryAlgorithm(), e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    LOG.debug("Unable to load private key from PEM data - {} algorithm '{}' not available, trying next: {}", alg.getClass().getSimpleName(), alg.keyFactoryAlgorithm(), e.getMessage());
                }
            }
            // If all fail, throw with the last exception
            throw new InvalidKeySpecException("Unable to determine key algorithm from PEM data");
        }

        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    /**
     * Extracts the algorithm from a PEM header using regex pattern matching.
     *
     * @param pem the PEM formatted string
     * @param keyType the type of key ("PUBLIC KEY" or "PRIVATE KEY")
     * @return the algorithm name for KeyFactory, or the keyType if no specific algorithm found
     * @throws InvalidKeySpecException if the PEM format is invalid or missing required headers
     */
    private static String extractAlgorithmFromPemHeader(String pem, String keyType) throws InvalidKeySpecException {
        // Add size limit for security
        if (pem.length() > 100_000) { // 100KB limit
            throw new InvalidKeySpecException("PEM data exceeds maximum allowed size");
        }

        String pattern = "-----BEGIN\\s+(?:(?<algorithm>\\w+)\\s+)?" + Pattern.quote(keyType) + "-----";
        Pattern pemPattern = Pattern.compile(pattern);
        Matcher matcher = pemPattern.matcher(pem);

        if (matcher.find()) {
            String algorithm = matcher.group("algorithm");
            return algorithm != null ? algorithm : keyType.split("\\s+")[0]; // Return "PUBLIC" or "PRIVATE" for standard format
        }

        // Fallback - shouldn't happen with valid PEM
        throw new InvalidKeySpecException("Invalid PEM format: missing required header");
    }
}
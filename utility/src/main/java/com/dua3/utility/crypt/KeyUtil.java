package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Utility class for cryptographic key operations.
 */
public final class KeyUtil {

    private static final int KEY_DERIVATION_DEFAULT_ITERATIONS = 10000;
    private static final int KEY_DERIVATION_DEFAULT_BITS = 256;

    /**
     * Utility class private constructor.
     */
    private KeyUtil() { /* nothing to do */ }

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
     *                                           for the selected algorithm
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
        }
    }

    /**
     * Derive an encryption key from a passphrase using PBKDF2-SHA256.
     * <p>
     * <strong>Make sure to store the salt to be able to retrieve the generated key again later.</strong>
     * <p>
     * <strong>Security Note:</strong> This method clears (overwrites with null characters)
     * the passphrase array after use to prevent sensitive data from remaining in memory.
     * Do not reuse the same array for subsequent operations.
     *
     * @param passphrase the passphrase (cleared after use)
     * @param salt random salt (minimum 16 bytes)
     * @param iterations iteration count (minimum 10000)
     * @param keyBits key size in bits (128, 192, or 256)
     * @param inputBufferHandling how to handle input buffers
     * @return derived encryption key
     * @throws InvalidAlgorithmParameterException if key derivation fails
     */
    private static byte[] deriveKey(char[] passphrase, byte[] salt, int iterations, int keyBits, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {

        try {
            if (salt.length < 16) {
                throw new InvalidAlgorithmParameterException("Salt must be at least 16 bytes");
            }
            if (iterations < 10000) {
                throw new InvalidAlgorithmParameterException("Iterations must be at least 10000");
            }
            SymmetricAlgorithm.AES.validateKeySize(keyBits);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, iterations, keyBits);
            SecretKey key = factory.generateSecret(spec);
            byte[] keyBytes = key.getEncoded();
            spec.clearPassword();
            return keyBytes;
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(passphrase, '\0');
                Arrays.fill(salt, (byte) 0);
            }
        }
    }

    /**
     * Derive a SecretKey from a passphrase using PBKDF2-SHA256.
     * <p>
     * <strong>Make sure to store the salt to be able to retrieve the generated key again later.</strong>
     * <p>
     * This method provides a type-safe alternative to {@link #deriveKey(char[], byte[], int, int, InputBufferHandling)}
     * that returns a SecretKey object instead of raw bytes.
     *
     * @param passphrase the passphrase (cleared after use)
     * @param salt random salt (minimum 16 bytes)
     * @param iterations iteration count (minimum 10000)
     * @param keyBits key size in bits (128, 192, or 256)
     * @param inputBufferHandling how to handle input buffers
     * @return derived SecretKey for symmetric encryption
     * @throws GeneralSecurityException if key derivation fails
     */
    public static SecretKey deriveSecretKey(char[] passphrase, byte[] salt, int iterations, int keyBits, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {
        byte[] keyBytes = deriveKey(passphrase, salt, iterations, keyBits, inputBufferHandling);
        try {
            return toSecretKey(keyBytes);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    /**
     * Derive a SecretKey using a context-based salt.
     * The context should be unique and stable for the intended use case.
     * <p>
     * <strong>Security Note:</strong> While more secure than a fixed salt, this approach
     * still produces deterministic results. For maximum security in multi-user systems,
     * consider using {@link #deriveSecretKey(char[], byte[], int, int, InputBufferHandling)} with a unique random salt
     * per user/session and store the salt securely.
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>{@code
     * char[] password = "mySecretPassword".toCharArray();
     * SecretKey key = KeyUtil.deriveSecretKey(password, "user:john.doe");
     * String encrypted = SymmetricCryptUtil.encrypt(key, "sensitive data");
     * }</pre>
     *
     * @param passphrase the passphrase (cleared after use)
     * @param context unique context (e.g., "user:john", "file:/path/to/file", "section:config")
     * @param inputBufferHandling how to handle input buffers
     * @return derived SecretKey for symmetric encryption
     * @throws GeneralSecurityException if key derivation fails
     */
    public static SecretKey deriveSecretKey(char[] passphrase, char[] context, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {
        byte[] contextSalt = TextUtil.toByteArray(context);
        // Pad salt to minimum 16 bytes
        byte[] salt = new byte[Math.max(16, contextSalt.length)];
        System.arraycopy(contextSalt, 0, salt, 0, contextSalt.length);

        try {
            return deriveSecretKey(passphrase, salt, KEY_DERIVATION_DEFAULT_ITERATIONS, KEY_DERIVATION_DEFAULT_BITS, inputBufferHandling);
        } finally {
            Arrays.fill(contextSalt, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

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
     * @param key the key to validate
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
     * @param keyBytes the byte array representing the key material
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
     * @param bits the number of bits
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
     * Converts a given byte array into a {@code PrivateKey} instance using the specified algorithm.
     *
     * @param bytes the private key in encoded byte format
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
     * Converts the provided byte array into a {@code PublicKey} instance using the specified algorithm.
     *
     * @param bytes the byte array containing the key data
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
     * Creates a {@code KeyPair} using the given public and private key byte arrays.
     *
     * @param publicKeyBytes the byte array representation of the public key
     * @param privateKeyBytes the byte array representation of the private key
     * @param algorithm the asymmetric algorithm
     * @return a {@code KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        PublicKey publicKey = toPublicKey(publicKeyBytes, algorithm);
        PrivateKey privateKey = toPrivateKey(privateKeyBytes, algorithm);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Generate an asymmetric key pair.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize the key size in bits
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
}
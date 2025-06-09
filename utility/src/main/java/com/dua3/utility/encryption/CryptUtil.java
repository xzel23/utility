package com.dua3.utility.encryption;

import com.dua3.utility.text.TextUtil;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/**
 * Cryptographic utilities.
 */
public final class CryptUtil {

    private static final SymmetricAlgorithm SYMMETRIC_ALGORITHM_DEFAULT = SymmetricAlgorithm.AES;
    private static final int GCM_TAG_LENGTH = 128;
    private static final AsymmetricAlgorithm ASYMMETRIC_ALGORITHM_DEFAULT = AsymmetricAlgorithm.RSA;
    private static final int KEY_DERIVATION_DEFAULT_ITERATIONS = 10000;
    private static final int KEY_DERIVATION_DEFAULT_BITS = 256;

    /**
     * Singleton holder for the {@link SecureRandom} instance, achieves lazy initialization.
     */
    private static final class RandomHolder {
        private static final SecureRandom RANDOM;

        static {
            try {
                RANDOM = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("could not get a secure random instance", e);
            }
        }
    }

    /**
     * Utility class private constructor.
     */
    private CryptUtil() {
        // nothing to do
    }

    /**
     * Returns the asymmetric transformation string for the given algorithm.
     * <p>
     * Supported algorithms:
     * <ul>
     *   <li>RSA: Returns RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING for secure padding</li>
     *   <li>ECIES: Returns ECIES (requires special provider like Bouncy Castle)</li>
     *   <li>EC/DSA: Throws exception as these are for signatures/key agreement only</li>
     * </ul>
     *
     * @param algorithm the asymmetric algorithm
     * @return the transformation string corresponding to the given algorithm
     * @throws IllegalArgumentException if algorithm doesn't support direct encryption
     */
    private static String getAsymmetricTransformation(AsymmetricAlgorithm algorithm) {
        return algorithm.getTransformation()
                .orElseThrow(() -> new IllegalArgumentException("Algorithm " + algorithm + " does not support direct encryption"));
    }

    /**
     * Validates the provided asymmetric encryption key to ensure it satisfies the requirements
     * for encrypting data of the specified length.
     * <p>
     * This method performs validation specific to the algorithm of the key. For RSA keys,
     * it ensures that the data length does not exceed the maximum allowed size based on
     * the key's modulus and padding restrictions.
     *
     * @param key the asymmetric encryption key to validate
     * @param dataLength the length of the data that is intended to be encrypted
     * @param isHybridEncryption whether this is for hybrid encryption (affects validation)
     * @throws InvalidKeyException if the algorithm doesn't support direct encryption
     * @throws IllegalBlockSizeException if the data is too large for the key/algorithm
     */
    private static void validateAsymmetricEncryptionKey(PublicKey key, int dataLength, boolean isHybridEncryption) throws GeneralSecurityException {
        String algorithm = key.getAlgorithm();

        switch (algorithm.toUpperCase()) {
            case "RSA":
                validateRSAEncryptionKey(key, dataLength);
                break;
            case "EC":
                if (!isHybridEncryption) {
                    throw new InvalidKeyException("EC keys do not support direct encryption. Use hybrid encryption or ECIES instead.");
                }
                break;
            case "DSA":
                throw new InvalidKeyException("DSA keys are for signatures only, not encryption");
            default:
                // For other algorithms like ECIES, assume they're valid if they got here
                break;
        }
    }

    /**
     * Validates the provided RSA encryption key and checks if the data length
     * is suitable for the key size.
     * <p>
     * This method ensures that the provided key is an RSA public key and calculates
     * the maximum permissible data size for the key, verifying that the input data length
     * does not exceed this limit. If the key type or data length is invalid, an
     * appropriate exception is thrown.
     *
     * @param key the public key to be validated; must be an RSA public key
     * @param dataLength the length of the data intended for encryption, in bytes
     * @throws GeneralSecurityException if the encryption key is not valid or if the data length
     *         exceeds the maximum allowed size for the given RSA key
     */
    private static void validateRSAEncryptionKey(PublicKey key, int dataLength) throws GeneralSecurityException {
        if (!(key instanceof java.security.interfaces.RSAPublicKey)) {
            throw new InvalidKeyException("Expected RSA key, but got: " + key.getClass().getSimpleName());
        }

        java.security.interfaces.RSAPublicKey rsaKey = (java.security.interfaces.RSAPublicKey) key;
        int keySize = rsaKey.getModulus().bitLength();

        // OAEP padding overhead: 2 + 2*hLen where hLen is 32 for SHA-256
        int maxDataSize = (keySize / 8) - 2 - (2 * 32);

        if (dataLength > maxDataSize) {
            throw new IllegalBlockSizeException(
                    String.format("Data too large for RSA key. Max size: %d bytes, actual: %d bytes",
                            maxDataSize, dataLength));
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
     */
    private static void validateAsymmetricKeySize(AsymmetricAlgorithm algorithm, int keySize) {
        switch (algorithm) {
            case RSA:
            case DSA:
                if (keySize < 2048) {
                    throw new IllegalArgumentException(algorithm + " key size must be at least 2048 bits, but was: " + keySize);
                }
                break;
            case EC:
            case ECIES:
                if (keySize != 256 && keySize != 384 && keySize != 521) {
                    throw new IllegalArgumentException("EC key size must be 256, 384, or 521 bits, but was: " + keySize);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
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
     * @throws GeneralSecurityException if key derivation fails
     */
    private static byte[] deriveKey(char[] passphrase, byte[] salt, int iterations, int keyBits, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {

        try {
            if (salt.length < 16) {
                throw new IllegalArgumentException("Salt must be at least 16 bytes");
            }
            if (iterations < 10000) {
                throw new IllegalArgumentException("Iterations must be at least 10000");
            }
            SYMMETRIC_ALGORITHM_DEFAULT.validateKeySize(keyBits);

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
     * SecretKey key = CryptUtil.deriveSecretKey(password, "user:john.doe");
     * String encrypted = CryptUtil.encrypt(key, "sensitive data");
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
        byte[] salt = new byte[length];
        RandomHolder.RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Symmetrically encrypt data using a Key object with the default algorithm.
     *
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param data the data to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encryptSymmetric(Key key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        return encryptSymmetric(SYMMETRIC_ALGORITHM_DEFAULT, key, data, inputBufferHandling);
    }

    /**
     * Symmetrically encrypt data using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param data the data to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encryptSymmetric(SymmetricAlgorithm algorithm, Key key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        try {
            validateSymmetricKey(key, algorithm);

            Cipher cipher = Cipher.getInstance(algorithm.getTransformation());

            if (algorithm.requiresIv()) {
                byte[] iv = new byte[algorithm.getIvLength()];
                RandomHolder.RANDOM.nextBytes(iv);

                if (algorithm.isAuthenticated()) {
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                    cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
                } else {
                    IvParameterSpec ivSpec = new IvParameterSpec(iv);
                    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
                }

                byte[] encrypted = cipher.doFinal(data);

                // Prepend IV to encrypted data
                byte[] result = new byte[iv.length + encrypted.length];
                System.arraycopy(iv, 0, result, 0, iv.length);
                System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
                return result;
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(data);
            }
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
        }
    }

    /**
     * Symmetrically decrypt data using a Key object with the default algorithm.
     * <p>
     * The data is decrypted using AES-GCM.
     *
     * @param key           the encryption key (must be compatible with the algorithm)
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decryptSymmetric(Key key, byte[] cipherMessage) throws GeneralSecurityException {
        return decryptSymmetric(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherMessage);
    }

    /**
     * Symmetrically decrypt data using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key           the encryption key (must be compatible with the algorithm)
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decryptSymmetric(SymmetricAlgorithm algorithm, Key key, byte[] cipherMessage) throws GeneralSecurityException {
        validateSymmetricKey(key, algorithm);

        Cipher cipher = Cipher.getInstance(algorithm.getTransformation());

        if (algorithm.requiresIv()) {
            int ivLength = algorithm.getIvLength();
            if (cipherMessage.length < ivLength) {
                throw new IllegalArgumentException("Cipher message too short to contain IV");
            }

            byte[] iv = Arrays.copyOfRange(cipherMessage, 0, ivLength);
            byte[] encrypted = Arrays.copyOfRange(cipherMessage, ivLength, cipherMessage.length);

            if (algorithm.isAuthenticated()) {
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            } else {
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            }

            return cipher.doFinal(encrypted);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(cipherMessage);
        }
    }

    /**
     * Validates a symmetric key for use with the specified algorithm.
     *
     * @param key the key to validate
     * @param algorithm the algorithm the key will be used with
     * @throws IllegalArgumentException if the key is not compatible
     */
    private static void validateSymmetricKey(Key key, SymmetricAlgorithm algorithm) {
        if (!algorithm.getKeyAlgorithm().equals(key.getAlgorithm())) {
            throw new IllegalArgumentException(
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
     */
    public static SecretKey toSecretKey(byte[] keyBytes) {
        return toSecretKey(keyBytes, SYMMETRIC_ALGORITHM_DEFAULT);
    }

    /**
     * Create a SecretKey from a byte array for the specified algorithm.
     *
     * @param keyBytes the key bytes
     * @param algorithm the symmetric algorithm
     * @return SecretKey instance
     */
    public static SecretKey toSecretKey(byte[] keyBytes, SymmetricAlgorithm algorithm) {
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
        return generateSecretKey(bits, SYMMETRIC_ALGORITHM_DEFAULT);
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
        keyGen.init(bits, RandomHolder.RANDOM);
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
        return toPrivateKey(bytes, ASYMMETRIC_ALGORITHM_DEFAULT);
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
        return toPublicKey(bytes, ASYMMETRIC_ALGORITHM_DEFAULT);
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
     * Asymmetrically encrypt data using a public key.
     *
     * @param publicKey the public key for encryption
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encryptAsymmetric(PublicKey publicKey, byte[] data) throws GeneralSecurityException {
        validateAsymmetricEncryptionKey(publicKey, data.length, false);

        String algorithm = publicKey.getAlgorithm();
        AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(algorithm.toUpperCase());
        String transformation = getAsymmetricTransformation(asymmAlg);

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Asymmetrically decrypt data using a private key.
     *
     * @param privateKey the private key for decryption
     * @param cipherData the encrypted data
     * @return the decrypted data
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decryptAsymmetric(PrivateKey privateKey, byte[] cipherData) throws GeneralSecurityException {
        String algorithm = privateKey.getAlgorithm();
        AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(algorithm.toUpperCase());
        String transformation = getAsymmetricTransformation(asymmAlg);

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(cipherData);
    }

    /**
     * Generate an asymmetric key pair.
     *
     * @param algorithm the asymmetric algorithm
     * @param keySize the key size in bits
     * @return the generated key pair
     */
    public static KeyPair generateKeyPair(AsymmetricAlgorithm algorithm, int keySize) {
        validateAsymmetricKeySize(algorithm, keySize);

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm.keyFactoryAlgorithm());
            keyGen.initialize(keySize, RandomHolder.RANDOM);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Generate an RSA key pair with default key size (2048 bits).
     *
     * @return the generated RSA key pair
     */
    public static KeyPair generateRSAKeyPair() {
        return generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
    }

    /**
     * Sign data using a private key.
     *
     * @param privateKey the private key for signing
     * @param data the data to sign
     * @param inputBufferHandling how to handle input buffers
     * @return the digital signature
     * @throws GeneralSecurityException if signing fails
     */
    public static byte[] sign(PrivateKey privateKey, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        try {
            String algorithm = privateKey.getAlgorithm();
            AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(algorithm.toUpperCase());
            String signatureAlgorithm = asymmAlg.getSignatureAlgorithm()
                    .orElseThrow(() -> new UnsupportedOperationException("Algorithm " + asymmAlg + " does not support signing"));

            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey, RandomHolder.RANDOM);
            signature.update(data);
            return signature.sign();
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
        }
    }

    /**
     * Verify a digital signature.
     *
     * @param publicKey the public key for verification
     * @param data the original data that was signed
     * @param signature the digital signature to verify
     * @param inputBufferHandling how to handle input buffers
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException if verification fails
     */
    public static boolean verify(PublicKey publicKey, byte[] data, byte[] signature, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        try {
            String algorithm = publicKey.getAlgorithm();
            AsymmetricAlgorithm asymmAlg = AsymmetricAlgorithm.valueOf(algorithm.toUpperCase());
            String signatureAlgorithm = asymmAlg.getSignatureAlgorithm()
                    .orElseThrow(() -> new UnsupportedOperationException("Algorithm " + asymmAlg + " does not support verification"));

            Signature sig = Signature.getInstance(signatureAlgorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
                Arrays.fill(signature, (byte) 0);
            }
        }
    }

    /**
     * Sign text using a private key.
     *
     * @param privateKey the private key for signing
     * @param text the text to sign
     * @return the digital signature as Base64 encoded String
     * @throws GeneralSecurityException if signing fails
     */
    public static byte[] sign(PrivateKey privateKey, CharSequence text) throws GeneralSecurityException {
        byte[] data = TextUtil.toByteArray(text);
        return sign(privateKey, data, InputBufferHandling.CLEAR_AFTER_USE);
    }

    /**
     * Sign text using a private key.
     * <p>
     * <strong>Security Note:</strong> This method clears (overwrites with null characters)
     * the input char array after use to prevent sensitive data from remaining in memory.
     * Do not reuse the same array for subsequent operations.
     *
     * @param privateKey the private key for signing
     * @param text the text to sign
     * @param inputBufferHandling how to handle input buffers
     * @return the digital signature as Base64 encoded String
     * @throws GeneralSecurityException if signing fails
     */
    public static byte[] sign(PrivateKey privateKey, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = TextUtil.toByteArray(text);
        return sign(privateKey, data, InputBufferHandling.CLEAR_AFTER_USE);
    }

    /**
     * Verify a digital signature for text.
     *
     * @param publicKey the public key for verification
     * @param text the original text that was signed
     * @param signature the digital signature as Base64 encoded String
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException if verification fails
     */
    public static boolean verify(PublicKey publicKey, CharSequence text, byte[] signature) throws GeneralSecurityException {
        char[] data = TextUtil.toCharArray(text);
        return verify(publicKey, data, signature, InputBufferHandling.CLEAR_AFTER_USE);
    }

    /**
     * Verify a digital signature for text.
     * <p>
     * <strong>Security Note:</strong> This method clears (overwrites with null characters)
     * the input char array after use to prevent sensitive data from remaining in memory.
     * Do not reuse the same array for subsequent operations.
     *
     * @param publicKey the public key for verification
     * @param text the original text that was signed
     * @param signature the digital signature
     * @param inputBufferHandling how to handle input buffers
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException if verification fails
     */
    public static boolean verify(PublicKey publicKey, char[] text, byte[] signature, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = TextUtil.toByteArray(text);
        try {
            return verify(publicKey, data, signature, InputBufferHandling.PRESERVE);
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(text, '\0');
                Arrays.fill(signature, (byte) 0);
            }
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Hybrid encryption for large data using RSA/EC for key encryption and AES-GCM for data encryption.
     * <p>
     * This method generates a random AES key, encrypts the data with AES-GCM, then encrypts
     * the AES key with the provided public key using asymmetric encryption. The result combines
     * both the encrypted AES key and the encrypted data in a single byte array.
     * <p>
     * Format: [4 bytes: encrypted key length][encrypted AES key][encrypted data]
     *
     * @param publicKey the public key for encrypting the AES key (RSA, EC, or ECIES)
     * @param data the data to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the hybrid encrypted data as a byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encryptHybrid(PublicKey publicKey, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        try {
            validateAsymmetricEncryptionKey(publicKey, 32, true); // 32 bytes = 256-bit AES key

            // Generate random AES key
            SecretKey aesKey = generateSecretKey(256, SYMMETRIC_ALGORITHM_DEFAULT);

            // Encrypt data with AES
            byte[] encryptedData = encryptSymmetric(SYMMETRIC_ALGORITHM_DEFAULT, aesKey, data, InputBufferHandling.PRESERVE);

            // Encrypt AES key with public key
            byte[] encryptedKey = encryptAsymmetric(publicKey, aesKey.getEncoded());

            // Combine: [4 bytes: key length][encrypted key][encrypted data]
            ByteBuffer buffer = ByteBuffer.allocate(4 + encryptedKey.length + encryptedData.length);
            buffer.putInt(encryptedKey.length);
            buffer.put(encryptedKey);
            buffer.put(encryptedData);

            return buffer.array();
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
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
        keyGen.initialize(ecSpec, RandomHolder.RANDOM);
        return keyGen.generateKeyPair();
    }

    /**
     * Decrypt hybrid encrypted data using the corresponding private key.
     * <p>
     * This method reverses the hybrid encryption process by first extracting and decrypting
     * the AES key using the private key, then using that AES key to decrypt the actual data.
     *
     * @param privateKey the private key corresponding to the public key used for encryption
     * @param cipherData the hybrid encrypted data as a byte array
     * @return the decrypted data
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decryptHybrid(PrivateKey privateKey, byte[] cipherData) throws GeneralSecurityException {
        if (cipherData.length < 4) {
            throw new IllegalArgumentException("Invalid hybrid encrypted data");
        }

        ByteBuffer buffer = ByteBuffer.wrap(cipherData);
        int keyLength = buffer.getInt();

        if (keyLength < 0 || keyLength > cipherData.length - 4) {
            throw new IllegalArgumentException("Invalid encrypted key length");
        }

        byte[] encryptedKey = new byte[keyLength];
        buffer.get(encryptedKey);

        byte[] encryptedData = new byte[cipherData.length - 4 - keyLength];
        buffer.get(encryptedData);

        // Decrypt AES key
        SecretKey aesKey = toSecretKey(decryptAsymmetric(privateKey, encryptedKey));

        // Decrypt data with AES key
        return decryptSymmetric(SYMMETRIC_ALGORITHM_DEFAULT, aesKey, encryptedData);
    }
}
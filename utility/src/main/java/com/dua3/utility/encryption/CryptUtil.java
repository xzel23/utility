package com.dua3.utility.encryption;

import com.dua3.utility.text.TextUtil;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
     * Validates the provided asymmetric encryption key to ensure it satisfies the requirements
     * for encrypting data of the specified length.
     * <p>
     * This method performs validation specific to the algorithm of the key. For RSA keys,
     * it ensures that the data length does not exceed the maximum allowed size based on
     * the key's modulus and padding restrictions.
     *
     * @param key the asymmetric encryption key to validate
     * @param dataLength the length of the data that is intended to be encrypted
     * @throws InvalidKeyException if the algorithm doesn't support direct encryption
     * @throws IllegalBlockSizeException if the data is too large for the key/algorithm
     */
    private static void validateAsymmetricEncryptionKey(PublicKey key, int dataLength) throws GeneralSecurityException {
        validateAsymmetricEncryptionKey(key, dataLength, false);
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
    public static byte[] deriveKey(char[] passphrase, byte[] salt, int iterations, int keyBits, InputBufferHandling inputBufferHandling)
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
     * Derive an encryption key using a context-based salt.
     * The context should be unique and stable for the intended use case.
     * <p>
     * <strong>Security Note:</strong> While more secure than a fixed salt, this approach
     * still produces deterministic results. For maximum security in multi-user systems,
     * consider using {@link #deriveKey(char[], byte[], int, int)} with a unique random salt
     * per user/session and store the salt securely.
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>{@code
     * char[] password = "mySecretPassword".toCharArray();
     * byte[] key = CryptUtil.deriveKey(password, "user:john.doe");
     * String encrypted = CryptUtil.encrypt(key, "sensitive data");
     * }</pre>
     *
     * @param passphrase the passphrase (cleared after use)
     * @param context unique context (e.g., "user:john", "file:/path/to/file", "section:config")
     * @param inputBufferHandling how to handle input buffers
     * @return derived encryption key
     * @throws GeneralSecurityException if key derivation fails
     */
    public static byte[] deriveKey(char[] passphrase, CharSequence context, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {
        byte[] contextSalt = context.toString().getBytes(StandardCharsets.UTF_8);
        // Pad salt to minimum 16 bytes
        byte[] salt = new byte[Math.max(16, contextSalt.length)];
        System.arraycopy(contextSalt, 0, salt, 0, contextSalt.length);

        try {
            return deriveKey(passphrase, salt, KEY_DERIVATION_DEFAULT_ITERATIONS, KEY_DERIVATION_DEFAULT_BITS, inputBufferHandling);
        } finally {
            Arrays.fill(contextSalt, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

    /**
     * Derive a SecretKey from a passphrase using PBKDF2-SHA256.
     * <p>
     * <strong>Make sure to store the salt to be able to retrieve the generated key again later.</strong>
     * <p>
     * This method provides a type-safe alternative to {@link #deriveKey(char[], byte[], int, int)}
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
     * consider using {@link #deriveSecretKey(char[], byte[], int, int)} with a unique random salt
     * per user/session and store the salt securely.
     * <p>
     * This method provides a type-safe alternative to {@link #deriveKey(char[], CharSequence)}
     * that returns a SecretKey object instead of raw bytes.
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
    public static SecretKey deriveSecretKey(char[] passphrase, CharSequence context, InputBufferHandling inputBufferHandling)
            throws GeneralSecurityException {
        byte[] keyBytes = deriveKey(passphrase, context, inputBufferHandling);
        try {
            return toSecretKey(keyBytes);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
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
     * Generate key.
     *
     * @param bits the number of bits; must be a multiple of 8
     * @return the generated key
     */
    public static byte[] generateKey(int bits) {
        if (bits % 8 != 0) {
            throw new IllegalArgumentException("Key size must be a multiple of 8 bits");
        }
        SYMMETRIC_ALGORITHM_DEFAULT.validateKeySize(bits);

        byte[] key = new byte[bits / 8];
        RandomHolder.RANDOM.nextBytes(key);
        return key;
    }

    /**
     * Symmetrically encrypt text using the default algorithm (AES-GCM).
     * <p>
     * The text is encrypted using AES-GCM and the resulting ciphertext is converted to
     * a String by applying the Base64 algorithm.
     *
     * @param key  the encryption key
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(byte[] key, CharSequence text) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, text);
    }

    /**
     * Symmetrically encrypt text using the specified algorithm.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(SymmetricAlgorithm algorithm, byte[] key, CharSequence text) throws GeneralSecurityException {
        char[] chars = TextUtil.toCharArray(text);
        return encrypt(algorithm, key, chars, InputBufferHandling.CLEAR_AFTER_USE);
    }

    /**
     * Symmetrically encrypt text using a Key object with the default algorithm.
     *
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(Key key, CharSequence text) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, text);
    }

    /**
     * Symmetrically encrypt text using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(SymmetricAlgorithm algorithm, Key key, CharSequence text) throws GeneralSecurityException {
        char[] chars = text.toString().toCharArray();
        try {
            return encrypt(algorithm, key, chars, InputBufferHandling.CLEAR_AFTER_USE);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Symmetrically encrypt text using the default algorithm (AES-GCM).
     * <p>
     * The text is encrypted using AES-GCM, and the resulting ciphertext is converted to
     * a String by applying the Base64 algorithm.
     * <p>
     * <strong>Security Note:</strong> This method clears (overwrites with null characters)
     * the input char array after use to prevent sensitive data from remaining in memory.
     * Do not reuse the same array for subsequent operations.
     *
     * @param key  the encryption key
     * @param text the text to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(byte[] key, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, text, inputBufferHandling);
    }

    /**
     * Symmetrically encrypt text using the specified algorithm.
     * <p>
     * <strong>Security Note:</strong> This method clears (overwrites with null characters)
     * the input char array after use to prevent sensitive data from remaining in memory.
     * Do not reuse the same array for subsequent operations.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key
     * @param text the text to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(SymmetricAlgorithm algorithm, byte[] key, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        if (inputBufferHandling != InputBufferHandling.PRESERVE) {
            Arrays.fill(text, '\0');
        }
        byte[] encrypted = encrypt(algorithm, key, data, inputBufferHandling);
        return TextUtil.base64Encode(encrypted);
    }

    /**
     * Symmetrically encrypt text using a Key object with the default algorithm.
     *
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param text the text to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(Key key, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, text, inputBufferHandling);
    }

    /**
     * Symmetrically encrypt text using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key (must be compatible with the algorithm)
     * @param text the text to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(SymmetricAlgorithm algorithm, Key key, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        if (inputBufferHandling != InputBufferHandling.PRESERVE) {
            Arrays.fill(text, '\0');
        }
        byte[] encrypted = encrypt(algorithm, key, data, InputBufferHandling.CLEAR_AFTER_USE);
        return TextUtil.base64Encode(encrypted);
    }

    /**
     * Symmetrically decrypt text using the default algorithm (AES-GCM).
     * <p>
     * The ciphertext is decrypted using AES-GCM after being decoded from Base64.
     *
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(byte[] key, String cipherText) throws GeneralSecurityException {
        return decrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherText);
    }

    /**
     * Symmetrically decrypt text using the specified algorithm.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(SymmetricAlgorithm algorithm, byte[] key, String cipherText) throws GeneralSecurityException {
        char[] chars = decryptToChars(algorithm, key, cipherText);
        try {
            return new String(chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Symmetrically decrypt text to a char array using the default algorithm.
     * <p>
     * The ciphertext is decrypted using AES-GCM after being decoded from Base64.
     * The caller is responsible for clearing the returned char array after use.
     *
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(byte[] key, String cipherText) throws GeneralSecurityException {
        return decryptToChars(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherText);
    }

    /**
     * Symmetrically decrypt text to a char array using the specified algorithm.
     * <p>
     * The ciphertext is decrypted after being decoded from Base64.
     * The caller is responsible for clearing the returned char array after use.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(SymmetricAlgorithm algorithm, byte[] key, String cipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(cipherText);
        byte[] decrypted = decrypt(algorithm, key, cipherData);
        try {
            return new String(decrypted, StandardCharsets.UTF_8).toCharArray();
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
    }

    /**
     * Symmetrically encrypt data using the default algorithm (AES-GCM).
     * <p>
     * The data is encrypted using AES-GCM.
     *
     * @param key  the encryption key
     * @param data the data to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encrypt(byte[] key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, data, inputBufferHandling);
    }

    /**
     * Symmetrically encrypt data using the specified algorithm.
     *
     * @param algorithm the symmetric algorithm to use
     * @param key  the encryption key
     * @param data the data to encrypt
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        try {
            algorithm.validateKeySize(key.length * 8);

            Cipher cipher = Cipher.getInstance(algorithm.getTransformation());
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm.getKeyAlgorithm());

            if (algorithm.requiresIv()) {
                byte[] iv = new byte[algorithm.getIvLength()];
                RandomHolder.RANDOM.nextBytes(iv);

                if (algorithm.isAuthenticated()) {
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
                } else {
                    IvParameterSpec ivSpec = new IvParameterSpec(iv);
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                }

                byte[] encrypted = cipher.doFinal(data);

                // Prepend IV to encrypted data
                byte[] result = new byte[iv.length + encrypted.length];
                System.arraycopy(iv, 0, result, 0, iv.length);
                System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
                Arrays.fill(iv, (byte) 0);
                Arrays.fill(encrypted, (byte) 0);
                return result;
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                return cipher.doFinal(data);
            }
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
        }
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
    public static byte[] encrypt(Key key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        return encrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, data, inputBufferHandling);
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
    public static byte[] encrypt(SymmetricAlgorithm algorithm, Key key, byte[] data, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
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
     * Symmetrically decrypt data using the default algorithm (AES-GCM).
     * <p>
     * The data is decrypted using AES-GCM.
     *
     * @param key           the encryption key
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decrypt(byte[] key, byte[] cipherMessage) throws GeneralSecurityException {
        return decrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherMessage);
    }

    /**
     * Symmetrically decrypt data using the specified algorithm.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key           the encryption key
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] cipherMessage) throws GeneralSecurityException {
        algorithm.validateKeySize(key.length * 8);

        Cipher cipher = Cipher.getInstance(algorithm.getTransformation());
        SecretKeySpec keySpec = new SecretKeySpec(key, algorithm.getKeyAlgorithm());

        if (algorithm.requiresIv()) {
            int ivLength = algorithm.getIvLength();
            if (cipherMessage.length < ivLength) {
                throw new IllegalArgumentException("Cipher message too short to contain IV");
            }

            byte[] iv = Arrays.copyOfRange(cipherMessage, 0, ivLength);
            byte[] encrypted = Arrays.copyOfRange(cipherMessage, ivLength, cipherMessage.length);

            if (algorithm.isAuthenticated()) {
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            } else {
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            }

            return cipher.doFinal(encrypted);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(cipherMessage);
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
    public static byte[] decrypt(Key key, byte[] cipherMessage) throws GeneralSecurityException {
        return decrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherMessage);
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
    public static byte[] decrypt(SymmetricAlgorithm algorithm, Key key, byte[] cipherMessage) throws GeneralSecurityException {
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
     * Symmetrically decrypt text using a Key object with the default algorithm.
     * <p>
     * The ciphertext is decrypted using AES-GCM after being decoded from Base64.
     *
     * @param key        the encryption key used (must be compatible with the algorithm)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(Key key, String cipherText) throws GeneralSecurityException {
        return decrypt(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherText);
    }

    /**
     * Symmetrically decrypt text using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key        the encryption key used (must be compatible with the algorithm)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(SymmetricAlgorithm algorithm, Key key, String cipherText) throws GeneralSecurityException {
        char[] chars = decryptToChars(algorithm, key, cipherText);
        try {
            return new String(chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Symmetrically decrypt text to a char array using a Key object with the default algorithm.
     * <p>
     * The ciphertext is decrypted using AES-GCM after being decoded from Base64.
     * The caller is responsible for clearing the returned char array after use.
     *
     * @param key        the encryption key used (must be compatible with the algorithm)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(Key key, String cipherText) throws GeneralSecurityException {
        return decryptToChars(SYMMETRIC_ALGORITHM_DEFAULT, key, cipherText);
    }

    /**
     * Symmetrically decrypt text to a char array using a Key object with the specified algorithm.
     *
     * @param algorithm the symmetric algorithm that was used for encryption
     * @param key        the encryption key used (must be compatible with the algorithm)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(SymmetricAlgorithm algorithm, Key key, String cipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(cipherText);
        byte[] decrypted = decrypt(algorithm, key, cipherData);
        try {
            return new String(decrypted, StandardCharsets.UTF_8).toCharArray();
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
    }

    /**
     * Validates the length of a cryptographic key to ensure it adheres to the expected lengths.
     * The allowed key lengths are 128, 192, or 256 bits for AES.
     *
     * @param key the byte array representing the cryptographic key whose length is to be validated.
     * @throws IllegalArgumentException if the key length does not match one of the allowed values.
     */
    private static void validateKeyLength(byte[] key) {
        SYMMETRIC_ALGORITHM_DEFAULT.validateKeySize(key.length * 8);
    }

    /**
     * Validates a symmetric key for use with the default algorithm.
     *
     * @param key the key to validate
     * @throws IllegalArgumentException if the key is not compatible
     */
    private static void validateSymmetricKey(Key key) {
        validateSymmetricKey(key, SYMMETRIC_ALGORITHM_DEFAULT);
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

    // Asymmetric encryption methods continue to use the existing AsymmetricAlgorithm enum...
    // [Rest of the asymmetric methods would continue as before, but I'll include key ones for completeness]

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
     * Converts the given byte arrays representing a public key and a private key
     * into a {@link KeyPair} using the default algorithm.
     *
     * @param publicKeyBytes the byte array representing the public key
     * @param privateKeyBytes the byte array representing the private key
     * @return a {@link KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes) throws GeneralSecurityException {
        return toKeyPair(publicKeyBytes, privateKeyBytes, ASYMMETRIC_ALGORITHM_DEFAULT);
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
        validateAsymmetricEncryptionKey(publicKey, data.length);

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
     * Asymmetrically encrypt text using a public key.
     *
     * @param publicKey the public key for encryption
     * @param text the text to encrypt
     * @return the encrypted message as Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encryptAsymmetric(PublicKey publicKey, CharSequence text) throws GeneralSecurityException {
        byte[] data = text.toString().getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encryptAsymmetric(publicKey, data);
        return TextUtil.base64Encode(encrypted);
    }

    /**
     * Encrypts the provided text using asymmetric encryption with the given public key.
     * Converts the provided character array to a byte array for encryption,
     * and returns the encrypted result as a base64-encoded string.
     *
     * @param publicKey the public key used to encrypt the data
     * @param text the character array containing the plaintext to be encrypted
     * @return the encrypted text as a base64-encoded string
     * @throws GeneralSecurityException if an error occurs during the encryption process
     */
    public static String encryptAsymmetric(PublicKey publicKey, char[] text) throws GeneralSecurityException {
        byte[] data = new String(text).getBytes(StandardCharsets.UTF_8);
        Arrays.fill(text, '\0');
        try {
            byte[] encrypted = encryptAsymmetric(publicKey, data);
            return TextUtil.base64Encode(encrypted);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Decrypts a given cipher text using the provided private key with asymmetric encryption.
     * This method returns the decrypted plain text as a string.
     *
     * @param privateKey the private key used for decryption. Must not be null.
     * @param cipherText the encrypted text to decrypt. Must not be null.
     * @return the decrypted plain text as a string.
     * @throws GeneralSecurityException if decryption fails due to invalid keys or other security errors.
     */
    public static String decryptAsymmetric(PrivateKey privateKey, String cipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(cipherText);
        byte[] decrypted = decryptAsymmetric(privateKey, cipherData);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Decrypts the given cipher text using asymmetric encryption and converts the result into a char array.
     *
     * @param privateKey the private key to be used for decryption
     * @param cipherText the Base64-encoded cipher text to be decrypted
     * @return the decrypted data as a char array
     * @throws GeneralSecurityException if decryption fails due to invalid keys or corrupted data
     */
    public static char[] decryptAsymmetricToChars(PrivateKey privateKey, String cipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(cipherText);
        byte[] decrypted = decryptAsymmetric(privateKey, cipherData);
        try {
            return new String(decrypted, StandardCharsets.UTF_8).toCharArray();
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
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
    public static String sign(PrivateKey privateKey, CharSequence text) throws GeneralSecurityException {
        char[] data = TextUtil.toCharArray(text);
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
    public static String sign(PrivateKey privateKey, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = new String(text).getBytes(StandardCharsets.UTF_8);
        if (inputBufferHandling != InputBufferHandling.PRESERVE) {
            Arrays.fill(text, '\0');
        }
        byte[] signature = sign(privateKey, data, com.dua3.utility.encryption.InputBufferHandling.CLEAR_AFTER_USE);
        return TextUtil.base64Encode(signature);
    }

    /**
     * Verify a digital signature for text.
     *
     * @param publicKey the public key for verification
     * @param text the original text that was signed
     * @param signatureBase64 the digital signature as Base64 encoded String
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException if verification fails
     */
    public static boolean verify(PublicKey publicKey, CharSequence text, String signatureBase64) throws GeneralSecurityException {
        char[] data = TextUtil.toCharArray(text);
        byte[] signature = TextUtil.base64Decode(signatureBase64);
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
        byte[] data = TextUtil.charsToBytes(text);
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
            byte[] aesKeyBytes = aesKey.getEncoded();

            try {
                // Encrypt data with AES
                byte[] encryptedData = encrypt(SYMMETRIC_ALGORITHM_DEFAULT, aesKeyBytes, data, InputBufferHandling.PRESERVE);

                // Encrypt AES key with public key
                byte[] encryptedKey = encryptAsymmetric(publicKey, aesKeyBytes);

                // Combine: [4 bytes: key length][encrypted key][encrypted data]
                ByteBuffer buffer = ByteBuffer.allocate(4 + encryptedKey.length + encryptedData.length);
                buffer.putInt(encryptedKey.length);
                buffer.put(encryptedKey);
                buffer.put(encryptedData);

                return buffer.array();
            } finally {
                Arrays.fill(aesKeyBytes, (byte) 0);
            }
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
        }
    }

    /**
     * Encrypts the provided text using a hybrid encryption scheme with the given public key.
     * <p>
     * This method first converts the provided text to a character array, encrypts it using
     * the specified public key, and encodes the result in Base64 format. Once the encryption
     * is complete, the character array is cleared to ensure sensitive data is not retained
     * in memory.
     *
     * @param publicKey the public key used for encrypting the data
     * @param text the text to be encrypted, provided as a sequence of characters
     * @return the encrypted text, encoded as a Base64 string
     * @throws GeneralSecurityException if an error occurs during encryption
     */
    public static String encryptHybrid(PublicKey publicKey, CharSequence text) throws GeneralSecurityException {
        return encryptHybrid(publicKey, TextUtil.toCharArray(text), InputBufferHandling.CLEAR_AFTER_USE);
    }

    /**
     * Encrypts the provided text using a hybrid encryption mechanism that combines
     * public key encryption for secure key exchange and symmetric encryption for
     * encrypting the provided text. The input text is securely cleared after processing.
     *
     * @param publicKey the public key used for encrypting the symmetric key
     * @param text the text to be encrypted, provided as a char array
     * @param inputBufferHandling how to handle input buffers
     * @return the encrypted text encoded in Base64 format
     * @throws GeneralSecurityException if any encryption-related error occurs
     */
    public static String encryptHybrid(PublicKey publicKey, char[] text, InputBufferHandling inputBufferHandling) throws GeneralSecurityException {
        byte[] data = new String(text).getBytes(StandardCharsets.UTF_8);
        if (inputBufferHandling != InputBufferHandling.PRESERVE) {
            Arrays.fill(text, '\0');
        }
        byte[] encrypted = encryptHybrid(publicKey, data, InputBufferHandling.CLEAR_AFTER_USE);
        return TextUtil.base64Encode(encrypted);
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
        byte[] aesKeyBytes = decryptAsymmetric(privateKey, encryptedKey);

        try {
            // Decrypt data with AES key
            return decrypt(SYMMETRIC_ALGORITHM_DEFAULT, aesKeyBytes, encryptedData);
        } finally {
            Arrays.fill(aesKeyBytes, (byte) 0);
        }
    }

    /**
     * Decrypts a base64-encoded ciphertext using a hybrid encryption scheme.
     * This method combines the use of public-private key cryptography and symmetric key encryption.
     *
     * @param privateKey the private key used for decryption in the hybrid encryption process
     * @param base64CipherText the encrypted text encoded in base64 format to be decrypted
     * @return the decrypted plaintext as a string in UTF-8 encoding
     * @throws GeneralSecurityException if there is an error during the decryption process
     */
    public static String decryptHybrid(PrivateKey privateKey, String base64CipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(base64CipherText);
        byte[] decrypted = decryptHybrid(privateKey, cipherData);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Decrypts a hybrid encrypted Base64-encoded cipher text to a character array.
     * This method first decodes the Base64-encoded cipher text, decrypts it using
     * the provided private key, and then converts the decrypted byte array to a character array.
     * The decrypted byte array is cleared after conversion to ensure sensitive data is not exposed.
     *
     * @param privateKey the private key used for decryption
     * @param base64CipherText the Base64-encoded cipher text to be decrypted
     * @return a character array containing the decrypted data
     * @throws GeneralSecurityException if any decryption error occurs
     */
    public static char[] decryptHybridToChars(PrivateKey privateKey, String base64CipherText) throws GeneralSecurityException {
        byte[] cipherData = TextUtil.base64Decode(base64CipherText);
        byte[] decrypted = decryptHybrid(privateKey, cipherData);
        try {
            return new String(decrypted, StandardCharsets.UTF_8).toCharArray();
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
    }
}
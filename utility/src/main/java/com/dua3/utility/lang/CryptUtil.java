// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.utility.text.TextUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;

/**
 * Cryptographic utilities.
 * <p>
 * Code is based on an
 * <a href="https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9">article</a>
 * by Patrick Favre-Bulle.
 */
public final class CryptUtil {
    /**
     * Represents the constant value for the RSA asymmetric encryption algorithm.
     * RSA (Rivest-Shamir-Adleman) is one of the widely used algorithms for secure data transmission.
     */
    public static final String ASYMMETRIC_ALGORITHM_RSA = "RSA";
    /**
     * A constant representing the Elliptic Curve (EC) algorithm for asymmetric cryptographic operations.
     */
    public static final String ASYMMETRIC_ALGORITHM_EC = "EC";
    /**
     * A constant that represents the identifier for the ECIES asymmetric encryption algorithm.
     * ECIES stands for Elliptic Curve Integrated Encryption Scheme, a hybrid encryption
     * method used for secure communication. It combines elliptic curve cryptography for
     * confidentiality and additional mechanisms for integrity and authentication.
     */
    public static final String ASYMMETRIC_ALGORITHM_ECIES = "ECIES";
    /**
     * A constant that represents the "DSA" (Digital Signature Algorithm) asymmetric cryptographic algorithm.
     */
    public static final String ASYMMETRIC_ALGORITHM_DSA = "DSA";
    /**
     * A set of strings representing the supported asymmetric cryptographic algorithms.
     * These algorithms include RSA, EC (Elliptic Curve), ECIES (Elliptic Curve Integrated Encryption Scheme),
     * and DSA (Digital Signature Algorithm).
     */
    public static final Set<String> ASYMMETRIC_ALGORITHMS = Set.of(ASYMMETRIC_ALGORITHM_RSA, ASYMMETRIC_ALGORITHM_EC, ASYMMETRIC_ALGORITHM_ECIES, ASYMMETRIC_ALGORITHM_DSA);
    /**
     * A constant that defines the cryptographic transformation used for asymmetric encryption.
     * This transformation specifies the RSA algorithm in ECB mode with OAEP padding. The OAEP scheme
     * uses SHA-256 as the message digest algorithm and MGF1 (Mask Generation Function 1) for padding.
     */
    public static final String ASYMMETRIC_TRANSFORMATION_RSA_ECB_OAEPWITHSHA_256_ANDMGF_1_PADDING = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    /**
     * A constant representing the asymmetric transformation mode named "ECIES" (Elliptic Curve Integrated Encryption Scheme).
     * This encryption scheme combines public-key cryptography with symmetric encryption
     * to provide a secure and efficient method for data encryption and decryption.
     */
    public static final String ASYMMETRIC_TRANSFORMATION_ECIES = "ECIES";

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String ASYMMETRIC_ALGORITHM_DEFAULT = ASYMMETRIC_ALGORITHM_RSA;  // Better default for asymmetric
    private static final int KEY_DERIVATION_DEFAULT_ITERATIONS = 10000;
    private static final int KEY_DERIVATION_DEFAULT_BITS = 256;

    private static final SecureRandom RANDOM;

    static {
        try {
            RANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("could not get a secure random instance", e);
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
     *   <li>EC/ECIES: Returns ECIES (available in Java 21+ per JEP 452)</li>
     *   <li>DSA: Throws exception as DSA is for signatures only</li>
     * </ul>
     *
     * @param algorithm the name of the algorithm ("RSA", "EC", "ECIES")
     * @return the transformation string corresponding to the given algorithm
     * @throws IllegalArgumentException if DSA is provided or algorithm is unsupported
     */
    private static String getAsymmetricTransformation(String algorithm) {
        return switch (algorithm) {
            case ASYMMETRIC_ALGORITHM_RSA -> ASYMMETRIC_TRANSFORMATION_RSA_ECB_OAEPWITHSHA_256_ANDMGF_1_PADDING; // Secure padding
            case ASYMMETRIC_ALGORITHM_EC, ASYMMETRIC_ALGORITHM_ECIES -> ASYMMETRIC_TRANSFORMATION_ECIES;
            case ASYMMETRIC_ALGORITHM_DSA ->
                    throw new IllegalArgumentException("DSA is for signatures only, not encryption");
            default -> algorithm; // Fallback to algorithm name
        };
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
     */
    private static void validateAsymmetricEncryptionKey(PublicKey key, int dataLength) {
        validateAsymmetricAlgorithm(key.getAlgorithm());

        // RSA can only encrypt data smaller than key size minus padding
        if (key instanceof RSAKey rsaKey) {
            LangUtil.check(dataLength <= (rsaKey.getModulus().bitLength() / 8) - 66,
                    "Data too large for RSA encryption: %d bytes", dataLength);
        }
    }

    /**
     * Validates that the provided private key is suitable for asymmetric decryption.
     * This method ensures the key's algorithm is compatible with the supported asymmetric algorithms.
     *
     * @param key the private key to be validated; must not be null and must correspond to a valid asymmetric algorithm
     */
    private static void validateAsymmetricDecryptionKey(PrivateKey key) {
        validateAsymmetricAlgorithm(key.getAlgorithm());
    }

    /**
     * Validates the size of an asymmetric key based on the specified algorithm.
     * Ensures that the key size meets the minimum or required standards for security purposes.
     *
     * @param algorithm the name of the asymmetric algorithm (e.g., "RSA", "EC", "ECIES", "DSA").
     *                  Must be a supported algorithm; otherwise, an exception will be thrown.
     * @param keySize   the size of the key in bits, which will be checked against the requirements
     *                  specified for the given algorithm.
     *                  For RSA and DSA, the minimum is 2048 bits.
     *                  For EC, only specific sizes such as 256 (P-256), 384 (P-384), or 521 (P-521) bits are allowed.
     *                  If the key size does not satisfy the constraints for the selected algorithm,
     *                  an exception will be thrown.
     */
    private static void validateAsymmetricKeySize(String algorithm, int keySize) {
        switch (algorithm) {
            case ASYMMETRIC_ALGORITHM_RSA -> LangUtil.check(keySize >= 2048, "RSA key size must be at least 2048 bits, got %d", keySize);
            case ASYMMETRIC_ALGORITHM_EC, ASYMMETRIC_ALGORITHM_ECIES -> // EC/ECIES uses curve names, not just bit sizes
                    LangUtil.check(List.of(256, 384, 521).contains(keySize),
                            "EC key size must be 256 (P-256), 384 (P-384), or 521 (P-521) bits, got %d", keySize);
            case ASYMMETRIC_ALGORITHM_DSA -> LangUtil.check(keySize >= 2048, "DSA key size must be at least 2048 bits, got %d", keySize);
            default -> throw new IllegalArgumentException("unsupported asymmetric algorithm: " + algorithm);
        }
    }

    /**
     * Validates whether the provided algorithm is a supported asymmetric algorithm.
     * Supported algorithms are "RSA", "EC", and "DSA". If the input algorithm is not
     * among these, an exception is thrown.
     *
     * @param algorithm the name of the asymmetric algorithm to validate
     */
    private static void validateAsymmetricAlgorithm(String algorithm) {
        LangUtil.check(ASYMMETRIC_ALGORITHMS.contains(algorithm), "Unsupported asymmetric algorithm: %s", algorithm);
    }

    /**
     * Derive an encryption key from a passphrase using PBKDF2-SHA256.
     * <p>
     * <strong>Make sure to store the salt to be able to retrieve the generated key again later.</strong>
     *
     * @param passphrase the passphrase (cleared after use)
     * @param salt random salt (minimum 16 bytes)
     * @param iterations iteration count (minimum 10000)
     * @param keyBits key size in bits (128, 192, or 256)
     * @return derived encryption key
     * @throws GeneralSecurityException if key derivation fails
     */
    public static byte[] deriveKey(char[] passphrase, byte[] salt, int iterations, int keyBits)
            throws GeneralSecurityException {

        LangUtil.check(salt.length >= 16, "salt must be at least 16 bytes");
        LangUtil.check(iterations >= 10000, "iterations must be at least 10000");
        LangUtil.check(keyBits == 128 || keyBits == 192 || keyBits == 256,
                "key size must be 128, 192, or 256 bits");

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, iterations, keyBits);
            try {
                return factory.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword(); // Clear the spec
            }
        } finally {
            Arrays.fill(passphrase, '\0'); // Clear the passphrase
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
     * @return derived encryption key
     * @throws GeneralSecurityException if key derivation fails
     */
    public static byte[] deriveKey(char[] passphrase, CharSequence context)
            throws GeneralSecurityException {
        LangUtil.check(context.length() > 0, "context must not be null or empty");

        String saltInput = "app.salt:" + context; // Version prefix for future upgrades
        byte[] salt = TextUtil.getDigest("SHA-256", saltInput.getBytes(StandardCharsets.UTF_8));
        return deriveKey(passphrase, salt, KEY_DERIVATION_DEFAULT_ITERATIONS, KEY_DERIVATION_DEFAULT_BITS);
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
     * @return derived SecretKey for symmetric encryption
     * @throws GeneralSecurityException if key derivation fails
     */
    public static SecretKey deriveSecretKey(char[] passphrase, byte[] salt, int iterations, int keyBits)
            throws GeneralSecurityException {
        byte[] keyBytes = deriveKey(passphrase, salt, iterations, keyBits);
        try {
            return toSecretKey(keyBytes);
        } finally {
            // Clear the intermediate byte array
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
     * @return derived SecretKey for symmetric encryption
     * @throws GeneralSecurityException if key derivation fails
     */
    public static SecretKey deriveSecretKey(char[] passphrase, CharSequence context)
            throws GeneralSecurityException {
        byte[] keyBytes = deriveKey(passphrase, context);
        try {
            return toSecretKey(keyBytes);
        } finally {
            // Clear the intermediate byte array
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
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Generate key.
     *
     * @param bits the number of bits; must be a multiple of 8
     * @return the generated key
     */
    public static byte[] generateKey(int bits) {
        int nBytes = bits / 8;
        LangUtil.check(nBytes * 8 == bits, "the bit length of the key must be a multiple of 8");

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGen.init(bits);
            return keyGen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Symmetrically encrypt text.
     * <p>
     * The text is encrypted using AES and the resulting ciphertext is converted to
     * a String by applying the Base64 algorithm.
     *
     * @param key  the encryption key
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(byte[] key, CharSequence text) throws GeneralSecurityException {
        validateKeyLength(key);
        char[] textChars = TextUtil.toCharArray(text);
        try {
            return encrypt(key, textChars);
        } finally {
            Arrays.fill(textChars, '\0');
        }
    }

    /**
     * Symmetrically encrypt text using a Key object.
     *
     * @param key  the encryption key (must be AES SecretKey)
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(Key key, CharSequence text) throws GeneralSecurityException {
        validateSymmetricKey(key);
        char[] textChars = TextUtil.toCharArray(text);
        try {
            return encrypt(key, textChars);
        } finally {
            Arrays.fill(textChars, '\0');
        }
    }

    /**
     * Symmetrically encrypt text.
     * <p>
     * The text is encrypted using the AES algorithm, and the resulting ciphertext is converted to
     * a String by applying the Base64 algorithm.
     *
     * @param key  the encryption key
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(byte[] key, char[] text) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        try {
            byte[] cipherMessage = encrypt(key, data);
            return TextUtil.base64Encode(cipherMessage);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Symmetrically decrypt text.
     * <p>
     * The ciphertext is decrypted using AES after being decoded from Base64.
     *
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(byte[] key, String cipherText) throws GeneralSecurityException {
        char[] decryptedChars = decryptToChars(key, cipherText);
        try {
            return new String(decryptedChars);
        } finally {
            Arrays.fill(decryptedChars, '\0');
        }
    }

    /**
     * Symmetrically decrypt text to a char array.
     * <p>
     * The ciphertext is decrypted using AES after being decoded from Base64.
     * The caller is responsible for clearing the returned char array after use.
     *
     * @param key        the encryption key used
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(byte[] key, String cipherText) throws GeneralSecurityException {
        byte[] cipherMessage = TextUtil.base64Decode(cipherText);
        byte[] data = decrypt(key, cipherMessage);
        try {
            return TextUtil.bytesToChars(data);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Symmetrically encrypt data.
     * <p>
     * The data is encrypted using AES.
     *
     * @param key  the encryption key
     * @param data the data to encrypt
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encrypt(byte[] key, byte[] data) throws GeneralSecurityException {
        validateKeyLength(key);

        // use AES encryption
        Key secretKey = new SecretKeySpec(key, SYMMETRIC_ALGORITHM);

        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(CIPHER);
        AlgorithmParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + iv.length + cipherText.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return byteBuffer.array();
    }

    /**
     * Symmetrically encrypt data using a Key object.
     *
     * @param key  the encryption key (must be AES SecretKey)
     * @param data the data to encrypt
     * @return the encrypted message as byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encrypt(Key key, byte[] data) throws GeneralSecurityException {
        validateSymmetricKey(key);

        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(CIPHER);
        AlgorithmParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);  // Use Key directly

        byte[] cipherText = cipher.doFinal(data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + iv.length + cipherText.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return byteBuffer.array();
    }

    /**
     * Symmetrically decrypt data.
     * <p>
     * The data is decrypted using AES.
     *
     * @param key           the encryption key
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decrypt(byte[] key, byte[] cipherMessage) throws GeneralSecurityException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        LangUtil.check(cipherMessage.length >= Integer.BYTES, "cipher message too short");
        int ivLength = byteBuffer.getInt();
        LangUtil.check(ivLength == IV_LENGTH, "invalid iv length, expected %d", IV_LENGTH);

        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, SYMMETRIC_ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return cipher.doFinal(cipherText);
    }

    /**
     * Converts a given byte array into a {@code PrivateKey} instance using the specified algorithm.
     *
     * @param bytes the private key in encoded byte format
     * @param algorithm the name of the key algorithm (e.g., "RSA", "EC")
     * @return a {@code PrivateKey} created from the provided byte array and algorithm
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PrivateKey toPrivateKey(byte[] bytes, String algorithm) throws GeneralSecurityException {
        validateAsymmetricAlgorithm(algorithm);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
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
     * @param algorithm the algorithm used to generate the {@code PublicKey} (e.g., "RSA", "EC")
     * @return the generated {@code PublicKey}
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static PublicKey toPublicKey(byte[] bytes, String algorithm) throws GeneralSecurityException {
        validateAsymmetricAlgorithm(algorithm);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
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
     * @param algorithm the algorithm used for generating the key pair (e.g., RSA, DSA, EC)
     * @return a {@code KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes, String algorithm) throws GeneralSecurityException {
        return new KeyPair(toPublicKey(publicKeyBytes, algorithm), toPrivateKey(privateKeyBytes, algorithm));
    }

    /**
     * Converts the given byte arrays representing a public key and a private key
     * into a {@link KeyPair} using the specified algorithm.
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
     * Validates the length of a cryptographic key to ensure it adheres to the expected lengths.
     * The allowed key lengths are 128, 192, or 256 bits.
     *
     * @param key the byte array representing the cryptographic key whose length is to be validated.
     *            The length must translate to 128, 192, or 256 bits.
     * @throws IllegalArgumentException if the key length does not match one of the allowed values.
     */
    private static void validateKeyLength(byte[] key) {
        int bits = key.length * 8;
        LangUtil.check(bits == 128 || bits == 192 || bits == 256,
                "Invalid AES key length: %d bits. AES requires 128, 192, or 256 bits", bits);
    }

    private static void validateSymmetricKey(Key key) {
        LangUtil.check(SYMMETRIC_ALGORITHM.equals(key.getAlgorithm()),
                "Key algorithm must be %s, got %s", SYMMETRIC_ALGORITHM, key.getAlgorithm());

        // Additional validation for extractable keys
        if ("RAW".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            if (encoded != null) {
                validateKeyLength(encoded);
            }
        }
    }

    /**
     * Symmetrically decrypt text using a Key object.
     * <p>
     * The ciphertext is decrypted using AES after being decoded from Base64.
     *
     * @param key        the encryption key used (must be AES SecretKey)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message
     * @throws GeneralSecurityException if decryption fails
     */
    public static String decrypt(Key key, String cipherText) throws GeneralSecurityException {
        char[] decryptedChars = decryptToChars(key, cipherText);
        try {
            return new String(decryptedChars);
        } finally {
            Arrays.fill(decryptedChars, '\0');
        }
    }

    /**
     * Symmetrically decrypt text to a char array using a Key object.
     * <p>
     * The ciphertext is decrypted using AES after being decoded from Base64.
     * The caller is responsible for clearing the returned char array after use.
     *
     * @param key        the encryption key used (must be AES SecretKey)
     * @param cipherText the Base64 encoded encrypted ciphertext
     * @return the decrypted message as char array
     * @throws GeneralSecurityException if decryption fails
     */
    public static char[] decryptToChars(Key key, String cipherText) throws GeneralSecurityException {
        byte[] cipherMessage = TextUtil.base64Decode(cipherText);
        byte[] data = decrypt(key, cipherMessage);
        try {
            return TextUtil.bytesToChars(data);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Symmetrically decrypt data using a Key object.
     * <p>
     * The data is decrypted using AES.
     *
     * @param key           the encryption key (must be AES SecretKey)
     * @param cipherMessage the encrypted data
     * @return the decrypted message as a byte array
     * @throws GeneralSecurityException if decryption fails
     */
    public static byte[] decrypt(Key key, byte[] cipherMessage) throws GeneralSecurityException {
        validateSymmetricKey(key);

        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        LangUtil.check(cipherMessage.length >= Integer.BYTES, "cipher message too short");
        int ivLength = byteBuffer.getInt();
        LangUtil.check(ivLength == IV_LENGTH, "invalid iv length, expected %d", IV_LENGTH);

        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return cipher.doFinal(cipherText);
    }

    /**
     * Symmetrically encrypt text using a Key object.
     * <p>
     * The text is encrypted using the AES algorithm, and the resulting ciphertext is converted to
     * a String by applying the Base64 algorithm.
     *
     * @param key  the encryption key (must be AES SecretKey)
     * @param text the text to encrypt
     * @return the encrypted message as a Base64 encoded String
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(Key key, char[] text) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        try {
            byte[] cipherMessage = encrypt(key, data);
            return TextUtil.base64Encode(cipherMessage);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Create a SecretKey from a byte array.
     *
     * @param keyBytes the key bytes (must be 128, 192, or 256 bits)
     * @return SecretKey instance
     */
    public static SecretKey toSecretKey(byte[] keyBytes) {
        validateKeyLength(keyBytes);
        return new SecretKeySpec(keyBytes, SYMMETRIC_ALGORITHM);
    }

    /**
     * Generate SecretKey.
     *
     * @param bits the number of bits (128, 192, or 256)
     * @return the generated SecretKey
     * @throws GeneralSecurityException if key generation fails
     */
    public static SecretKey generateSecretKey(int bits) throws GeneralSecurityException {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGen.init(bits);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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

        String transformation = getAsymmetricTransformation(publicKey.getAlgorithm());

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
        validateAsymmetricDecryptionKey(privateKey);
        String transformation = getAsymmetricTransformation(privateKey.getAlgorithm());

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
        char[] textChars = TextUtil.toCharArray(text);
        try {
            return encryptAsymmetric(publicKey, textChars);
        } finally {
            Arrays.fill(textChars, '\0');
        }
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
        byte[] data = TextUtil.charsToBytes(text);
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
        char[] decryptedChars = decryptAsymmetricToChars(privateKey, cipherText);
        try {
            return new String(decryptedChars);
        } finally {
            Arrays.fill(decryptedChars, '\0');
        }
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
        byte[] data = decryptAsymmetric(privateKey, cipherData);
        try {
            return TextUtil.bytesToChars(data);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Generate an asymmetric key pair.
     *
     * @param algorithm the algorithm (RSA, EC, DSA)
     * @param keySize the key size in bits
     * @return the generated key pair
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize) {
        validateAsymmetricAlgorithm(algorithm);
        validateAsymmetricKeySize(algorithm, keySize);

        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance(algorithm);
            keyGen.initialize(keySize, RANDOM);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate an RSA key pair with default key size (2048 bits).
     *
     * @return the generated RSA key pair
     */
    public static KeyPair generateRSAKeyPair() {
        return generateKeyPair(ASYMMETRIC_ALGORITHM_RSA, 2048);
    }

    /**
     * Sign data using a private key.
     *
     * @param privateKey the private key for signing
     * @param data the data to sign
     * @return the digital signature
     * @throws GeneralSecurityException if signing fails
     */
    public static byte[] sign(PrivateKey privateKey, byte[] data) throws GeneralSecurityException {
        validateAsymmetricSigningKey(privateKey);
        String signatureAlgorithm = getSignatureAlgorithm(privateKey.getAlgorithm());

        java.security.Signature signature = java.security.Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey, RANDOM);
        signature.update(data);

        return signature.sign();
    }

    /**
     * Verify a digital signature.
     *
     * @param publicKey the public key for verification
     * @param data the original data that was signed
     * @param signature the digital signature to verify
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException if verification fails
     */
    public static boolean verify(PublicKey publicKey, byte[] data, byte[] signature) throws GeneralSecurityException {
        validateAsymmetricVerificationKey(publicKey);
        String signatureAlgorithm = getSignatureAlgorithm(publicKey.getAlgorithm());

        java.security.Signature sig = java.security.Signature.getInstance(signatureAlgorithm);
        sig.initVerify(publicKey);
        sig.update(data);

        return sig.verify(signature);
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
        char[] textChars = TextUtil.toCharArray(text);
        try {
            return sign(privateKey, textChars);
        } finally {
            Arrays.fill(textChars, '\0');
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
    public static String sign(PrivateKey privateKey, char[] text) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        try {
            byte[] signature = sign(privateKey, data);
            return TextUtil.base64Encode(signature);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
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
        char[] textChars = TextUtil.toCharArray(text);
        try {
            return verify(publicKey, textChars, signatureBase64);
        } finally {
            Arrays.fill(textChars, '\0');
        }
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
    public static boolean verify(PublicKey publicKey, char[] text, String signatureBase64) throws GeneralSecurityException {
        byte[] data = TextUtil.charsToBytes(text);
        try {
            byte[] signature = TextUtil.base64Decode(signatureBase64);
            return verify(publicKey, data, signature);
        } finally {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Get the appropriate signature algorithm for a given key algorithm.
     *
     * @param keyAlgorithm the key algorithm (RSA, EC, DSA)
     * @return the signature algorithm to use
     */
    private static String getSignatureAlgorithm(String keyAlgorithm) {
        return switch (keyAlgorithm) {
            case ASYMMETRIC_ALGORITHM_RSA -> "SHA256withRSA";
            case ASYMMETRIC_ALGORITHM_EC -> "SHA256withECDSA";
            case ASYMMETRIC_ALGORITHM_DSA -> "SHA256withDSA";
            default -> throw new IllegalArgumentException("Unsupported key algorithm for signing: " + keyAlgorithm);
        };
    }

    /**
     * Validate a private key for signing operations.
     *
     * @param privateKey the private key to validate
     */
    private static void validateAsymmetricSigningKey(PrivateKey privateKey) {
        validateAsymmetricAlgorithm(privateKey.getAlgorithm());
    }

    /**
     * Validate a public key for verification operations.
     *
     * @param publicKey the public key to validate
     */
    private static void validateAsymmetricVerificationKey(PublicKey publicKey) {
        validateAsymmetricAlgorithm(publicKey.getAlgorithm());
    }

    /**
     * Hybrid encryption for large data using RSA/EC for key encryption and AES for data encryption.
     * <p>
     * This method generates a random AES key, encrypts the data with AES-GCM, then encrypts
     * the AES key with the provided public key using asymmetric encryption. The result combines
     * both the encrypted AES key and the encrypted data in a single Base64-encoded string.
     * <p>
     * Format: [4 bytes: encrypted key length][encrypted AES key][encrypted data]
     *
     * @param publicKey the public key for encrypting the AES key (RSA, EC, or ECIES)
     * @param data the data to encrypt
     * @return the hybrid encrypted data as a byte array
     * @throws GeneralSecurityException if encryption fails
     */
    public static byte[] encryptHybrid(PublicKey publicKey, byte[] data) throws GeneralSecurityException {
        // Generate random AES-256 key
        SecretKey aesKey = generateSecretKey(256);

        try {
            // Encrypt data with AES
            byte[] encryptedData = encrypt(aesKey, data);

            // Encrypt AES key with public key
            byte[] encryptedKey = encryptAsymmetric(publicKey, aesKey.getEncoded());

            // Combine encrypted key and data
            // Format: [4 bytes: key length][encrypted key][encrypted data]
            ByteBuffer result = ByteBuffer.allocate(4 + encryptedKey.length + encryptedData.length);
            result.putInt(encryptedKey.length);
            result.put(encryptedKey);
            result.put(encryptedData);

            return result.array();
        } finally {
            // Clear the AES key from memory
            Arrays.fill(aesKey.getEncoded(), (byte) 0);
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
        // Validate curve name
        if (!Set.of("secp256r1", "secp384r1", "secp521r1").contains(curveName)) {
            throw new IllegalArgumentException("Unsupported curve: " + curveName +
                    ". Supported curves: secp256r1, secp384r1, secp521r1");
        }

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");

            // Use ECGenParameterSpec to specify the named curve
            java.security.spec.ECGenParameterSpec ecSpec =
                    new java.security.spec.ECGenParameterSpec(curveName);
            keyGen.initialize(ecSpec, RANDOM);

            return keyGen.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new GeneralSecurityException("Failed to generate EC key pair with curve " + curveName, e);
        }
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
            throw new GeneralSecurityException("Invalid hybrid cipher data: too short");
        }

        ByteBuffer buffer = ByteBuffer.wrap(cipherData);

        // Read encrypted key length
        int keyLength = buffer.getInt();
        if (keyLength <= 0 || keyLength > cipherData.length - 4) {
            throw new GeneralSecurityException("Invalid encrypted key length");
        }

        // Extract encrypted AES key
        byte[] encryptedKey = new byte[keyLength];
        buffer.get(encryptedKey);

        // Extract encrypted data
        byte[] encryptedData = new byte[buffer.remaining()];
        buffer.get(encryptedData);

        try {
            // Decrypt AES key
            byte[] aesKeyBytes = decryptAsymmetric(privateKey, encryptedKey);
            SecretKey aesKey = toSecretKey(aesKeyBytes);

            try {
                // Decrypt data with AES key
                return decrypt(aesKey, encryptedData);
            } finally {
                // Clear sensitive data
                Arrays.fill(aesKeyBytes, (byte) 0);
                Arrays.fill(aesKey.getEncoded(), (byte) 0);
            }
        } finally {
            // Clear temporary arrays
            Arrays.fill(encryptedKey, (byte) 0);
            Arrays.fill(encryptedData, (byte) 0);
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
        char[] data = TextUtil.toCharArray(text);
        try {
            byte[] encrypted = encryptHybrid(publicKey, TextUtil.charsToBytes(data));
            return TextUtil.base64Encode(encrypted);
        } finally {
            Arrays.fill(data, (char) 0);
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
        try {
            return new String(decrypted, StandardCharsets.UTF_8);
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
    }

    /**
     * Encrypts the provided text using a hybrid encryption mechanism that combines
     * public key encryption for secure key exchange and symmetric encryption for
     * encrypting the provided text. The input text is securely cleared after processing.
     *
     * @param publicKey the public key used for encrypting the symmetric key
     * @param text the text to be encrypted, provided as a char array
     * @return the encrypted text encoded in Base64 format
     * @throws GeneralSecurityException if any encryption-related error occurs
     */
    public static String encryptHybrid(PublicKey publicKey, char[] text) throws GeneralSecurityException {
        try {
            byte[] encrypted = encryptHybrid(publicKey, TextUtil.charsToBytes(text));
            return TextUtil.base64Encode(encrypted);
        } finally {
            Arrays.fill(text, (char) 0);
        }
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
            return TextUtil.bytesToChars(decrypted);
        } finally {
            Arrays.fill(decrypted, (byte) 0);
        }
    }
}

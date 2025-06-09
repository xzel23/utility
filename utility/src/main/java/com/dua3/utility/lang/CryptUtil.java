package com.dua3.utility.lang;

import com.dua3.utility.text.TextUtil;
import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;

/**
 * Cryptographic utilities.
 */
public final class CryptUtil {
    /**
     * Enum representing asymmetric encryption algorithms and their transformations.
     */
    public enum AsymmetricAlgorithm {
        /**
         * RSA (Rivest-Shamir-Adleman) algorithm with OAEP padding
         */
        RSA("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING", "SHA256withRSA", "RSA"),
        /**
         * Elliptic Curve Cryptography (primarily for signatures and key agreement)
         */
        EC(null, "SHA256withECDSA", "EC"),
        /**
         * Elliptic Curve Integrated Encryption Scheme (requires special provider)
         */
        ECIES("ECIES", null, "EC"),
        /**
         * Digital Signature Algorithm (for signatures only)
         */
        DSA(null, "SHA256withDSA", "DSA");

        private final @Nullable String transformation;
        private final @Nullable String signatureAlgorithm;
        private final String keyFactoryAlgorithm;

        AsymmetricAlgorithm(@Nullable String transformation, @Nullable String signatureAlgorithm, String keyFactoryAlgorithm) {
            this.transformation = transformation;
            this.signatureAlgorithm = signatureAlgorithm;
            this.keyFactoryAlgorithm = keyFactoryAlgorithm;
        }

        /**
         * Retrieves the name of the algorithm represented by this instance.
         *
         * @return the name of the algorithm as a string
         */
        public String algorithm() {
            return name();
        }

        /**
         * Retrieves the algorithm name to use for KeyFactory operations.
         * This may differ from the algorithm name for schemes like ECIES,
         * which use EC keys internally.
         *
         * @return the KeyFactory algorithm name
         */
        public String keyFactoryAlgorithm() {
            return keyFactoryAlgorithm;
        }

        /**
         * Retrieves the signature algorithm associated with this asymmetric algorithm, if available.
         *
         * @return an {@code Optional} containing the signature algorithm as a string, or an empty
         * {@code Optional} if no signature algorithm is defined.
         */
        public Optional<String> getSignatureAlgorithm() {
            return Optional.ofNullable(signatureAlgorithm);
        }

        /**
         * Retrieves the transformation string associated with the asymmetric algorithm.
         *
         * @return an {@code Optional} containing the transformation string, or empty if
         * this algorithm doesn't support direct encryption
         */
        public Optional<String> getTransformation() {
            return Optional.ofNullable(transformation);
        }

        /**
         * Checks if this algorithm supports direct asymmetric encryption.
         *
         * @return true if the algorithm supports direct encryption, false otherwise
         */
        public boolean isEncryptionSupported() {
            return transformation != null;
        }
    }

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String SYMMETRIC_CIPHER = "AES/GCM/NoPadding";
    private static final String SYMMETRIC_ALGORITHM = "AES";
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
     * @param algorithm the name of the algorithm ("RSA", "ECIES")
     * @return the transformation string corresponding to the given algorithm
     * @throws IllegalArgumentException if algorithm doesn't support direct encryption
     */
    private static String getAsymmetricTransformation(String algorithm) {
        return Arrays.stream(AsymmetricAlgorithm.values())
                .filter(a -> a.algorithm().equals(algorithm))
                .flatMap(a -> a.getTransformation().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Algorithm " + algorithm + " does not support direct asymmetric encryption. " +
                                "Use hybrid encryption instead, or use this algorithm for signatures only."));
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
    private static void validateAsymmetricEncryptionKey(PublicKey key, int dataLength, boolean isHybridEncryption) throws GeneralSecurityException {
        String algorithm = key.getAlgorithm();

        // Check if the algorithm supports direct encryption
        AsymmetricAlgorithm asymAlg = Arrays.stream(AsymmetricAlgorithm.values())
                .filter(a -> a.algorithm().equals(algorithm))
                .findFirst()
                .orElse(null);

        if (asymAlg == null || !asymAlg.isEncryptionSupported()) {
            String context = isHybridEncryption ? "hybrid encryption" : "direct asymmetric encryption";
            throw new InvalidKeyException(
                    "Algorithm " + algorithm + " does not support encryption and cannot be used for " + context + ". " +
                            "This algorithm is suitable for signatures only.");
        }

        // Algorithm-specific validation
        switch (algorithm) {
            case "RSA":
                validateRSAEncryptionKey(key, dataLength);
                break;
            case "ECIES":
                // ECIES validation would go here if needed
                break;
            default:
                // For other algorithms, basic validation or none
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
        if (!(key instanceof java.security.interfaces.RSAPublicKey rsaKey)) {
            throw new InvalidKeyException("Expected RSA public key, but got: " + key.getClass().getSimpleName());
        }

        int keySize = rsaKey.getModulus().bitLength();
        // For RSA with OAEP SHA-256: max_data = (key_size_bytes - 2 - 2*hash_size)
        int maxDataLength = (keySize / 8) - 2 - (2 * 32); // 32 bytes for SHA-256

        if (dataLength > maxDataLength) {
            throw new IllegalBlockSizeException(
                    String.format("Data length (%d bytes) exceeds maximum for %d-bit RSA key (%d bytes). " +
                                    "Use hybrid encryption for larger data.",
                            dataLength, keySize, maxDataLength));
        }
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
    private static void validateAsymmetricKeySize(AsymmetricAlgorithm algorithm, int keySize) {
        switch (algorithm) {
            case RSA -> LangUtil.check(keySize >= 2048, "RSA key size must be at least 2048 bits, got %d", keySize);
            case EC, ECIES -> // EC/ECIES uses curve names, not just bit sizes
                    LangUtil.check(List.of(256, 384, 521).contains(keySize),
                            "EC key size must be 256 (P-256), 384 (P-384), or 521 (P-521) bits, got %d", keySize);
            case DSA -> LangUtil.check(keySize >= 2048, "DSA key size must be at least 2048 bits, got %d", keySize);
            default -> throw new IllegalArgumentException("unsupported asymmetric algorithm: " + algorithm);
        }
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
        RandomHolder.RANDOM.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
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
        RandomHolder.RANDOM.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
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

        final Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
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
     * @param algorithm the algorithm used to generate the {@code PublicKey} (e.g., "RSA", "EC")
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
     * @param algorithm the algorithm used for generating the key pair (e.g., RSA, DSA, EC)
     * @return a {@code KeyPair} consisting of the public and private keys
     * @throws GeneralSecurityException if the key conversion fails
     */
    public static KeyPair toKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes, AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
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

        final Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER);
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
    public static KeyPair generateKeyPair(AsymmetricAlgorithm algorithm, int keySize) {
        validateAsymmetricKeySize(algorithm, keySize);

        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance(algorithm.algorithm());
            keyGen.initialize(keySize, RandomHolder.RANDOM);
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
        return generateKeyPair(AsymmetricAlgorithm.RSA, 2048);
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
        AsymmetricAlgorithm algorithm = AsymmetricAlgorithm.valueOf(privateKey.getAlgorithm());
        String signatureAlgorithm = algorithm.getSignatureAlgorithm()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported signature algorithm: " + algorithm));

        java.security.Signature signature = java.security.Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey, RandomHolder.RANDOM);
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
        AsymmetricAlgorithm algorithm = AsymmetricAlgorithm.valueOf(publicKey.getAlgorithm());
        String signatureAlgorithm = algorithm.getSignatureAlgorithm()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported signature algorithm: " + algorithm));

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
        // Generate random AES key
        SecretKey aesKey = generateSecretKey(256);

        // Validate that the public key can be used for encryption (with hybrid context)
        validateAsymmetricEncryptionKey(publicKey, aesKey.getEncoded().length, true);

        // Encrypt data with AES
        byte[] encryptedData = encrypt(aesKey, data);

        // Encrypt AES key with public key
        byte[] encryptedKey = encryptAsymmetric(publicKey, aesKey.getEncoded());

        // Combine: [key length][encrypted key][encrypted data]
        ByteBuffer result = ByteBuffer.allocate(4 + encryptedKey.length + encryptedData.length);
        result.putInt(encryptedKey.length);
        result.put(encryptedKey);
        result.put(encryptedData);

        return result.array();
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
        char[] chars = text.toString().toCharArray();
        try {
            return encryptHybrid(publicKey, chars);
        } finally {
            Arrays.fill(chars, '\0');
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
        byte[] data = new String(text).getBytes(StandardCharsets.UTF_8);
        try {
            byte[] encrypted = encryptHybrid(publicKey, data);
            return Base64.getEncoder().encodeToString(encrypted);
        } finally {
            Arrays.fill(data, (byte) 0);
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
            keyGen.initialize(ecSpec, RandomHolder.RANDOM);

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

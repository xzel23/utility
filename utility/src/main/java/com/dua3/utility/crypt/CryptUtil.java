package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Cryptographic utilities.
 */
public final class CryptUtil {

    private static final SymmetricAlgorithm SYMMETRIC_ALGORITHM_DEFAULT = SymmetricAlgorithm.AES;
    private static final int GCM_TAG_LENGTH = 128;
    public static final int ARGON2_MEMORY_MB = 64;
    public static final int ARGON2_ITERATIONS = 5;

    /**
     * Utility class private constructor.
     */
    private CryptUtil() { /* utility class */ }

    /**
     * Returns the asymmetric transformation string for the given algorithm.
     * <p>
     * Supported algorithms:
     * <ul>
     *   <li>RSA: Returns RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING for secure padding</li>
     *   <li>EC: Returns ECIES (requires special provider like Bouncy Castle)</li>
     *   <li>DSA: Throws exception as DSA is for signatures/key agreement only</li>
     * </ul>
     *
     * @param algorithm the asymmetric algorithm
     * @return the transformation string corresponding to the given algorithm
     * @throws InvalidKeyException if algorithm doesn't support direct encryption
     */
    private static String getAsymmetricTransformation(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        Optional<String> transformation = algorithm.getTransformation();
        if (!transformation.isPresent()) {
            throw new InvalidKeyException("Algorithm " + algorithm + " does not support direct encryption");
        }
        return transformation.get();
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
    public static void validateAsymmetricEncryptionKey(PublicKey key, int dataLength) throws GeneralSecurityException {
        try {
            AsymmetricAlgorithm algorithm = AsymmetricAlgorithm.valueOf(key.getAlgorithm());
            if (!algorithm.isEncryptionSupported()) {
                throw new InvalidKeyException(key.getAlgorithm() + " keys are for signatures only, not encryption");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidKeyException("Unsupported asymmetric algorithm: " + key.getAlgorithm(), e);
        }

        switch (key) {
            case RSAPublicKey rsaKey -> validateRSAEncryptionKey(rsaKey, dataLength);
            default -> {
                // For other supported algorithms like EC, no additional validation needed
            }
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
        if (!(key instanceof RSAPublicKey rsaKey)) {
            throw new InvalidKeyException("Expected RSA key, but got: " + key.getClass().getSimpleName());
        }

        int keySize = rsaKey.getModulus().bitLength();

        // OAEP padding overhead: 2 + 2*hLen where hLen is 32 for SHA-256
        int maxDataSize = (keySize / 8) - 2 - (2 * 32);

        if (dataLength > maxDataSize) {
            throw new IllegalBlockSizeException(
                    java.lang.String.format("Data too large for RSA key. Max size: %d bytes, actual: %d bytes",
                            maxDataSize, dataLength));
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

        AsymmetricAlgorithm algorithm = AsymmetricAlgorithm.valueOf(publicKey.getAlgorithm());
        String transformation = getAsymmetricTransformation(algorithm);

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
            KeyUtil.validateSymmetricKey(key, algorithm);

            Cipher cipher = Cipher.getInstance(algorithm.getTransformation());

            if (algorithm.requiresIv()) {
                byte[] iv = new byte[algorithm.getIvLength()];
                RandomUtil.getRandom().nextBytes(iv);

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
            if (inputBufferHandling != com.dua3.utility.crypt.InputBufferHandling.PRESERVE) {
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
        KeyUtil.validateSymmetricKey(key, algorithm);

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
            validateAsymmetricEncryptionKey(publicKey, 32); // 32 bytes = 256-bit AES key

            // Generate random AES key
            SecretKey aesKey = KeyUtil.generateSecretKey(256, SYMMETRIC_ALGORITHM_DEFAULT);

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
            if (inputBufferHandling != com.dua3.utility.crypt.InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
            }
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
        SecretKey aesKey = KeyUtil.toSecretKey(decryptAsymmetric(privateKey, encryptedKey));

        // Decrypt data with AES key
        return decryptSymmetric(SYMMETRIC_ALGORITHM_DEFAULT, aesKey, encryptedData);
    }

    /**
     * Computes an HMAC using the SHA-256 algorithm on the normalized form of the provided email.
     *
     * @param email the email address to be processed; it will be normalized before computing the HMAC
     * @param key the secret key to use for HMAC generation
     * @return the computed HMAC as a hexadecimal string
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not supported
     * @throws InvalidKeyException if the provided secret key is invalid
     */
    public static String hmacSha256(String email, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        String normalizedEmail = TextUtil.normalizeEmail(email);

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(key);

        byte[] hmacBytes = hmacSha256.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hmacBytes);
    }

    /**
     * Derives a 256-bit hash using the Argon2id algorithm based on the provided input, salt,
     * and secret key parameters.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the input data to be hashed
     * @param salt the cryptographic salt to use for the hashing process; must be 16 bytes
     * @param secretKey the secret key incorporated into the hash generation
     * @return a byte array containing the 256-bit Argon2id hash
     * @throws IllegalArgumentException if the provided salt is not 16 bytes long
     */
    public static byte[] getArgon2idBytes(byte[] input, byte[] salt, SecretKey secretKey) {
        return getArgon2idBytes(input, salt, secretKey.getEncoded());
    }

    /**
     * Generates a derived key using the Argon2id hashing algorithm.
     * This method combines the provided input, salt, and pepper to produce a secure byte array.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input The input byte array to be hashed.
     * @param salt The salt byte array to be used in the hashing process for added security.
     * @param pepper A string value used as an additional secret to strengthen the hash.
     * @return A byte array representing the securely hashed result using the Argon2id algorithm.
     */
    public static byte[] getArgon2idBytes(byte[] input, byte[] salt, String pepper) {
        return getArgon2idBytes(input, salt, pepper.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a 256-bit Argon2id hash based on the provided input, salt, and secret.
     * The salt must be exactly 16 bytes in length; otherwise, an exception is thrown.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input The input byte array to be hashed. Cannot be null.
     * @param salt The salt used in the hashing process. Must be exactly 16 bytes in length.
     * @param secret An optional secret used as additional input for the hash. Can be null.
     * @return A byte array representing the 256-bit Argon2id hash of the input.
     * @throws IllegalArgumentException If the salt is not 16 bytes in length.
     */
    public static byte[] getArgon2idBytes(byte[] input, byte[] salt, byte[] secret) {
        LangUtil.checkArg(salt.length == 16, "salt must be 16 bytes");

        BouncyCastle.ensureAvailable();

        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ARGON2_ITERATIONS)
                .withMemoryAsKB(ARGON2_MEMORY_MB * 1024) // 64 MB
                .withParallelism(1)
                .withSalt(salt)
                .withSecret(secret);

        Argon2Parameters params = builder.build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[32]; // 256-bit output
        generator.generateBytes(input, hash);

        return hash;
    }

    /**
     * Returns a string containing the Argon2id hash and the associated salt, encoded in Base64 format.
     * The hash is computed using the provided input data and secret key, with a randomly generated salt.
     * The salt and hash are concatenated with a "$" delimiter.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the input data to be hashed
     * @param secretKey the secret key incorporated into the Argon2id hash generation
     * @return a Base64-encoded string containing the salt and the Argon2id hash, separated by "$"
     */
    public static String getArgon2id(byte[] input, SecretKey secretKey) {
        byte[] salt = RandomUtil.generateRandomBytes(16);
        byte[] hash = getArgon2idBytes(input, salt, secretKey);
        return Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generates an Argon2id hash from the given input using a random salt and the provided pepper.
     * The resulting hash includes the Base64-encoded salt and hash separated by a "$" symbol.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input The input byte array to be hashed.
     * @param pepper A string used as an additional security parameter in the hash computation.
     * @return A string containing the Base64-encoded salt and hash, separated by a "$".
     */
    public static String getArgon2id(byte[] input, String pepper) {
        byte[] salt = RandomUtil.generateRandomBytes(16);
        byte[] hash = getArgon2idBytes(input, salt, pepper);
        return Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generates an Argon2id hash using the provided input and secret key.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input      the input string to be hashed
     * @param secretKey  the secret key used in the hashing process
     * @return           the generated Argon2id hash as a string
     */
    public static String getArgon2id(String input, SecretKey secretKey) {
        return getArgon2id(input.getBytes(StandardCharsets.UTF_8), secretKey);
    }

    /**
     * Generates an Argon2id hash of the given input string using the provided pepper.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the input string to be hashed
     * @param pepper the pepper value to be used alongside the input for hashing
     * @return the resulting Argon2id hash as a string
     */
    public static String getArgon2id(String input, String pepper) {
        return getArgon2id(input.getBytes(StandardCharsets.UTF_8), pepper);
    }

    /**
     * Verifies if the input data, when combined with the provided secret key and salt,
     * produces the same Argon2id hash as the one specified in the saltAndHash string.
     * <p>
     * The salt and the expected hash are extracted from the saltAndHash parameter, where
     * they are separated by the '$' delimiter. The method computes the actual Argon2id hash
     * using the provided input data, salt, and secret key, and compares it with the expected hash.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the input data to be hashed
     * @param secretKey the secret key used in the Argon2id hashing algorithm
     * @param saltAndHash a string containing the base64-encoded salt and hash, separated by '$'
     * @return true if the computed hash matches the expected hash stored in saltAndHash; false otherwise
     * @throws IllegalArgumentException if saltAndHash has an invalid format
     */
    public static boolean verifyArgon2id(byte[] input, SecretKey secretKey, String saltAndHash) {
        return verifyArgon2id(input, secretKey.getEncoded(), saltAndHash);
    }

    /**
     * Verifies the provided input against a given Argon2id hash and salt combination.
     * This method compares the actual Argon2id hash derived from the input, salt, and secret
     * with the expected hash to ensure integrity and authenticity.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input The input data to be verified.
     * @param secret A secret key used in the Argon2id hash derivation process.
     * @param saltAndHash A string containing the Base64-encoded salt and hash, delimited by a '$' character.
     *                     The format of the string must be "salt$hash".
     * @return true if the input data matches the expected hash when processed with the given salt and secret,
     *         false otherwise.
     * @throws IllegalArgumentException if the format of the saltAndHash string is invalid.
     */
    public static boolean verifyArgon2id(byte[] input, byte[] secret, String saltAndHash) {
        int splitAt = saltAndHash.indexOf('$');
        LangUtil.checkArg(splitAt > 0, "Invalid saltAndHash format");

        String saltBase64 = saltAndHash.substring(0, splitAt);
        String expectedHashBase64 = saltAndHash.substring(splitAt + 1);

        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] expectedHash = Base64.getDecoder().decode(expectedHashBase64);

        byte[] actualHash = getArgon2idBytes(input, salt, secret);

        return MessageDigest.isEqual(actualHash, expectedHash);
    }

    /**
     * Verifies an Argon2id hash by comparing the provided input, pepper, and the
     * combined salt and hash string.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the raw input data to be verified, typically a password, as a byte array
     * @param pepper the secret value added to the input for additional security, as a string
     * @param saltAndHash the concatenation of the salt and hashed result to be used for verification, as a string
     * @return true if the verification succeeds (i.e., the input matches the hash), false otherwise
     */
    public static boolean verifyArgon2id(byte[] input, String pepper, String saltAndHash) {
        return verifyArgon2id(input, pepper.getBytes(StandardCharsets.UTF_8), saltAndHash);
    }

    /**
     * Verifies whether the provided input matches the given Argon2id hash.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the plain text input to verify
     * @param secretKey the secret key used for the verification process
     * @param saltAndHash the combined salt and hash string to validate against
     * @return true if the input matches the provided hash, otherwise false
     */
    public static boolean verifyArgon2id(String input, SecretKey secretKey, String saltAndHash) {
        return verifyArgon2id(input.getBytes(StandardCharsets.UTF_8), secretKey, saltAndHash);
    }

    /**
     * Verifies if the input string, along with a secret, matches the provided Argon2id hash.
     * This method uses the Argon2id password hashing algorithm for verification.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the input string to be verified against the provided hash
     * @param secret a byte array representing the additional secret used for hashing
     * @param saltAndHash the salt and Argon2id hash to verify the input and secret against
     * @return true if the input and secret match the provided Argon2id hash, false otherwise
     */
    public static boolean verifyArgon2id(String input, byte[] secret, String saltAndHash) {
        return verifyArgon2id(input.getBytes(StandardCharsets.UTF_8), secret, saltAndHash);
    }

    /**
     * Verifies if the provided input, combined with a pepper, matches the given Argon2id-derived hash.
     * This method performs validation by combining the input and the pepper with the Argon2id parameters
     * embedded in the provided salt-and-hash string and checks if the input corresponds to the same hash.
     * <p>
     * <strong>Note:</strong> This method requires bouncycastle to be on the classpath.
     *
     * @param input the plain text input to verify
     * @param pepper the additional secret value used to harden the hashing process
     * @param saltAndHash the combined string of salt and the Argon2id hash to validate against
     * @return true if the provided input and pepper produce the same hash as saltAndHash; false otherwise
     */
    public static boolean verifyArgon2id(String input, String pepper, String saltAndHash) {
        return verifyArgon2id(input.getBytes(StandardCharsets.UTF_8), pepper, saltAndHash);
    }

}

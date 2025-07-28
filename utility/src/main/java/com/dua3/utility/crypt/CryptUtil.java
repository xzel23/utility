package com.dua3.utility.crypt;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Optional;

/**
 * Cryptographic utilities.
 */
public final class CryptUtil {

    private static final SymmetricAlgorithm SYMMETRIC_ALGORITHM_DEFAULT = SymmetricAlgorithm.AES;
    private static final int GCM_TAG_LENGTH = 128;

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
        String algorithm = key.getAlgorithm();

        switch (algorithm.toUpperCase()) {
            case "RSA":
                validateRSAEncryptionKey(key, dataLength);
                break;
            case "DSA":
                throw new InvalidKeyException("DSA keys are for signatures only, not encryption");
            default:
                // for EC the validity cannot be checked here
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
        if (!(key instanceof RSAPublicKey rsaKey)) {
            throw new InvalidKeyException("Expected RSA key, but got: " + key.getClass().getSimpleName());
        }

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
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
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
     * Derives a cryptographic key using the specified symmetric algorithm, salt, input data, and additional info.
     * The method utilizes the HKDF (HMAC-based Extract-and-Expand Key Derivation Function) with SHA-256 to
     * securely derive a key.
     *
     * @param algorithm the symmetric algorithm for which the key is being derived; it determines the key size
     *                  and the key algorithm (e.g., AES)
     * @param salt a non-secret random value used to ensure uniqueness of derived keys; must be at least 16 bytes
     * @param input the input keying material (IKM) used as a source of entropy for key derivation
     * @param info optional context and application-specific information used for domain separation during
     *             key derivation
     * @return a {@link SecretKey} instance containing the derived key that is compatible with the specified algorithm
     * @throws IllegalArgumentException if the provided salt is shorter than 16 bytes
     */
    public static SecretKey deriveKey(SymmetricAlgorithm algorithm, byte[] salt, byte[] input, byte[] info) {
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
    }

    /**
     * Derives a cryptographic key with a randomly generated salt using the specified
     * symmetric algorithm, input keying material, and additional context information.
     * This method securely generates a 256-bit random salt and uses it in conjunction
     * with the input parameters to derive the key.
     *
     * @param algorithm the symmetric algorithm for which the key is being derived;
     *                  it determines the key size and the key algorithm (e.g., AES)
     * @param input     the input keying material (IKM) used as a source of entropy for key derivation
     * @param info      optional context and application-specific information used for domain
     *                  separation during key derivation; can be null
     * @return a {@link SecretKey} instance containing the derived key that is
     *         compatible with the specified algorithm
     */
    public static SecretKey deriveKeyWithRandomSalt(SymmetricAlgorithm algorithm, byte[] input, byte[] info) {
        byte[] salt = new byte[32]; // 256-bit salt
        new SecureRandom().nextBytes(salt);
        return deriveKey(algorithm, salt, input, info);
    }
}
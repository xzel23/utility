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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * Cryptographic utilities.
 * <p>
 * Code is based on an
 * <a href="https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9">article</a>
 * by Patrick Favre-Bulle.
 */
public final class CryptUtil {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String KEY_GENERATION_ALGORITHM = "AES";
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

    private CryptUtil() {
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
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_GENERATION_ALGORITHM);
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
        char[] textChars = toCharArray(text);
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
        byte[] data = charsToBytes(text);
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
            return bytesToChars(data);
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
        // use AES encryption
        Key secretKey = new SecretKeySpec(key, KEY_GENERATION_ALGORITHM);

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
        LangUtil.check(ivLength == IV_LENGTH, "invalid iv length, expected " + IV_LENGTH);

        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_GENERATION_ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return cipher.doFinal(cipherText);
    }

    /**
     * Converts a {@code CharSequence} into a {@code char[]} array.
     * If the input is a {@code String}, its {@code toCharArray()} method is used directly.
     * Otherwise, the characters are extracted manually.
     * <p>
     * <strong>Security Note:</strong> This method avoids calling {@code toString()} on
     * non-String inputs, preventing potential string interning of sensitive data.
     * This allows complete cleanup of sensitive character data from memory.
     *
     * @param text the {@code CharSequence} to be converted to a {@code char[]} array
     * @return a {@code char[]} array representation of the input {@code CharSequence}
     */
    private static char[] toCharArray(CharSequence text) {
        if (text instanceof String s) {
            return s.toCharArray();
        }

        char[] textChars = new char[text.length()];
        for (int i = 0; i < text.length(); i++) {
            textChars[i] = text.charAt(i);
        }
        return textChars;
    }

    /**
     * Convert a char array to a byte array using UTF-8 encoding.
     *
     * @param chars the char array to convert
     * @return the byte array
     */
    private static byte[] charsToBytes(char[] chars) {
        return new String(chars).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert a byte array to a char array using UTF-8 encoding.
     *
     * @param bytes the byte array to convert
     * @return the char array
     */
    private static char[] bytesToChars(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).toCharArray();
    }
}
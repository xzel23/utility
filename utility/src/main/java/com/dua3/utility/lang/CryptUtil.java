// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.utility.text.TextUtil;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Cryptographic utilities.
 * <p>
 * Code is based on an article by Patrick Favre-Bulle.
 * https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
 */
public final class CryptUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptUtil() {
    }

    /**
     * Generate key.
     * @param bits number of bits, must be a multiple of 8
     * @return the generated key
     */
    public static byte @NotNull [] generateKey(int bits) {
        int nBytes = bits / 8;
        LangUtil.check(nBytes * 8 == bits, "bit length of key must be a multiple of 8");

        byte[] key = new byte[nBytes];
        RANDOM.nextBytes(key);

        return key;
    }

    /**
     * Symmetrically encrypt text.
     * <p>
     * The text is encrypted using AES and the resulting ciphertext is converted to
     * a String
     * by applying the Base64 algorithm.
     *
     * @param  text
     *                                  the text to encrypt
     * @param  key
     *                                  the encryption key
     * @return
     *                                  the encrypted message as a Base64 encoded
     *                                  String
     * @throws GeneralSecurityException
     *                                  if encryption fails
     */
    public static @NotNull String encrypt(@NotNull String text, byte @NotNull [] key) throws GeneralSecurityException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        byte[] cipherMessage = encrypt(key, data);
        return TextUtil.base64Encode(cipherMessage);
    }

    /**
     * Symmetrically decrypt text.
     * <p>
     * The text is encrypted using AES and the resulting ciphertext is converted to
     * a String
     * by applying the Base64 algorithm.
     *
     * @param  cipherText
     *                                  the Base64 encoded encrypted ciphertext
     * @param  key
     *                                  the encryption key used
     * @return
     *                                  the decrypted message
     * @throws GeneralSecurityException
     *                                  if decryption fails
     */
    public static @NotNull String decrypt(@NotNull String cipherText, byte @NotNull [] key) throws GeneralSecurityException {
        byte[] cipherMessage = TextUtil.base64Decode(cipherText);
        byte[] data = decrypt(key, cipherMessage);
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Symmetrically encrypt data.
     * <p>
     * The text is encrypted using AES.
     *
     * @param  data
     *                                  the data to encrypt
     * @param  key
     *                                  the encryption key
     * @return
     *                                  the encrypted message as a Base64 encoded
     *                                  String
     * @throws GeneralSecurityException
     *                                  if encryption fails
     */
    public static byte @NotNull [] encrypt(byte @NotNull [] key, byte @NotNull [] data) throws GeneralSecurityException {
        // use AES encryption
        Key secretKey = new SecretKeySpec(key, "AES");

        byte[] iv = new byte[12];
        RANDOM.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        AlgorithmParameterSpec parameterSpec = new GCMParameterSpec(128, iv); // 128 bit auth tag length
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return byteBuffer.array();
    }

    /**
     * Symmetrically decrypt data.
     * <p>
     * The text is decrypted using AES.
     *
     * @param  cipherMessage
     *                                  the encrypted data
     * @param  key
     *                                  the encryption key
     * @return
     *                                  the encrypted message as a Base64 encoded
     *                                  String
     * @throws GeneralSecurityException
     *                                  if encryption fails
     */
    public static byte @NotNull [] decrypt(byte @NotNull [] key, byte @NotNull [] cipherMessage) throws GeneralSecurityException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        int ivLength = byteBuffer.getInt();
        LangUtil.check(ivLength >= 12 && ivLength < 16, "invalid iv length");

        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));

        return cipher.doFinal(cipherText);
    }
}

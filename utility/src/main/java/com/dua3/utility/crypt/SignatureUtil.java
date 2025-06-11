package com.dua3.utility.crypt;

import com.dua3.utility.text.TextUtil;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Utility class for digital signatures.
 */
public final class SignatureUtil {

    /**
     * Utility class private constructor.
     */
    private SignatureUtil() { /* utility class */ }

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
                    .orElseThrow(() -> new NoSuchElementException("Algorithm " + asymmAlg + " does not support signing"));

            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey, RandomUtil.getRandom());
            signature.update(data);
            return signature.sign();
        } finally {
            if (inputBufferHandling != InputBufferHandling.PRESERVE) {
                Arrays.fill(data, (byte) 0);
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
        if (inputBufferHandling != InputBufferHandling.PRESERVE) {
            Arrays.fill(text, (char) 0);
        }
        return sign(privateKey, data, InputBufferHandling.CLEAR_AFTER_USE);
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
                    .orElseThrow(() -> new NoSuchElementException("Algorithm " + asymmAlg + " does not support verification"));

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
}

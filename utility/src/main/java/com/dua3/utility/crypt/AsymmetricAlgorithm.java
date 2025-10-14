package com.dua3.utility.crypt;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Enum representing asymmetric encryption algorithms and their transformations.
 */
public enum AsymmetricAlgorithm {
    /**
     * RSA (Rivest-Shamir-Adleman) algorithm with OAEP padding
     */
    RSA(
            "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING",
            "SHA256withRSA",
            "RSA",
            2048,
            512, 1024, 2048, 4096
    ),
    /**
     * Elliptic Curve Cryptography, requires a special provider for encryption (not signatures and key agreement)
     */
    EC(
            "ECIES",
            "SHA256withECDSA",
            "EC",
            384,
            256, 384, 521
    ),
    /**
     * Digital Signature Algorithm (for signatures only)
     */
    DSA(
            null,
            "SHA256withDSA",
            "DSA",
            2048,
            1024, 2048, 3072
    );

    private final @Nullable String transformation;
    private final @Nullable String signatureAlgorithm;
    private final String keyFactoryAlgorithm;
    private final int defaultKeySize;
    private final List<Integer> supportedKySizes;

    AsymmetricAlgorithm(@Nullable String transformation, @Nullable String signatureAlgorithm, String keyFactoryAlgorithm, int defaultKeySize, Integer... supportedKySizes) {
        this.transformation = transformation;
        this.signatureAlgorithm = signatureAlgorithm;
        this.keyFactoryAlgorithm = keyFactoryAlgorithm;
        this.defaultKeySize = defaultKeySize;
        this.supportedKySizes = List.of(supportedKySizes);
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

    /**
     * Retrieves the supported key sizes for the asymmetric algorithm.
     *
     * @return a list of integers representing the supported key sizes
     */
    public List<Integer> getSupportedKeySizes() {
        return supportedKySizes;
    }

    /**
     * Retrieves the default key size for the asymmetric algorithm.
     * <p>
     * <strong>Note:</strong> Using lower values is possible, but not recommended.
     *
     * @return the default key size as an integer
     */
    public int getDefaultKeySize() {
        return defaultKeySize;
    }
}

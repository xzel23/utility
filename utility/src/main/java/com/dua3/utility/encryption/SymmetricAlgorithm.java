package com.dua3.utility.encryption;

/**
 * Enum representing symmetric encryption algorithms.
 */
public enum SymmetricAlgorithm {
    /**
     * Advanced Encryption Standard with Galois/Counter Mode (AES-GCM)
     * Provides both encryption and authentication with a 256-bit key by default
     */
    AES("AES/GCM/NoPadding", "AES", 256, 12);

    private final String transformation;
    private final String keyAlgorithm;
    private final int defaultKeySize;
    private final int ivLength;

    SymmetricAlgorithm(String transformation, String keyAlgorithm, int defaultKeySize, int ivLength) {
        this.transformation = transformation;
        this.keyAlgorithm = keyAlgorithm;
        this.defaultKeySize = defaultKeySize;
        this.ivLength = ivLength;
    }

    /**
     * Retrieves the transformation string for use with Cipher.getInstance().
     *
     * @return the transformation string (algorithm/mode/padding)
     */
    public String getTransformation() {
        return transformation;
    }

    /**
     * Retrieves the key algorithm name for use with KeyGenerator or SecretKeySpec.
     *
     * @return the key algorithm name
     */
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * Retrieves the default key size in bits for this algorithm.
     *
     * @return the default key size in bits
     */
    public int getDefaultKeySize() {
        return defaultKeySize;
    }

    /**
     * Retrieves the initialization vector (IV) length in bytes.
     *
     * @return the IV length in bytes
     */
    public int getIvLength() {
        return ivLength;
    }

    /**
     * Checks if this algorithm requires an initialization vector (IV).
     *
     * @return true if an IV is required, false otherwise
     */
    public boolean requiresIv() {
        return ivLength > 0;
    }

    /**
     * Checks if this algorithm provides authenticated encryption.
     * Authenticated encryption algorithms provide both confidentiality and authenticity.
     *
     * @return true if the algorithm provides authenticated encryption, false otherwise
     */
    public boolean isAuthenticated() {
        return this == AES; // AES-GCM is authenticated
    }

    /**
     * Validates that the given key size is valid for this algorithm.
     *
     * @param keySize the key size in bits to validate
     * @throws IllegalArgumentException if the key size is not valid for this algorithm
     */
    public void validateKeySize(int keySize) {
        if (this == AES) {
            if (keySize != 128 && keySize != 192 && keySize != 256) {
                throw new IllegalArgumentException("AES key size must be 128, 192, or 256 bits, but was: " + keySize);
            }
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + this);
        }
    }

    /**
     * Returns the algorithm name for this symmetric algorithm.
     *
     * @return the algorithm name
     */
    public String algorithm() {
        return name();
    }
}

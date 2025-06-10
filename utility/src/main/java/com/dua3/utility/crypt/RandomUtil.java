package com.dua3.utility.crypt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for secure random number generation.
 */
public final class RandomUtil {

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
    private RandomUtil() { /* nothing to do */ }

    /**
     * Get the secure random instance.
     *
     * @return the secure random instance
     */
    public static SecureRandom getRandom() {
        return RandomHolder.RANDOM;
    }

    /**
     * Generate random bytes.
     *
     * @param length the number of bytes to generate
     * @return a byte array filled with random bytes
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        RandomHolder.RANDOM.nextBytes(bytes);
        return bytes;
    }
}
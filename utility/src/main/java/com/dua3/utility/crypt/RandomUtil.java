package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;

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
    private RandomUtil() { /* utility class */ }

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

    /**
     * Generates the next pseudo-random integer using a secure random number generator.
     *
     * @return a pseudo-randomly generated integer
     */
    public static int nextInt() {
        return RandomHolder.RANDOM.nextInt();
    }

    /**
     * Generates a random integer within the specified range using a secure random number generator.
     *
     * @param min the inclusive lower bound of the random number range
     * @param max the exclusive upper bound of the random number range
     * @return a pseudo-randomly generated integer within the range [min, max)
     * @throws IllegalArgumentException if min >= max
     */
    public static int nextInt(int min, int max) {
        LangUtil.checkArg(min < max, "min must be < max");
        return min + RandomHolder.RANDOM.nextInt(max - min);
    }
}
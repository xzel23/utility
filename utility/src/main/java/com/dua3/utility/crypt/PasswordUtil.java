package com.dua3.utility.crypt;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for password analysis and generation.
 * <p>
 * Provides a set of methods for evaluating password strength, calculating its characteristics,
 * identifying potential weaknesses, and generating secure random passwords.
 */
public final class PasswordUtil {

    private static final List<String> COMMON_PATTERNS;

    static {
        List<String> patterns = new ArrayList<>(LangUtil.asUnmodifiableList(
                "password", "1234", "qwert", "abc", "admin",
                "user", "login", "welcome", "hello", "test",
                "1q2w3e", "asdf", "yxcv"
        ));

        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 100; i++) {
            patterns.add(Integer.toString(currentYear - 90 + i));
        }

        COMMON_PATTERNS = List.copyOf(patterns);
    }

    private static final String TOO_SHORT = "Password is too short (minimum 8 characters recommended)";
    private static final String TOO_SIMPLE = "Password should be longer or more complex";
    private static final String TOO_MANY_REPEATED_CHARACTERS = "Contains too many repeated characters";
    private static final String SEQUENTIAL_CHARACTERS = "Contains sequential character patterns";
    private static final String COMMON_PASSWORD_PATTERNS = "Contains common password patterns";
    private static final String PASSWORD_PATTERNS_WITH_REPLACED_CHARACTERS = "Contains common password patterns with replaced characters";
    private static final String LOW_CHARACTER_DIVERSITY = "Low character diversity";
    private static final String PREDICTABLE_PATTERNS = "Password contains predictable patterns";

    private PasswordUtil() { /* utility class constructor */ }

    /**
     * Represents the strength level of a password.
     */
    public enum StrengthLevel {
        /**
         * Represents a password strength level categorized as "Very Weak."
         * <p>
         * This is the lowest strength level for a password, often indicating
         * minimal security or poor password practices.
         */
        VERY_WEAK("Very Weak", 0),
        /**
         * Represents a password strength level classified as "Weak".
         * <p>
         * Indicates that the password has a low level of strength and is relatively easy to compromise.
         */
        WEAK("Weak", 1),
        /**
         * Represents the moderate strength level of a password.
         */
        MODERATE("Moderate", 2),
        /**
         * Represents a strong level of password strength.
         */
        STRONG("Strong", 3),
        /**
         * Indicates the highest level of password strength, labeled as "Very Strong".
         */
        VERY_STRONG("Very Strong", 4);

        private final String description;
        private final int level;

        StrengthLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }

        public String getDescription() {return description;}

        public int getLevel() {return level;}

        @Override
        public String toString() {return description;}
    }

    /**
     * Contains password strength analysis results.
     *
     * @param shannonEntropy     the Shannon entropy in bits (measures randomness)
     * @param theoreticalEntropy the theoretical maximum entropy based on character space
     * @param strengthLevel      the overall strength level
     * @param issues             list of identified issues with the password
     */
    public record PasswordStrength(
            double shannonEntropy,
            double theoreticalEntropy,
            StrengthLevel strengthLevel,
            List<String> issues
    ) {
        @SuppressWarnings("MissingJavadoc")
        public PasswordStrength {
            issues = List.copyOf(issues);
        }

        /**
         * @return true if the password is considered secure (Strong or Very Strong)
         */
        public boolean isSecure() {
            return strengthLevel.getLevel() >= StrengthLevel.STRONG.getLevel();
        }

        /**
         * Calculates the entropy efficiency of the password as a percentage.
         * This measures how well the password uses its available character space.
         * Lower efficiency indicates patterns, repetition, or predictable structure.
         *
         * @return the entropy efficiency percentage (0-100), or 0 if theoretical entropy is zero
         */
        public double getEntropyEfficiency() {
            return theoreticalEntropy > 0 ? (shannonEntropy / theoreticalEntropy) * 100 : 0;
        }

        /**
         * Gets the estimated time to crack this password using a modern attack.
         * This is a rough estimate based on theoretical entropy and assumes
         * an attacker can try 10^12 combinations per second.
         *
         * @return a human-readable string describing the estimated crack time
         */
        public String getEstimatedCrackTime() {
            // Assume attacker can try 10^12 combinations per second (modern hardware)
            double combinations = Math.pow(2, theoreticalEntropy);
            double secondsToHalfCrack = combinations / (2 * 1.0e12); // Average time is half the keyspace

            if (secondsToHalfCrack < 1) {
                return "Less than 1 second";
            } else if (secondsToHalfCrack < 60) {
                return String.format("%.0f seconds", secondsToHalfCrack);
            } else if (secondsToHalfCrack < 3600) {
                return String.format("%.0f minutes", secondsToHalfCrack / 60);
            } else if (secondsToHalfCrack < 86400) {
                return String.format("%.0f hours", secondsToHalfCrack / 3600);
            } else if (secondsToHalfCrack < 31536000) {
                return String.format("%.0f days", secondsToHalfCrack / 86400);
            } else if (secondsToHalfCrack < 31536000000L) {
                return String.format("%.0f years", secondsToHalfCrack / 31536000);
            } else {
                return "Many centuries";
            }
        }

        @Override
        public String toString() {
            return String.format("Password Strength: %s (Shannon: %.1f bits, Theoretical: %.1f bits, Efficiency: %.1f%%, Est. crack time: %s)",
                    strengthLevel, shannonEntropy, theoreticalEntropy, getEntropyEfficiency(), getEstimatedCrackTime());
        }
    }

    /**
     * Evaluates the entropy and strength of a password.
     * <p>
     * This method calculates the Shannon entropy of the password and provides
     * a comprehensive strength assessment based on multiple factors including
     * character set diversity, length, and common patterns.
     * </p>
     *
     * @param password the password to evaluate
     * @return a {@link PasswordStrength} object containing entropy and strength information
     */
    public static PasswordStrength evaluatePasswordStrength(char[] password) {
        if (password.length == 0) {
            return new PasswordStrength(0.0, 0, StrengthLevel.VERY_WEAK,
                    Collections.singletonList("Password is empty"));
        }

        // Calculate Shannon entropy
        double entropy = calculateShannonEntropy(password);

        // Calculate theoretical entropy based on character space
        int characterSpace = calculateCharacterSpace(password);
        double theoreticalEntropy = password.length * Math.log(characterSpace) / Math.log(2);

        // Analyze password characteristics
        List<String> issues = new ArrayList<>();
        StrengthLevel strength = assessStrengthLevel(password, entropy, theoreticalEntropy, issues);

        return new PasswordStrength(entropy, theoreticalEntropy, strength, issues);
    }

    /**
     * Calculates the Shannon entropy of a password.
     * <p>
     * Shannon entropy measures the average amount of information contained in each character.
     * Higher entropy indicates more randomness and unpredictability.
     * </p>
     *
     * @param password the password to analyze
     * @return the Shannon entropy in bits
     */
    private static double calculateShannonEntropy(char[] password) {
        if (password.length == 0) {
            return 0.0;
        }

        // Count frequency of each character
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : password) {
            frequencies.merge(c, 1, Integer::sum);
        }

        // Calculate entropy using Shannon's formula: H = -Σ(p * log2(p))
        double entropy = 0.0;
        int length = password.length;

        for (Integer frequency : frequencies.values()) {
            double probability = (double) frequency / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    /**
     * Calculates the character space (alphabet size) used in the password.
     *
     * @param password the password to analyze
     * @return the size of the character space
     */
    private static int calculateCharacterSpace(char[] password) {
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigits = false;
        boolean hasSpecialChars = false;
        boolean hasExtendedChars = false;

        for (char c : password) {
            if (c >= 'a' && c <= 'z') {
                hasLowercase = true;
            } else if (c >= 'A' && c <= 'Z') {
                hasUppercase = true;
            } else if (c >= '0' && c <= '9') {
                hasDigits = true;
            } else if (c >= 32 && c <= 126) {
                hasSpecialChars = true;
            } else {
                hasExtendedChars = true;
            }
        }

        int space = 0;
        if (hasLowercase) space += 26;
        if (hasUppercase) space += 26;
        if (hasDigits) space += 10;
        if (hasSpecialChars) space += 33; // Printable ASCII special characters
        if (hasExtendedChars) space += 95; // Extended character set approximation

        return Math.max(space, 1);
    }

    /**
     * Assesses the overall strength level of the password.
     *
     * @param password           the password to assess
     * @param entropy            the calculated Shannon entropy
     * @param theoreticalEntropy the theoretical maximum entropy
     * @param issues             list to collect identified issues
     * @return the strength level
     */
    private static StrengthLevel assessStrengthLevel(
            char[] password,
            double entropy,
            double theoreticalEntropy,
            List<String> issues
    ) {
        String passwordStr = new String(password); // For pattern analysis only

        // Check for common weaknesses
        if (password.length < 8) {
            issues.add(TOO_SHORT);
        }

        if (password.length < 12 && theoreticalEntropy < 50) {
            issues.add(TOO_SIMPLE);
        }

        // Check for repeated characters
        if (hasExcessiveRepeats(password)) {
            issues.add(TOO_MANY_REPEATED_CHARACTERS);
        }

        // Check for sequential patterns
        if (hasSequentialPattern(password)) {
            issues.add(SEQUENTIAL_CHARACTERS);
        }

        // Check for common patterns
        if (hasCommonPatterns(passwordStr)) {
            issues.add(COMMON_PASSWORD_PATTERNS);
        }

        // Check for derived common patterns
        if (hasDerivedCommonPatterns(passwordStr)) {
            issues.add(PASSWORD_PATTERNS_WITH_REPLACED_CHARACTERS);
        }

        // Check character diversity
        int uniqueChars = calculateUniqueCharacters(password);
        if (uniqueChars < password.length * 0.6) {
            issues.add(LOW_CHARACTER_DIVERSITY);
        }

        // Use theoretical entropy (NIST approach) for strength assessment
        // but also consider entropy efficiency (Shannon/Theoretical ratio)
        double entropyEfficiency = theoreticalEntropy > 0 ? (entropy / theoreticalEntropy) * 100 : 0;

        // Penalize passwords with very low entropy efficiency (indicates patterns/repetition)
        if (entropyEfficiency < 50 && !issues.isEmpty()) {
            issues.add(PREDICTABLE_PATTERNS);
        }

        // Calculate base strength level from theoretical entropy and length
        StrengthLevel baseStrength;
        if (theoreticalEntropy < 28 || password.length < 8) {
            baseStrength = StrengthLevel.VERY_WEAK;
        } else if (theoreticalEntropy < 36 || password.length < 10) {
            baseStrength = StrengthLevel.WEAK;
        } else if (theoreticalEntropy < 50 || password.length < 12) {
            baseStrength = StrengthLevel.MODERATE;
        } else if (theoreticalEntropy < 64 || password.length < 14) {
            baseStrength = StrengthLevel.STRONG;
        } else {
            baseStrength = StrengthLevel.VERY_STRONG;
        }

        // Apply penalties based on issues found
        int severePenalty = 0;
        int moderatePenalty = 0;

        for (String issue : issues) {
            if (isSevereIssue(issue)) {
                severePenalty++;
            } else {
                moderatePenalty++;
            }
        }

        // Calculate final strength level considering penalties
        int finalLevel = baseStrength.getLevel();

        // Severe issues cause significant downgrade
        finalLevel -= severePenalty;

        // Moderate issues cause lesser downgrade
        finalLevel -= (moderatePenalty + 1) / 4; // Round up division

        // Ensure we don't go below VERY_WEAK
        finalLevel = Math.max(finalLevel, StrengthLevel.VERY_WEAK.getLevel());

        // Convert back to enum
        return switch (finalLevel) {
            case 0 -> StrengthLevel.VERY_WEAK;
            case 1 -> StrengthLevel.WEAK;
            case 2 -> StrengthLevel.MODERATE;
            case 3 -> StrengthLevel.STRONG;
            default -> StrengthLevel.VERY_STRONG;
        };
    }

    /**
     * Determines if an issue is considered severe and should cause significant penalty.
     *
     * @param issue the issue description
     * @return true if the issue is severe
     */
    private static boolean isSevereIssue(String issue) {
        return issue.contains(COMMON_PASSWORD_PATTERNS) ||
                issue.contains(SEQUENTIAL_CHARACTERS) ||
                issue.contains(TOO_MANY_REPEATED_CHARACTERS) ||
                issue.contains(TOO_SHORT);
    }

    /**
     * Checks for excessive character repetition.
     */
    private static boolean hasExcessiveRepeats(char[] password) {
        if (password.length < 3) return false;

        Map<Character, Integer> counts = new HashMap<>();
        for (char c : password) {
            counts.merge(c, 1, Integer::sum);
        }

        // Flag if any character appears more than 30% of the time
        int threshold = Math.max(2, password.length / 3);
        return counts.values().stream().anyMatch(count -> count > threshold);
    }

    /**
     * Checks for sequential character patterns (abc, 123, etc.).
     */
    private static boolean hasSequentialPattern(char[] password) {
        for (int i = 0; i <= password.length - 3; i++) {
            char c1 = password[i];
            char c2 = password[i + 1];
            char c3 = password[i + 2];

            // Check for ascending or descending sequences
            if ((c2 == c1 + 1 && c3 == c2 + 1) ||
                    (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the provided password contains any common patterns.
     * This method evaluates both the raw and normalized forms of the password
     * (e.g., replacing characters like "0" with "o" or "1" with "i") to identify
     * common patterns that might reduce password strength.
     *
     * @param password the password to be checked for common patterns
     * @return true if the password contains any common patterns, otherwise false
     */
    private static boolean hasCommonPatterns(String password) {
        String lower = password.toLowerCase(Locale.ROOT);

        for (String pattern : COMMON_PATTERNS) {
            if (lower.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the provided password contains any derived common patterns.
     * This method normalizes the password by replacing specific characters (e.g.,
     * "0" is replaced with "o", "1" is replaced with "i", "4" and "@" are replaced
     * with "a") to identify derived forms of common patterns. If the normalized
     * password matches any known common patterns, the method returns true.
     *
     * @param password the password to be checked for derived common patterns
     * @return true if the password contains any derived common patterns, otherwise false
     */
    private static boolean hasDerivedCommonPatterns(String password) {
        String normalized = password.toLowerCase(Locale.ROOT)
                .replace("0", "o")
                .replace("1", "i")
                .replace("3", "e")
                .replace("4", "a")
                .replace("@", "a");

        for (String pattern : COMMON_PATTERNS) {
            if (normalized.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the number of unique characters in the password.
     */
    private static int calculateUniqueCharacters(char[] password) {
        Set<Character> unique = new HashSet<>();
        for (char c : password) {
            unique.add(c);
        }
        return unique.size();
    }

    /**
     * Generates a random password encoded in Base64 format.
     * The password is created using 18 random bytes, which do not require additional padding.
     *
     * @return a Base64-encoded string representing the randomly generated password
     */
    public static String generatePassword() {
        // 18 bytes do not require padding
        return generatePassword(18);
    }

    /**
     * Generates a random password of specified length and encodes it in Base64 format.
     * The password is created using securely generated random bytes.
     *
     * @param length the length of the password to be generated. Must be greater than 8.
     * @return a Base64-encoded string representing the randomly generated password.
     * @throws IllegalArgumentException if the specified length is less than or equal to 8.
     */
    public static String generatePassword(int length) {
        LangUtil.checkArg(length >= 8, "length must be at least than 8");
        String password;
        for (int i = 0; i < 10; i++) {
            password = TextUtil.base64Encode(RandomUtil.generateRandomBytes(length));
            if (evaluatePasswordStrength(password.toCharArray()).isSecure()) {
                return password;
            }
        }
        throw new IllegalStateException("Could not generate a secure password");
    }
}

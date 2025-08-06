// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void testGeneratePasswordValidLength() {
        // Generate a password
        char[] password = PasswordUtil.generatePassword();

        // Verify that the password length matches the expected size
        assertNotNull(password, "Password should not be null");
        assertEquals(24, password.length, "Password length should be 24 characters (Base64 encoding of 18 bytes)");
    }

    @Test
    void testGeneratePasswordUniqueValues() {
        // Generate multiple passwords
        char[] password1 = PasswordUtil.generatePassword();
        char[] password2 = PasswordUtil.generatePassword();

        // Verify that the passwords are unique
        assertNotNull(password1, "Password1 should not be null");
        assertNotNull(password2, "Password2 should not be null");
        assertFalse(Arrays.equals(password1, password2), "Generated passwords should be unique");
    }

    @Test
    void testGeneratePasswordWithLength() {
        // Test with minimum valid length
        int length = 9;
        char[] password = PasswordUtil.generatePassword(length);

        // Verify the password is not null and has appropriate length
        assertNotNull(password, "Password should not be null");
        assertTrue(password.length >= length, "Password length should be at least the specified length");

        // Test with a larger length
        length = 20;
        password = PasswordUtil.generatePassword(length);

        assertNotNull(password, "Password should not be null");
        assertTrue(password.length >= length, "Password length should be at least the specified length");
    }

    @Test
    void testGeneratePasswordWithInvalidLength() {
        // Test with invalid length (8 or less)
        int invalidLength = 7;

        // Verify that an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.generatePassword(invalidLength));
    }

    /**
     * Provides test data for password strength evaluation tests.
     * Each argument contains:
     * 1. Password to evaluate
     * 2. Expected strength level
     * 3. Expected secure status
     */
    private static Stream<Arguments> passwordStrengthTestData() {
        return Stream.of(
                // Very weak passwords
                Arguments.of("", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("123456", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("password", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("password123", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("p@ssw0rd", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("Qwerty123!", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("qwerty123", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("Password123", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("P@ssw0rd123", PasswordUtil.StrengthLevel.VERY_WEAK, false),
                Arguments.of("Qw3rty123!", PasswordUtil.StrengthLevel.VERY_WEAK, false),

                // Weak passwords
                Arguments.of("Qw3rty394!", PasswordUtil.StrengthLevel.WEAK, false),

                // Moderate passwords
                Arguments.of("Password!2023Complex", PasswordUtil.StrengthLevel.MODERATE, false),
                Arguments.of("P@ssw0rd!2023Complex", PasswordUtil.StrengthLevel.MODERATE, false),
                Arguments.of("Tr0ub4dor&3XcellentPassword", PasswordUtil.StrengthLevel.MODERATE, false),

                // Strong passwords
                Arguments.of("P@ssw0rd!sGGewsv#", PasswordUtil.StrengthLevel.STRONG, true),

                // Very strong passwords
                Arguments.of("uE8+2fD4^7bN9q@5zR3#vT6*mX1pL0sY", PasswordUtil.StrengthLevel.VERY_STRONG, true),
                Arguments.of("7zXs9Yb2@pL5mK8*rQ3wE6&tU1oI4gF", PasswordUtil.StrengthLevel.VERY_STRONG, true)
        );
    }

    @ParameterizedTest
    @MethodSource("passwordStrengthTestData")
    void testEvaluatePasswordStrength(String password, PasswordUtil.StrengthLevel expectedLevel, boolean expectedSecure) {
        // Evaluate the password strength
        PasswordUtil.PasswordStrength strength = PasswordUtil.evaluatePasswordStrength(password.toCharArray());

        // Verify the strength level matches the expected level
        assertEquals(expectedLevel, strength.strengthLevel(),
                "Password '" + password + "' should have strength level " + expectedLevel);

        // Verify the secure status matches the expected status
        assertEquals(expectedSecure, strength.isSecure(),
                "Password '" + password + "' secure status should be " + expectedSecure);
    }

    @ParameterizedTest
    @MethodSource("passwordStrengthTestData")
    void testPasswordStrengthIssues(String password, PasswordUtil.StrengthLevel expectedLevel, boolean expectedSecure) {
        // Skip empty password as it's a special case
        if (password.isEmpty()) {
            return;
        }

        // Evaluate the password strength
        PasswordUtil.PasswordStrength strength = PasswordUtil.evaluatePasswordStrength(password.toCharArray());

        // Verify that issues list is not null
        assertNotNull(strength.issues(), "Issues list should not be null");

        // For weak passwords, verify that there are issues reported
        if (expectedLevel.getLevel() < PasswordUtil.StrengthLevel.STRONG.getLevel()) {
            assertFalse(strength.issues().isEmpty(), "Weak password '" + password + "' should have issues reported");
        }

        assertEquals(expectedSecure, strength.isSecure());
    }

    @Test
    void testPasswordStrengthEntropyEfficiency() {
        // Test with a strong password
        String strongPassword = "Tr0ub4dor&3XcellentPassword";
        PasswordUtil.PasswordStrength strength = PasswordUtil.evaluatePasswordStrength(strongPassword.toCharArray());

        // Verify entropy values are positive
        assertTrue(strength.shannonEntropy() > 0, "Shannon entropy should be positive");
        assertTrue(strength.theoreticalEntropy() > 0, "Theoretical entropy should be positive");

        // Verify entropy efficiency is calculated correctly
        double expectedEfficiency = (strength.shannonEntropy() / strength.theoreticalEntropy()) * 100;
        assertEquals(expectedEfficiency, strength.getEntropyEfficiency(), 0.001,
                "Entropy efficiency calculation should match expected formula");
    }

    @ParameterizedTest
    @MethodSource("passwordStrengthTestData")
    void testPasswordStrengthToString(String password, PasswordUtil.StrengthLevel expectedLevel, boolean expectedSecure) {
        // Skip empty password as it's a special case
        if (password.isEmpty()) {
            return;
        }

        // Evaluate the password strength
        PasswordUtil.PasswordStrength strength = PasswordUtil.evaluatePasswordStrength(password.toCharArray());

        // Verify that toString() returns a non-null, non-empty string
        String toString = strength.toString();
        assertNotNull(toString, "toString() should not return null");
        assertNotEquals("", toString, "toString() should not return empty string");

        // Verify that toString() contains all the expected components
        assertTrue(toString.contains("Password Strength: " + expectedLevel),
                "toString() should contain the strength level");
        assertTrue(toString.contains("Shannon: " + String.format("%.1f", strength.shannonEntropy()) + " bits"),
                "toString() should contain Shannon entropy");
        assertTrue(toString.contains("Theoretical: " + String.format("%.1f", strength.theoreticalEntropy()) + " bits"),
                "toString() should contain theoretical entropy");
        assertTrue(toString.contains("Efficiency: " + String.format("%.1f", strength.getEntropyEfficiency()) + "%"),
                "toString() should contain entropy efficiency");
        assertTrue(toString.contains("Est. crack time: " + strength.getEstimatedCrackTime()),
                "toString() should contain estimated crack time");
    }
}
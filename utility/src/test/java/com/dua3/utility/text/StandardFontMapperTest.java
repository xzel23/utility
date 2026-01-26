package com.dua3.utility.text;

import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for {@link StandardFontMapper}.
 */
@NullUnmarked
class StandardFontMapperTest {

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
                // input, IDENTITY, IGNORE_SUBSETS, KNOWN_ALIASES, IGNORE_SUBSETS_AND_KNOWN_ALIASES
                Arguments.of("Arial", "Arial", "Arial", "Arial", "Arial"), Arguments.of("ArialMT", "ArialMT", "ArialMT", "Arial", "Arial"), Arguments.of("ABCDEF+Arial", "ABCDEF+Arial", "Arial", "ABCDEF+Arial", "Arial"), Arguments.of("ABCDEF+ArialMT", "ABCDEF+ArialMT", "ArialMT", "ABCDEF+ArialMT", "Arial"), Arguments.of("TimesNewRomanPSMT", "TimesNewRomanPSMT", "TimesNewRomanPSMT", "Times New Roman", "Times New Roman"), Arguments.of("ABCDEF+TimesNewRomanPSMT", "ABCDEF+TimesNewRomanPSMT", "TimesNewRomanPSMT", "ABCDEF+TimesNewRomanPSMT", "Times New Roman"), Arguments.of("Times-Roman", "Times-Roman", "Times-Roman", "Times New Roman", "Times New Roman"), Arguments.of("ABCDEF+Times-Roman", "ABCDEF+Times-Roman", "Times-Roman", "ABCDEF+Times-Roman", "Times New Roman"), Arguments.of("CourierNewPSMT", "CourierNewPSMT", "CourierNewPSMT", "Courier New", "Courier New"), Arguments.of("ABCDEF+CourierNewPSMT", "ABCDEF+CourierNewPSMT", "CourierNewPSMT", "ABCDEF+CourierNewPSMT", "Courier New"), Arguments.of(null, null, null, null, null));
    }

    @Test
    void testIdentity() {
        // IDENTITY should return the input unchanged
        assertEquals("Arial", StandardFontMapper.IDENTITY.apply("Arial"));
        assertEquals("Times New Roman", StandardFontMapper.IDENTITY.apply("Times New Roman"));
        assertEquals("ABCDEF+Arial", StandardFontMapper.IDENTITY.apply("ABCDEF+Arial"));
        assertNull(StandardFontMapper.IDENTITY.apply(null));
    }

    @Test
    void testIgnoreSubsets() {
        // IGNORE_SUBSETS should remove subset tags
        assertEquals("Arial", StandardFontMapper.IGNORE_SUBSETS.apply("ABCDEF+Arial"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS.apply("ABCDEF+Times New Roman"));

        // If no subset tag is present, the input should be returned unchanged
        assertEquals("Arial", StandardFontMapper.IGNORE_SUBSETS.apply("Arial"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS.apply("Times New Roman"));

        // Null input should return null
        assertNull(StandardFontMapper.IGNORE_SUBSETS.apply(null));

        // Test with invalid subset tags (should not be removed)
        assertEquals("ABCDE+Arial", StandardFontMapper.IGNORE_SUBSETS.apply("ABCDE+Arial")); // Only 5 characters
        assertEquals("abcdef+Arial", StandardFontMapper.IGNORE_SUBSETS.apply("abcdef+Arial")); // Lowercase
        assertEquals("123456+Arial", StandardFontMapper.IGNORE_SUBSETS.apply("123456+Arial")); // Numbers
    }

    @Test
    void testKnownAliases() {
        // KNOWN_ALIASES should map known font aliases to standard names
        assertEquals("Arial", StandardFontMapper.KNOWN_ALIASES.apply("ArialMT"));
        assertEquals("Times New Roman", StandardFontMapper.KNOWN_ALIASES.apply("TimesNewRomanPSMT"));
        assertEquals("Times New Roman", StandardFontMapper.KNOWN_ALIASES.apply("Times-Roman"));
        assertEquals("Courier New", StandardFontMapper.KNOWN_ALIASES.apply("CourierNewPSMT"));

        // Unknown fonts should be returned unchanged
        assertEquals("Helvetica", StandardFontMapper.KNOWN_ALIASES.apply("Helvetica"));
        assertEquals("Calibri", StandardFontMapper.KNOWN_ALIASES.apply("Calibri"));

        // Null input should return null
        assertNull(StandardFontMapper.KNOWN_ALIASES.apply(null));
    }

    @Test
    void testIgnoreSubsetsAndKnownAliases() {
        // IGNORE_SUBSETS_AND_KNOWN_ALIASES should remove subset tags and map known aliases
        assertEquals("Arial", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+ArialMT"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+TimesNewRomanPSMT"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+Times-Roman"));
        assertEquals("Courier New", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+CourierNewPSMT"));

        // Should also work with just aliases without subset tags
        assertEquals("Arial", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ArialMT"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("TimesNewRomanPSMT"));

        // Should also work with just subset tags without aliases
        assertEquals("Arial", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+Arial"));
        assertEquals("Times New Roman", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+Times New Roman"));

        // Unknown fonts should be returned unchanged, but subset tags should still be removed
        assertEquals("Helvetica", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+Helvetica"));
        assertEquals("Calibri", StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply("ABCDEF+Calibri"));

        // Null input should return null
        assertNull(StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply(null));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testAllMappers(String input, String expectedIdentity, String expectedIgnoreSubsets, String expectedKnownAliases, String expectedIgnoreSubsetsAndKnownAliases) {
        assertEquals(expectedIdentity, StandardFontMapper.IDENTITY.apply(input));
        assertEquals(expectedIgnoreSubsets, StandardFontMapper.IGNORE_SUBSETS.apply(input));
        assertEquals(expectedKnownAliases, StandardFontMapper.KNOWN_ALIASES.apply(input));
        assertEquals(expectedIgnoreSubsetsAndKnownAliases, StandardFontMapper.IGNORE_SUBSETS_AND_KNOWN_ALIASES.apply(input));
    }
}
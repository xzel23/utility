package com.dua3.utility.application;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseDataTest {

    private static final String LICENSEE = "John Doe";
    private static final LocalDate VALID_UNTIL = LocalDate.of(2030, 1, 31);
    private static final String LICENSE_ID = "ID-123";

    private static void assertThrowsExcpetionOrAssertionError(Class<? extends Exception> exceptionClass, Runnable r) {
        try {
            r.run();
        } catch (Exception | AssertionError e) {
            assertTrue(LangUtil.isOneOf(e.getClass(), AssertionError.class, exceptionClass), "unexpected exception thrown: " + e.getClass());
            return;
        }
        throw new AssertionError("expected exception not thrown");
    }

    @Test
    void constructor_throwsNPE_whenLicenseeIsNull() {
        // given
        String licensee = null;
        LocalDate validUntil = LocalDate.now();
        String licenseId = "ID-123";
        Optional<Supplier<RichText>> licenseText = Optional.empty();

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, licenseText));
    }

    @Test
    void constructor_throwsNPE_whenValidUntilIsNull() {
        // given
        String licensee = "John Doe";
        LocalDate validUntil = null;
        String licenseId = "ID-123";
        Optional<Supplier<RichText>> licenseText = Optional.empty();

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, licenseText));
    }

    @Test
    void constructor_throwsNPE_whenLicenseIdIsNull() {
        // given
        String licensee = "John Doe";
        LocalDate validUntil = LocalDate.now();
        String licenseId = null;
        Optional<Supplier<RichText>> licenseText = Optional.empty();

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, licenseText));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void constructor_throwsNPE_whenLicenseTextIsNull() {
        // given
        String licensee = "John Doe";
        LocalDate validUntil = LocalDate.now();
        String licenseId = "ID-123";

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, null));
    }

    @Test
    void licenseText_buildsLicenseDetails_whenNoTextSupplierIsPresent() {
        LicenseData licenseData = new LicenseData(
                LICENSEE,
                VALID_UNTIL,
                LICENSE_ID,
                Optional.empty()
        );

        RichText licenseText = licenseData.licenseText();

        assertEquals(
                "License Details\n\nLicensee: John Doe\nLicense ID: ID-123\nValid Until: 2030-01-31\n",
                licenseText.toString()
        );
        assertTrue(licenseText.runs().getFirst().getStyles().contains(Style.BOLD));
        assertTrue(licenseText.runs().stream()
                .filter(run -> run.toString().equals("Licensee:"))
                .findFirst()
                .orElseThrow()
                .getStyles()
                .contains(Style.BOLD));
    }

    @Test
    void licenseText_returnsTextFromSupplier_whenPresent() {
        RichText suppliedText = RichText.valueOf("custom license text");
        LicenseData licenseData = new LicenseData(
                LICENSEE,
                VALID_UNTIL,
                LICENSE_ID,
                Optional.of(() -> suppliedText)
        );

        assertSame(suppliedText, licenseData.licenseText());
    }
}

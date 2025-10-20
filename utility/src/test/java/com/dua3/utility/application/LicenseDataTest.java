package com.dua3.utility.application;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseDataTest {

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
        Optional<Supplier<CharSequence>> licenseText = Optional.empty();

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
        Optional<Supplier<CharSequence>> licenseText = Optional.empty();

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
        Optional<Supplier<CharSequence>> licenseText = Optional.empty();

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, licenseText));
    }

    @Test
    void constructor_throwsNPE_whenLicenseTextIsNull() {
        // given
        String licensee = "John Doe";
        LocalDate validUntil = LocalDate.now();
        String licenseId = "ID-123";
        Optional<Supplier<CharSequence>> licenseText = null;

        // then
        assertThrowsExcpetionOrAssertionError(NullPointerException.class,
                () -> new LicenseData(licensee, validUntil, licenseId, licenseText));
    }
}
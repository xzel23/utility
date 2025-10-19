package com.dua3.utility.application;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple recored of license data for a software.
 *
 * @param licensee    the Name of the licensee
 * @param validUntil  the last date of the license period
 * @param licenseId   the license ID
 * @param licenseText the license text
 */
public record LicenseData(
        String licensee,
        LocalDate validUntil,
        String licenseId,
        Optional<Supplier<CharSequence>> licenseText
) {}

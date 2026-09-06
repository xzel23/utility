package com.dua3.utility.application;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple recored of license data for a software.
 *
 * @param licensee    the Name of the licensee
 * @param validUntil  the last date of the license period
 * @param licenseId   the license ID
 * @param textSupplier the license text
 */
public record LicenseData(
        String licensee,
        LocalDate validUntil,
        String licenseId,
        Optional<Supplier<RichText>> textSupplier
) {
    /**
     * Generates and returns the license information as a RichText object.
     * If a pre-existing license text is available from the supplier, it uses that text.
     * Otherwise, it builds the license details including licensee, license ID, and validity period.
     *
     * @return a RichText object containing the license details.
     */
    public RichText licenseText() {
        return textSupplier.map(Supplier::get).orElseGet(() -> {
            RichTextBuilder rtb = new RichTextBuilder();
            rtb.push(Style.BOLD);
            rtb.append("License Details");
            rtb.pop(Style.BOLD);
            rtb.append("\n\n");

            appendField(rtb, "Licensee", licensee);
            appendField(rtb, "License ID", licenseId);
            appendField(rtb, "Valid Until", validUntil.toString());

            return rtb.toRichText();
        });
    }

    private void appendField(RichTextBuilder rtb, String name, String value) {
        rtb.push(Style.BOLD);
        rtb.append(name).append(":");
        rtb.pop(Style.BOLD);
        rtb.append(' ');
        rtb.append(value);
        rtb.append("\n");
    }
}

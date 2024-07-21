/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.io;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Base class for CSV-input/output classes.
 */
public abstract class CsvIo implements AutoCloseable {

    /**
     * A constant string representing the set of allowed characters (besides letters and digits that are also allowed)
     * in unquoted fields.
     */
    private static final String UNQUOTED_CONTENT_ALLOWED_CHARS = "!§$%&/()=?`°^'.,:;-_#'+~*<>|@ \t";
    /**
     * The line delimiter used when writing CSV files.
     */
    protected final String lineDelimiter;
    /**
     * The separator character used for CSV data.
     */
    protected final char separator;
    /**
     * This variable represents the delimiter character used in CSV (Comma-Separated Values) files.
     */
    protected final char delimiter;
    /**
     * The {@link Locale} used for formatting and parsing data in CSV I/O.
     */
    protected final Locale locale;
    /**
     * The formatter used for formatting date-time values.
     */
    protected final DateTimeFormatter dateTimeFormatter;
    /**
     * The formatter used for formatting date objects.
     */
    protected final DateTimeFormatter dateFormatter;
    /**
     * The formatter used for formatting time values.
     */
    protected final DateTimeFormatter timeFormatter;
    /**
     * The NumberFormat used for formatting numbers.
     */
    protected final NumberFormat numberFormat;

    /**
     * Constructor for creating a CsvIo object.
     * Initializes the CsvIo object with the provided options.
     *
     * @param options The options to be used for configuring the CsvIo object.
     */
    protected CsvIo(Arguments options) {
        this.separator = IoOptions.getFieldSeparator(options);
        this.delimiter = IoOptions.getTextDelimiter(options);
        this.lineDelimiter = "\r\n";
        this.locale = IoOptions.getLocale(options);
        this.dateTimeFormatter = IoOptions.getDateTimeFormat(options).getDateTimeFormatter(locale);
        this.dateFormatter = IoOptions.getDateFormat(options).getDateFormatter(locale);
        this.timeFormatter = IoOptions.getDateFormat(options).getTimeFormatter(locale);
        this.numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(15);
    }

    /**
     * Get the list of options controlling CSV I/O.
     *
     * @return list of options
     */
    public static Collection<Option<?>> getOptions() {
        return List.of(
                IoOptions.textDelimiter(),
                IoOptions.fieldSeparator(),
                IoOptions.locale(),
                IoOptions.dateTimeFormat()
        );
    }

    /**
     * Format an object into a string based on its type.
     *
     * @param obj the object to be formatted
     * @return the formatted string
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    protected String format(@Nullable Object obj) {
        final String text;
        if (obj instanceof Number n) {
            text = numberFormat.format(n);
        } else if (obj instanceof LocalDateTime ldt) {
            text = ldt.format(dateTimeFormatter);
        } else if (obj instanceof LocalDate ld) {
            text = ld.format(dateFormatter);
        } else if (obj instanceof LocalTime lt) {
            text = lt.format(timeFormatter);
        } else {
            text = Objects.toString(obj, "");
        }
        return quoteIfNeeded(text);
    }

    /**
     * Checks if a string needs to be surrounded by quotes.
     *
     * @param text the string to be checked
     * @return true if quotes are needed, false otherwise
     */
    protected boolean isQuoteNeeded(CharSequence text) {
        // also quote if unusual characters are present
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == separator || c == delimiter || !Character.isLetterOrDigit(c) && UNQUOTED_CONTENT_ALLOWED_CHARS.indexOf(c) == -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Surrounds a string with quotes.
     *
     * @param text the string to be surrounded with quotes
     * @return the quoted string
     */
    protected String quote(String text) {
        return delimiter + text.replace("\"", "\"\"") + delimiter;
    }

    /**
     * Returns a quoted string if needed, otherwise returns the original string.
     *
     * @param text the string to be checked if quoting is needed
     * @return the quoted string if needed, otherwise the original string
     */
    protected String quoteIfNeeded(String text) {
        return isQuoteNeeded(text) ? quote(text) : text;
    }
}

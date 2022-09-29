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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * Get list of options controlling CSV I/O.
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

    private static final String ALLOWED_CHARS = "!§$%&/()=?`°^'.,:;-_#'+~*<>|@ \t";

    protected final String lineDelimiter;
    protected final char separator;
    protected final char delimiter;
    protected final Locale locale;
    protected final DateTimeFormatter dateTimeFormatter;
    protected final DateTimeFormatter dateFormatter;
    protected final NumberFormat numberFormat;

    protected CsvIo(Arguments options) {
        this.separator = IoOptions.getFieldSeparator(options);
        this.delimiter = IoOptions.getTextDelimiter(options);
        this.lineDelimiter = "\r\n";
        this.locale = IoOptions.getLocale(options);
        this.dateTimeFormatter = IoOptions.getDateTimeFormat(options).getDateTimeFormatter(locale);
        this.dateFormatter = IoOptions.getDateFormat(options).getFormatter(locale);
        this.numberFormat = DecimalFormat.getInstance(locale);
        this.numberFormat.setGroupingUsed(false);
        this.numberFormat.setMinimumFractionDigits(0);
        this.numberFormat.setMaximumFractionDigits(15);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected String format(@Nullable Object obj) {
        final String text;
        if (obj instanceof Number) {
            text = numberFormat.format(obj);
        } else if (obj instanceof LocalDate) {
            text = ((LocalDate)obj).format(dateFormatter);
        } else if (obj instanceof LocalDateTime) {
            text = ((LocalDateTime)obj).format(dateTimeFormatter);
        } else {
            text = Objects.toString(obj, "");
        }
        return quoteIfNeeded(text);
    }

    protected boolean isQuoteNeeded(String text) {
        // also quote if unusual characters are present
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c==separator || c == delimiter || !Character.isLetterOrDigit(c) && ALLOWED_CHARS.indexOf(c) == -1) {
                return true;
            }
        }
        return false;
    }

    protected String quote(String text) {
        return delimiter + text.replace("\"", "\"\"") + delimiter;
    }

    protected String quoteIfNeeded(String text) {
        return isQuoteNeeded(text) ? quote(text) : text;
    }
}

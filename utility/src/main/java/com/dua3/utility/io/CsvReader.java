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

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that reads data from CSV files.
 * <p>
 * This class extends the CsvIo class and provides methods for reading CSV data from various sources.
 */
public class CsvReader extends CsvIo {

    // the UNICODE codepoint for the UTF-8 BOM
    private static final int UTF8_BOM = 0xfeff;
    // the bytes sequence the UTF-8 BOM
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final byte[] UTF8_BOM_BYTES = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    private final RowBuilder rowBuilder;
    private final Pattern patternField;
    private final BufferedReader reader;
    private final @Nullable URI source;
    private int rowNumber;
    private int rowsRead;
    private int lineNumber;
    private @Nullable List<String> columnNames;
    private boolean ignoreExcessFields;
    private boolean ignoreMissingFields;

    /**
     * A utility class for reading CSV files.
     *
     * @param rowBuilder  the row builder for creating CSV rows
     * @param reader      the buffered reader for reading the CSV file
     * @param source      the optional URI of the CSV file
     * @param options     the arguments for configuring the CSV reader
     * @throws IOException if an I/O error occurs while reading the CSV file
     */
    public CsvReader(RowBuilder rowBuilder, BufferedReader reader, @Nullable URI source, Arguments options)
            throws IOException {
        super(options);

        this.rowBuilder = rowBuilder;
        this.reader = reader;
        this.columnNames = null;
        this.ignoreExcessFields = false;
        this.ignoreMissingFields = false;
        this.source = source;

        // remove optional UTF-8 BOM from content
        // this should be ok independent of the actual encoding since the
        // UNICODE representing
        // the UTF-8 BOM marker should only occur at the beginning of UTF-8
        // texts, and the old
        // (now obsolete) meaning as "ZERO WIDTH NON-BREAKING SPACE (ZWNBSP)"
        // does not make sense
        // at the beginning of a text.
        //
        // http://www.unicode.org/faq/utf_bom.html:
        // > In the absence of a protocol supporting its use as a BOM and when
        // > not at the beginning of a text stream, U+FEFF should normally not occur.
        reader.mark(1);
        if (reader.read() != UTF8_BOM) {
            reader.reset();
        }

        String sep = Pattern.quote(Character.toString(separator));
        String del = Pattern.quote(Character.toString(delimiter));

        // create a pattern for matching of csv fields
        patternField = Pattern.compile(generateFieldRegex(sep, del));
    }

    private static String generateFieldRegex(String sep, String del) {
        String regexEnd = "(?:" + sep + "|$)";
        // pattern group1: unquoted field
        String regexUnquotedField = "(?:((?:[^" + del + sep + "][^" + sep + "]*)?)" + regexEnd + ")";
        // pattern group2: quoted field
        String regexQuotedField = " *(?:" + del + "((?:[^" + del + "]|" + del + del + ")*)" + del + " *" + regexEnd
                + ")";
        // pattern group3: start of quoted field with embedded newline (group
        // must contain delimiter!)
        String regexStartQuotedFieldWithLineBreak = "(" + del + "(?:[^" + del + "]*(?:" + del + del + ")?)*$)";

        return "^(?:" + regexQuotedField + "|" + regexUnquotedField + "|" + regexStartQuotedFieldWithLineBreak + ")";
    }

    /**
     * Creates a new instance of `CsvReader`.
     *
     * @param builder  the row builder for creating CSV rows
     * @param reader      the buffered reader for reading the CSV file
     * @param options     the arguments for configuring the CSV reader
     * @return a new instance of `CsvReader`
     * @throws IOException if an I/O error occurs while reading the CSV file
     */
    public static CsvReader create(RowBuilder builder, BufferedReader reader, Arguments options) throws IOException {
        return new CsvReader(builder, reader, null, options);
    }

    /**
     * Creates a new instance of `CsvReader`.
     *
     * @param builder  the row builder for creating CSV rows
     * @param path      the path to the CSV file
     * @param options     the arguments for configuring the CSV reader
     * @return a new instance of `CsvReader`
     * @throws IOException if an I/O error occurs while reading the CSV file
     */
    public static CsvReader create(RowBuilder builder, Path path, Arguments options) throws IOException {
        Charset cs = IoOptions.getCharset(options);
        return create(builder, Files.newBufferedReader(path, cs), options);
    }

    /**
     * Creates a new instance of `CsvReader`.
     *
     * @param builder  the row builder for creating CSV rows
     * @param in       the input stream of the CSV data
     * @param options  the arguments for configuring the CSV reader
     * @return a new instance of `CsvReader`
     * @throws IOException if an I/O error occurs while reading the CSV data
     */
    public static CsvReader create(RowBuilder builder, InputStream in, Arguments options) throws IOException {
        // auto-detect UTF-8 with BOM (BOM marker overrides the CharSet
        // selection in options)
        Charset charset = IoOptions.getCharset(options);
        if (in.markSupported()) {
            int bomLength = UTF8_BOM_BYTES.length;
            byte[] buffer = new byte[bomLength];
            in.mark(bomLength);
            if (in.read(buffer) != bomLength || !Arrays.equals(UTF8_BOM_BYTES, buffer)) {
                in.reset();
            } else {
                charset = StandardCharsets.UTF_8;
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        return create(builder, reader, options);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Get the column name.
     *
     * @param columnNr the column number
     * @return name of column or columnNr as String if no name was set
     */
    public String getColumnName(int columnNr) {
        if (columnNames != null && columnNr < columnNames.size()) {
            return columnNames.get(columnNr);
        } else {
            return Integer.toString(columnNr);
        }
    }

    /**
     * Indicates whether excess fields in the CSV data should be ignored during parsing.
     *
     * @return true if excess fields are ignored; false otherwise
     */
    public boolean getIgnoreExcessFields() {
        return ignoreExcessFields;
    }

    /**
     * Configures whether excess fields in the CSV data should be ignored during parsing.
     *
     * @param ignoreExcessFields a boolean value indicating if excess fields should be ignored.
     *                           If true, excess fields are ignored; if false, an exception
     *                           or error may occur during parsing when excess fields are encountered.
     */
    public void setIgnoreExcessFields(boolean ignoreExcessFields) {
        this.ignoreExcessFields = ignoreExcessFields;
    }

    /**
     * Indicates whether missing fields in the CSV data should be ignored during parsing.
     *
     * @return true if missing fields are ignored; false otherwise
     */
    public boolean getIgnoreMissingFields() {
        return ignoreMissingFields;
    }

    /**
     * Configures whether missing fields in the CSV data should be ignored during parsing.
     *
     * @param ignoreMissingFields a boolean value indicating if missing fields should be ignored.
     *                            If true, missing fields are ignored; if false, an exception or
     *                            error may occur during parsing when missing fields are encountered.
     */
    public void setIgnoreMissingFields(boolean ignoreMissingFields) {
        this.ignoreMissingFields = ignoreMissingFields;
    }

    /**
     * Returns the current line number being processed in the CSV reader.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Retrieves the current row number being processed in the CSV reader.
     *
     * @return the current row number
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Returns the total number of rows read by the CSV reader.
     *
     * @return the total count of rows read
     */
    public int getRowsRead() {
        return rowsRead;
    }

    /**
     * Returns the source URI.
     *
     * @return the source URI
     */
    private @Nullable URI getSource() {
        return source;
    }

    /**
     * Ignores the specified number of rows in a file.
     *
     * @param rowsToIgnore the number of rows to ignore
     * @return the number of rows actually ignored
     * @throws IOException if an I/O error occurs
     */
    public int ignoreRows(int rowsToIgnore) throws IOException {
        int ignored = 0;
        while (ignored < rowsToIgnore) {
            LangUtil.ignore(reader.readLine());
            lineNumber++;
            ignored++;
        }
        return ignored;
    }

    /**
     * Reads all rows in a file.
     *
     * @return the number of rows read
     * @throws IOException if an I/O error occurs
     */
    public int readAll() throws IOException {
        return readRows(0);
    }

    /**
     * Read the column names from the file.
     *
     * @throws IOException if an I/O error occurs
     */
    public void readColumnNames() throws IOException {
        ListRowBuilder rb = new ListRowBuilder();
        readRow(rb);
        columnNames = rb.getRow();
    }

    /**
     * Read a single row of CSV data.
     *
     * @return number of fields in row or -1 when end of input is
     * reached
     * @throws IOException if an error occurs during reading
     */
    private int readRow(RowBuilder rb) throws IOException {
        String line = reader.readLine();

        if (line == null) {
            return -1;
        }

        lineNumber++;

        rb.startRow();
        int columnNr = 0;
        Matcher matcher = patternField.matcher(line);
        while (matcher.lookingAt()) {
            // group 1 refers to a quoted field, group 2 to an unquoted field
            // since we have a match, either group 1 or group 2 matches and
            // contains the field's value.
            String field = matcher.group(1);
            if (field != null) {
                field = field.replace("\"\"", "\"");
            } else {
                field = matcher.group(2);
            }

            // check for linebreak in quoted field
            int currentLine = lineNumber;
            if (field == null && matcher.group(3) != null) {
                String nextLine = reader.readLine();

                LangUtil.check(
                        nextLine != null,
                        () -> new CsvFormatException("Unexpected end of input while looking for matching delimiter.",
                                getSource(), currentLine));

                lineNumber++;
                line = matcher.group(3) + "\n" + nextLine;
                matcher = patternField.matcher(line);
                continue;
            }

            assert field != null : "field must not be null";
            rb.add(field);
            columnNr++;

            // check for end of line
            if (matcher.hitEnd()) {
                break;
            }

            // move region behind end of last match
            matcher.region(matcher.end(), matcher.regionEnd());
        }

        // if unparsed input remains, the input line is not in csv-format
        LangUtil.check(matcher.hitEnd(), () -> new CsvFormatException("invalid csv data.", getSource(), getLineNumber()));

        // check number of fields
        LangUtil.check(ignoreMissingFields || columnNames == null || columnNr >= columnNames.size(),
                () -> new CsvFormatException("not enough fields.", getSource(), getLineNumber()));

        rowNumber++;
        rowsRead++;
        rb.endRow();

        return columnNr;
    }

    /**
     * Read some rows of CSV data.
     *
     * @param maxRows maximum number of rows to be read or 0 to read till end of input
     * @return number of rows read
     * @throws IOException        if an error occurs during reading
     * @throws CsvFormatException if the data read can not be correctly interpreted
     */
    private int readRows(int maxRows) throws IOException {
        int read = 0;
        while (maxRows == 0 || read < maxRows) {
            if (readRow(rowBuilder) < 0) {
                break;
            }
            read++;
        }
        return read;
    }

    /**
     * Read some rows of CSV data.
     *
     * @param rowsToRead number of rows to be read
     * @return number of rows read
     * @throws IOException        if an error occurs during reading
     * @throws CsvFormatException if the data read can not be correctly interpreted
     */
    public int readSome(int rowsToRead) throws IOException {
        return rowsToRead > 0 ? readRows(rowsToRead) : 0;
    }

    /**
     * Sets the column names for the CSV reader.
     *
     * @param columnNames the list of column names to be used
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Interface used to build rows when reading CSV files.
     */
    public interface RowBuilder {
        /**
         * Add a value.
         *
         * @param value the value to add
         */
        void add(String value);

        /**
         * End the current row.
         */
        void endRow();

        /**
         * Start a new row.
         */
        void startRow();
    }

    /**
     * A {@link RowBuilder} implementation that creates a list of Strings for the cells contained in each row read.
     */
    public static class ListRowBuilder implements RowBuilder {

        private final List<String> row = new ArrayList<>();

        /**
         * Constructs a new instance of ListRowBuilder.
         */
        public ListRowBuilder() {}

        @Override
        public void add(String value) {
            row.add(value);
        }

        @Override
        public void endRow() {
            // nop
        }

        /**
         * Get row data.
         *
         * @return list of values contained in the row
         */
        public List<String> getRow() {
            return Collections.unmodifiableList(row);
        }

        @Override
        public void startRow() {
            assert row.isEmpty() : "row ist not empty";
        }
    }

}

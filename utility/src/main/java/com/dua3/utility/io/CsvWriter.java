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

import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class that writes data in CSV format.
 */
public class CsvWriter extends CsvIo implements Flushable {
    private final BufferedWriter out;
    private int fieldsInRow;

    /**
     * Constructs a new CsvWriter.
     *
     * @param out     the BufferedWriter to write the CSV data to
     * @param options the Arguments object representing the options for writing CSV formatted data
     */
    public CsvWriter(BufferedWriter out, Arguments options) {
        super(options);
        this.out = out;
    }

    /**
     * Creates a new CsvWriter by using the provided BufferedWriter and Arguments options.
     *
     * @param writer  the BufferedWriter to write the CSV data to
     * @param options the Arguments object representing the options for writing CSV formatted data
     * @return a new CsvWriter instance
     */
    public static CsvWriter create(BufferedWriter writer, Arguments options) {
        return new CsvWriter(writer, options);
    }

    /**
     * Creates a new CsvWriter by using the provided file path, Arguments options.
     * The method reads the charset from the options and creates a BufferedWriter from the file path using the charset.
     *
     * @param path    the path of the file to write the CSV data to
     * @param options the Arguments object representing the options for writing CSV formatted data
     * @return a new CsvWriter instance
     * @throws IOException if an I/O error occurs
     */
    public static CsvWriter create(Path path, Arguments options) throws IOException {
        Charset cs = IoOptions.getCharset(options);
        return create(Files.newBufferedWriter(path, cs), options);
    }

    /**
     * Creates a new CsvWriter by using the provided OutputStream and Arguments options.
     * The method creates a BufferedWriter from the OutputStream using the charset from the options.
     *
     * @param out     the OutputStream to write the CSV data to
     * @param options the Arguments object representing the options for writing CSV formatted data
     * @return a new CsvWriter instance
     */
    public static CsvWriter create(OutputStream out, Arguments options) {
        Charset cs = IoOptions.getCharset(options);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, cs));
        return create(writer, options);
    }

    /**
     * Adds a field to the current row in the CSV writer.
     * If this is not the first field in the row, a separator is added before the new field.
     * The field value is converted to a string representation using the format method.
     *
     * @param obj the object to be added as a field in the CSV writer.
     *            This object will be converted to a string representation using the format method.
     * @throws IOException if an I/O error occurs while writing the field to the CSV writer.
     */
    public void addField(@Nullable Object obj) throws IOException {
        if (fieldsInRow > 0) {
            out.write(separator);
        }
        out.write(format(obj));
        fieldsInRow++;
    }

    @Override
    public void close() throws IOException {
        if (fieldsInRow > 0) {
            nextRow();
        }
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Moves to the next row in the CSV writer.
     * After calling this method, the writer will start writing fields to a new row.
     * If the previous row had any fields, a line delimiter is added before moving to the next row.
     * The fieldsInRow variable is reset to 0, indicating that the next field will be the first in the new row.
     *
     * @throws IOException if an I/O error occurs while moving to the next row in the CSV writer.
     */
    public void nextRow() throws IOException {
        out.write(lineDelimiter);
        fieldsInRow = 0;
    }

}

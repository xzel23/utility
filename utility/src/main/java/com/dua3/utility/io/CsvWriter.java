/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dua3.utility.options.OptionValues;

/**
 *
 * @author axel@dua3.com
 */
public class CsvWriter extends Csv implements AutoCloseable, Flushable {
    private static final String ALLOWED_CHARS = "!§$%&/()=?`°^'.,:;-_#'+~*<>|@ \t";

    public static CsvWriter create(BufferedWriter writer, OptionValues options) {
        return new CsvWriter(writer, options);
    }

    public static CsvWriter create(File file, OptionValues options) throws IOException {
        return create(file.toPath(), options);
    }

    public static CsvWriter create(Path path, OptionValues options) throws IOException {
        Charset cs = getCharset(options);
        return create(Files.newBufferedWriter(path, cs), options);
    }

    public static CsvWriter create(OutputStream out, OptionValues options) {
        Charset cs = getCharset(options);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, cs));
        return create(writer, options);
    }

    private final BufferedWriter out;
    private final String lineDelimiter;
    private final String separator;
    private final String delimiter;
    private int fieldsInRow = 0;

    public CsvWriter(BufferedWriter out, OptionValues options) {
        this.separator = String.valueOf(getSeparator(options));
        this.delimiter = String.valueOf(getDelimiter(options));
        this.lineDelimiter = String.format("%n");
        this.out = out;
    }

    public void addField(String text) throws IOException {
        if (fieldsInRow > 0) {
            out.write(separator);
        }
        out.write(quoteIfNeeded(text));
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

    private boolean isQuoteNeeded(String text) {
        // quote if separator or delimiter are present
        if (text.indexOf(separator) >= 0 || text.indexOf(delimiter) >= 0) {
            return true;
        }

        // also quote if unusual characters are present
        for (char c : text.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && ALLOWED_CHARS.indexOf(c) == -1) {
                return true;
            }
        }
        return false;
    }

    public void nextRow() throws IOException {
        out.write(lineDelimiter);
        fieldsInRow = 0;
    }

    private String quote(String text) {
        return delimiter + text.replace("\"", "\"\"") + delimiter;
    }

    private String quoteIfNeeded(String text) {
        return isQuoteNeeded(text) ? quote(text) : text;
    }

}

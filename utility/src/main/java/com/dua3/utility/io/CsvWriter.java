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

import com.dua3.utility.options.Arguments;
import com.dua3.cabe.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author axel@dua3.com
 */
public class CsvWriter extends CsvIo implements Flushable {
    public static CsvWriter create(@NotNull BufferedWriter writer, @NotNull Arguments options) {
        return new CsvWriter(writer, options);
    }

    public static CsvWriter create(@NotNull File file, @NotNull Arguments options) throws IOException {
        return create(file.toPath(), options);
    }

    public static CsvWriter create(@NotNull Path path, @NotNull Arguments options) throws IOException {
        Charset cs = IoOptions.getCharset(options);
        return create(Files.newBufferedWriter(path, cs), options);
    }

    public static CsvWriter create(@NotNull OutputStream out, @NotNull Arguments options) {
        Charset cs = IoOptions.getCharset(options);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, cs));
        return create(writer, options);
    }

    private final BufferedWriter out;

    private int fieldsInRow = 0;

    public CsvWriter(@NotNull BufferedWriter out, @NotNull Arguments options) {
        super(options);
        this.out = Objects.requireNonNull(out);
    }

    public void addField(Object obj) throws IOException {
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

    public void nextRow() throws IOException {
        out.write(lineDelimiter);
        fieldsInRow = 0;
    }

}

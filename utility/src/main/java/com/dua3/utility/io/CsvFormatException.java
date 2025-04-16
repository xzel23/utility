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

import java.io.IOException;
import java.io.Serial;
import java.net.URI;

/**
 * Exception class for reporting CSV format violations.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvFormatException extends IOException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The {@link URI} of the CSV source that caused the exception, if available.
     */
    private final @Nullable URI source;
    /**
     * The number of the line in the input data that caused the exception.
     */
    private final int line;

    /**
     * Construct a new CsvFormatException.
     *
     * @param message the message to report
     * @param source  a text describing the source (preferably the filename)
     * @param line    the line number of the CSV file where the error occurred
     */
    public CsvFormatException(String message, @Nullable URI source, int line) {
        super(message);
        this.source = source;
        this.line = line;
    }

    /**
     * Get the exception message.
     *
     * @return the exception message
     */
    @Override
    public String getMessage() {
        if (source != null) {
            return "[" + source + ":" + line + "] " + super.getMessage();
        } else {
            return "[" + line + "] " + super.getMessage();
        }
    }

}

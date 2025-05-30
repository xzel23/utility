// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the CsvWriter class.
 */
class CsvWriterTest {

    /**
     * Test writing CSV data using a StringWriter.
     */
    @Test
    void testWriteCsv() throws IOException {
        // Create arguments with default options
        Arguments arguments = Arguments.of();
        
        // Create a StringWriter to capture the output
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        
        // Create a CsvWriter
        try (CsvWriter csvWriter = CsvWriter.create(bufferedWriter, arguments)) {
            // Write header row
            csvWriter.addField("Nr.");
            csvWriter.addField("Name");
            csvWriter.addField("Age");
            csvWriter.addField("Address");
            csvWriter.nextRow();
            
            // Write data rows
            csvWriter.addField(1);
            csvWriter.addField("John");
            csvWriter.addField(34);
            csvWriter.addField("Street 1");
            csvWriter.nextRow();
            
            csvWriter.addField(2);
            csvWriter.addField("Jane");
            csvWriter.addField(23);
            csvWriter.addField("c/o John\nStreet 2, upstairs");
            csvWriter.nextRow();
            
            csvWriter.addField(3);
            csvWriter.addField("Peter, aka \"Pete\"");
            csvWriter.addField(17);
            csvWriter.addField("Street 3");
            csvWriter.nextRow();
            
            csvWriter.addField(4);
            csvWriter.addField("Jon Doe");
            csvWriter.nextRow();
        }
        
        // Define expected output
        String expected = "Nr.,Name,Age,Address\r\n" +
                "1,John,34,Street 1\r\n" +
                "2,Jane,23,\"c/o John\nStreet 2, upstairs\"\r\n" +
                "3,\"Peter, aka \"\"Pete\"\"\",17,Street 3\r\n" +
                "4,Jon Doe\r\n";
        
        // Verify output
        assertEquals(expected, stringWriter.toString());
    }

    /**
     * Test writing CSV data with custom separator and delimiter.
     */
    @Test
    void testWriteCsvWithCustomOptions() throws IOException {
        // Create arguments with custom options
        Arguments arguments = Arguments.of(
                Arguments.createEntry(IoOptions.fieldSeparator(), ';'),
                Arguments.createEntry(IoOptions.textDelimiter(), '\'')
        );
        
        // Create a StringWriter to capture the output
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        
        // Create a CsvWriter
        try (CsvWriter csvWriter = CsvWriter.create(bufferedWriter, arguments)) {
            // Write header row
            csvWriter.addField("Nr.");
            csvWriter.addField("Name");
            csvWriter.addField("Age");
            csvWriter.nextRow();
            
            // Write data rows
            csvWriter.addField(1);
            csvWriter.addField("John");
            csvWriter.addField(34);
            csvWriter.nextRow();
            
            csvWriter.addField(2);
            csvWriter.addField("Jane; with semicolon");
            csvWriter.addField(23);
            csvWriter.nextRow();
        }
        
        // Define expected output
        String expected = """
                Nr.;Name;Age\r
                1;John;34\r
                2;'Jane; with semicolon';23\r
                """;
        
        // Verify output
        assertEquals(expected, stringWriter.toString());
    }

    /**
     * Test writing CSV data with different data types.
     */
    @Test
    void testWriteCsvWithDifferentDataTypes() throws IOException {
        // Create arguments with custom locale
        Arguments arguments = Arguments.of(
                Arguments.createEntry(IoOptions.locale(), Locale.US)
        );
        
        // Create a StringWriter to capture the output
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        
        // Create a CsvWriter
        try (CsvWriter csvWriter = CsvWriter.create(bufferedWriter, arguments)) {
            // Write header row
            csvWriter.addField("Type");
            csvWriter.addField("Value");
            csvWriter.nextRow();
            
            // Write different data types
            csvWriter.addField("Integer");
            csvWriter.addField(42);
            csvWriter.nextRow();
            
            csvWriter.addField("Double");
            csvWriter.addField(3.14159);
            csvWriter.nextRow();
            
            csvWriter.addField("String");
            csvWriter.addField("Hello, World!");
            csvWriter.nextRow();
            
            csvWriter.addField("LocalDate");
            csvWriter.addField(LocalDate.of(2023, 7, 15));
            csvWriter.nextRow();
            
            csvWriter.addField("LocalTime");
            csvWriter.addField(LocalTime.of(14, 30, 0));
            csvWriter.nextRow();
            
            csvWriter.addField("LocalDateTime");
            csvWriter.addField(LocalDateTime.of(2023, 7, 15, 14, 30, 0));
            csvWriter.nextRow();
            
            csvWriter.addField("Null");
            csvWriter.addField(null);
            csvWriter.nextRow();
        }
        
        // Verify output contains the expected data types
        String output = stringWriter.toString();
        assertTrue(output.contains("Integer,42"));
        assertTrue(output.contains("Double,3.14159"));
        assertTrue(output.contains("String,\"Hello, World!\""));
        assertTrue(output.contains("LocalDate,2023-07-15"));
        assertTrue(output.contains("LocalTime,14:30:00"));
        assertTrue(output.contains("LocalDateTime,2023-07-15T14:30:00"));
        assertTrue(output.contains("Null,"));
    }

    /**
     * Test writing CSV data to an OutputStream.
     */
    @Test
    void testWriteCsvToOutputStream() throws IOException {
        // Create arguments with default options
        Arguments arguments = Arguments.of();
        
        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Create a CsvWriter
        try (CsvWriter csvWriter = CsvWriter.create(outputStream, arguments)) {
            // Write header row
            csvWriter.addField("Nr.");
            csvWriter.addField("Name");
            csvWriter.nextRow();
            
            // Write data rows
            csvWriter.addField(1);
            csvWriter.addField("John");
            csvWriter.nextRow();
            
            csvWriter.addField(2);
            csvWriter.addField("Jane");
            csvWriter.nextRow();
        }
        
        // Define expected output
        String expected = """
                Nr.,Name\r
                1,John\r
                2,Jane\r
                """;
        
        // Verify output
        assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
    }
}
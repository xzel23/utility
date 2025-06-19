package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvReaderTest {

    private static final String CSV_DATA = """
            Nr.,Name,Age,Address
            1,John,34,Street 1
            2,Jane,23,"c/o John\nStreet 2, upstairs"
            3,"Peter, aka ""Pete""\",17,Street 3
            4,Jon Doe
            """;

    private static final List<String> CSV_COLUMNS = List.of("Nr.", "Name", "Age", "Address");

    private static final List<List<String>> CSV_ROWS = List.of(
            List.of("1", "John", "34", "Street 1"),
            List.of("2", "Jane", "23", "c/o John\nStreet 2, upstairs"),
            List.of("3", "Peter, aka \"Pete\"", "17", "Street 3"),
            List.of("4", "Jon Doe")
    );

    @Test
    void testReadCsv() throws IOException {
        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, true),
                Arguments.createEntry(CsvReader.IGNORE_MISSING_FIELDS, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (BufferedReader reader = new BufferedReader(new StringReader(CSV_DATA));
             CsvReader csvReader = CsvReader.create(rowBuilder, reader, arguments)) {
            csvReader.readAll();
            assertEquals(CSV_COLUMNS, csvReader.getColumnNames(), "column names differ");
            assertIterableEquals(CSV_ROWS, actualRows, "rows differ");
        }
    }

    @Test
    void testReadCsv_reportMissingFields() throws IOException {
        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (BufferedReader reader = new BufferedReader(new StringReader(CSV_DATA));
             CsvReader csvReader = CsvReader.create(rowBuilder, reader, arguments)) {
            assertThrows(CsvFormatException.class, csvReader::readAll);
        }
    }

    @Test
    void testReadCsvFromInputStream() throws IOException {
        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, true),
                Arguments.createEntry(CsvReader.IGNORE_MISSING_FIELDS, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (InputStream in = new ByteArrayInputStream(CSV_DATA.getBytes(StandardCharsets.UTF_8));
             CsvReader csvReader = CsvReader.create(rowBuilder, in, arguments)) {
            csvReader.readAll();
            assertEquals(CSV_COLUMNS, csvReader.getColumnNames(), "column names differ");
            assertIterableEquals(CSV_ROWS, actualRows, "rows differ");
        }
    }

}
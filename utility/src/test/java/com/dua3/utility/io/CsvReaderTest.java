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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testReadCsvFromPath(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws IOException {
        java.nio.file.Path file = tempDir.resolve("test.csv");
        java.nio.file.Files.writeString(file, CSV_DATA, StandardCharsets.UTF_8);

        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, true),
                Arguments.createEntry(CsvReader.IGNORE_MISSING_FIELDS, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (CsvReader csvReader = CsvReader.create(rowBuilder, file, arguments)) {
            csvReader.readAll();
            assertEquals(CSV_COLUMNS, csvReader.getColumnNames());
            assertIterableEquals(CSV_ROWS, actualRows);
        }
    }

    @Test
    void testReadCsvWithBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
        byte[] csvBytes = "A,B\n1,2\n".getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, withBom, 0, bom.length);
        System.arraycopy(csvBytes, 0, withBom, bom.length, csvBytes.length);

        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (InputStream in = new ByteArrayInputStream(withBom);
             CsvReader csvReader = CsvReader.create(rowBuilder, in, arguments)) {
            csvReader.readAll();
            assertEquals(List.of("A", "B"), csvReader.getColumnNames());
            assertEquals(List.of(List.of("1", "2")), actualRows);
        }
    }

    @Test
    void testCsvReaderGettersSettersAndPartialReading() throws IOException {
        Arguments arguments = Arguments.of(
                Arguments.createEntry(CsvReader.READ_COLUMN_NAMES, false),
                Arguments.createEntry(CsvReader.IGNORE_EXCESSIVE_FIELDS, true),
                Arguments.createEntry(CsvReader.IGNORE_MISSING_FIELDS, true)
        );
        List<List<String>> actualRows = new ArrayList<>();
        CsvReader.RowBuilder rowBuilder = new CsvReader.ListRowBuilder(actualRows::add);
        try (BufferedReader reader = new BufferedReader(new StringReader("a,b,c\n1,2,3\n4,5,6\n7,8,9\n"));
             CsvReader csvReader = CsvReader.create(rowBuilder, reader, arguments)) {

            assertTrue(csvReader.getIgnoreExcessFields());
            assertTrue(csvReader.getIgnoreMissingFields());

            csvReader.setIgnoreExcessFields(false);
            csvReader.setIgnoreMissingFields(false);
            org.junit.jupiter.api.Assertions.assertFalse(csvReader.getIgnoreExcessFields());
            org.junit.jupiter.api.Assertions.assertFalse(csvReader.getIgnoreMissingFields());

            csvReader.setColumnNames(List.of("ColA", "ColB", "ColC"));
            assertEquals("ColA", csvReader.getColumnName(0));
            assertEquals("ColB", csvReader.getColumnName(1));
            assertEquals("ColC", csvReader.getColumnName(2));

            assertEquals(0, csvReader.getRowsRead());
            assertEquals(0, csvReader.getRowNumber());

            // ignore 1 row
            assertEquals(1, csvReader.ignoreRows(1));
            // read next 1 row
            assertEquals(1, csvReader.readSome(1));
            assertEquals(1, csvReader.getRowsRead());

            // read remaining
            csvReader.readAll();
            assertEquals(3, csvReader.getRowsRead());
            assertEquals(3, actualRows.size());
        }
    }

    @Test
    void testCsvIoGetOptions() {
        Arguments arguments = Arguments.of();
        CsvIo csvIo = new CsvIo(arguments) {
            @Override
            public void close() {/* do nothing */}
        };
        assertNotNull(csvIo.getOptions());
        org.junit.jupiter.api.Assertions.assertFalse(csvIo.getOptions().isEmpty());
    }
}
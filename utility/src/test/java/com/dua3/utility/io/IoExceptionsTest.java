package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IoExceptionsTest {

    @Test
    void testAbsolutePathException() {
        AbsolutePathException ex2 = new AbsolutePathException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        AbsolutePathException ex3 = new AbsolutePathException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        AbsolutePathException ex4 = new AbsolutePathException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testFolderNotEmptyException() {
        FolderNotEmptyException ex2 = new FolderNotEmptyException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        FolderNotEmptyException ex3 = new FolderNotEmptyException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        FolderNotEmptyException ex4 = new FolderNotEmptyException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testIllegalPathException() {
        IllegalPathException ex2 = new IllegalPathException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        IllegalPathException ex3 = new IllegalPathException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        IllegalPathException ex4 = new IllegalPathException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testNotAFolderException() {
        NotAFolderException ex2 = new NotAFolderException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        NotAFolderException ex3 = new NotAFolderException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        NotAFolderException ex4 = new NotAFolderException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testObjectExistsException() {
        ObjectExistsException ex2 = new ObjectExistsException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        ObjectExistsException ex3 = new ObjectExistsException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        ObjectExistsException ex4 = new ObjectExistsException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testObjectNotFoundException() {
        ObjectNotFoundException ex2 = new ObjectNotFoundException("msg");
        assertEquals("msg", ex2.getMessage());

        Throwable cause = new RuntimeException("cause");
        ObjectNotFoundException ex3 = new ObjectNotFoundException("msg", cause);
        assertEquals("msg", ex3.getMessage());
        assertSame(cause, ex3.getCause());

        ObjectNotFoundException ex4 = new ObjectNotFoundException(cause);
        assertSame(cause, ex4.getCause());
    }

    @Test
    void testCsvFormatException() {
        java.net.URI uri = java.net.URI.create("file:///test.csv");
        CsvFormatException ex1 = new CsvFormatException("invalid csv", uri, 12);
        assertEquals("[file:///test.csv:12] invalid csv", ex1.getMessage());

        CsvFormatException ex3 = new CsvFormatException("invalid csv", null, 5);
        assertEquals("[5] invalid csv", ex3.getMessage());
    }
}

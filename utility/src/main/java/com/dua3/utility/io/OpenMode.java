package com.dua3.utility.io;

/**
 * Mode for opening files.
 */
public enum OpenMode {
    /**
     * None of reading and writing.
     */
    NONE(0),
    /**
     * Open file for reading.
     */
    READ(1),
    /**
     * Open file for writing.
     */
    WRITE(2),
    /**
     * Open file for reading and/or writing.
     */
    READ_AND_WRITE(3);

    int n;

    OpenMode(int n) {
        this.n = n;
    }
}

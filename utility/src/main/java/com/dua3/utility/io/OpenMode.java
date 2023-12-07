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

    final int n;

    OpenMode(int n) {
        this.n = n;
    }

    /**
     * Test if the functionality provided by this mode is a superset of that provided by another mode.
     * <p>
     * <b>Example:</b>
     * {@code OpenMode.READ.includes(OpenMode.READ_AND_WRITE) = false}
     * {@code OpenMode.READ_AND_WRITE.includes(OpenMode.READ) = true}
     * </p>
     *
     * @param other the other mode
     * @return true, if all of other mode's functionality is included in this mode
     */
    public boolean isIncluded(OpenMode other) {
        return (n & other.n) == other.n;
    }
}

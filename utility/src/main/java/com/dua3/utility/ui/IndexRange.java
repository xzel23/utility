package com.dua3.utility.ui;

/**
 * Toolkit-neutral text index range.
 *
 * @param start start offset (inclusive)
 * @param end end offset (exclusive)
 */
public record IndexRange(int start, int end) {
    /**
     * Returns start offset (inclusive).
     *
     * @return start offset
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns end offset (exclusive).
     *
     * @return end offset
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns range length.
     *
     * @return range length
     */
    public int getLength() {
        return end - start;
    }

    /**
     * Returns range length.
     *
     * @return range length
     */
    public int length() {
        return getLength();
    }
}

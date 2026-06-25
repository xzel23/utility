package com.dua3.utility.ui;

/**
 * Changed interval between two text snapshots.
 *
 * @param start first differing offset
 * @param endInCurrent end offset in current/original text
 * @param endInUpdated end offset in updated text
 */
public record ChangeRange(int start, int endInCurrent, int endInUpdated) {
    /**
     * Indicates whether the range is empty.
     *
     * @return {@code true} if both texts are equal in this range representation
     */
    public boolean isEmpty() {
        return start == endInCurrent && start == endInUpdated;
    }
}

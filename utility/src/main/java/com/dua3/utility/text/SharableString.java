// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

/**
 * A class that provides a sharable view of a {@link String} by implementing
 * the {@link CharSequence} interface. It allows operations of {@link String}
 * while enabling efficient sharing and subsequencing without copying
 * underlying character data.
 */
public final class SharableString implements CharSequence {

    private final String base;

    /**
     * Constructs a new SharableString using the specified string.
     *
     * @param s the underlying string to be shared and viewed through this SharableString
     */
    public SharableString(String s) {
        this.base = s;
    }

    @Override
    public int length() {
        return base.length();
    }

    @Override
    public char charAt(int index) {
        return base.charAt(index);
    }

    @Override
    public SharedString subSequence(int start, int end) {
        return new SharedString(base, start, end);
    }

    @Override
    public String toString() {
        return base;
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj || (obj instanceof SharableString ss) && (ss.base.equals(base));
    }

    /**
     * Finds the first occurrence of a specified character in the sequence starting
     * from a given index.
     *
     * @param c the character to locate within the sequence
     * @param start the starting index from which the search begins
     * @return the index of the first occurrence of the specified character, or -1
     *         if the character is not found
     */
    public int indexOf(char c, int start) {
        final int haystackLength = length();
        for (int i = start; i < haystackLength; i++) {
            if (charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
}

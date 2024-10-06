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
public class SharableString implements CharSequence {

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
        if (this == obj) {
            return true;
        }
        if (obj instanceof SharableString anotherString) {
            //noinspection CallToSuspiciousStringMethod
            return base.equals(anotherString.base);
        }
        return false;
    }
}

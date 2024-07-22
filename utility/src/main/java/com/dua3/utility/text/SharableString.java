// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;

public class SharableString implements CharSequence {

    private final String base;

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

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Objects;

public class SharableString implements CharSequence {

    private final String base;

    public SharableString(String s) {
        this.base = Objects.requireNonNull(s);
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
    public SharedString subSequence(int s, int e) {
        return new SharedString(base, s, e);
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
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof SharableString) {
            SharableString anotherString = (SharableString) anObject;
            return base.equals(anotherString.base);
        }
        return false;
    }
}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * A class that provides a shared view of a portion of a base string, implementing
 * the {@link CharSequence} interface. This allows efficient sharing of substrings
 * without copying the underlying character data.
 */
public final class SharedString implements CharSequence {

    private final String base;
    private final int start;
    private final int end;
    private int hash;

    SharedString(String base, int start, int end) {
        this.base = base;
        this.start = Objects.checkIndex(start, base.length());
        Objects.checkFromToIndex(start, end, base.length());
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        return base.charAt(start + Objects.checkIndex(index, end));
    }

    @Override
    public SharedString subSequence(int start, int end) {
        LangUtil.check(end >= start && this.start + end <= this.end);
        return new SharedString(base, this.start + start, this.start + end);
    }

    @Override
    public String toString() {
        return base.substring(start, end);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && end != start) {
            int len = end - start;
            for (int i = 0; i < len; i++) {
                //noinspection CharUsedInArithmeticContext - by design
                h = 31 * h + base.charAt(start + i);
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SharedString anotherString) {
            int n = length();
            if (n == anotherString.length()) {
                return IntStream.range(0, n).noneMatch(i -> anotherString.charAt(i) != charAt(i));
            }
        }
        return false;
    }
}

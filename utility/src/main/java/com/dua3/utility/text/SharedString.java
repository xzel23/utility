// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Objects;

import com.dua3.utility.lang.LangUtil;

public class SharedString implements CharSequence {

    private final String base;
    private final int start;
    private final int end;
    private int hash = 0;

    SharedString(String base, int start, int end) {
        this.base = Objects.requireNonNull(base);
        LangUtil.checkIndex(start, base.length());
        this.start = start;
        LangUtil.check(end >= start && end <= base.length());
        this.end = end;
    }

    @Override
    public int length() {
        return end-start;
    }

    @Override
    public char charAt(int index) {
        LangUtil.checkIndex(index, end);
        return base.charAt(start+index);
    }

    @Override
    public SharedString subSequence(int s, int e) {
        LangUtil.check(e>=s && this.start+e<=this.end);
        return new SharedString(base, this.start+s, this.start+e);
    }
    
    @Override
    public String toString() {
        return base.substring(start, end);
    }
    
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && end!=start) {
            int len = end-start;
            for (int i = 0; i < len; i++) {
                h = 31 * h + base.charAt(start+i);
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof SharedString) {
            SharedString anotherString = (SharedString)anObject;
            int n = length();
            if (n == anotherString.length()) {
                for (int i=0;i<n; i++) {
                    if (anotherString.charAt(i)!=charAt(i)) {                        
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

public class ToStringBuilder {
    private final StringBuilder sb = new StringBuilder();
    private String sep = "";

    public ToStringBuilder() {
        sb.append("[");
    }

    public ToStringBuilder add(String name, Object value) {
        sb.append(sep);
        sb.append(name).append(":").append(String.valueOf(value));
        sep = ", ";
        return this;
    }

    @Override
    public String toString() {
        return sb.toString() + "]"; // do not append so that more items can be added
    }
}
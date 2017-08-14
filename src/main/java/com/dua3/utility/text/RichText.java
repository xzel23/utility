/*
 * Copyright 2016 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A class for rich text, i.e. text together with attributes like color, font etc.
 * <p>Sequences of characters that share the same formatting attributes form a {@link Run}.
 */
public class RichText
        implements Iterable<Run> {

    private static final RichText EMPTY_TEXT = RichText.valueOf("");

    private static final Comparator<RichText> COMPARATOR = (RichText o1, RichText o2) -> o1.text.compareTo(o2.text);

    private static final Comparator<RichText> COMPARATOR_CASE_INSENSITIVE = (RichText o1, RichText o2) -> o1.text
            .compareToIgnoreCase(o2.text);

    /**
     * Get comparator.
     *
     * <b>Note:</b> This ordering is inconsistent with equals because the
     * comparator returned only compares texts and completely ignores
     * formatting. In consequence, the comparator violates the condition
     * {@code (x.compareTo(y)==0) == (x.equals(y)) } if {@code x} and {@code y}
     * are instances of {@code RichText} that differ only in formatting.
     *
     * @return comparator for instances of {@code RichText}.
     */
    public static Comparator<RichText> comparator() {
        return COMPARATOR;
    }

    /**
     * Get case insensitive comparator. <b>Note:</b> this ordering is
     * inconsistent with equals.
     *
     * @return case insensitive comparator for instances of {@code RichText}.
     */
    public static Comparator<RichText> comparatorIgnoreCase() {
        return COMPARATOR_CASE_INSENSITIVE;
    }

    /**
     * Returns the empty String as RichText.
     * @return the empty text
     */
    public static RichText emptyText() {
        return EMPTY_TEXT;
    }

    public static RichText valueOf(Object o) {
        return valueOf(String.valueOf(o));
    }

    /**
     * Convert String to RichText.
     * @param s String to convert
     * @return RichText representation of s
     */
    public static RichText valueOf(String s) {
        return new RichText(s, Arrays.asList(new Run(s, 0, s.length(), TextAttributes.none())));
    }

    private final String text;

    private final List<Run> runs;

    RichText(String text, List<Run> runs) {
        this.text = Objects.requireNonNull(text);
        this.runs = runs;
    }

    RichText(String text, Run[] runs) {
        this(text, Arrays.asList(runs));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RichText other = (RichText) obj;
        return runs.equals(other.runs);
    }

    @Override
    public int hashCode() {
        return text.hashCode() + 17 * runs.size();
    }

    /**
     * Test if empty.
     * @return true, if the text is empty.
     */
    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public Iterator<Run> iterator() {
        return runs.iterator();
    }

    /**
     * Length of text in characters.
     * @return the text length
     */
    public int length() {
        return text.length();
    }

    /**
     * A stream of the Runs this text consists of.
     * @return stream of Runs
     */
    public Stream<Run> stream() {
        return runs.stream();
    }

    @Override
    public String toString() {
        return text;
    }

}

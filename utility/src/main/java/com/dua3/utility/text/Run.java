// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dua3.utility.lang.LangUtil;

/**
 * A sequence of characters that share the same properties.
 */
public class Run
        implements CharSequence {

    private final CharSequence text;
    private final int start;
    private final int length;
    private final TextAttributes attributes;

    /**
     * Construct a new Run.
     *
     * @param text
     *               the text that contains the Run
     * @param start
     *               start of Run
     * @param length
     *               length of Run in characters
     * @param style
     *               style for the Run
     */
    Run(CharSequence text, int start, int length, TextAttributes style) {
        LangUtil.check(start >= 0 && start <= text.length() && length >= 0 && start + length <= text.length());

        this.text = Objects.requireNonNull(text);
        this.attributes = Objects.requireNonNull(style);
        this.start = start;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        LangUtil.checkIndex(index, length);
        return text.charAt(start + index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Run other = (Run) obj;
        // it is not sufficient to compare text since even for different
        // strings, the char sequences represented by two runs might be
        // the same.
        if (length != other.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (charAt(i) != other.charAt(i)) {
                return false;
            }
        }

        return attributes.equals(other.attributes);
    }

    /**
     * Get position of end of Run.
     *
     * @return end of Run
     */
    public int getEnd() {
        return start + length;
    }

    /**
     * Get position of start of Run.
     *
     * @return start of Run
     */
    public int getStart() {
        return start;
    }

    /**
     * Get style of this Run.
     *
     * @return style of this Run
     */
    public TextAttributes getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        int h = attributes.hashCode();
        for (int i = 0; i < length; i++) {
            //noinspection CharUsedInArithmeticContext
            h = 31 * h + charAt(i);
        }
        return h;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Run subSequence(int start, int end) {
        return new Run(text, this.start + start, end - start, attributes);
    }

    @Override
    public String toString() {
        return text.subSequence(start, start + length).toString();
    }

    CharSequence base() {
        return text;
    }

    /**
     * Check if run is empty.
     *
     * @return true, if this run does not contain text.
     */
    public boolean isEmpty() {
        return length == 0;
    }

    /**
     * Get the list of styles to apply at this run's start.
     * @return list of styles to apply when this run starts.
     */
    @SuppressWarnings("unchecked")
    public List<Style> getRunStartStyles() {
        return Collections.unmodifiableList((List<Style>) attributes.getOrDefault(TextAttributes.STYLE_START_RUN, Collections.emptyList()));
    }

    /**
     * Get the list of styles to discard at this run's end.
     * @return list of styles to discard when this run ends.
     */
    @SuppressWarnings("unchecked")
    public List<Style> getRunEndStyles() {
        return Collections.unmodifiableList((List<Style>) attributes.getOrDefault(TextAttributes.STYLE_START_RUN, Collections.emptyList()));
    }

}

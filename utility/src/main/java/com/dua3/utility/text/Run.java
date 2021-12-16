// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.lang.LangUtil;
import com.dua3.cabe.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A sequence of characters that share the same properties.
 */
public class Run implements AttributedCharSequence {

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
    Run(@NotNull CharSequence text, int start, int length, @NotNull TextAttributes style) {
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
     * Get the base sequence.
     * @return base seqeunce
     */
    CharSequence base() {
        return text;
    }

    /**
     * Convert index into this {@link RichText} instance to index into base sequence.
     * @return index into base seqeunce
     */
    int convertIndex(int baseIndex){
        return baseIndex-this.start;
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
    public AttributedCharacter attributedCharAt(int index) {
        return AttributedCharacter.create(charAt(index), attributes);
    }

    @Override
    public Run subSequence(int start, int end) {
        return new Run(text, this.start + start, end - start, attributes);
    }

    @Override
    public String toString() {
        return text.subSequence(start, start + length).toString();
    }

    @SuppressWarnings("unchecked")
    public List<Style> getStyles() {
        return (List<Style>) attributes.getOrDefault(RichText.ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList());
    }

    /**
     * Get the FontDef for this style.
     * @return the FontDef
     */
    public FontDef getFontDef() {
        FontDef collected = new FontDef();
        for (Style style: getStyles()) {
            collected.merge(style.getFontDef());
        }
        return collected;
    } 
}

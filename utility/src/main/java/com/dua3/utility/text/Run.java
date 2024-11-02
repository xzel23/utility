// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A sequence of characters that share the same properties.
 */
public final class Run implements AttributedCharSequence {

    private final CharSequence text;
    private final int start;
    private final int length;
    private final TextAttributes attributes;
    private int hash;
    private @Nullable FontDef fd;

    /**
     * Construct a new Run.
     *
     * @param text   the text that contains the Run
     * @param start  start of Run
     * @param length length of Run in characters
     * @param attributes  the {@link TextAttributes} for the Run
     */
    Run(CharSequence text, int start, int length, TextAttributes attributes) {
        LangUtil.check(start >= 0 && start <= text.length() && length >= 0 && start + length <= text.length());

        this.text = text;
        this.attributes = attributes;
        this.start = start;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        return text.charAt(start + Objects.checkIndex(index, length));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Run other) || hashCode() != obj.hashCode()) {
            return false;
        }

        return other == this || equals(other, TextAttributes::equals);
    }

    /**
     * Compare text ignoring attributes.
     *
     * @param other the object to compare to
     * @return result true, if obj is an instance of Run and its characters compare equal
     */
    public boolean textEquals(@Nullable Run other) {
        if (other == null || length != other.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (charAt(i) != other.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare using a user supplied predicate for comparing TextAttributes.
     *
     * @param other            the object to compare to
     * @param attributesEquals the BiPredicate used for comparing
     * @return result true, if obj is an instance of Run and all runs compare as equal to this instance's
     * runs using the supplied predicate
     */
    public boolean equals(@Nullable Run other, BiPredicate<? super TextAttributes, ? super TextAttributes> attributesEquals) {
        return textEquals(other) && attributesEquals.test(attributes, other.attributes);
    }

    /**
     * Get the end position of this Run.
     *
     * @return end of Run
     */
    public int getEnd() {
        return start + length;
    }

    /**
     * Get the start position this Run.
     *
     * @return start of Run
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the base sequence.
     *
     * @return base sequence
     */
    CharSequence base() {
        return text;
    }

    /**
     * Convert index into this {@link RichText} instance to index into base sequence.
     *
     * @return index into base sequence
     */
    int convertIndex(int baseIndex) {
        return baseIndex - start;
    }

    /**
     * Get style of this Run.
     *
     * @return style of this Run
     */
    public TextAttributes getAttributes() {
        return attributes;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && length > 0) {
            h = attributes.hashCode();
            for (int i = 0; i < length; i++) {
                //noinspection CharUsedInArithmeticContext
                h = 31 * h + charAt(i);
            }
            hash = h;
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

    /**
     * Get text attributes
     *
     * @return the text attributes
     */
    public TextAttributes attributes() {
        return attributes;
    }

    /**
     * Get list of styles.
     *
     * @return the list of styles
     */
    @SuppressWarnings("unchecked")
    public List<Style> getStyles() {
        return (List<Style>) attributes().getOrDefault(RichText.ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList());
    }

    /**
     * Get the FontDef for this style.
     *
     * @return the FontDef
     */
    public FontDef getFontDef() {
        if (fd == null) {
            FontDef collected = new FontDef();
            for (Style style : getStyles()) {
                collected.merge(style.getFontDef());
            }
            fd = collected;
        }
        return fd;
    }
}

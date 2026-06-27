package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;

import java.io.IOException;

/**
 * Toolkit-independent rich-text editing helpers.
 */
public final class RichTextEditUtil {

    private RichTextEditUtil() {
        // utility class
    }

    /**
     * Returns a detached rich-text copy of plain text.
     *
     * @param text source text
     * @return detached rich text
     */
    public static RichText detachedRichText(CharSequence text) {
        if (text.isEmpty()) {
            return RichText.emptyText();
        }

        RichTextBuilder builder = new RichTextBuilder(text.length());
        builder.append(text);
        return builder.toRichText();
    }

    /**
     * Returns a detached rich-text subsequence.
     *
     * @param text source rich text
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @return detached subsequence
     */
    public static RichText detachedSubSequence(RichText text, int start, int end) {
        int s = Math.clamp(Math.min(start, end), 0, text.length());
        int e = Math.clamp(Math.max(start, end), 0, text.length());
        if (s == e) {
            return RichText.emptyText();
        }

        RichTextBuilder builder = new RichTextBuilder(e - s);
        text.appendTo(builder, s, e);
        return builder.toRichText();
    }

    /**
     * Writes plain text representation (without split markers) to an appendable.
     *
     * @param text rich text
     * @param appendable append target
     * @throws IOException if writing fails
     */
    public static void appendPlainText(RichText text, Appendable appendable) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != RichText.SPLIT_MARKER) {
                appendable.append(c);
            }
        }
    }

    /**
     * Finds the minimal changed range between current and updated snapshots.
     *
     * @param current current text
     * @param updated updated text
     * @return changed range
     */
    public static ChangeRange findChangedRange(RichText current, RichText updated) {
        int prefix = current.commonPrefixLength(updated);
        int maxSuffix = Math.min(current.length(), updated.length()) - prefix;
        int suffix = Math.min(current.commonSuffixLength(updated), maxSuffix);
        return new ChangeRange(prefix, current.length() - suffix, updated.length() - suffix);
    }

    /**
     * Computes start offset of the previous word.
     *
     * @param text plain text
     * @param from starting offset
     * @return previous-word start
     */
    public static int previousWordStart(CharSequence text, int from) {
        int i = Math.clamp(from, 0, text.length());
        if (i == 0) {
            return 0;
        }

        do {
            i--;
        } while (i > 0 && !isWordChar(text.charAt(i)));

        while (i > 0 && isWordChar(text.charAt(i - 1))) {
            i--;
        }

        return i;
    }

    /**
     * Computes start offset of the next word.
     *
     * @param text plain text
     * @param from starting offset
     * @return next-word start
     */
    public static int nextWordStart(CharSequence text, int from) {
        int i = Math.clamp(from, 0, text.length());
        int n = text.length();
        while (i < n && isWordChar(text.charAt(i))) {
            i++;
        }
        while (i < n && !isWordChar(text.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Computes end offset of the next word.
     *
     * @param text plain text
     * @param from starting offset
     * @return next-word end
     */
    public static int nextWordEnd(CharSequence text, int from) {
        int i = Math.clamp(from, 0, text.length());
        int n = text.length();
        while (i < n && !isWordChar(text.charAt(i))) {
            i++;
        }
        while (i < n && isWordChar(text.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Computes the word/non-word range around a position.
     *
     * @param text plain text
     * @param position probe position
     * @return detected range
     */
    public static WordRange wordRangeAt(CharSequence text, int position) {
        int n = text.length();
        if (n == 0) {
            return new WordRange(0, 0);
        }

        int p = Math.clamp(position, 0, n);
        if (p == n) {
            p--;
        }

        boolean word = isWordChar(text.charAt(p));
        int start = p;
        int end = p;
        while (start > 0 && isWordChar(text.charAt(start - 1)) == word) {
            start--;
        }
        while (end < n && isWordChar(text.charAt(end)) == word) {
            end++;
        }

        return new WordRange(start, end);
    }

    /**
     * Indicates whether a character is considered part of a word.
     *
     * @param c character
     * @return {@code true} for word character
     */
    public static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Word/non-word range.
     *
     * @param start range start (inclusive)
     * @param end range end (exclusive)
     */
    public record WordRange(int start, int end) {}
}

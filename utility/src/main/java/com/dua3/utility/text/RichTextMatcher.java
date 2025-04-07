package com.dua3.utility.text;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RichText} version of the {@link Matcher} class.
 */
public class RichTextMatcher implements MatchResult {

    private final RichText text;
    private final Matcher matcher;

    RichTextMatcher(Pattern pattern, RichText text) {
        this.text = text;
        this.matcher = pattern.matcher(text);
    }

    @Override
    public int start() {
        return matcher.start();
    }

    @Override
    public int start(int group) {
        return matcher.start(group);
    }

    @Override
    public int end() {
        return matcher.end();
    }

    @Override
    public int end(int group) {
        return matcher.end(group);
    }

    @Override
    public String group() {
        return matcher.group();
    }

    /**
     * Returns the input subsequence matched by the previous match, see {@link Matcher#group()}.
     *
     * @return the (possibly empty) subsequence matched by the previous match, in {@link RichText} form
     * @throws IllegalStateException if no match has yet been attempted, or if the previous match operation failed
     */
    public RichText rgroup() {
        return text.subSequence(start(), end());
    }

    @Override
    public String group(int group) {
        return matcher.group(group);
    }

    /**
     * Returns the input subsequence captured by the given group during the previous match operation, see {@link Matcher#group(int)}.
     *
     * @param group the index of a capturing group in this matcher's pattern
     * @return the (possibly empty) subsequence captured by the group during the previous match, or null if the group failed to match part of the input
     * @throws IllegalStateException     if no match has yet been attempted, or if the previous match operation failed
     * @throws IndexOutOfBoundsException if there is no capturing group in the pattern with the given index
     */
    public RichText rgroup(int group) {
        return text.subSequence(start(group), end(group));
    }

    @Override
    public int groupCount() {
        return matcher.groupCount();
    }

    /**
     * Attempts to find the next occurrence of the pattern within the input sequence.
     *
     * @return true if the pattern is found within the input sequence, false otherwise
     * See {@link Matcher#find()}.
     */
    public boolean find() {
        return matcher.find();
    }

    /**
     * Attempts to find the next occurrence of the pattern within the input sequence,
     * starting at the specified position.
     *
     * @param start the position in the input sequence to start searching for the pattern
     * @return true if the pattern is found starting from the specified position, false otherwise
     * See {@link Matcher#find(int)}.
     */
    public boolean find(int start) {
        return matcher.find(start);
    }

    /**
     * Replaces the first occurrence of the pattern in the input sequence with the specified replacement.
     *
     * @param replacement the sequence to be substituted for the first match
     * @return a new {@code RichText} instance with the first occurrence of the pattern replaced
     * See {@link Matcher#replaceFirst(String)}.
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText replaceFirst(CharSequence replacement) {
        return replace(replacement, 1);
    }

    /**
     * Replaces all occurrences of the pattern in the input sequence with the specified replacement.
     *
     * @param replacement the sequence to be substituted for each match
     * @return a new {@code RichText} instance with all occurrences of the pattern replaced
     * See {@link Matcher#replaceAll(String)}.
     */
    public RichText replaceAll(CharSequence replacement) {
        return replace(replacement, Integer.MAX_VALUE);
    }

    private RichText replace(CharSequence replacement, int maxOccurrences) {
        boolean found = find();
        if (!found) {
            return text;
        }

        RichTextBuilder rtb = new RichTextBuilder(text.length() + replacement.length());
        int off, i;
        for (off = 0, i = 0; i++ < maxOccurrences && found; off = end(), found = find()) {
            if (replacement instanceof ToRichText trt) {
                rtb.append(text.subSequence(off, start())).append(trt.toRichText());
            } else {
                rtb.append(text.subSequence(off, start())).append(replacement);
            }
        }

        rtb.append(text.subSequence(off, text.length()));
        return rtb.toRichText();
    }
}

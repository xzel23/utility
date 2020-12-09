package com.dua3.utility.text;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link RichText} version of the {@link Matcher} class.
 */
public class RichTextMatcher implements MatchResult {

    private final Pattern parentPattern;
    private final RichText text;
    private final Matcher matcher;
    
    RichTextMatcher(Pattern pattern, RichText text) {
        this.parentPattern = Objects.requireNonNull(pattern);
        this.text = Objects.requireNonNull(text);
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
     * @param group the index of a capturing group in this matcher's pattern
     * @return the (possibly empty) subsequence captured by the group during the previous match, or null if the group failed to match part of the input
     * @throws IllegalStateException if no match has yet been attempted, or if the previous match operation failed
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
     * See {@link Matcher#find()}.
     */
    public boolean find() {
        return matcher.find();
    }
    
    /**
     * See {@link Matcher#find(int)}.
     */
    public boolean find(int start) {
        return matcher.find(start);
    }
    
}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for rich text, i.e. text together with attributes like color, font
 * etc.
 * <p>
 * Sequences of characters that share the same formatting attributes form a
 * {@link Run}.
 */
@SuppressWarnings("MagicCharacter")
public final class RichText
        implements Iterable<Run>, AttributedCharSequence, ToRichText {

    /**
     * A character used as a delimiter or marker to insert where subsequent text segment should not be
     * joined into a single one.
     * <p>
     * When two texts both marked to have italic style are joined, the {@code RichTextBuilder} class
     * will not detect the attribute change and for example when converted to HTML, the result of the
     * joined texts will not be "&lt;i&gt;some text &lt;/i&gt;&lt;i&gt;other text&lt;/i&gt;" but instead
     * "&lt;i&gt;some text other text&lt;i&gt;".
     * <p>
     * For attributes that merely control text appearance, this is usually the desired result. But to
     * prevent two paragraphs to be joined into a single one, at least one character must come between
     * both texts so that the change can be detected. A SPLIT_MARKER can thus be inserted to make sure
     * the paragraphs are not joined.
     * <p>
     * The {@link HtmlConverter} class for example automatically removes SPLIT_MARKER from the output.
     */
    public static final char SPLIT_MARKER = '\u0000';

    static final String ATTRIBUTE_NAME_STYLE_LIST = "__styles";

    private static final RichText EMPTY_TEXT = valueOf("");
    private static final RichText SPACE = valueOf(" ");
    private static final RichText TAB = valueOf("\t");
    private static final RichText NEWLINE = valueOf("\n");
    /**
     * The underlying CharSequence.
     */
    private final CharSequence text;
    private final int start;
    private final int length;
    private final int[] runStart;
    private final Run[] run;
    // calculate the hashCode on demand
    private int textHash = 0;
    private int hash = 0;

    RichText(Run... runs) {
        this.run = Arrays.copyOf(runs, runs.length);
        this.runStart = new int[runs.length];

        if (runs.length == 0) {
            this.text = "";
            this.start = 0;
            this.length = 0;
        } else {
            this.text = run[0].base();
            assert checkAllRunsHaveTextAsBase() : "runs are based on different texts";

            for (int idx = 0; idx < run.length; idx++) {
                runStart[idx] = run[idx].getStart();
            }

            this.start = runStart[0];
            this.length = run[run.length - 1].getEnd() - start;
        }
    }

    /**
     * Returns the empty String as RichText.
     *
     * @return the empty text
     */
    public static RichText emptyText() {
        return EMPTY_TEXT;
    }

    /**
     * Returns RichText containing a single space character.
     *
     * @return RichText.valueOf(" ")
     */
    public static RichText space() {
        return SPACE;
    }

    /**
     * Returns RichText containing a single tabulator.
     *
     * @return RichText.valueOf(" \ t ")
     */
    public static RichText tab() {
        return TAB;
    }

    /**
     * Returns RichText containing a single newline character.
     *
     * @return RichText.valueOf(" \ n ")
     */
    public static RichText newline() {
        return NEWLINE;
    }

    /**
     * Get RichText containing an objects string representation.
     *
     * @param obj the object to convert to RichText
     * @return RichText.valueOf(String.valueOf ( obj))
     */
    public static RichText valueOf(Object obj) {
        return valueOf(String.valueOf(obj));
    }

    /**
     * Convert String to RichText.
     *
     * @param s the String to convert
     * @return RichText representation of s
     */
    public static RichText valueOf(String s) {
        return new RichText(new Run(s, 0, s.length(), TextAttributes.none()));
    }

    /**
     * Convert char to RichText.
     *
     * @param c the character
     * @return RichText containing only the character c
     */
    public static RichText valueOf(char c) {
        return valueOf(Character.toString(c));
    }

    /**
     * Get styled RichText containing an object's string representation.
     *
     * @param obj    the object to convert to RichText
     * @param styles the styles to apply
     * @return RichText.valueOf(String.valueOf ( obj))
     */
    public static RichText valueOf(Object obj, Style... styles) {
        return valueOf(String.valueOf(obj), Arrays.asList(styles));
    }

    /**
     * Get styled RichText containing an object's string representation.
     *
     * @param obj    the object to convert to RichText
     * @param styles the styles to apply
     * @return RichText representation of s
     */
    public static RichText valueOf(Object obj, Collection<Style> styles) {
        String s = String.valueOf(obj);
        RichTextBuilder rtb = new RichTextBuilder((s.length()));
        styles.forEach(rtb::push);
        rtb.append(s);
        styles.forEach(rtb::pop);
        return rtb.toRichText();
    }

    /**
     * Convert char to RichText.
     *
     * @param styles the styles to apply
     * @param c      the character
     * @return RichText containing only the character c
     */
    public static RichText valueOf(char c, Style... styles) {
        return valueOf(Character.toString(c), styles);
    }

    /**
     * Convert char to RichText.
     *
     * @param styles the styles to apply
     * @param c      the character
     * @return RichText containing only the character c
     */
    public static RichText valueOf(char c, Collection<Style> styles) {
        return valueOf(Character.toString(c), styles);
    }

    /**
     * Check if texts and style are equal, ignoring other attributes.
     *
     * @param a text
     * @param b text
     * @return true, if a and b consist of the same characters with the same styling
     */
    public static boolean textAndFontEquals(@Nullable RichText a, @Nullable RichText b) {
        if (a == b) {
            return true;
        }

        if (a == null || !a.equalsText(b)) {
            return false;
        }

        assert a.length == b.length : "a and b should have the same length since textEquals() returned true";

        int step;
        for (int idx = 0; idx < a.length(); idx += step) {
            Run runA = a.runAt(idx);
            Run runB = b.runAt(idx);

            if (!Objects.equals(runA.getFontDef(), runB.getFontDef())) {
                return false;
            }

            step = Math.min(runA.getEnd() - a.start, runB.getEnd() - b.start);
            assert step > 0 : "invalid step: " + step;
        }

        return true;
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(RichText delimiter, RichText... elements) {
        return join(delimiter, List.of(elements));
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(RichText delimiter, Iterable<RichText> elements) {
        RichTextBuilder rtb = new RichTextBuilder();

        RichText d = emptyText();
        for (RichText element : elements) {
            rtb.append(d).append(element);
            d = delimiter;
        }

        return rtb.toRichText();
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, RichText... elements) {
        return join(valueOf(delimiter), elements);
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, Iterable<RichText> elements) {
        return join(valueOf(delimiter), elements);
    }

    /**
     * Create a {@link RichTextMatcher}.
     *
     * @param pattern the pattern
     * @param text    the text
     * @return a matcher
     */
    public static RichTextMatcher matcher(Pattern pattern, RichText text) {
        return new RichTextMatcher(pattern, text);
    }

    /**
     * Create a {@link RichTextJoiner}.
     *
     * @param delimiter the delimiter to use
     * @return the joiner
     */
    public static RichTextJoiner joiner(RichText delimiter) {
        return new RichTextJoiner(delimiter);
    }

    /**
     * Create a {@link RichTextJoiner}.
     *
     * @param delimiter the delimiter to use
     * @param prefix    the prefix
     * @param suffix    the suffix
     * @return the joiner
     */
    public static RichTextJoiner joiner(RichText delimiter,
                                        RichText prefix,
                                        RichText suffix) {
        return new RichTextJoiner(delimiter, prefix, suffix);
    }

    /**
     * Create a {@link RichTextJoiner}.
     *
     * @param delimiter the delimiter to use
     * @return the joiner
     */
    public static RichTextJoiner joiner(String delimiter) {
        return new RichTextJoiner(delimiter);
    }

    /**
     * Create a {@link RichTextJoiner}.
     *
     * @param delimiter the delimiter to use
     * @param prefix    the prefix
     * @param suffix    the suffix
     * @return the joiner
     */
    public static RichTextJoiner joiner(String delimiter,
                                        String prefix,
                                        String suffix) {
        return new RichTextJoiner(delimiter, prefix, suffix);
    }

    private boolean checkAllRunsHaveTextAsBase() {
        boolean ok = true;
        for (Run r : run) {
            //noinspection ObjectEquality - we explicitly want to check for identity not equality
            ok = ok && (r.base() == text);
        }
        return ok;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof RichText other) || other.hashCode() != hashCode() || other.length != length) {
            return false;
        }
        return equals(other, Run::equals);
    }

    /**
     * Compare using a user supplied predicate for comparing Run instances.
     *
     * @param other     the object to compare to
     * @param runEquals the BiPredicate used for comparing
     * @return result true, if obj is an instance of RichText and all runs compare as equal to this instance's
     * runs using the supplied predicate
     */
    public boolean equals(@Nullable RichText other, BiPredicate<? super Run, ? super Run> runEquals) {
        if (other == null || other.length != length) {
            return false;
        }

        // compare contents
        Iterator<Run> iter1 = iterator();
        Iterator<Run> iter2 = other.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (!runEquals.test(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return iter1.hasNext() == iter2.hasNext();
    }

    /**
     * Check if texts and style are equal, ignoring other attributes.
     *
     * @param other the text to compare with
     * @return true, if other consist of the same characters with the same styling as this instance
     */
    public boolean equalsTextAndFont(@Nullable RichText other) {
        return textAndFontEquals(this, other);
    }

    /**
     * Get the index of the run the character at a position belongs to.
     *
     * @param pos the character position
     * @return the run index
     */
    @SuppressWarnings("fallthrough")
    private int runIndex(int pos) {
        final int pos_ = start + pos;
        switch (runStart.length) {
            case 7:
                if (pos_ >= runStart[6]) return 6;
            case 6:
                if (pos_ >= runStart[5]) return 5;
            case 5:
                if (pos_ >= runStart[4]) return 4;
            case 4:
                if (pos_ >= runStart[3]) return 3;
            case 3:
                if (pos_ >= runStart[2]) return 2;
            case 2:
                if (pos_ >= runStart[1]) return 1;
            case 0, 1:
                return 0;
            default:
                // if pos is not contained in the array, binarySearch will return -insert position -1,
                // so -idx-1 will point at the next entry -> we have to subtract 2
                int idx = Arrays.binarySearch(runStart, pos_);
                return idx >= 0 ? idx : -idx - 2;
        }
    }

    /**
     * Textual compare.
     *
     * @param other the {@link CharSequence} to compare to
     * @return true, if this instance contains the same sequence of characters as {@code other}
     */
    public boolean equalsText(@Nullable CharSequence other) {
        if (other == null
                || other.length() != length
                || other instanceof RichText otherRichText && otherRichText.textHash() != textHash()) {
            return false;
        }

        for (int idx = 0; idx < length; idx++) {
            if (other.charAt(idx) != charAt(idx)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Textual compare ignoring case.
     *
     * @param other the {@link CharSequence} to compare to
     * @return true, if this instance contains the same sequence of characters as {@code other}
     */
    public boolean equalsTextIgnoreCase(@Nullable CharSequence other) {
        if (other == null || other.length() != length) {
            return false;
        }

        return toString().equalsIgnoreCase(other.toString());
    }

    private int textHash() {
        int h = textHash;
        if (h == 0 && length > 0) {
            for (int i = 0; i < length; i++) {
                //noinspection CharUsedInArithmeticContext - by design
                h = 31 * h + text.charAt(start + i);
            }
            textHash = h;
        }
        return h;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && length > 0) {
            for (Run r : run) {
                h = 17 * h + r.attributes().hashCode();
            }
            hash = h += textHash();
        }
        return h;
    }

    /**
     * Test if empty.
     *
     * @return true, if the text is empty.
     */
    @Override
    public boolean isEmpty() {
        return length() == 0;
    }

    /**
     * Test if blank.
     *
     * @return {@code true} if this instance is empty or contains only whitespace characters,
     *         {@code false} otherwise
     */
    public boolean isBlank() {
        return length() == 0 || TextUtil.isBlank(this);
    }

    @Override
    public Iterator<Run> iterator() {
        return Arrays.stream(run).iterator();
    }

    /**
     * Length of text in characters.
     *
     * @return the text length
     */
    @Override
    public int length() {
        return length;
    }

    /**
     * A stream of the Runs this text consists of.
     *
     * @return stream of Runs
     */
    public Stream<Run> stream() {
        return Arrays.stream(run);
    }

    @Override
    public String toString() {
        return text.subSequence(start, start + length).toString();
    }

    @Override
    public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + length());
        forEach(builder::appendRun);
    }

    @Override
    public RichText toRichText() {
        return this;
    }

    /**
     * Get stream of lines contained in this instance.
     *
     * @return stream of this text's lines
     */
    public Stream<RichText> lines() {
        return StreamSupport.stream(lineSpliterator(), false);
    }

    /**
     * Get a {@link Spliterator<RichText>} over the lines of this instance.
     *
     * @return spliterator
     */
    private Spliterator<RichText> lineSpliterator() {
        return new Spliterator<>() {
            private int idx = 0;

            @Override
            public boolean tryAdvance(Consumer<? super RichText> action) {
                int split = TextUtil.indexOf(text, '\n', idx);

                if (split < 0) {
                    split = length;
                }

                action.accept(subSequence(idx, split));
                idx = split + 1;
                return idx < length();
            }

            @Override
            public @Nullable Spliterator<RichText> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        };
    }

    /**
     * Get a subrange of this instance.
     *
     * @param start begin index (inclusive)
     * @param end   end index (exclusive)
     * @return RichText instance of the subrange
     */
    @Override
    public RichText subSequence(int start, int end) {
        if (start == 0 && end == length) {
            return this;
        }
        if (end == start) {
            return emptyText();
        }
        if (end == start + 1) {
            Run r = runAt(start);
            int pos = start + this.start - r.getStart();
            return new RichText(r.subSequence(pos, pos + 1));
        }

        int floorKey = runIndex(start);
        int ceilingKey = runIndex(end - 1);
        Run[] subRuns = Arrays.copyOfRange(run, floorKey, ceilingKey + 1);

        Run firstRun = subRuns[0];
        if (firstRun.getStart() < this.start + start) {
            subRuns[0] = firstRun.subSequence(start + this.start - firstRun.getStart(), firstRun.length());
        }

        Run lastRun = subRuns[subRuns.length - 1];
        if (lastRun.getEnd() > this.start + end) {
            subRuns[subRuns.length - 1] = lastRun.subSequence(0, lastRun.length() - (lastRun.getEnd() - (this.start + end)));
        }

        return new RichText(subRuns);
    }

    /**
     * Get a subrange of this instance.
     *
     * @param start start index (inclusive)
     * @return RichText instance of the subrange from beginIndex to the end
     */
    public RichText subSequence(int start) {
        return subSequence(start, length());
    }

    @Override
    public char charAt(int index) {
        return text.charAt(start + index);
    }

    @Override
    public AttributedCharacter attributedCharAt(int index) {
        Run r = runAt(index);
        return r.attributedCharAt(r.convertIndex(start + index));
    }

    /**
     * Remove leading and trailing whitespace, defined as "any character whose codepoint is less than or equal to
     * {@code 'U+0020'} (the space character)".
     * @return copy of this instance with leading and trailing whitespace according to above criteria removed
     * @see String#trim()
     * @see #strip()
     */
    public RichText trim() {
        int st = 0;
        int len = length;
        while ((st < len) && isSimpleWhitespace(charAt(st))) {
            st++;
        }
        while ((st < len) && isSimpleWhitespace(charAt(len - 1))) {
            len--;
        }
        return subSequence(st, len);
    }

    private static boolean isSimpleWhitespace(char ch) {
        //noinspection CharacterComparison - by design
        return ch <= ' ';
    }

    /**
     * Remove leading and trailing whitespace, as defined by Unicode, removed.
     * @return copy of this instance with leading and trailing whitespace (according to Unicode) removed
     * @see String#strip()
     * @see #trim()
     */
    public RichText strip() {
        int st = 0;
        int len = length;
        while ((st < len) && isWhitespace(charAt(st))) {
            st++;
        }
        while ((st < len) && isWhitespace(charAt(len - 1))) {
            len--;
        }
        return subSequence(st, len);
    }

    /**
     * Remove leading whitespace, as defined by Unicode, removed.
     * @return copy of this instance with leading whitespace (according to Unicode) removed
     * @see String#stripLeading()
     */
    public RichText stripLeading() {
        int st = 0;
        while ((st < length) && isWhitespace(charAt(st))) {
            st++;
        }
        return subSequence(st, length);
    }

    /**
     * Remove trailing whitespace, as defined by Unicode, removed.
     * @return copy of this instance with trailing whitespace (according to Unicode) removed
     * @see String#stripTrailing() ()
     */
    public RichText stripTrailing() {
        int len = length;
        while ((0 < len) && isWhitespace(charAt(len - 1))) {
            len--;
        }
        return subSequence(0, len);
    }

    private static boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    /**
     * Wrap RichText in style.
     *
     * @param style the style
     * @return copy of this RichText instance with style applied
     */
    public RichText wrap(Style style) {
        RichTextBuilder rtb = new RichTextBuilder(length);
        rtb.push(style);
        rtb.append(this);
        rtb.pop(style);
        return rtb.toRichText();
    }

    /**
     * Splits this RichText into an array of RichText objects based on the provided regular expression.
     *
     * @param regex the regular expression to use as a delimiter for splitting this RichText
     * @return an array of RichText objects computed by splitting this RichText around matches of the given regular expression
     * @see String#split(String)
     */
    public RichText[] split(String regex) {
        return split(regex, 0);
    }

    /**
     * Splits the current RichText into an array of RichText instances based on the provided pattern.
     *
     * @param pattern the regular expression pattern used to split the RichText. It determines the boundaries for splitting.
     * @return an array of RichText objects resulting from splitting the current RichText based on the given pattern.
     * @see String#split(String)
     */
    public RichText[] split(Pattern pattern) {
        return split(pattern, 0);
    }

    /**
     * Splits the current RichText object into an array of RichText objects based on
     * the given regular expression and limit.
     * <p>
     * The method splits the text into segments using the provided regular expression
     * as the delimiter. The maximum number of resulting segments can be controlled
     * by the limit parameter.
     *
     * @param regex the regular expression used to determine the boundaries for splitting
     * @param limit the maximum number of segments to include in the result; if the limit is zero
     *              or negative, the method will include all segments, including trailing empty strings
     *              if any
     * @return an array of RichText objects resulting from the split operation
     * @see String#split(String, int)
     */
    public RichText[] split(String regex, int limit) {
        return split(Pattern.compile(regex), limit);
    }

    /**
     * Splits the current text into an array of RichText objects based on the given pattern and limit.
     *
     * @param pattern the regular expression pattern used to define delimiters for splitting the text
     * @param limit the maximum number of elements in the result array; if limit is less than or equal to zero,
     *              splits are unlimited; if limit is 0, trailing empty matches are removed from the result
     * @return an array of RichText objects, split from the current text based on the specified pattern and limit
     * @see String#split(String, int)
     */
    public RichText[] split(Pattern pattern, int limit) {
        // create a matcher and split using matcher
        RichTextMatcher m = matcher(pattern, this);
        boolean unlimited = limit <= 0;
        List<RichText> result = new ArrayList<>();
        int index = 0;
        while (m.find() && (unlimited || result.size() < limit - 1)) {
            int end = unlimited || result.size() < limit - 1 ? m.start() : length;
            result.add(subSequence(index, end));
            index = m.end();
        }

        // If no match was found, return this
        if (index == 0) {
            return new RichText[]{this};
        }

        // Add remaining segment and remove empty trailing segments
        if (unlimited || result.size() < limit) {
            result.add(subSequence(index, length()));
        }

        if (limit == 0) {
            LangUtil.removeTrailing(result, RichText::isEmpty);
        }

        return result.toArray(RichText[]::new);
    }

    /**
     * Replaces the first substring of this RichText that matches the given regular expression
     * with the specified replacement RichText.
     *
     * @param regex the regular expression to which this RichText is to be matched
     * @param replacement the RichText to be used as a replacement for the first match
     * @return a new RichText with the first matching substring replaced by the replacement RichText
     * @see String#replaceFirst(String, String)
     */
    public RichText replaceFirst(String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * Replaces the first substring of this RichText object that matches the given
     * regular expression with the specified replacement string.
     *
     * @param regex the regular expression to which the substring should match
     * @param replacement the string to replace the first matching substring
     * @return a new RichText object with the first matching substring replaced
     * @see String#replaceFirst(String, String)
     */
    public RichText replaceFirst(String regex, String replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * Replaces each substring of this RichText that matches the given regular expression
     * with the specified RichText replacement.
     *
     * @param regex the regular expression to which this RichText is matched
     * @param replacement the RichText to be substituted for each match
     * @return a new RichText resulting from replacing all occurrences of the pattern
     *         with the specified replacement
     * @see String#replaceAll(String, String)
     */
    public RichText replaceAll(String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceAll(replacement);
    }

    /**
     * Replaces every occurrence of the specified regular expression
     * in the text with the given replacement string.
     *
     * @param regex the regular expression to be matched
     * @param replacement the string to replace each match
     * @return a new RichText object with the replacements made
     * @see String#replaceAll(String, String)
     */
    public RichText replaceAll(String regex, String replacement) {
        return matcher(Pattern.compile(regex), this).replaceAll(replacement);
    }

    /**
     * Find character.
     *
     * @param ch the character
     * @return the index of the first occurrence of {@code ch}, or -1 if not found
     * @see String#indexOf(int)
     */
    public int indexOf(int ch) {
        return TextUtil.indexOf(this, ch);
    }

    /**
     * Find character.
     *
     * @param ch  the character
     * @param off the starting position
     * @return the position where the char was found or -1 if not found
     * @see String#indexOf(int, int)
     */
    public int indexOf(char ch, int off) {
        return TextUtil.indexOf(this, ch, off);
    }

    /**
     * Return the index of the needle in this RichText instance.
     *
     * @param s the text to find
     * @return the first index, where s is found within this instance
     * @see String#indexOf(String)
     */
    public int indexOf(CharSequence s) {
        return TextUtil.indexOf(this, s);
    }

    /**
     * Return the index of the needle in this RichText instance.
     *
     * @param s         the text to find
     * @param fromIndex the starting position
     * @return the first index, where s is found within this instance
     * @see String#indexOf(String, int)
     */
    public int indexOf(CharSequence s, int fromIndex) {
        return TextUtil.indexOf(this, s, fromIndex);
    }

    /**
     * Test whether this instance starts with the given {@link CharSequence}.
     *
     * @param s the sequence to test
     * @return true, if this instance starts with s
     * @see String#startsWith(String)
     */
    public boolean startsWith(CharSequence s) {
        return TextUtil.startsWith(this, s);
    }

    /**
     * Test if CharSequence is contained.
     *
     * @param s the sequence to search for
     * @return true, if s is contained
     * @see String#contains(CharSequence)
     */
    public boolean contains(CharSequence s) {
        return indexOf(s) >= 0;
    }

    /**
     * Gat styled copy of this instance.
     *
     * @param style the style
     * @return styled copy
     */
    public RichText apply(Style style) {
        RichTextBuilder rtb = new RichTextBuilder(length);
        rtb.append(this);
        rtb.apply(style);
        return rtb.toRichText();
    }

    /**
     * Get active styles at position.
     *
     * @param pos the position (character index)
     * @return (unmodifiable) list of styles
     */
    public List<Style> stylesAt(int pos) {
        return Collections.unmodifiableList(runAt(pos).getStyles());
    }

    /**
     * Get run at position.
     *
     * @param pos the position (character index)
     * @return the Run the character at the given position belongs to
     */
    public Run runAt(int pos) {
        return run[runIndex(pos)];
    }

    /**
     * Get the runs of this instance.
     *
     * @return unmodifiable list of runs
     */
    public List<Run> runs() {
        return List.of(run);
    }

    /**
     * Get a stream containing the runs of this instance.
     *
     * @return stream of runs
     */
    public Stream<Run> runStream() {
        return Arrays.stream(run);
    }

    /**
     * Returns a BiPredicate that checks whether two RichText objects are equal based on the provided ComparisonSettings.
     *
     * @param s the ComparisonSettings to be used for the equality check
     * @return the BiPredicate that checks for equality based on the ComparisonSettings
     */
    public static BiPredicate<@Nullable RichText, @Nullable RichText> equalizer(ComparisonSettings s) {
        BiPredicate<RichText, @Nullable RichText> compareText = s.ignoreCase()
                ? RichText::equalsTextIgnoreCase
                : RichText::equalsText;

        return (a, b) -> {
            if (a == b) {
                return true;
            }
            if (a == null || !compareText.test(a, b)) {
                return false;
            }

            // b cannot be null: if b were null, above test a == null || !compareText.test(a, b) would be true
            assert b != null;

            int step;
            for (int idx = 0; idx < a.length(); idx += step) {
                Run runA = a.runAt(idx);
                Run runB = b.runAt(idx);

                FontDef fda = runA.getFontDef();
                FontDef fdb = runB.getFontDef();

                if (!isIgnoredOrEqual(s.ignoreFontSize(), fda.getSize(), fdb.getSize())
                        || !isIgnoredOrEqual(s.ignoreFontWeight(), fda.getBold(), fdb.getBold())
                        || !isIgnoredOrEqual(s.ignoreItalic(), fda.getItalic(), fdb.getItalic())
                        || !isIgnoredOrEqual(s.ignoreTextColor(), fda.getColor(), fdb.getColor())
                        || !isIgnoredOrEqual(s.ignoreUnderline(), fda.getUnderline(), fdb.getUnderline())
                        || !isIgnoredOrEqual(s.ignoreStrikeThrough(), fda.getStrikeThrough(), fdb.getStrikeThrough())
                        || !isIgnoredOrEqual(s.ignoreFontFamily(), s.fontMapper().apply(fda.getFamily()), s.fontMapper().apply(fdb.getFamily()))) {
                    return false;
                }

                step = Math.min(runA.getEnd() - a.start, runB.getEnd() - b.start);
                assert step > 0 : "invalid step: " + step;
            }

            return true;
        };
    }

    /**
     * Determines if the given condition is considered ignored or if two objects are equal.
     *
     * @param ignored a boolean value indicating whether the condition should be ignored
     * @param a the first object to be compared, can be null
     * @param b the second object to be compared, can be null
     * @return true if the condition is ignored, or if the two objects are equal; false otherwise
     */
    private static boolean isIgnoredOrEqual(boolean ignored, @Nullable Object a, @Nullable Object b) {
        return ignored || Objects.equals(a, b);
    }

    /**
     * Calculates the baseline value based on the maximum descent of fonts within the runs of text.
     *
     * @param defaultFont the default font used as a fallback when deriving fonts from font definitions
     * @return the maximum descent value among all derived fonts; returns 0.0 if no runs are present
     */
    public double getBaseline(Font defaultFont) {
        final FontUtil<?> fu = FontUtil.getInstance();
        return runStream()
                .mapToDouble(r -> fu.deriveFont(defaultFont, r.getFontDef()).getDescent())
                .max()
                .orElse(0.0);
    }

}

// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.dua3.utility.lang.LangUtil.NULL_STRING;

/**
 * A class for rich text, i.e., text together with attributes like color, font
 * etc.
 * <p>
 * Sequences of characters that share the same formatting attributes form a
 * {@link Run}. Use {@link RichTextBuilder} create {@code RichText} instances.
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
    public static final int SPLIT_MARKER = '\u0000';

    static final String ATTRIBUTE_NAME_STYLE_LIST = "__styles";

    private static final RichText EMPTY_TEXT = valueOfInternal("");
    private static final RichText SPACE = valueOfInternal(" ");
    private static final RichText TAB = valueOfInternal("\t");
    private static final RichText NEWLINE = valueOfInternal("\n");
    private static final RichText NULL_TEXT = valueOfInternal(NULL_STRING);
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
    public static RichText valueOf(@Nullable Object obj) {
        return switch (obj) {
            case ToRichText trt -> trt.toRichText();
            case CharSequence cs -> cs.isEmpty() ? EMPTY_TEXT : valueOf(cs.toString());
            case null -> NULL_TEXT;
            default -> valueOf(String.valueOf(obj));
        };
    }

    /**
     * Convert String to RichText.
     *
     * @param s the String to convert
     * @return RichText representation of s
     */
    public static RichText valueOf(@Nullable String s) {
        return switch (s) {
            case "" -> EMPTY_TEXT;
            case " " -> SPACE;
            case "\t" -> TAB;
            case "\n" -> NEWLINE;
            case null -> NULL_TEXT;
            default -> new RichText(new Run(s, 0, s.length(), TextAttributes.none()));
        };
    }

    /**
     * Creates a new RichText instance with the given string and applies the specified style.
     *
     * @param s the source string to be converted to RichText, may be null.
     * @param attributes the attributes to be applied to the RichText instance.
     * @return a RichText instance containing the specified string with the applied style.
     */
    public static RichText valueOf(@Nullable String s, Map<String, @Nullable Object> attributes) {
        return valueOf(s).apply(attributes);
    }

    private static RichText valueOfInternal(String s) {
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
    public static RichText valueOf(@Nullable Object obj, Style... styles) {
        return valueOf(obj, Arrays.asList(styles));
    }

    /**
     * Get styled RichText containing an object's string representation.
     *
     * @param obj    the object to convert to RichText
     * @param styles the styles to apply
     * @return RichText representation of s
     */
    public static RichText valueOf(@Nullable Object obj, Collection<Style> styles) {
        RichTextBuilder rtb = new RichTextBuilder();
        styles.forEach(rtb::push);
        switch (obj) {
            case CharSequence cs -> rtb.append(cs);
            case null -> rtb.append(NULL_TEXT);
            default -> rtb.append(String.valueOf(obj));
        }
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
    @SuppressWarnings("java:S4274") // keep the assertion!
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
    public static RichText join(CharSequence delimiter, CharSequence... elements) {
        return join(delimiter, List.of(elements));
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        RichTextBuilder rtb = new RichTextBuilder();

        CharSequence d = "";
        for (CharSequence element : elements) {
            rtb.append(d).append(element);
            d = delimiter;
        }

        return rtb.toRichText();
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
     * Test if blank.
     *
     * @return {@code true} if this instance is empty or contains only whitespace characters,
     *         {@code false} otherwise
     */
    public boolean isBlank() {
        return isEmpty() || TextUtil.isBlank(this);
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
        CharSequence sequence = text.subSequence(start, start + length);
        StringBuilder sb = new StringBuilder(sequence.length());
        sequence.codePoints().filter(cp -> cp != SPLIT_MARKER).forEach(sb::appendCodePoint);
        return sb.toString();
    }

    @Override
    public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + length());
        forEach(builder::appendRun);
    }

    /**
     * Appends a subset of rich text content to the specified {@code RichTextBuilder}.
     * The content to be appended is determined by the specified start and end indices.
     *
     * @param builder the {@code RichTextBuilder} to which the content will be appended
     * @param from the start index (inclusive) of the content to be appended
     * @param to the end index (exclusive) of the content to be appended
     * @throws IndexOutOfBoundsException if the specified start or end indices are out of bounds
     *                                   or if start is greater than end
     * @throws NullPointerException if {@code builder} is {@code null}
     */
    @Override
    public void appendTo(RichTextBuilder builder, int from, int to) {
        Objects.checkFromToIndex(from, to, length());
        builder.ensureCapacity(builder.length() + to - from);
        forEach(r -> {
            if (r.getEnd() <= from || r.getStart() >= to) {
                return;
            } else if (r.getStart() >= from && r.getEnd() <= to) {
                builder.appendRun(r);
            } else {
                int start = Math.max(from, r.getStart()) - r.getStart();
                int end = Math.min(to, r.getEnd()) - r.getStart();
                builder.appendRun(r.subSequence(start, end));
            }
        });
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
     * Retrieves the text attributes at the specified index.
     *
     * @param idx the index for which the text attributes are requested
     * @return the text attributes associated with the run at the specified index
     */
    public TextAttributes attributesAt(int idx) {
        return runAt(idx).attributes();
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
     * Replaces a text range with the supplied {@code replacement}.
     * <p>
     * This method avoids rebuilding a monolithic backing string. Instead it splices
     * run metadata and uses a segmented shared character sequence as backing storage.
     *
     * @param start start index (inclusive)
     * @param end end index (exclusive)
     * @param replacement replacement rich text
     * @return updated rich text
     */
    public RichText replace(int start, int end, RichText replacement) {
        Objects.requireNonNull(replacement, "replacement");
        Objects.checkFromToIndex(start, end, length);

        if (start == end && replacement.isEmpty()) {
            return this;
        }
        if (start == 0 && end == length) {
            return replacement;
        }

        CharSequence newBase = SegmentedCharSequence.splice(
                new Segment(text, this.start, this.start + start),
                new Segment(replacement.text, replacement.start, replacement.start + replacement.length),
                new Segment(text, this.start + end, this.start + length)
        );

        List<Run> runs = new ArrayList<>(Math.max(1, run.length + replacement.run.length + 4));
        int writePos = 0;
        writePos = appendRuns(runs, newBase, this, 0, start, writePos);
        writePos = appendRuns(runs, newBase, replacement, 0, replacement.length, writePos);
        appendRuns(runs, newBase, this, end, length, writePos);

        return runs.isEmpty() ? emptyText() : new RichText(runs.toArray(Run[]::new));
    }

    /**
     * Returns the number of leading characters that are equal in this text and {@code other},
     * including matching text attributes.
     *
     * @param other other text
     * @return common prefix length
     */
    public int commonPrefixLength(RichText other) {
        Objects.requireNonNull(other, "other");
        if (this == other) {
            return length;
        }

        int limit = Math.min(length, other.length);
        int prefix = 0;

        while (prefix < limit) {
            Run runA = runAt(prefix);
            Run runB = other.runAt(prefix);
            if (!runA.attributes().equals(runB.attributes())) {
                break;
            }

            int runEndA = runA.getEnd() - start;
            int runEndB = runB.getEnd() - other.start;
            int chunkLength = Math.min(limit - prefix, Math.min(runEndA - prefix, runEndB - prefix));
            int matched = matchingPrefixChars(this, other, prefix, prefix, chunkLength);
            prefix += matched;
            if (matched < chunkLength) {
                break;
            }
        }

        return prefix;
    }

    /**
     * Returns the number of trailing characters that are equal in this text and {@code other},
     * including matching text attributes.
     *
     * @param other other text
     * @return common suffix length
     */
    public int commonSuffixLength(RichText other) {
        Objects.requireNonNull(other, "other");
        if (this == other) {
            return length;
        }

        int limit = Math.min(length, other.length);
        int suffix = 0;
        int endThis = length;
        int endOther = other.length;

        while (suffix < limit) {
            Run runThis = runAt(endThis - 1);
            Run runOther = other.runAt(endOther - 1);
            if (!runThis.attributes().equals(runOther.attributes())) {
                break;
            }

            int runStartThis = runThis.getStart() - start;
            int runStartOther = runOther.getStart() - other.start;
            int chunkLength = Math.min(limit - suffix, Math.min(endThis - runStartThis, endOther - runStartOther));
            int matched = matchingSuffixChars(this, other, endThis, endOther, chunkLength);
            suffix += matched;
            endThis -= matched;
            endOther -= matched;
            if (matched < chunkLength) {
                break;
            }
        }

        return suffix;
    }

    private static int matchingPrefixChars(RichText a, RichText b, int startA, int startB, int length) {
        int matched = 0;
        while (matched < length && a.charAt(startA + matched) == b.charAt(startB + matched)) {
            matched++;
        }
        return matched;
    }

    private static int matchingSuffixChars(RichText a, RichText b, int endA, int endB, int length) {
        int matched = 0;
        while (matched < length && a.charAt(endA - matched - 1) == b.charAt(endB - matched - 1)) {
            matched++;
        }
        return matched;
    }

    private static int appendRuns(List<Run> target, CharSequence base, RichText source, int from, int to, int writePos) {
        if (from >= to) {
            return writePos;
        }

        int absFrom = source.start + from;
        int absTo = source.start + to;
        for (Run srcRun : source.run) {
            if (srcRun.getEnd() <= absFrom || srcRun.getStart() >= absTo) {
                continue;
            }

            int segStart = Math.max(srcRun.getStart(), absFrom);
            int segEnd = Math.min(srcRun.getEnd(), absTo);
            int segLength = segEnd - segStart;
            if (segLength <= 0) {
                continue;
            }

            TextAttributes attributes = srcRun.attributes();
            if (!target.isEmpty()) {
                int lastIndex = target.size() - 1;
                Run lastRun = target.get(lastIndex);
                if (lastRun.getEnd() == writePos && lastRun.attributes().equals(attributes)) {
                    target.set(lastIndex, new Run(base, lastRun.getStart(), lastRun.length() + segLength, attributes));
                    writePos += segLength;
                    continue;
                }
            }

            target.add(new Run(base, writePos, segLength, attributes));
            writePos += segLength;
        }

        return writePos;
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
     * Returns a copy of this text with the supplied attributes applied to all characters.
     * Existing attributes are preserved unless overwritten by an entry in {@code attributes}.
     *
     * @param attributes attributes to apply
     * @return copy with the attributes applied
     */
    public RichText apply(Map<String, @Nullable Object> attributes) {
        return apply(attributes, 0, length);
    }

    /**
     * Returns a copy of this text with the supplied attributes applied to a subrange.
     * Existing attributes are preserved unless overwritten by an entry in {@code attributes}.
     *
     * @param attributes attributes to apply
     * @param from start index (inclusive)
     * @param to end index (exclusive)
     * @return copy with the attributes applied to the selected range
     */
    public RichText apply(Map<String, @Nullable Object> attributes, int from, int to) {
        Objects.checkFromToIndex(from, to, length);

        if (from == to) {
            return this;
        }

        return switch (attributes) {
            case Style style -> {
                RichTextBuilder rtb = new RichTextBuilder(length);
                subSequence(0, from).appendTo(rtb);
                rtb.push(style);
                subSequence(from, to).appendTo(rtb);
                rtb.pop(style);
                subSequence(to, length).appendTo(rtb);
                yield rtb.toRichText();
            }
            default -> {
                if (attributes.isEmpty()) {
                    yield this;
                }

                RichTextBuilder rtb = new RichTextBuilder(length);
                subSequence(0, from).appendTo(rtb);
                subSequence(from, to).forEach(r -> rtb.appendRun(withAppliedAttributes(r, attributes)));
                subSequence(to, length).appendTo(rtb);
                yield rtb.toRichText();
            }
        };
    }

    /**
     * Returns a copy of this text with the specified style removed from a subrange.
     * Other attributes are preserved.
     *
     * @param style the style to remove
     * @param start start index (inclusive)
     * @param end end index (exclusive)
     * @return copy with the style removed from the selected range
     */
    public RichText removeStyle(Style style, int start, int end) {
        Objects.checkFromToIndex(start, end, length);

        if (start == end) {
            return this;
        }

        boolean changed = false;
        RichTextBuilder rtb = new RichTextBuilder(length);
        subSequence(0, start).appendTo(rtb);
        for (Run run : subSequence(start, end)) {
            List<Style> styles = run.getStyles();
            if (styles.isEmpty()) {
                rtb.appendRun(run);
                continue;
            }

            List<Style> filteredStyles = styles.stream()
                    .filter(s -> !s.equals(style))
                    .toList();

            if (filteredStyles.size() == styles.size()) {
                rtb.appendRun(run);
                continue;
            }

            changed = true;
            Map<String, @Nullable Object> attributes = new HashMap<>(run.attributes());
            if (filteredStyles.isEmpty()) {
                attributes.remove(ATTRIBUTE_NAME_STYLE_LIST);
            } else {
                attributes.put(ATTRIBUTE_NAME_STYLE_LIST, filteredStyles);
            }
            rtb.appendRun(new Run(run.base(), run.getStart(), run.length(), TextAttributes.of(attributes)));
        }
        subSequence(end, length).appendTo(rtb);
        return changed ? rtb.toRichText() : this;
    }

    private static Run withAppliedAttributes(Run run, Map<String, @Nullable Object> attributes) {
        Map<String, @Nullable Object> merged = new HashMap<>(run.attributes());
        attributes.forEach(merged::put);
        return new Run(run.base(), run.getStart(), run.length(), TextAttributes.of(merged));
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

    private record Segment(CharSequence base, int start, int end) {
        private Segment {
            Objects.checkFromToIndex(start, end, base.length());
        }

        private int length() {
            return end - start;
        }
    }

    private static final class SegmentedCharSequence implements CharSequence {
        private static final int MAX_SEGMENTS = 512;

        private final Segment[] segments;
        private final int[] segmentEnds;
        private final int length;

        private int cachedSegmentIndex = -1;
        private int cachedSegmentStart;
        private int cachedSegmentEnd;

        private SegmentedCharSequence(Segment[] segments, int[] segmentEnds, int length) {
            this.segments = segments;
            this.segmentEnds = segmentEnds;
            this.length = length;
        }

        private static CharSequence splice(Segment... segments) {
            List<Segment> flatSegments = new ArrayList<>(segments.length + 4);
            for (Segment segment : segments) {
                appendSlice(flatSegments, segment.base, segment.start, segment.end);
            }
            return compact(flatSegments);
        }

        private static CharSequence compact(List<Segment> flatSegments) {
            if (flatSegments.isEmpty()) {
                return "";
            }

            if (flatSegments.size() == 1) {
                Segment segment = flatSegments.getFirst();
                return segment.start == 0 && segment.end == segment.base.length()
                        ? segment.base
                        : segment.base.subSequence(segment.start, segment.end);
            }

            int totalLength = 0;
            for (Segment segment : flatSegments) {
                totalLength += segment.length();
            }

            if (flatSegments.size() > MAX_SEGMENTS) {
                StringBuilder sb = new StringBuilder(totalLength);
                for (Segment segment : flatSegments) {
                    sb.append(segment.base, segment.start, segment.end);
                }
                return sb.toString();
            }

            Segment[] segmentArray = flatSegments.toArray(Segment[]::new);
            int[] segmentEnds = new int[segmentArray.length];
            int end = 0;
            for (int i = 0; i < segmentArray.length; i++) {
                end += segmentArray[i].length();
                segmentEnds[i] = end;
            }
            return new SegmentedCharSequence(segmentArray, segmentEnds, totalLength);
        }

        private static void appendSlice(List<Segment> target, CharSequence base, int from, int to) {
            if (from == to) {
                return;
            }
            Objects.checkFromToIndex(from, to, base.length());

            if (base instanceof SegmentedCharSequence segmented) {
                segmented.appendSliceTo(target, from, to);
                return;
            }

            appendSegment(target, new Segment(base, from, to));
        }

        private static void appendSegment(List<Segment> target, Segment segment) {
            if (segment.length() == 0) {
                return;
            }
            if (!target.isEmpty()) {
                int lastIndex = target.size() - 1;
                Segment last = target.get(lastIndex);
                if (last.base == segment.base && last.end == segment.start) {
                    target.set(lastIndex, new Segment(last.base, last.start, segment.end));
                    return;
                }
            }
            target.add(segment);
        }

        private void appendSliceTo(List<Segment> target, int from, int to) {
            if (from == to) {
                return;
            }
            Objects.checkFromToIndex(from, to, length);

            int cursor = 0;
            for (Segment segment : segments) {
                int segmentLength = segment.length();
                int segmentStart = cursor;
                int segmentEnd = cursor + segmentLength;

                if (segmentEnd <= from) {
                    cursor = segmentEnd;
                    continue;
                }
                if (segmentStart >= to) {
                    break;
                }

                int localStart = Math.max(from, segmentStart) - segmentStart;
                int localEnd = Math.min(to, segmentEnd) - segmentStart;
                appendSegment(target, new Segment(segment.base, segment.start + localStart, segment.start + localEnd));
                cursor = segmentEnd;
            }
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            int i = Objects.checkIndex(index, length);
            int segmentIndex = locateSegment(i);
            Segment segment = segments[segmentIndex];
            return segment.base.charAt(segment.start + (i - cachedSegmentStart));
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            Objects.checkFromToIndex(start, end, length);
            if (start == 0 && end == length) {
                return this;
            }
            if (start == end) {
                return "";
            }

            List<Segment> sliced = new ArrayList<>(4);
            appendSliceTo(sliced, start, end);
            return compact(sliced);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(length);
            for (Segment segment : segments) {
                sb.append(segment.base, segment.start, segment.end);
            }
            return sb.toString();
        }

        private int locateSegment(int index) {
            int cached = cachedSegmentIndex;
            if (cached >= 0 && index >= cachedSegmentStart && index < cachedSegmentEnd) {
                return cached;
            }

            int segmentIndex = Arrays.binarySearch(segmentEnds, index + 1);
            if (segmentIndex < 0) {
                segmentIndex = -segmentIndex - 1;
            }

            cachedSegmentIndex = segmentIndex;
            cachedSegmentStart = segmentIndex == 0 ? 0 : segmentEnds[segmentIndex - 1];
            cachedSegmentEnd = segmentEnds[segmentIndex];
            return segmentIndex;
        }
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

                if (isSignificantChange(s.ignoreFontSize(), fda.getSize(), fdb.getSize())
                        || isSignificantChange(s.ignoreFontWeight(), fda.getBold(), fdb.getBold())
                        || isSignificantChange(s.ignoreItalic(), fda.getItalic(), fdb.getItalic())
                        || isSignificantChange(s.ignoreTextColor(), fda.getColor(), fdb.getColor())
                        || isSignificantChange(false, normalizeBackgroundColor(fda.getBackgroundColor()), normalizeBackgroundColor(fdb.getBackgroundColor()))
                        || isSignificantChange(s.ignoreUnderline(), fda.getUnderline(), fdb.getUnderline())
                        || isSignificantChange(s.ignoreStrikeThrough(), fda.getStrikeThrough(), fdb.getStrikeThrough())
                        || isSignificantChange(s.ignoreFontFamily(), s.fontMapper().apply(fda.getFamily()), s.fontMapper().apply(fdb.getFamily()))) {
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
    private static boolean isSignificantChange(boolean ignored, @Nullable Object a, @Nullable Object b) {
        return !ignored && !Objects.equals(a, b);
    }

    private static @Nullable Color normalizeBackgroundColor(@Nullable Color color) {
        return color != null && color.isTransparent() ? null : color;
    }

    /**
     * Calculates the baseline value based on the maximum descent of fonts within the runs of text.
     *
     * @param defaultFont the default font used as a fallback when deriving fonts from font definitions
     * @return the maximum descent value among all derived fonts; returns 0.0 if no runs are present
     */
    public double getBaseline(Font defaultFont) {
        final FontUtil fu = FontUtil.getInstance();
        return runStream()
                .mapToDouble(r -> fu.deriveFont(defaultFont, r.getFontDef()).getDescent())
                .max()
                .orElse(0.0);
    }

}

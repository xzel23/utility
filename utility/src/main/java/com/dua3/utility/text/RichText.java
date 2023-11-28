// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;

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
public final class RichText
        implements Iterable<Run>, AttributedCharSequence, ToRichText {

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
    private transient int textHash;
    private transient int hash;

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

    RichText(List<Run> runs) {
        this(runs.toArray(Run[]::new));
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
        return new RichText(Collections.singletonList(new Run(s, 0, s.length(), TextAttributes.none())));
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
        return valueOf(String.valueOf(obj), List.of(styles));
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

        if (a == null || b == null || !a.equalsText(b)) {
            return false;
        }

        assert a.length == b.length : "a and b should have same length since textEquals() returned true";

        for (int idx = 0; idx < a.length(); ) {
            Run runA = a.runAt(idx);
            Run runB = b.runAt(idx);

            if (!Objects.equals(runA.getFontDef(), runB.getFontDef())) {
                return false;
            }

            int step = Math.min(runA.getEnd() - a.start, runB.getEnd() - b.start);
            assert step > 0 : "invalid step: " + step;
            idx += step;
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
        return join(valueOf(Objects.requireNonNull(delimiter)), elements);
    }

    /**
     * Join RichText instances together.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, Iterable<RichText> elements) {
        return join(valueOf(Objects.requireNonNull(delimiter)), elements);
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
        for (Run run : run) {
            //noinspection ObjectEquality
            ok = ok && (run.base() == text);
        }
        return ok;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof RichText other)) {
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
        if (other == null || other.length != length || other.textHash() != textHash()) {
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
            case 1:
            case 0:
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
        if (other == null || other.length() != length) {
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
            for (Run r : run) {
                h = 17 * h + r.hashCode();
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
                h = 17 * h + r.hashCode();
            }
            hash = h;
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
        stream().forEach(builder::appendRun);
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
            private int idx;

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
            public Spliterator<RichText> trySplit() {
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
     * Get a sub range of this instance.
     *
     * @param start begin index (inclusive)
     * @param end   end index (exclusive)
     * @return RichText instance of the sub range
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
     * Get a sub range of this instance.
     *
     * @param begin begin index (inclusive)
     * @return RichText instance of the sub range from beginIndex to the end
     */
    public RichText subSequence(int begin) {
        return subSequence(begin, length());
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

    private boolean isSimpleWhitespace(char ch) {
        return ch <= '\u0020';
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

    private boolean isWhitespace(char ch) {
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
     * @see String#split(String)
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText[] split(String regex) {
        return split(regex, 0);
    }

    /**
     * @see String#split(String, int)
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText[] split(String regex, int limit) {
        /* fastpath if the regex is a
         * (1) one-char String and this character is not one of the
         *     RegEx's meta characters ".$|()[{^?*+\\", or
         * (2) two-char String and the first char is the backslash and
         *     the second is not the ascii digit or ascii letter.
         */
        char ch;
        if (((regex.length() == 1 &&
                ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) ||
                (regex.length() == 2 &&
                        regex.charAt(0) == '\\' &&
                        (((ch = regex.charAt(1)) - '0') | ('9' - ch)) < 0 &&
                        ((ch - 'a') | ('z' - ch)) < 0 &&
                        ((ch - 'A') | ('Z' - ch)) < 0)) &&
                (ch < Character.MIN_HIGH_SURROGATE ||
                        ch > Character.MAX_LOW_SURROGATE)) {
            int off = 0;
            int next;
            boolean limited = limit > 0;
            List<RichText> list = new ArrayList<>();
            while ((next = indexOf(ch, off)) != -1) {
                if (!limited || list.size() < limit - 1) {
                    list.add(subSequence(off, next));
                    off = next + 1;
                } else {    // last one
                    //assert (list.size() == limit - 1);
                    int last = length();
                    list.add(subSequence(off, last));
                    off = last;
                    break;
                }
            }
            // If no match was found, return this
            if (off == 0)
                return new RichText[]{this};

            // Add remaining segment
            if (!limited || list.size() < limit)
                list.add(subSequence(off, length()));

            // Construct result
            int resultSize = list.size();
            if (limit == 0) {
                while (resultSize > 0 && list.get(resultSize - 1).isEmpty()) {
                    resultSize--;
                }
            }
            return list.subList(0, resultSize).toArray(RichText[]::new);
        }

        // create a matcher and split using matcher
        RichTextMatcher m = matcher(Pattern.compile(regex), this);
        boolean unlimited = limit <= 0;
        List<RichText> result = new ArrayList<>();
        int index = 0;
        while (m.find()) {
            if (unlimited || result.size() < limit - 1) {
                if (index == 0 && m.end() == index) {
                    // skip empty leading substring at beginning
                    continue;
                }
                RichText match = subSequence(index, m.start());
                result.add(match);
                index = m.end();
            } else if (result.size() == limit - 1) { // last one
                RichText match = subSequence(index, length());
                result.add(match);
                index = m.end();
            }
        }

        // If no match was found, return this
        if (index == 0) {
            return new RichText[]{this};
        }

        // Add remaining segment
        if (unlimited || result.size() < limit) {
            result.add(subSequence(index, length()));
        }

        // remove trailing empty segments
        if (limit == 0) {
            int s = result.size();
            while (s > 0 && result.get(s - 1).isEmpty()) {
                s--;
            }
            result = result.subList(0, s);
        }

        return result.toArray(RichText[]::new);
    }

    /**
     * @see String#replaceFirst(String, String)
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText replaceFirst(String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * @see String#replaceFirst(String, String)
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText replaceFirst(String regex, String replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * @see String#replaceAll(String, String)
     */
    @SuppressWarnings("MissingJavadoc")
    public RichText replaceAll(String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceAll(replacement);
    }

    /**
     * @see String#replaceAll(String, String)
     */
    @SuppressWarnings("MissingJavadoc")
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
     */
    public int indexOf(char ch, int off) {
        return TextUtil.indexOf(this, ch, off);
    }

    /**
     * Return the index of the needle in this RichText instance.
     *
     * @param s the text to find
     * @return the first index, where s is found within this instance
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
     */
    public int indexOf(CharSequence s, int fromIndex) {
        return TextUtil.indexOf(this, s, fromIndex);
    }

    /**
     * Test whether this instance starts with the given {@link CharSequence}.
     *
     * @param s the sequence to test
     * @return true, if this instance starts with s
     */
    public boolean startsWith(CharSequence s) {
        return TextUtil.startsWith(this, s);
    }

    /**
     * Test if CharSequence is contained.
     *
     * @param s the sequence to search for
     * @return true, if s is contained
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
     * Returns a BiPredicate that checks whether two RichText objects are equal based on the provided ComparisonSettings.
     *
     * @param s the ComparisonSettings to be used for the equality check
     * @return the BiPredicate that checks for equality based on the ComparisonSettings
     */
    public static BiPredicate<RichText, RichText> equalizer(ComparisonSettings s) {
        return (a, b) -> {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            if (s.ignoreCase() && !a.equalsTextIgnoreCase(b) || !s.ignoreCase() && !a.equalsText(b)) {
                return false;
            }

            for (int idx = 0; idx < a.length(); ) {
                Run runA = a.runAt(idx);
                Run runB = b.runAt(idx);

                FontDef fda = runA.getFontDef();
                FontDef fdb = runB.getFontDef();

                if (!s.ignoreFontSize() && !Objects.equals(fda.getSize(), fdb.getSize())
                        || !s.ignoreTextColor() && !Objects.equals(fda.getColor(), fdb.getColor())
                        || !s.ignoreUnderline() && !Objects.equals(fda.getUnderline(), fdb.getUnderline())
                        || !s.ignoreStrikeThrough() && !Objects.equals(fda.getStrikeThrough(), fdb.getStrikeThrough())
                        || !s.ignoreFontWeight() && !Objects.equals(fda.getBold(), fdb.getBold())
                        || !s.ignoreItalic() && !Objects.equals(fda.getItalic(), fdb.getItalic())
                        || !s.ignoreFontFamily() && !Objects.equals(s.fontMapper().apply(fda.getFamily()), s.fontMapper().apply(fdb.getFamily()))) {
                    return false;
                }

                int step = Math.min(runA.getEnd() - a.start, runB.getEnd() - b.start);
                assert step > 0 : "invalid step: " + step;
                idx += step;
            }

            return true;
        };
    }

}

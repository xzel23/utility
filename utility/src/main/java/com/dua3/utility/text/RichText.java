// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
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
        implements Iterable<Run>, AttributedCharSequence, ToRichText, Comparable<CharSequence> {

    static final String ATTRIBUTE_NAME_STYLE_LIST = "__styles";

    private static final RichText EMPTY_TEXT = RichText.valueOf("");
    private static final RichText SPACE = RichText.valueOf(" ");
    private static final RichText TAB = RichText.valueOf("\t");
    private static final RichText NEWLINE = RichText.valueOf("\n");

    /**
     * Returns the empty String as RichText.
     *
     * @return the empty text
     */
    public static @NotNull RichText emptyText() {
        return EMPTY_TEXT;
    }

    /**
     * Returns RichText containing a single space character.
     *
     * @return RichText.valueOf(" ")
     */
    public static @NotNull RichText space() {
        return SPACE;
    }

    /**
     * Returns RichText containing a single tabulator.
     *
     * @return RichText.valueOf("\t")
     */
    public static @NotNull RichText tab() {
        return TAB;
    }

    /**
     * Returns RichText containing a single newline character.
     *
     * @return RichText.valueOf("\n")
     */
    public static @NotNull RichText newline() {
        return NEWLINE;
    }

    /**
     * Get RichText containing an objects string representation.
     * @param obj the object to convert to RichText
     * @return RichText.valueOf(String.valueOf(obj))
     */
    public static @NotNull RichText valueOf(Object obj) {
        return valueOf(String.valueOf(obj));
    }

    /**
     * Convert String to RichText.
     *
     * @param  s the String to convert
     * @return   RichText representation of s
     */
    public static @NotNull RichText valueOf(@NotNull String s) {
        return new RichText(Collections.singletonList(new Run(s, 0, s.length(), TextAttributes.none())));
    }

    /**
     * Convert char to RichText.
     * 
     * @param c the character
     * @return RichText containing only the character c
     */
    public static @NotNull RichText valueOf(char c) {
        return RichText.valueOf(Character.toString(c));
    }

    /**
     * Get RichText containing an objects string representation.
     * @param obj the object to convert to RichText
     * @param styles the styles to apply
     * @return RichText.valueOf(String.valueOf(obj))
     */
    public static RichText valueOf(Object obj, @NotNull Collection<Style> styles) {
        return valueOf(String.valueOf(obj), styles);
    }

    /**
     * Convert String to RichText.
     *
     * @param  s the String to convert
     * @param styles the styles to apply
     * @return   RichText representation of s
     */
    public static RichText valueOf(@NotNull String s, @NotNull Collection<Style> styles) {
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
     * @param c the character
     * @return RichText containing only the character c
     */
    public static RichText valueOf(char c, @NotNull List<Style> styles) {
        return RichText.valueOf(Character.toString(c), styles);
    }

    /** The underlying CharSequence. */
    private final CharSequence text;
    
    private final int start;
    private final int length;

    private final int @NotNull [] runStart;
    private final Run @NotNull [] run;
    
    @SuppressWarnings("unchecked")
    RichText(Run @NotNull ... runs) {
        this.run = Arrays.copyOf(runs, runs.length);
        this.runStart = new int[runs.length];
        
        if (runs.length==0) {
            this.text="";
            this.start=0;
            this.length=0;
        } else {
            this.text = run[0].base();
            assert checkAllRunsHaveTextAsBase();

            for (int idx=0; idx<run.length; idx++) {
                runStart[idx] = run[idx].getStart();
            }

            this.start = runStart[0];
            this.length = run[run.length-1].getEnd()-this.start;
        }
    }

    RichText(@NotNull List<Run> runs) {
        this(runs.toArray(Run[]::new));
    }

    private boolean checkAllRunsHaveTextAsBase() {
        boolean ok = true;
        for (Run run : run) {
            //noinspection ObjectEquality
            ok &= run.base() == text;
        }
        return ok;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RichText other = (RichText) obj;
        
        // compare the text length and hashcode
        if (length!=other.length() || hashCode()!=other.hashCode()) {
            return false;
        }
        
        // compare contents
        Iterator<Run> iter1 = this.iterator();
        Iterator<Run> iter2 = other.iterator();
        while (iter1.hasNext()&&iter2.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return iter1.hasNext()==iter2.hasNext();
    }
    
    /**
     * Get the index of the run the character at a position belongs to.
     * @param pos the character position
     * @return the run index
     */
    @SuppressWarnings("fallthrough")
    private int runIndex(int pos) {
        final int pos_ = start+pos;
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
                return idx >=0 ? idx : -idx-2;
        }
    }

    /**
     * Textual compare.
     * @param other the {@link CharSequence} to compare to
     * @return true, if the other
     */
    public boolean textEquals(@Nullable CharSequence other) {
        if (other==null || other.length()!=this.length) {
            return false;
        }

        if (this.isEmpty()) {
            // we already know that both sequences have the same length
            return true;
        }

        for (int idx=0; idx<length; idx++) {
            if (other.charAt(idx)!=charAt(idx)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int compareTo(@NotNull CharSequence other) {
        for (int idx=0; idx<length; idx++) {
            char a = charAt(idx);
            char b = other.charAt(idx);
            if (a != b) {
                return a - b;
            }
        }

        return Integer.compare(length, other.length());
    }

    // calculate the hashCode on demand
    private int hash = 0;
    
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && length > 0) {
            for (int i = start; i < start+length; i++) {
                h = 31 * h + text.charAt(i);
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
    public boolean isEmpty() {
        return length()==0;
    }

    @Override
    public @NotNull Iterator<Run> iterator() {
        return Arrays.stream(run).iterator();
    }

    /**
     * Length of text in characters.
     *
     * @return the text length
     */
    public int length() {
        return length;
    }

    /**
     * A stream of the Runs this text consists of.
     *
     * @return stream of Runs
     */
    public @NotNull Stream<Run> stream() {
        return Arrays.stream(run);
    }

    @Override
    public @NotNull String toString() {
        return text.subSequence(start, start+length).toString();
    }

    @Override
    public void appendTo(@NotNull RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + this.length());
        stream().forEach(builder::appendRun);
    }

    @Override
    public @NotNull RichText toRichText() {
        return this;
    }

    /**
     * Get stream of lines contained in this instance.
     * @return stream of lines of this text
     */
    public @NotNull Stream<RichText> lines() {
        return StreamSupport.stream(lineSpliterator(), false);
    }

    /**
     * Get a {@link Spliterator<RichText>} over the lines of this instance.
     * @return spliterator
     */
    private @NotNull Spliterator<RichText> lineSpliterator() {
        return new Spliterator<>() {
            private int idx = 0;

            @Override
            public boolean tryAdvance(@NotNull Consumer<? super RichText> action) {
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
     * Get a sub range of this instance.
     * @param begin begin index (inclusive)
     * @param end end index (exclusive)
     * @return RichText instance of the sub range
     */
    @Override
    public @NotNull RichText subSequence(int begin, int end) {
        if (begin==0 && end==length) {
            return this;
        }
        if (end==begin) {
            return emptyText();
        }
        if (end==begin+1) {
            Run r = runAt(begin);
            int pos = begin + this.start - r.getStart();
            return new RichText(r.subSequence(pos, pos+1));
        }
        
        int floorKey = runIndex(begin);
        int ceilingKey = runIndex(end-1);
        Run[] subRuns = Arrays.copyOfRange(run, floorKey, ceilingKey+1);
        
        Run firstRun = subRuns[0];
        if (firstRun.getStart() < start+begin) {
            subRuns[0] = firstRun.subSequence(begin+start-firstRun.getStart(), firstRun.length());
        }
        
        Run lastRun = subRuns[subRuns.length-1];
        if (lastRun.getEnd() > start+end) {
            subRuns[subRuns.length-1] = lastRun.subSequence(0, lastRun.length()-(lastRun.getEnd()-(start+end)));
        }
        
        return new RichText(subRuns);
    }

    /**
     * Get a sub range of this instance.
     * @param beginIndex begin index (inclusive)
     * @return RichText instance of the sub range from beginIndex to the end
     */
    public @NotNull RichText subSequence(int beginIndex) {
        return subSequence(beginIndex, length());
    }
    
    @Override
    public char charAt(int index) {
        return text.charAt(start+index);
    }

    @Override
    public @NotNull AttributedCharacter attributedCharAt(int pos) {
        Run r = runAt(pos);
        return r.attributedCharAt(r.convertIndex(pos));
    }

    /**
     * See {@link String#trim()}.
     */
    public @NotNull RichText trim() {
        int st = 0;
        int len = length;
        while ((st < len) && Character.isWhitespace(charAt(st))) {
            st++;
        }
        while ((st < len) && Character.isWhitespace(charAt(len - 1))) {
            len--;
        }
        return subSequence(st, len);
    }

    /**
     * Join RichText instances together.
     * @param delimiter the delimiter
     * @param elements the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(RichText delimiter, RichText... elements) {
        return join(delimiter, Arrays.asList(elements));
    }

    /**
     * Join RichText instances together.
     * @param delimiter the delimiter
     * @param elements the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(RichText delimiter, @NotNull Iterable<RichText> elements) {
        RichTextBuilder rtb = new RichTextBuilder();

        RichText d = RichText.emptyText();
        for (RichText element: elements) {
            rtb.append(d).append(element);
            d = delimiter;
        }

        return rtb.toRichText();
    }
    
    /**
     * Join RichText instances together.
     * @param delimiter the delimiter
     * @param elements the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, RichText... elements) {
        return join(RichText.valueOf(Objects.requireNonNull(delimiter)), elements);
    }

    /**
     * Join RichText instances together.
     * @param delimiter the delimiter
     * @param elements the elements to join
     * @return RichText containing the joined elements
     */
    public static RichText join(CharSequence delimiter, @NotNull Iterable<RichText> elements) {
        return join(RichText.valueOf(Objects.requireNonNull(delimiter)), elements);
    }

    public RichText[] split(@NotNull String regex) {
        return split(regex, 0);
    }

    public RichText[] split(@NotNull String regex, int limit) {
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
              (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
              ((ch-'a')|('z'-ch)) < 0 &&
              ((ch-'A')|('Z'-ch)) < 0)) &&
            (ch < Character.MIN_HIGH_SURROGATE ||
             ch > Character.MAX_LOW_SURROGATE))
        {
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

        RichTextMatcher m = matcher(Pattern.compile(regex), this);
        List<RichText> result = new ArrayList<>();
        int off = 0;
        while (m.find(off)) {
            result.add(subSequence(off, m.start()));
            off = m.end();
        }
        result.add(subSequence(off));
        return result.toArray(RichText[]::new);
    }

    /**
     * Replace the first occurence of a regular expresseion.
     * @param regex the regular expression
     * @return this text with the first instances of regex replaced by the replacement
     * @see String#replaceFirst(String, String) 
     */
    public RichText replaceFirst(@NotNull String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * Replace the first occurence of a regular expresseion.
     * @param regex the regular expression
     * @return this text with the first instances of regex replaced by the replacement
     * @see String#replaceFirst(String, String)
     */
    public RichText replaceFirst(@NotNull String regex, String replacement) {
        return matcher(Pattern.compile(regex), this).replaceFirst(replacement);
    }

    /**
     * Replace all occurences a regular expresseion.
     * @param regex the regular expression
     * @return this text with all instances of regex replaced by the replacement
     * @see String#replaceAll(String, String) 
     */
    public RichText replaceAll(@NotNull String regex, RichText replacement) {
        return matcher(Pattern.compile(regex), this).replaceAll(replacement);
    }

    /**
     * Replace all occurences a regular expresseion.
     * @param regex the regular expression
     * @return this text with all instances of regex replaced by the replacement
     * @see String#replaceAll(String, String)
     */
    public RichText replaceAll(@NotNull String regex, String replacement) {
        return matcher(Pattern.compile(regex), this).replaceAll(replacement);
    }

    /**
     * Find character.
     * @param ch the character
     * @return the position of the first occurrence of ch, or -1 if not found
     */
    public int indexOf(char ch) {
        return TextUtil.indexOf(this, ch);
    }

    /**
     * Find character.
     * @param ch the character
     * @param off the starting position
     * @return the position where the char was found or -1 if not found
     */
    public int indexOf(char ch, int off) {
        return TextUtil.indexOf(this, ch, off);
    }

    /**
     * Create a {@link RichTextMatcher}.
     * @param pattern the pattern
     * @param text the text
     * @return a matcher
     */
    public static @NotNull RichTextMatcher matcher(@NotNull Pattern pattern, RichText text) {
        return new RichTextMatcher(pattern, text);
    }

    /**
     * Return the index of the needle in this RichText instance.
     * @param s the text to find
     * @return the first index, where s is found within this instance
     */
    public int indexOf(CharSequence s) {
        return TextUtil.indexOf(this, s);
    }

    /**
     * Return the index of the needle in this RichText instance.
     * @param s the text to find
     * @param fromIndex the starting position
     * @return the first index, where s is found within this instance
     */
    public int indexOf(@NotNull CharSequence s, int fromIndex) {
        return TextUtil.indexOf(this, s, fromIndex);
    }

    /**
     * Test whether this instance starts with the given {@link CharSequence}.
     * @param s the sequence to test
     * @return true, if this instance starts with s
     */
    public boolean startsWith(@NotNull CharSequence s) {
        return TextUtil.startsWith(this, s);
    }

    /**
     * Test if CharSequence is contained.
     * @param s the sequence to search for
     * @return true, if s is contained
     */
    public boolean contains(CharSequence s) {
        return indexOf(s) >= 0;
    }

    /**
     * Gat styled copy of this instance.
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
     * @param pos the position (character index)
     * @return (unmodifiable) list of styles
     */
    public @NotNull List<Style> stylesAt(int pos) {
        return Collections.unmodifiableList(runAt(pos).getStyles());
    }

    /**
     * Get run at position.
     * @param pos the position (character index)
     * @return the Run the character at the given position belongs to
     */
    public Run runAt(int pos) {
        return run[runIndex(pos)];    
    }

    /**
     * Get the runs of this instance.
     * @return unmodifiable list of runs
     */
    public List<Run> runs() {
        return List.of(run);
    }
    
    /**
     * Create a {@link RichTextJoiner}.
     * @param delimiter the delimiter to use
     * @return the joiner
     */
    public static @NotNull RichTextJoiner joiner(RichText delimiter) {
        return new RichTextJoiner(delimiter);
    }

    /**
     * Create a {@link RichTextJoiner}.
     * @param delimiter the delimiter to use
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the joiner
     */
    public static @NotNull RichTextJoiner joiner(RichText delimiter, RichText prefix, RichText suffix) {
        return new RichTextJoiner(delimiter, prefix, suffix);
    }

    /**
     * Create a {@link RichTextJoiner}.
     * @param delimiter the delimiter to use
     * @return the joiner
     */
    public static @NotNull RichTextJoiner joiner(String delimiter) {
        return new RichTextJoiner(delimiter);
    }

    /**
     * Create a {@link RichTextJoiner}.
     * @param delimiter the delimiter to use
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the joiner
     */
    public static @NotNull RichTextJoiner joiner(String delimiter, String prefix, String suffix) {
        return new RichTextJoiner(delimiter, prefix, suffix);
    }

}

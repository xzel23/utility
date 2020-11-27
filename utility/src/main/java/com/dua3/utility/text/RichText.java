// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for rich text, i.e. text together with attributes like color, font
 * etc.
 * <p>
 * Sequences of characters that share the same formatting attributes form a
 * {@link Run}.
 */
public class RichText
        implements Iterable<Run>, AttributedCharSequence, ToRichText, Comparable<CharSequence> {

    private static final RichText EMPTY_TEXT = RichText.valueOf("");

    /**
     * Returns the empty String as RichText.
     *
     * @return the empty text
     */
    public static RichText emptyText() {
        return EMPTY_TEXT;
    }

    public static RichText valueOf(Object o) {
        return valueOf(String.valueOf(o));
    }

    /**
     * Convert String to RichText.
     *
     * @param  s
     *           String to convert
     * @return   RichText representation of s
     */
    public static RichText valueOf(String s) {
        return new RichText(Collections.singletonList(new Run(s, 0, s.length(), TextAttributes.none())));
    }

    /** The underlying CharSequence. */
    private final CharSequence text;
    
    private final int start;
    private final int length;

    /** The a map of text runs for this text with the position of the first character of the Run as key. */
    private final TreeMap<Integer,Run> runs = new TreeMap<>();
    
    RichText(List<Run> runs) {
        if (runs.isEmpty()) {
            this.text="";
            this.start=0;
            this.length=0;
        } else {
            this.text = runs.get(0).base();
            assert checkAllRunsHaveTextAsBase(runs);

            runs.forEach(r -> this.runs.put(r.getStart(), r));
            this.start = this.runs.firstKey();
            this.length = this.runs.lastEntry().getValue().getEnd()-this.start;
        }
    }

    private boolean checkAllRunsHaveTextAsBase(Iterable<Run> runs) {
        boolean ok = true;
        for (Run run : runs) {
            //noinspection ObjectEquality
            ok &= run.base() == text;
        }
        return ok;
    }

    RichText(Run[] runs) {
        this(Arrays.asList(runs));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RichText other = (RichText) obj;
        return runs.equals(other.runs);
    }

    /**
     * Textual compare.
     * @param other the {@link CharSequence} to compare to
     * @return true, if the other
     */
    public boolean textEquals(CharSequence other) {
        if (other==null || other.length()!=this.length) {
            return false;
        }

        if (this.isEmpty()) {
            // we already know that both sequences have the same length
            return true;
        }

        for (int idx=0; idx<length; idx++) {
            if (other.charAt(idx)!=text.charAt(start+idx)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int compareTo(CharSequence other) {
        for (int idx=0; idx<length; idx++) {
            char a = text.charAt(start + idx);
            char b = other.charAt(idx);
            if (a != b) {
                return a - b;
            }
        }

        return length - other.length();
    }
    
    @Override
    public int hashCode() {
        return text.hashCode() + 17 * runs.size();
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
    public Iterator<Run> iterator() {
        return runs.values().iterator();
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
    public Stream<Run> stream() {
        return runs.values().stream();
    }

    @Override
    public String toString() {
        return text.subSequence(start, start+length).toString();
    }

    @Override
    public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + this.length());
        stream().forEach(builder::appendRun);
    }

    @Override
    public RichText toRichText() {
        return this;
    }

    /**
     * Get stream of lines contained in this instance.
     * @return stream of lines of this text
     */
    public Stream<RichText> lines() {
        return StreamSupport.stream(lineSpliterator(), false);
    }

    /**
     * Get a {@link Spliterator<RichText>} over the lines of this instance.
     * @return spliterator
     */
    private Spliterator<RichText> lineSpliterator() {
        return new Spliterator<RichText>() {
            private int idx=0;
            
            @Override
            public boolean tryAdvance(Consumer<? super RichText> action) {
                int split = TextUtil.indexOf(text, '\n', idx);
                
                if (split<0) {
                    split = length;
                }
                
                action.accept(subSequence(idx, split));
                idx = split+1;
                return idx<length();
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
     * @param begin begin index (inclusive)
     * @param end end index (exclusive)
     * @return RichText instance of the sub range
     */
    @Override
    public RichText subSequence(int begin, int end) {
        int floorKey = runs.floorKey(begin);
        int ceilingKey = runs.floorKey(end);
        List<Run> subRuns = new ArrayList<>(runs.subMap(floorKey, true, ceilingKey, true).values());
        
        Run firstRun = subRuns.get(0);
        if (firstRun.getStart() < begin) {
            subRuns.set(0, firstRun.subSequence(begin-firstRun.getStart(), firstRun.length()));
        }
        
        Run lastRun = subRuns.get(subRuns.size()-1);
        if (lastRun.getEnd() > end) {
            subRuns.set(subRuns.size()-1, lastRun.subSequence(0, lastRun.length()-(lastRun.getEnd()-end)));
        }
        
        return new RichText(subRuns);
    }

    /**
     * Get a sub range of this instance.
     * @param beginIndex begin index (inclusive)
     * @return RichText instance of the sub range from beginIndex to the end
     */
    public RichText subSequence(int beginIndex) {
        return subSequence(beginIndex, length());
    }
    
    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public AttributedCharacter attributedCharAt(int index) {
        Run run = runs.floorEntry(index).getValue();
        return run.attributedCharAt(run.convertIndex(index));
    }

}

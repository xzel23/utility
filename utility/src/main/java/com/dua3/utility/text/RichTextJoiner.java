package com.dua3.utility.text;

import com.dua3.utility.data.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A Joiner for {@link RichText} to be used with {@link java.util.stream.Stream#collect(Collector)}.
 */
public class RichTextJoiner implements Collector<RichText, Pair<List<RichText>, AtomicInteger>, RichText> {

    final Consumer<RichTextBuilder> appendDelimiter;
    final Consumer<RichTextBuilder> appendPrefix;
    final Consumer<RichTextBuilder> appendSuffix;
    final IntUnaryOperator calculateSupplementaryLength;

    /**
     * Creates a RichTextJoiner instance with the given delimiter, prefix, and suffix.
     *
     * @param delimiter the CharSequence used to separate the joined elements
     * @param prefix    the CharSequence to be prepended to the joined elements
     * @param suffix    the CharSequence to be appended to the joined elements
     */
    public RichTextJoiner(CharSequence delimiter,
                          CharSequence prefix,
                          CharSequence suffix) {
        this.appendDelimiter = rtb -> rtb.append(delimiter);
        this.appendPrefix = rtb -> rtb.append(prefix);
        this.appendSuffix = rtb -> rtb.append(suffix);
        this.calculateSupplementaryLength = n -> prefix.length() + n * delimiter.length() + suffix.length();
    }

    /**
     * Creates a RichTextJoiner instance with the given delimiter.
     *
     * @param delimiter the CharSequence used to separate the joined elements
     */
    public RichTextJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    /**
     * Creates a RichTextJoiner instance with the given delimiter, prefix, and suffix.
     *
     * @param delimiter the RichText used to separate the joined elements
     * @param prefix the RichText added before the joined elements
     * @param suffix the RichText added after the joined elements
     */
    public RichTextJoiner(RichText delimiter,
                          RichText prefix,
                          RichText suffix) {
        this.appendDelimiter = rtb -> rtb.append(delimiter);
        this.appendPrefix = rtb -> rtb.append(prefix);
        this.appendSuffix = rtb -> rtb.append(suffix);
        this.calculateSupplementaryLength = n -> prefix.length() + n * delimiter.length() + suffix.length();
    }

    /**
     * Creates a RichTextJoiner instance with the given delimiter.
     *
     * @param delimiter the RichText used to separate the joined elements
     */
    public RichTextJoiner(RichText delimiter) {
        this(delimiter, RichText.emptyText(), RichText.emptyText());
    }

    @Override
    public Supplier<Pair<List<RichText>, AtomicInteger>> supplier() {
        return () -> Pair.of(new ArrayList<>(), new AtomicInteger());
    }

    @Override
    public BiConsumer<Pair<List<RichText>, AtomicInteger>, RichText> accumulator() {
        return (accu, text) -> {
            accu.first().add(text);
            accu.second().addAndGet(text.length());
        };
    }

    @Override
    public BinaryOperator<Pair<List<RichText>, AtomicInteger>> combiner() {
        return (a, b) -> {
            a.first().addAll(b.first());
            a.second().addAndGet(b.second().get());
            return a;
        };
    }

    @Override
    public Function<Pair<List<RichText>, AtomicInteger>, RichText> finisher() {
        return accu -> {
            int length = accu.second().get();

            if (length == 0) {
                return RichText.emptyText();
            }

            // calculate needed text length and create builder with sufficient capacity
            int n = accu.first().size();
            int supplementaryLength = calculateSupplementaryLength.applyAsInt(n);
            int totalLength = length + supplementaryLength;
            RichTextBuilder rtb = new RichTextBuilder(totalLength);

            // append prefix and first item
            appendPrefix.accept(rtb);
            rtb.append(accu.first().get(0));

            // append remaining items separated by delimiter
            List<RichText> first = accu.first();
            for (int i = 1, firstSize = first.size(); i < firstSize; i++) {
                appendDelimiter.accept(rtb);
                rtb.append(first.get(i));
            }

            // append suffix
            appendSuffix.accept(rtb);

            return rtb.toRichText();
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.jetbrains.annotations.NotNull;

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
    
    final @NotNull Consumer<RichTextBuilder> appendDelimiter;
    final @NotNull Consumer<RichTextBuilder> appendPrefix;
    final @NotNull Consumer<RichTextBuilder> appendSuffix;
    final @NotNull IntUnaryOperator calculateSupplementaryLength;

    public RichTextJoiner(@NotNull CharSequence delimiter,
                          @NotNull CharSequence prefix,
                          @NotNull CharSequence suffix) {
        this.appendDelimiter = rtb -> rtb.append(delimiter);
        this.appendPrefix = rtb -> rtb.append(prefix);
        this.appendSuffix = rtb -> rtb.append(suffix);
        this.calculateSupplementaryLength = n -> prefix.length() + n * delimiter.length() + suffix.length(); 
    }

    public RichTextJoiner(@NotNull CharSequence delimiter) {
        this(delimiter,"","");
    }

    public RichTextJoiner(@NotNull RichText delimiter,
                          @NotNull RichText prefix,
                          @NotNull RichText suffix) {
        this.appendDelimiter = rtb -> rtb.append(delimiter);
        this.appendPrefix = rtb -> rtb.append(prefix);
        this.appendSuffix = rtb -> rtb.append(suffix);
        this.calculateSupplementaryLength = n -> prefix.length() + n * delimiter.length() + suffix.length();
    }

    public RichTextJoiner(@NotNull RichText delimiter) {
        this(delimiter,RichText.emptyText(), RichText.emptyText());
    }

    @Override
    public @NotNull Supplier<Pair<List<RichText>, AtomicInteger>> supplier() {
        return () -> Pair.of(new ArrayList<>(), new AtomicInteger());
    }

    @Override
    public @NotNull BiConsumer<Pair<List<RichText>, AtomicInteger>, RichText> accumulator() {
        return (accu, text) -> { accu.first().add(text); accu.second().addAndGet(text.length()); };
    }

    @Override
    public @NotNull BinaryOperator<Pair<List<RichText>, AtomicInteger>> combiner() {
        return (a, b) -> { a.first().addAll(b.first()); a.second().addAndGet(b.second().get()); return a; };
    }

    @Override
    public @NotNull Function<Pair<List<RichText>, AtomicInteger>, RichText> finisher() {
        return accu -> {
            int length = accu.second().get();

            if (length==0) {
                return RichText.emptyText();
            }

            // calculate needed text length and create builder wiith sufficient capacity
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
    public @NotNull Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}

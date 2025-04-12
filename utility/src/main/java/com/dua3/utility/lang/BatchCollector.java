package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector that puts subsequent items into batches. A new batch is started by generating a new batch key.
 * <p>
 * Example:
 * This code collects strings based on their length and places them in buckets.
 * <pre><code>
 *     BatchCollector<String, Integer> bc = new BatchCollector<>(s -> s != null ? s.length() : null);
 *     var result = Stream.of("one", "two", "three", "four", "five", "six").collect(bc);
 * </code></pre>
 * The result will be a {@code List<Integer, Pair<Integer, List<String>>} equal to this one:
 * <pre><code>
 *     List.of(
 *         Pair.of(3, List.of("one", "two"),
 *         Pair.of(5, List.of("three"),
 *         Pair.of(4, List.of("four", "five"),
 *         Pair.of(3, List.of("six")
 *     )
 * </code></pre>
 * <p>
 * Note: For Java 24+, have a look at stream gatherers (JEP 461, JEP 485) that might be a better fit for your use case.
 *
 * @param <T> the item type
 * @param <K> the key type
 */
public class BatchCollector<T extends @Nullable Object, K> implements Collector<T, Deque<Pair<K, List<T>>>, List<Pair<K, List<T>>>> {
    private final Function<? super T, ? extends K> keyMapper;

    /**
     * Constructor.
     *
     * @param keyMapper mapping that maps each item to the grouping key
     */
    public BatchCollector(Function<? super T, ? extends K> keyMapper) {
        this.keyMapper = keyMapper;
    }

    /**
     * Create a new BatchCollector.
     * <p>
     * For each item in the stream, a key is determined by applying the keyMapper. If the generated key is null, or
     * equals the last item's key, the item is added to the current batch. If not, a new batch is created and the
     * item added.
     *
     * @param keyMapper  the key mapper
     * @param defaultKey the default key
     */
    public BatchCollector(Function<? super T, ? extends @Nullable K> keyMapper, K defaultKey) {
        this(t -> Objects.requireNonNullElse(keyMapper.apply(t), defaultKey));
    }

    @Override
    public Supplier<Deque<Pair<K, List<T>>>> supplier() {
        return ArrayDeque::new;
    }

    @Override
    public BiConsumer<Deque<Pair<K, List<T>>>, T> accumulator() {
        return (Deque<Pair<K, List<T>>> accu, T item) -> {
            K key = keyMapper.apply(item);

            List<T> bucket;
            if (accu.isEmpty() || (!Objects.equals(key, accu.peekLast().first()))) {
                bucket = new ArrayList<>();
                accu.addLast(Pair.of(key, bucket));
            } else {
                bucket = accu.peekLast().second();
            }

            //noinspection DataFlowIssue - false positive; T is @Nullable
            bucket.add(item);
        };
    }

    @Override
    public BinaryOperator<Deque<Pair<K, List<T>>>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<Deque<Pair<K, List<T>>>, List<Pair<K, List<T>>>> finisher() {
        return ArrayList::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.noneOf(Characteristics.class);
    }
}

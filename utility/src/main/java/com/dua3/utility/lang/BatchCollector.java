package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector that puts subsequent items into batches. A new batch is started by generating a new batch key.
 * @param <T> the item type
 * @param <K> the key type
 */
public class BatchCollector<T,K> implements Collector<T, Deque<Pair<K, List<T>>>, List<Pair<K, List<T>>>> {
    private final Function<? super T, ? extends K> keyMapper;
    private final K defaultKey;

    public BatchCollector(Function<? super T, K> keyMapper) {
        this(keyMapper, null);
    }

    /**
     * Create a new BatchCollector.
     * <p>
     * For each item in the stream, a key is determined applying the keyMapper. If the generated key is null, or
     * equals the last item's key, the item is added to the current batch. If not, a new batch is created and the 
     * item added.
     * 
     * @param keyMapper the key mapper
     * @param defaultKey the default key
     */
    public BatchCollector(Function<? super T, ? extends K> keyMapper, K defaultKey) {
        this.keyMapper = keyMapper;
        this.defaultKey = defaultKey;
    }

    @Override
    public Supplier<Deque<Pair<K, List<T>>>> supplier() {
        return LinkedList::new;
    }

    @Override
    public BiConsumer<Deque<Pair<K, List<T>>>, T> accumulator() {
        return (accu, item) -> {
            K key = keyMapper.apply(item);

            List<T> bucket;
            if (accu.isEmpty() || (key!=null && !Objects.equals(key, accu.peekLast().first)) ) {
                bucket = new ArrayList<>();
                accu.addLast(Pair.of(key == null ? defaultKey : key, bucket));
            } else {
                bucket = accu.peekLast().second;
            }
            
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
        return EnumSet.of(Characteristics.IDENTITY_FINISH);
    }
}

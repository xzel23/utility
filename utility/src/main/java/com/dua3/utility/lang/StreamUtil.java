package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import com.dua3.cabe.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class for Java streams.
 */
public final class StreamUtil {
    
    private StreamUtil() {}

    /**
     * Zip two streams, creating a new stream consisting of pairs of items from either stream.
     * <p>
     * Consider two streams {@code a = a1, a2, a3, ...} and {@code b = b1, b2, b3, ...}. The result of zipping
     * is a new stream {@code zip(a,b) = Pair.of(a1,b1), Pair.of(a2,b2), Pair.of(a3,b3) ...}. The strem ends
     * when either {@code as} or {@code bs} end.
     * @param as first stream
     * @param bs second stream
     * @param <A> first stream generic item type
     * @param <B> second stream generic item type
     * @return stream cosisting of pairs od items created from items of either stream 
     */
    public static <A, B> Stream<Pair<A, B>> zip(@NotNull Stream<A> as, @NotNull Stream<B> bs) {
        Iterator<A> i1 = as.iterator();
        Iterator<B> i2 = bs.iterator();
        Iterable<Pair<A, B>> i = () -> new Iterator<>() {
            public boolean hasNext() {
                return i1.hasNext() && i2.hasNext();
            }

            public Pair<A, B> next() {
                return new Pair<>(i1.next(), i2.next());
            }
        };
        return StreamSupport.stream(i.spliterator(), false);
    }

}

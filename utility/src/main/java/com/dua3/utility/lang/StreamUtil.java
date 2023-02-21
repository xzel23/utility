package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import com.dua3.utility.data.PeekIterator;
import com.dua3.utility.io.IoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class for Java streams.
 */
public final class StreamUtil {

    private StreamUtil() {
    }

    /**
     * Zip two streams, creating a new stream consisting of pairs of items from either stream.
     * <p>
     * Consider two streams {@code a = a1, a2, a3, ...} and {@code b = b1, b2, b3, ...}. The result of zipping
     * is a new stream {@code zip(a,b) = Pair.of(a1,b1), Pair.of(a2,b2), Pair.of(a3,b3) ...}. The stream ends
     * when either {@code a} or {@code b} end.
     * @param a first stream
     * @param b second stream
     * @param <A> first stream generic item type
     * @param <B> second stream generic item type
     * @return stream consisting of pairs od items created from items of either stream 
     */
    public static <A, B> Stream<Pair<A, B>> zip(Stream<A> a, Stream<B> b) {
        Iterator<A> i1 = a.iterator();
        Iterator<B> i2 = b.iterator();
        Iterable<Pair<A, B>> i = () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i1.hasNext() && i2.hasNext();
            }

            @Override
            public Pair<A, B> next() {
                return new Pair<>(i1.next(), i2.next());
            }
        };
        Stream<Pair<A, B>> stream = StreamSupport.stream(i.spliterator(), false);
        return stream.onClose(IoUtil.composedClose(a, b));
    }

    /**
     * Concat multiple streams. The resulting stream contains all elements from the first passed stream followed
     * by the elements of the second passed stream, and so on.
     * @param streams the streams to concat
     * @param <T> the generic element type
     * @return the stream creating by concatenating elements from the argument streams
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams).flatMap(i -> i);
    }

    private static class MergeIterator<T> implements Iterator<T> {

        private final Comparator<T> comparator;
        private final List<PeekIterator<T>> iters = new ArrayList<>();

        MergeIterator(Comparator<T> comparator, Collection<Iterator<T>> iters) {
            this.comparator = Objects.requireNonNull(comparator);
            iters.stream().map(PeekIterator::new).forEach(this.iters::add);
        }

        @Override
        public boolean hasNext() {
            return iters.stream().anyMatch(Iterator::hasNext);
        }

        @Override
        public T next() {
            return iters.stream().min(this::compareNextElement).orElseThrow(NoSuchElementException::new).next();
        }

        private int compareNextElement(PeekIterator<T> i1, PeekIterator<T> i2) {
            if (!i1.hasNext()) {
                return 1;
            }
            if (!i2.hasNext()) {
                return -1;
            }
            return comparator.compare(i1.peek(), i2.peek());
        }

    }

    /**
     * Merge several <strong>sorted</strong> streams into a single sorted stream containing the elements from the
     * streams passed as the argument.
     * @param comparator the comparator that defines the sort order
     * @param streams the sorted streams to merge
     * @return sorted stream containing the elements of the argument streams
     * @param <T> the generic element type
     */
    @SafeVarargs
    public static <T> Stream<T> merge(Comparator<T> comparator, Stream<T>... streams) {
        var iters = Arrays.stream(streams).map(Stream::iterator).toList();
        Iterable<T> i = () -> new MergeIterator<>(comparator, iters);
        Stream<T> stream = StreamSupport.stream(i.spliterator(), false);
        return stream.onClose(IoUtil.composedClose(streams));
    }

    /**
     * Merge several <strong>sorted</strong> streams into a single sorted stream containing the elements from the
     * streams passed as the argument. Natural sort order is used as implemented in the element class.
     * @param streams the sorted streams to merge
     * @return sorted stream containing the elements of the argument streams
     * @param <T> the generic element type
     */
    @SafeVarargs
    public static <T extends Comparable<T>> Stream<T> merge(Stream<T>... streams) {
        return merge(Comparator.naturalOrder(), streams);
    }

    /**
     * Create a stream from the elements obtained from an iterator.
     * @param iter the iterator
     * @return stream of elements
     * @param <T> the element type
     */
    public static <T> Stream<T> stream(Iterator<T> iter) {
        return stream(() -> iter);
    }

    /**
     * Create a stream from the elements obtained from an iterable.
     * @param iterable the iterable
     * @return stream of elements
     * @param <T> the element type
     */
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}

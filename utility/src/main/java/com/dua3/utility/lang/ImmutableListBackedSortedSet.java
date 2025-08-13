package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

/**
 * An immutable implementation of {@link SortedSet}, backed by a sorted array.
 * The element order is defined by the set's comparator if provided, otherwise by
 * the elements' natural order. Since this is an immutable set, all modification
 * operations are unsupported.
 * <p>
 * Notes on ordering and nullness:
 * - If a comparator was specified at creation time, it is used for all
 *   comparisons. If no comparator was specified, natural ordering applies and
 *   {@link #comparator()} returns {@code null} (following the {@link SortedSet}
 *   contract).
 * - The project is compiled in a {@code @NullMarked} context; null elements are
 *   not permitted and parameters are checked for nullness.
 * <p>
 * Use this implementation for immutable sets with a small number of elements to
 * reduce memory consumption compared to, for example, {@link java.util.TreeSet}.
 *
 * @param <T> the element type
 */
public final class ImmutableListBackedSortedSet<T> extends AbstractList<T> implements ImmutableSortedListSet<T> {

    private static final ImmutableListBackedSortedSet<?> EMPTY_SET = ofNaturalOrder();

    private final T[] elements;
    private final @Nullable Comparator<? super T> comparator;
    private int hash = 0;

    /**
     * Constructs an instance of {@code ImmutableListBackedSortedSet} with the given array
     * of elements and an optional comparator. The array must represent a strictly
     * increasing sequence according to the effective ordering (the provided comparator
     * if non-null, otherwise the elements' natural order). Duplicates are not allowed.
     * This constructor is package-private and used internally after elements have been
     * validated and prepared.
     *
     * @param elements the backing array (elements must be non-null, strictly increasing, and unique)
     * @param comparator the comparator to use; {@code null} means natural order
     */
    ImmutableListBackedSortedSet(T[] elements, @Nullable Comparator<? super T> comparator) {
        this.elements = elements;
        this.comparator = comparator;
        assert elementsAreSortedAndUnique() : "elements are not sorted or not unique";
    }

    private boolean elementsAreSortedAndUnique() {
        Comparator<? super T> cmp = LangUtil.orNaturalOrder(comparator);
        for (int i = 1; i < elements.length; i++) {
            if (cmp.compare(elements[i - 1], elements[i]) >= 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (T element : elements) {
                h += Objects.hashCode(element);
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return switch(o) {
            case ImmutableListBackedSortedSet<?> other when Objects.equals(other.comparator(), comparator()) ->
                    Arrays.equals(elements, other.elements);
            case SortedSet<?> other when Objects.equals(other.comparator(), comparator()) ->
                    Arrays.equals(elements, other.toArray());
            case Set<?> other -> other.size() == size() && other.containsAll(this);
            case null, default -> false;
        };
    }

    /**
     * Sorts the given array and removes duplicate elements, preserving the relative order
     * of unique elements. The resulting array is sorted and contains only unique values.
     *
     * @param <T1> the type of elements in the array
     * @param array the input array to be sorted and de-duplicated
     * @param comparator the comparator to use; {@code null} means natural order
     * @return a new array containing the sorted, unique elements from the input array
     */
    private static <T1> T1[] sortAndRemoveDuplicates(T1[] array, @Nullable Comparator<? super T1> comparator) {
        T1[] copy = array.clone();
        Arrays.sort(copy, LangUtil.orNaturalOrder(comparator));
        int uniqueCount = 0;
        for (int i = 0; i < copy.length; i++) {
            if (i == 0 || !copy[i].equals(copy[i - 1])) {
                copy[uniqueCount++] = copy[i];
            }
        }
        return uniqueCount == copy.length ? copy : Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Creates a new instance containing the specified elements in their natural order.
     * Duplicate elements are removed.
     *
     * @param <T> the element type; must extend {@link Comparable}
     * @param elements the elements to include
     * @return a new immutable sorted set containing the unique, naturally ordered elements
     */
    @SafeVarargs
    public static <T extends Comparable<T>> ImmutableListBackedSortedSet<T> ofNaturalOrder(T... elements) {
        return new ImmutableListBackedSortedSet<>(sortAndRemoveDuplicates(elements, null), null);
    }

    /**
     * Creates a new instance containing the specified elements ordered by the given comparator.
     * Duplicate elements (w.r.t. the comparator) are removed.
     *
     * @param <T> the element type
     * @param comparator the comparator to define the order (must not be {@code null})
     * @param elements the elements to include
     * @return a new immutable sorted set containing the unique elements in comparator order
     */
    @SafeVarargs
    public static <T> ImmutableListBackedSortedSet<T> of(Comparator<T> comparator, T... elements) {
        return new ImmutableListBackedSortedSet<>(sortAndRemoveDuplicates(elements, comparator), comparator);
    }

    @Override
    public @Nullable Comparator<? super T> comparator() {
        // Returns null to indicate natural ordering, as per SortedSet contract
        return comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableListBackedSortedSet<T> subSet(T fromElement, T toElement) {
        int start = getIndex(fromElement);
        int end = getIndex(toElement);
        if (start < 0) {
            start = -start - 1;
        }
        if (end < 0) {
            end = -end - 1;
        }
        if (start >= end) {
            if (start == end) {
                return (ImmutableListBackedSortedSet<T>) EMPTY_SET;
            }
            throw new IllegalArgumentException("fromElement > toElement");
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, start, end), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableSortedListSet<T> headSet(T toElement) {
        int end = getIndex(toElement);
        if (end < 0) {
            end = -end - 1;
        }
        if (end == 0) {
            return (ImmutableSortedListSet<T>) EMPTY_SET;
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, 0, end), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableSortedListSet<T> tailSet(T fromElement) {
        int start = getIndex(fromElement);
        if (start < 0) {
            start = -start - 1;
        }
        if (start >= elements.length) {
            return (ImmutableSortedListSet<T>) EMPTY_SET;
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, start, elements.length), comparator);
    }

    @Override
    public T first() {
        return getFirst();
    }

    @Override
    public T last() {
        return getLast();
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return elements.length == 0;
    }

    /**
     * Returns the index of the specified element in the sorted set, or
     * a negative value if the element is not present.
     *
     * @param element the element to search for in the sorted set
     * @return the index of the specified element if it is present;
     *         otherwise, a negative value indicating the insertion point
     *         where the element would be added to maintain sorted order
     */
    private int getIndex(Object element) {
        @SuppressWarnings("unchecked")
        Comparator<? super T> cmp = LangUtil.orNaturalOrder(comparator);
        @SuppressWarnings("unchecked")
        T e = (T) element;
        return Arrays.binarySearch(elements, e, cmp);
    }

    @Override
    public int indexOf(Object o) {
        int index = getIndex(o);
        return index >= 0 ? index : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o); // elememts are unique
    }

    @Override
    public boolean contains(Object o) {
        return getIndex(o) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(elements).iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.clone();
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    @Override
    public <T1> T1[] toArray(T1[] a) {
        T1[] r = a.length >= elements.length ? a :
                (T1[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), elements.length);

        int n = Math.min(elements.length, r.length);
        System.arraycopy(elements, 0, r, 0, n);
        Arrays.fill(r, elements.length, r.length, null);

        return r;
    }

    @Override
    public T get(int index) {
        Objects.checkIndex(index, elements.length);
        return elements[index];
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (getIndex(o) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public T getFirst() {
        if (elements.length == 0) {
            throw new NoSuchElementException("the collection is empty");
        }
        return elements[0];
    }

    @Override
    public T getLast() {
        if (elements.length == 0) {
            throw new NoSuchElementException("the collection is empty");
        }
        return elements[elements.length - 1];
    }

    @Override
    public ImmutableSortedListSet<T> reversed() {
        return new ReversedImmutableSortedListSet<>(this, super.reversed());
    }

    /**
     * Reversed view of an {@link ImmutableListBackedSortedSet}. The comparator of the
     * reversed view is the reversed comparator of the original (or natural order reversed
     * if the original used natural order). Null elements are not permitted.
     */
    private static final class ReversedImmutableSortedListSet<T> implements ImmutableSortedListSet<T> {
        private static final ImmutableSortedListSet<?> EMPTY_SET_REVERSED = EMPTY_SET.reversed();

        private final ImmutableListBackedSortedSet<T> original;
        private final List<T> elementList;

        private ReversedImmutableSortedListSet(ImmutableListBackedSortedSet<T> original, List<T> elementList) {
            this.original = original;
            this.elementList = elementList;
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            return switch(o) {
                case ReversedImmutableSortedListSet<?> other when Objects.equals(other.comparator(), comparator()) ->
                        other.original.equals(original);
                case ImmutableListBackedSortedSet<?> other when Objects.equals(other.comparator(), comparator()) ->
                        Arrays.equals(original.elements, other.elements);
                case Set<?> other -> other.size() == size() && other.containsAll(this);
                case null, default -> false;
            };
        }

        @Override
        public ImmutableSortedListSet<T> reversed() {
            return original;
        }

        @Override
        public int size() {
            return original.size();
        }

        @Override
        public boolean isEmpty() {
            return original.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return original.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return elementList.listIterator();
        }

        @Override
        public Object[] toArray() {
            return elementList.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return elementList.toArray(a);
        }

        // suppress the IntelliJ/Qodana warning about using List.containsAll().
        // The implementation uses O(log(n)) lookup, not O(n), and also also the
        // whole point of the class is to reduce memory consumption and GC load,
        // so introducing a temporary Set would be counter-productive.
        @SuppressWarnings("SlowListContainsAll")
        @Override
        public boolean containsAll(Collection<?> c) {
            return original.containsAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            return false;
        }

        @Override
        public T get(int index) {
            return elementList.get(index);
        }

        @Override
        public T set(int index, T element) {
            throw new UnsupportedOperationException("the collection is immutable");
        }

        @Override
        public void add(int index, T element) {
            throw new UnsupportedOperationException("the collection is immutable");
        }

        @Override
        public T remove(int index) {
            throw new UnsupportedOperationException("the collection is immutable");
        }

        @Override
        public int indexOf(Object o) {
            int originalIndex = original.getIndex(o);
            return originalIndex >= 0 ? original.size() - originalIndex - 1 : -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o); // elements are unique
        }

        @Override
        public ListIterator<T> listIterator() {
            return elementList.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return elementList.listIterator(index);
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return elementList.subList(fromIndex, toIndex);
        }

        @Override
        public @Nullable Comparator<? super T> comparator() {
            return LangUtil.orNaturalOrder(original.comparator()).reversed();
        }

        @Override
        public ImmutableSortedListSet<T> subSet(T fromElement, T toElement) {
            int start = original.getIndex(toElement);
            if (start < 0) {
                start = -start - 1;
            } else if (start + 1 < size()) {
                start++;
            }
            int end = original.getIndex(fromElement);
            if (end >= 0) {
                end++;
            } else {
                end = -end - 1;
            }

            if (end == size()) {
                return original.tailSet(original.get(start)).reversed();
            } else {
                return original.subSet(original.get(start), original.get(end)).reversed();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public ImmutableSortedListSet<T> headSet(T toElement) {
            int start = original.getIndex(toElement);
            if (start < 0) {
                start = -start - 1;
            } else {
                start++;
            }

            if (start < 0) {
                return this;
            } else if (start < size()) {
                return original.tailSet(original.get(start)).reversed();
            } else {
                return (ImmutableSortedListSet<T>) EMPTY_SET_REVERSED;
            }
        }

        @Override
        public SortedSet<T> tailSet(T fromElement) {
            int end = original.getIndex(fromElement);
            if (end >= 0) {
                end++;
            } else {
                end = -end - 1;
            }

            if (end == size()) {
                return this;
            } else {
                return original.headSet(original.get(end)).reversed();
            }
        }

        @Override
        public T first() {
            return elementList.getFirst();
        }

        @Override
        public T last() {
            return elementList.getLast();
        }

        @Override
        public T getFirst() {
            return elementList.getFirst();
        }

        @Override
        public T getLast() {
            return elementList.getLast();
        }
    }
}

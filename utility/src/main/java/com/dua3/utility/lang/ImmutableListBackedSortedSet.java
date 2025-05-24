package com.dua3.utility.lang;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;

/**
 * An immutable implementation of {@link SortedSet}, backed by a sorted array.
 * This set guarantees natural ordering of elements as defined by their
 * {@link Comparable} implementation. Since this is an immutable set, all
 * modification operations are unsupported.
 * <p>
 * Use this Set implementation for immutable Sets with a small number of elements
 * to reduce memory consumption as compared to for example {@link java.util.TreeSet}.
 *
 * @param <T> the type of elements maintained by this set, which must
 *            implement {@link Comparable}
 */
public final class ImmutableListBackedSortedSet<T extends Comparable<T>> extends AbstractList<T> implements ImmutableSortedListSet<T> {

    private static final ImmutableListBackedSortedSet<?> EMPTY_SET = of();

    private final T[] elements;
    private int hash = 0;

    /**
     * Constructs an instance of {@code ImmutableListBackedSortedSet} with the given array
     * of elements. The array is expected to represent the elements of the sorted set.
     *
     * @param elements the array of elements to initialize the sorted set with
     */
    ImmutableListBackedSortedSet(T[] elements) {
        this.elements = elements;
        assert elementsAreSortedAndUnique() : "elements are not sorted or not unique";
    }

    private boolean elementsAreSortedAndUnique() {
        for (int i = 1; i < elements.length; i++) {
            if (elements[i - 1].compareTo(elements[i]) >= 0) {
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
            h = 37 * (1 + size()); // make sure an empty collection does not have hash 0
            for (T element : elements) {
                // only use the value when it is immutable
                h = h * 11 + (LangUtil.isOfKnownImmutableType(element) ? Objects.hashCode(element) : 0);
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImmutableSortedListSet<?> && o.hashCode() == hashCode() && super.equals(o);
    }

    /**
     * Sorts the given array and removes duplicate elements, preserving the relative order
     * of unique elements. The resulting array is sorted and contains only unique values.
     *
     * @param <T1> the type of elements in the array
     * @param array the input array to be sorted and de-duplicated
     * @return a new array containing the sorted, unique elements from the input array
     */
    private static <T1 extends Comparable<T1>> T1[] sortAndRemoveDuplicates(T1[] array) {
        T1[] copy = array.clone();
        Arrays.sort(copy);
        int uniqueCount = 0;
        for (int i = 0; i < copy.length; i++) {
            if (i == 0 || !copy[i].equals(copy[i - 1])) {
                copy[uniqueCount++] = copy[i];
            }
        }
        return uniqueCount == copy.length ? copy : Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Creates a new instance of {@code ImmutableListBackedSortedSet} containing the specified elements.
     * The input elements will be sorted and any duplicates will be removed.
     *
     * @param <T> the type of elements in the set; must extend {@link Comparable}
     * @param elements the varargs array of elements to include in the sorted set
     * @return a new immutable sorted set containing the unique, sorted elements
     */
    @SafeVarargs
    public static <T extends Comparable<T>> ImmutableListBackedSortedSet<T> of(T... elements) {
        return new ImmutableListBackedSortedSet<>(sortAndRemoveDuplicates(elements));
    }

    @Override
    public Comparator<? super T> comparator() {
        return Comparator.naturalOrder();
    }

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
                //noinspection unchecked
                return (ImmutableListBackedSortedSet<T>) EMPTY_SET;
            }
            throw new IllegalArgumentException("fromElement > toElement");
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, start, end));
    }

    @Override
    public ImmutableSortedListSet<T> headSet(T toElement) {
        int end = getIndex(toElement);
        if (end < 0) {
            end = -end - 1;
        }
        if (end == 0) {
            //noinspection unchecked
            return (ImmutableSortedListSet<T>) EMPTY_SET;
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, 0, end));
    }

    @Override
    public ImmutableSortedListSet<T> tailSet(T fromElement) {
        int start = getIndex(fromElement);
        if (start < 0) {
            start = -start - 1;
        }
        if (start >= elements.length) {
            //noinspection unchecked
            return (ImmutableSortedListSet<T>) EMPTY_SET;
        }
        return new ImmutableListBackedSortedSet<>(Arrays.copyOfRange(elements, start, elements.length));
    }

    @Override
    public T first() {
        if (elements.length == 0) {
            throw new NoSuchElementException("the collection is empty");
        }
        return elements[0];
    }

    @Override
    public T last() {
        if (elements.length == 0) {
            throw new NoSuchElementException("the collection is empty");
        }
        return elements[elements.length - 1];
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
        return Arrays.binarySearch(elements, element);
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

    private static final class ReversedImmutableSortedListSet<T extends Comparable<T>> implements ImmutableSortedListSet<T> {
        private static final ImmutableSortedListSet<?> EMPTY_SET_REVERSED = EMPTY_SET.reversed();

        private final ImmutableListBackedSortedSet<T> original;
        private final List<T> elementList;

        private ReversedImmutableSortedListSet(ImmutableListBackedSortedSet<T> original, List<T> elementList) {
            this.original = original;
            this.elementList = elementList;
        }

        @Override
        public int hashCode() {
            return -original.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ReversedImmutableSortedListSet<?> && obj.hashCode() == hashCode() && super.equals(obj);
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
        public Comparator<? super T> comparator() {
            return original.comparator().reversed();
        }

        @Override
        public ImmutableSortedListSet<T> subSet(T fromElement, T toElement) {
            return original.subSet(toElement, fromElement).reversed();
        }

        @Override
        public ImmutableSortedListSet<T> headSet(T toElement) {
            int start = original.getIndex(toElement);
            if (start == 0) {
                //noinspection unchecked
                return (ImmutableSortedListSet<T>) EMPTY_SET_REVERSED;
            } else if (start > 0) {
                start--;
            } else {
                start = -start - 1;
            }
            return original.tailSet(original.get(start)).reversed();
        }

        @Override
        public SortedSet<T> tailSet(T fromElement) {
            int end = original.getIndex(fromElement);
            if (end == 0) {
                return this;
            } else if (end > 0) {
                end++;
            } else {
                end = -end - 1;
            }
            return original.headSet(original.get(end)).reversed();
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

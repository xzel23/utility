package com.dua3.utility.text;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Interface for an attributed {@link CharSequence}.
 */
public interface AttributedCharSequence extends CharSequence {

    /**
     * Returns the {@code AttributedCharacter} value at the specified index.
     *
     * @param index the index of the {@code AttributedCharacter} value to be returned
     * @return the specified {@code AttributedCharacter} value
     * @throws IndexOutOfBoundsException if the {@code index} argument is negative or not less than
     *                                   {@code length()}
     */
    AttributedCharacter attributedCharAt(int index);

    @Override
    AttributedCharSequence subSequence(int start, int end);

    /**
     * Returns a stream of {@code AttributedCharacter} values from this sequence.
     *
     * @return a stream of {@link AttributedCharacter} values from this sequence
     */
    default Stream<AttributedCharacter> attributedChars() {
        class AttributedCharIterator implements Iterator<AttributedCharacter> {
            int cur = 0;

            @Override
            public boolean hasNext() {
                return cur < length();
            }

            @Override
            public AttributedCharacter next() {
                if (hasNext()) {
                    return attributedCharAt(cur++);
                } else {
                    throw new NoSuchElementException("there are no characters left");
                }
            }
        }

        return StreamSupport.stream(() ->
                        Spliterators.spliterator(
                                new AttributedCharIterator(),
                                length(),
                                Spliterator.ORDERED),
                Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.ORDERED,
                false);
    }
}

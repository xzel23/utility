package com.dua3.utility.text;

import org.jetbrains.annotations.NotNull;

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
     * @param   index   the index of the {@code AttributedCharacter} value to be returned
     *
     * @return  the specified {@code AttributedCharacter} value
     *
     * @throws  IndexOutOfBoundsException
     *          if the {@code index} argument is negative or not less than
     *          {@code length()}
     */
    @NotNull AttributedCharacter attributedCharAt(int index);

    @Override
    @NotNull AttributedCharSequence subSequence(int start, int end);

    /**
     * Returns a stream of {@code AttributedCharacter} values from this sequence.
     * 
     * @return a stream of {@link AttributedCharacter} values from this sequence
     */
    default @NotNull Stream<AttributedCharacter> attributedChars() {
        class AttributedCharIterator implements Iterator<AttributedCharacter> {
            int cur = 0;

            public boolean hasNext() {
                return cur < length();
            }

            @Override
            public @NotNull AttributedCharacter next() {
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

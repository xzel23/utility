package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * Decoder interface for reading objects from {@link DataInputStream}.
 *
 * @param <T> the object type
 */
@FunctionalInterface
public interface Decoder<T extends @Nullable Object> {

    /**
     * Decode a collection of objects.
     *
     * @param <T>                   the object type
     * @param is                    the {@link DataInputStream}
     * @param codec                 the element {@link Codec}
     * @param collectionConstructor the collection constructor / factory method
     * @return the collection of decoded objects
     * @throws IOException if an error occurs
     */
    static <T extends @Nullable Object> Collection<T> decode(DataInputStream is, Decoder<? extends T> codec, IntFunction<? extends Collection<T>> collectionConstructor) throws IOException {
        int size = is.readInt();
        LangUtil.check(size >= 0, "invalid collection size: %d", size);

        Collection<T> collection = collectionConstructor.apply(size);
        for (int i = 0; i < size; i++) {
            //noinspection DataFlowIssue - false positive
            collection.add(codec.decode(is));
        }

        return collection;
    }

    /**
     * Read an object from the {@link DataInputStream}.
     *
     * @param is the {@link DataInputStream} to read from
     * @return the object read
     * @throws IOException if an error occurs
     */
    T decode(DataInputStream is) throws IOException;

    /**
     * Read an object from the {@link DataInputStream}.
     *
     * @param is the {@link DataInputStream} to read from
     * @return the object read
     * @throws UncheckedIOException if an error occurs
     */
    default T decodeUnchecked(DataInputStream is) throws UncheckedIOException {
        try {
            return decode(is);
        } catch (IOException e) {
            throw new UncheckedIOException("IOException in decode()", e);
        }
    }

    /**
     * Decode an optional object.
     *
     * @param is the {@link DataInputStream}
     * @return an optional holding the decoded object or an empty optional if object is not present
     * @throws IOException if an error occurs
     */
    default Optional<T> decodeOptional(DataInputStream is) throws IOException {
        return Optional.ofNullable(is.readBoolean() ? decode(is) : null);
    }

}

package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.IntFunction;

@FunctionalInterface
public interface Decoder<T> {

    /**
     * Read an object from the {@link DataInputStream}.
     * @param is the {@link DataInputStream} to read from
     * @return the object read
     * @throws IOException if an error occurs
     */
    T decode(DataInputStream is) throws IOException;

    /**
     * Read an object from the {@link DataInputStream}.
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
     * Decode a collection of objects.
     * @param <T> the object type
     * @param is the {@link DataInputStream}
     * @param codec the element {@link Codec}
     * @param collectionConstructor the collection constructor / factory method
     * @throws IOException if an error occurs
     * @return the collection of decoded objects
     */
    static <T> Collection<T> decode(DataInputStream is, Decoder<? extends T> codec, IntFunction<? extends Collection<T>> collectionConstructor) throws IOException {
        int size = is.readInt();
        LangUtil.check(size >= 0, "invalid collection size: %d", size);

        Collection<T> collection = collectionConstructor.apply(size);
        for (int i=0; i<size; i++) {
            collection.add(codec.decode(is));
        }

        return collection;
    }

    /**
     * Decode an optional object.
     * @param is the {@link DataInputStream}
     * @throws IOException if an error occurs
     * @return an optional holding the decoded object or an empty optional if object is notr present
     */
    default Optional<T> decodeOptional(DataInputStream is) throws IOException {
        return Optional.ofNullable(is.readBoolean() ? decode(is) : null);
    }

}

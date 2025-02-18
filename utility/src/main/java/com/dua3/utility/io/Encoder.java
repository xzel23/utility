package com.dua3.utility.io;

import org.jspecify.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;

/**
 * Encoder interface for writing objects to {@link DataOutputStream}.
 *
 * @param <T> the object type
 */
@FunctionalInterface
public interface Encoder<T> {

    /**
     * Encode a collection of objects.
     *
     * @param os    the {@link DataOutputStream}
     * @param coll  the collection
     * @param codec the element {@link Codec}
     * @param <T>   the object type
     * @throws IOException if an error occurs
     */
    static <T> void encode(DataOutputStream os, Collection<T> coll, Encoder<? super T> codec) throws IOException {
        os.writeInt(coll.size());
        for (T element : coll) {
            codec.encode(os, element);
        }
    }

    /**
     * Write instance to stream.
     *
     * @param os the {@link DataOutputStream} to write to
     * @param t  the instance to write
     * @throws IOException if an error occurs
     */
    void encode(DataOutputStream os, T t) throws IOException;

    /**
     * Write instance to stream, not throwing checked exceptions.
     *
     * @param os the {@link DataOutputStream} to write to
     * @param t  the instance to write
     * @throws UncheckedIOException if an error occurs
     */
    default void encodeUnchecked(DataOutputStream os, T t) throws UncheckedIOException {
        try {
            encode(os, t);
        } catch (IOException e) {
            throw new UncheckedIOException("IOException in encode()", e);
        }
    }

    /**
     * Encode an optional object.
     *
     * @param os the {@link DataOutputStream}
     * @param t  the object, might be {@code null}
     * @throws IOException if an error occurs
     */
    default void encodeOptional(DataOutputStream os, @Nullable T t) throws IOException {
        if (t == null) {
            os.writeBoolean(false);
        } else {
            os.writeBoolean(true);
            encode(os, t);
        }
    }

}

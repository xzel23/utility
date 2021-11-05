package com.dua3.utility.io;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Codec interface for reading/writing objects to/from {@link DataInputStream}/{@link DataOutputStream}.
 * @param <T> the object type
 */
public interface Codec<T> extends Encoder<T>, Decoder<T> {

    /**
     * The encoder name, usually a corresponding to the object type.
     * @return name
     */
    @NotNull String name();

}

package com.dua3.utility.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Codec interface for reading/writing objects to/from {@link DataInputStream}/{@link DataOutputStream}.
 * @param <T> the object type
 */
public interface Codec<T> extends Encoder<T>, Decoder<T> {

    /**
     * The encoder name, usually corresponds to the object type.
     * @return name
     */
    String name();

}

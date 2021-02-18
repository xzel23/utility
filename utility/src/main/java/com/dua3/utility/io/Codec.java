package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.IntFunction;

/**
 * Codec interface for reading/writing objects to/from {@link DataInputStream}/{@link DataOutputStream}.
 * @param <T> the object type
 */
public interface Codec<T> extends Encoder<T>, Decoder<T> {

    /**
     * The encoder name, usually a corresponding to the object type.
     * @return name
     */
    String name();

}

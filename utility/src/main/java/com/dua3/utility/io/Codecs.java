package com.dua3.utility.io;

import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * A registry for {@link Codec} instances.
 * <p>
 * Codecs are registered by calling {@link #registerCodec(Class, Encoder, Decoder)}.
 * Registered codecs for a class can be obtained by using {@link #get(Class)}.
 */
public class Codecs {
    private static final Logger LOG  = LogManager.getLogger(Codecs.class);

    private final Map<String, Codec<?>> codecs = new HashMap<>();

    /**
     * Constructor.
     * Default codecs for the Java primitive types are automatically registered.
     */
    public Codecs() {
        // JDK standard classes
        registerCodec(String.class, DataOutput::writeUTF, DataInput::readUTF);
        registerCodec(Boolean.class, DataOutput::writeBoolean, DataInput::readBoolean);
        registerCodec(Byte.class, (Encoder<Byte>) DataOutput::writeByte, DataInput::readByte);
        registerCodec(Character.class, (Encoder<Character>) DataOutput::writeChar, DataInput::readChar);
        registerCodec(Short.class, (Encoder<Short>) DataOutputStream::writeShort, DataInput::readShort);
        registerCodec(Integer.class, DataOutput::writeInt, DataInput::readInt);
        registerCodec(Long.class, DataOutput::writeLong, DataInput::readLong);

        // dua3 utility classes
        registerCodec(RGBColor.class, (DataOutputStream os, RGBColor c) -> os.writeInt(c.argb()), (DataInputStream is) -> RGBColor.argb(is.readInt()));
    }

    /**
     * Create new Codec instance from {@link Encoder} and {@link Decoder}.
     *
     * @param name the codec name
     * @param enc  the encoder
     * @param dec  the decoder
     * @param <T>  the object type
     * @return the new codec
     */
    public static <T> Codec<T> createCodec(String name, Encoder<? super T> enc, Decoder<? extends T> dec) {
        return new Codec<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void encode(DataOutputStream os, T t) throws IOException {
                enc.encode(os, t);
            }

            @Override
            public T decode(DataInputStream is) throws IOException {
                return dec.decode(is);
            }
        };
    }

    /**
     * Get {@link Codec} for a {@link java.util.Collection}.
     *
     * @param <T>       the type of collection elements
     * @param <C>       the collection type
     * @param name      the codec name
     * @param codec     the element codec
     * @param construct collection factory method
     * @return collection codec
     */
    public static <T, C extends Collection<T>> Codec<C> collectionCodec(String name, Codec<T> codec, IntFunction<? extends C> construct) {
        return new Codec<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void encode(DataOutputStream os, C collection) throws IOException {
                os.writeInt(collection.size());
                for (T item : collection) {
                    codec.encode(os, Objects.requireNonNull(item, "null values in collections are not supported"));
                }
            }

            @Override
            public C decode(DataInputStream is) throws IOException {
                int n = is.readInt();

                LangUtil.check(n >= 0, "negative size for collection: %d", n);
                C collection = construct.apply(n);
                for (int i = 0; i < n; i++) {
                    collection.add(codec.decode(is));
                }
                return collection;
            }
        };
    }

    /**
     * Get {@link Codec} for a {@link Map.Entry}.
     *
     * @param <K>     the type of map keys
     * @param <V>     the type of map values
     * @param codecK  the codec for keys
     * @param codecV  the codec for values
     * @return the map entry codec
     */
    public static <K, V> Codec<Map.Entry<K, V>> mapEntryCodec(Codec<K> codecK, Codec<? extends V> codecV) {
        return createCodec(
                Map.Entry.class.getCanonicalName() + "<" + codecK.name() + "," + codecV.name() + ">",
                (DataOutputStream os, Map.Entry<K, V> entry) -> codecK.encode(os, entry.getKey()),
                (DataInputStream is) -> Pair.of(codecK.decode(is), codecV.decode(is))
        );
    }

    /**
     * Get {@link Codec} for a {@link java.util.Map}.
     *
     * @param <K>       the key type
     * @param <V>       the value type
     * @param <M>       the map type
     * @param codecK    the key codec
     * @param codecV    the value codec
     * @param construct the map construction method
     * @return map codec
     */
    public static <K, V, M extends Map<K, V>> Codec<M> mapCodec(Codec<K> codecK, Codec<V> codecV, Supplier<? extends M> construct) {
        final String name = Map.class.getCanonicalName() + "<" + codecK.name() + "," + codecV.name() + ">";
        final Codec<Map.Entry<K, V>> ENTRY_CODEC = mapEntryCodec(codecK, codecV);
        final Codec<Collection<Map.Entry<K, V>>> ENTRIES_CODEC = collectionCodec("entrySet", ENTRY_CODEC, ArrayList::new);

        return new Codec<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void encode(DataOutputStream os, M map) throws IOException {
                ENTRIES_CODEC.encode(os, map.entrySet());
            }

            @Override
            public M decode(DataInputStream is) throws IOException {
                M map = construct.get();
                ENTRIES_CODEC.decode(is).forEach(entry -> map.put(entry.getKey(), entry.getValue()));
                return map;
            }
        };
    }

    /**
     * Register a codec for a specific class.
     *
     * @param <T>  the class type
     * @param cls  the class to register the codec for
     * @param enc  the encoder for the class
     * @param dec  the decoder for the class
     * @throws IllegalArgumentException if a codec is already registered for the class
     */
    public <T extends @Nullable Object> void registerCodec(Class<T> cls, Encoder<? super T> enc, Decoder<? extends T> dec) {
        Object prev = codecs.putIfAbsent(cls.getCanonicalName(), createCodec(cls.getCanonicalName(), enc, dec));
        LangUtil.check(prev == null, "Codec already registered for class: " + cls);
    }

    /**
     * Retrieves the codec registered for a specific class.
     *
     * @param <T>  the class type
     * @param cls  the class to retrieve the codec for
     * @return an Optional containing the codec, or an empty Optional if no codec is registered for the class
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Codec<T>> get(Class<T> cls) {
        return Optional.ofNullable((Codec<T>) codecs.get(cls.getCanonicalName()));
    }

}

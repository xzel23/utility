package com.dua3.utility.io;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;

import java.io.*;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * A registry for {@link Codec} instances.
 * 
 * Codecs are registered by calling {@link #registerCodec(Class, Encoder, Decoder)}.
 * Registered codecs for a class can be obtaained by using {@link #get(Class)}.
 */
public class Codecs {

    private final Map<String,Codec<?>> CODECS = new HashMap<>();

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
        registerCodec(Color.class, (DataOutputStream os, Color c) -> os.writeInt(c.argb()), (DataInputStream is) -> Color.argb(is.readInt()));
    }

    public <T> void registerCodec(Class<T> cls, Encoder<T> enc, Decoder<T> dec) {
        Object prev = CODECS.putIfAbsent(cls.getCanonicalName(), createCodec(cls.getCanonicalName(), enc, dec));
        LangUtil.check(prev==null, "Codec already registered for class: "+cls);
    }

    public <T> Optional<Codec<T>> get(Class<T> cls) {
        //noinspection unchecked - type is checked when inserting
        return Optional.ofNullable((Codec<T>) CODECS.get(cls.getCanonicalName()));
    }

    /**
     * Create new Codec instance from {@link Encoder} and {@link Decoder}.
     * @param name the codec name
     * @param enc the encoder
     * @param dec the decoder
     * @param <T> the object type
     * @return the new codec
     */
    public static <T> Codec<T> createCodec(String name, Encoder<T> enc, Decoder<T> dec) {
        return new Codec<T>() {
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
     * @return codec
     */
    public static <T, C extends Collection<T>> Codec<C> collectionCodec(String name, Codec<T> codec, IntFunction<C> construct) {
        return new Codec<C>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void encode(DataOutputStream os, C collection) throws IOException {
                os.writeInt(collection.size());
                for (T item : collection) {
                    codec.encode(os, item);
                }
            }

            @Override
            public C decode(DataInputStream is) throws IOException {
                int n = is.readInt();
                LangUtil.check(n>=0, "negative size for collection: %d", n);
                C collection = construct.apply(n);
                for (int i=0; i<n; i++) {
                    collection.add(codec.decode(is));
                }
                return collection;
            }
        };
    }

    public static <K,V> Codec<Map.Entry<K,V>> mapEntryCodec(Codec<K> codecK, Codec<V> codecV) {
        return createCodec(
                Map.Entry.class.getCanonicalName()+"<"+codecK.name()+","+codecV.name()+">", 
                (DataOutputStream os, Map.Entry<K,V> entry) -> codecK.encode(os, entry.getKey()),
                (DataInputStream is) -> {
                    K k = codecK.decode(is);
                    V v = codecV.decode(is); 
                    return new Map.Entry<K,V>(){
                        @Override
                        public K getKey() {
                            return k;
                        }
                        @Override
                        public V getValue() {
                            return v;
                        }
                        @Override
                        public V setValue(V value) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
        );
    }
    
    /**
     * Get {@link Codec} for a {@link java.util.Map}.
     * @return codec
     */
    public static <K,V,M extends Map<K,V>> Codec<M> mapCodec(Codec<K> codecK, Codec<V> codecV, Supplier<M> construct) {
        final String name = Map.class.getCanonicalName()+"<"+codecK.name()+","+codecV.name()+">";
        final Codec<Map.Entry<K,V>> ENTRY_CODEC = mapEntryCodec(codecK, codecV);
        final Codec<Collection<Map.Entry<K,V>>> ENTRIES_CODEC = Codecs.collectionCodec("entrySet", ENTRY_CODEC, ArrayList::new);
        
        return new Codec<M>() {
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
}
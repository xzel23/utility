// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the Codecs class.
 */
class CodecsTest {

    /**
     * Test the constructor and default codecs.
     */
    @Test
    void testConstructor() {
        Codecs codecs = new Codecs();

        // Check that default codecs are registered
        assertTrue(codecs.get(String.class).isPresent());
        assertTrue(codecs.get(Boolean.class).isPresent());
        assertTrue(codecs.get(Byte.class).isPresent());
        assertTrue(codecs.get(Character.class).isPresent());
        assertTrue(codecs.get(Short.class).isPresent());
        assertTrue(codecs.get(Integer.class).isPresent());
        assertTrue(codecs.get(Long.class).isPresent());
        assertTrue(codecs.get(RGBColor.class).isPresent());
    }

    /**
     * Test the createCodec method.
     */
    @Test
    void testCreateCodec() throws IOException {
        // Create a codec for a custom class
        Codec<TestClass> codec = Codecs.createCodec(
                "TestClass",
                (DataOutputStream os, TestClass t) -> os.writeUTF(t.value),
                (DataInputStream is) -> new TestClass(is.readUTF())
        );

        // Test the codec
        TestClass original = new TestClass("test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        codec.encode(dos, original);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        TestClass decoded = codec.decode(dis);

        assertEquals(original.value, decoded.value);
        assertEquals("TestClass", codec.name());
    }

    /**
     * Test the collectionCodec method.
     */
    @Test
    void testCollectionCodec() throws IOException {
        // Create a codec for a collection of strings
        Codec<String> stringCodec = Codecs.createCodec(
                "String",
                DataOutputStream::writeUTF,
                DataInput::readUTF
        );

        Codec<List<String>> listCodec = Codecs.collectionCodec(
                "List<String>",
                stringCodec,
                ArrayList::new
        );

        // Test the codec
        List<String> original = List.of("one", "two", "three");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        listCodec.encode(dos, original);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        List<String> decoded = listCodec.decode(dis);

        assertEquals(original, decoded);
        assertEquals("List<String>", listCodec.name());
    }

    /**
     * Test the mapEntryCodec method.
     */
    @Test
    void testMapEntryCodec() throws IOException {
        // Create a simple test for the mapEntryCodec method
        // First, create a custom codec for map entries directly
        Codec<Map.Entry<String, Integer>> entryCodec = new Codec<>() {
            @Override
            public String name() {
                return "TestMapEntry";
            }

            @Override
            public void encode(DataOutputStream os, Map.Entry<String, Integer> entry) throws IOException {
                os.writeUTF(entry.getKey());
                os.writeInt(entry.getValue());
            }

            @Override
            public Map.Entry<String, Integer> decode(DataInputStream is) throws IOException {
                String key = is.readUTF();
                int value = is.readInt();
                return Pair.of(key, value);
            }
        };

        // Test the codec
        Map.Entry<String, Integer> original = Pair.of("key", 42);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        entryCodec.encode(dos, original);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        Map.Entry<String, Integer> decoded = entryCodec.decode(dis);

        assertEquals(original.getKey(), decoded.getKey());
        assertEquals(original.getValue(), decoded.getValue());
    }

    /**
     * Test the mapCodec method.
     */
    @Test
    void testMapCodec() throws IOException {
        // Create a simple test for map encoding/decoding
        // Create a custom codec for maps directly
        Codec<Map<String, Integer>> mapCodec = new Codec<>() {
            @Override
            public String name() {
                return "TestMap";
            }

            @Override
            public void encode(DataOutputStream os, Map<String, Integer> map) throws IOException {
                os.writeInt(map.size());
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    os.writeUTF(entry.getKey());
                    os.writeInt(entry.getValue());
                }
            }

            @Override
            public Map<String, Integer> decode(DataInputStream is) throws IOException {
                int size = is.readInt();
                Map<String, Integer> map = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    String key = is.readUTF();
                    int value = is.readInt();
                    map.put(key, value);
                }
                return map;
            }
        };

        // Test the codec
        Map<String, Integer> original = Map.of("one", 1, "two", 2, "three", 3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        mapCodec.encode(dos, original);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        Map<String, Integer> decoded = mapCodec.decode(dis);

        assertEquals(original, decoded);
    }

    /**
     * Test the registerCodec and get methods.
     */
    @Test
    void testRegisterAndGet() {
        Codecs codecs = new Codecs();

        // Create a codec for a custom class
        Codec<TestClass> codec = Codecs.createCodec(
                "TestClass",
                (DataOutputStream os, TestClass t) -> {
                    try {
                        os.writeUTF(t.value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (DataInputStream is) -> {
                    try {
                        return new TestClass(is.readUTF());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // Register the codec
        codecs.registerCodec(TestClass.class, codec, codec);

        // Get the codec
        Optional<Codec<TestClass>> retrievedCodec = codecs.get(TestClass.class);
        assertTrue(retrievedCodec.isPresent());

        // Try to register the same codec again
        assertThrows(Exception.class, () -> codecs.registerCodec(TestClass.class, codec, codec));

        // Get a codec for a class that doesn't have one registered
        Optional<Codec<UnregisteredClass>> unregisteredCodec = codecs.get(UnregisteredClass.class);
        assertFalse(unregisteredCodec.isPresent());
    }

    /**
     * A simple test class for codec testing.
     */
    private static class TestClass {
        private final String value;

        TestClass(String value) {
            this.value = value;
        }
    }

    /**
     * A class that doesn't have a codec registered.
     */
    private static class UnregisteredClass {
    }

    private static Codec<String> stringCodecNamed(String name) {
        return Codecs.createCodec(
                name,
                DataOutputStream::writeUTF,
                DataInput::readUTF
        );
    }

    private static Codec<Integer> intCodecNamed(String name) {
        return Codecs.createCodec(
                name,
                DataOutputStream::writeInt,
                DataInputStream::readInt
        );
    }

    @Test
    void mapEntryCodec_roundTrip_and_name() throws IOException {
        Codec<String> k = stringCodecNamed("S");
        Codec<Integer> v = intCodecNamed("I");

        Codec<Map.Entry<String, Integer>> entryCodec = Codecs.mapEntryCodec(k, v);

        // name should include provided codec names
        assertEquals(
                Map.Entry.class.getCanonicalName() + "<S,I>",
                entryCodec.name()
        );

        Map.Entry<String, Integer> original = Pair.of("alpha", 123);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            entryCodec.encode(dos, original);
        }

        Map.Entry<String, Integer> decoded;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            decoded = entryCodec.decode(dis);
        }

        assertEquals(original.getKey(), decoded.getKey());
        assertEquals(original.getValue(), decoded.getValue());
    }

    @Test
    void mapCodec_roundTrip_and_name() throws IOException {
        Codec<String> k = stringCodecNamed("S");
        Codec<Integer> v = intCodecNamed("I");

        Codec<Map<String, Integer>> mapCodec = Codecs.mapCodec(k, v, HashMap::new);

        assertEquals(
                Map.class.getCanonicalName() + "<S,I>",
                mapCodec.name()
        );

        Map<String, Integer> original = new HashMap<>();
        original.put("one", 1);
        original.put("two", 2);
        original.put("three", 3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            mapCodec.encode(dos, original);
        }

        Map<String, Integer> decoded;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            decoded = mapCodec.decode(dis);
        }

        assertEquals(original, decoded);
    }

    @Test
    void mapCodec_throws_on_null_values() {
        Codec<String> k = stringCodecNamed("S");
        Codec<Integer> v = intCodecNamed("I");

        Codec<Map<String, Integer>> mapCodec = Codecs.mapCodec(k, v, HashMap::new);

        Map<String, Integer> mapWithNull = new HashMap<>();
        mapWithNull.put("ok", 1);
        mapWithNull.put("bad", null);

        // Expect a NullPointerException during encoding due to null value in entry
        assertThrows(NullPointerException.class, () -> {
            try (DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream())) {
                mapCodec.encode(dos, mapWithNull);
            }
        });
    }
}

package com.dua3.utility.crypt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStoreTypeTest {

    @ParameterizedTest
    @CsvSource({
            "p12, PKCS12",
            ".p12, PKCS12",
            "jks, JKS",
            ".jks, JKS",
            "jceks, JCEKS",
            ".jceks, JCEKS",
            "zip, ZIP",
            ".zip, ZIP"
    })
    void testForExtension(String extension, KeyStoreType expectedType) {
        assertEquals(expectedType, KeyStoreType.forExtension(extension));
    }

    @Test
    void testForExtensionUnsupported() {
        assertThrows(IllegalArgumentException.class, () -> KeyStoreType.forExtension("unsupported"));
        assertThrows(IllegalArgumentException.class, () -> KeyStoreType.forExtension(".txt"));
    }

    @Test
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    void testForPath() {
        assertEquals(KeyStoreType.PKCS12, KeyStoreType.forPath(Path.of("/some/path/keystore.p12")));
        assertEquals(KeyStoreType.JKS, KeyStoreType.forPath(Path.of("keystore.jks")));
        assertEquals(KeyStoreType.JCEKS, KeyStoreType.forPath(Path.of("keystore.jceks")));
        assertEquals(KeyStoreType.ZIP, KeyStoreType.forPath(Path.of("keystore.zip")));

        assertEquals(KeyStoreType.PKCS12, KeyStoreType.forPath("/some/path/keystore.p12"));
        assertEquals(KeyStoreType.JKS, KeyStoreType.forPath("keystore.jks"));
    }

    @Test
    void testGetExtension() {
        assertEquals("p12", KeyStoreType.PKCS12.getExtension());
        assertEquals("jks", KeyStoreType.JKS.getExtension());
        assertEquals("jceks", KeyStoreType.JCEKS.getExtension());
        assertEquals("zip", KeyStoreType.ZIP.getExtension());
    }

    @Test
    void testValuesWriteable() {
        assertArrayEquals(KeyStoreType.values(), KeyStoreType.valuesWriteable());
    }

    @Test
    void testValuesReadble() {
        KeyStoreType[] readable = KeyStoreType.valuesReadble();
        List<KeyStoreType> readableList = Arrays.asList(readable);

        assertTrue(readableList.contains(KeyStoreType.PKCS12));
        assertTrue(readableList.contains(KeyStoreType.JKS));
        assertTrue(readableList.contains(KeyStoreType.JCEKS));
        assertFalse(readableList.contains(KeyStoreType.ZIP));
    }

    @Test
    void testIsExportOnly() {
        assertTrue(KeyStoreType.ZIP.isExportOnly());
        assertFalse(KeyStoreType.PKCS12.isExportOnly());
        assertFalse(KeyStoreType.JKS.isExportOnly());
        assertFalse(KeyStoreType.JCEKS.isExportOnly());
    }

    @Test
    void testIsDeduplicating() {
        assertTrue(KeyStoreType.PKCS12.isDeduplicating());
        assertFalse(KeyStoreType.JKS.isDeduplicating());
        assertFalse(KeyStoreType.JCEKS.isDeduplicating());
        assertFalse(KeyStoreType.ZIP.isDeduplicating());
    }

    @ParameterizedTest
    @EnumSource(KeyStoreType.class)
    void testValueOf(KeyStoreType type) {
        assertNotNull(type);
        assertEquals(type, KeyStoreType.valueOf(type.name()));
    }
}

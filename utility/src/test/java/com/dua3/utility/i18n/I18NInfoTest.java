package com.dua3.utility.i18n;

import com.dua3.utility.I18NInstance;
import com.dua3.utility.i18n.fixtures.invalidtags.InvalidTagsFixture;
import com.dua3.utility.i18n.fixtures.missingbundle.MissingBundleFixture;
import com.dua3.utility.i18n.fixtures.missinglanguages.MissingLanguagesFixture;
import com.dua3.utility.i18n.fixtures.valid.ValidFixture;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18NInfoTest {

    @Test
    void testRecordBasics() {
        I18NInfo info1 = new I18NInfo("bundleA", Set.of("en", "de"));
        I18NInfo info2 = new I18NInfo("bundleA", Set.of("en", "de"));
        I18NInfo info3 = new I18NInfo("bundleB", Set.of("en"));

        assertEquals("bundleA", info1.bundle());
        assertEquals(Set.of("en", "de"), info1.languageTags());

        assertEquals(info1, info2);
        assertNotEquals(info1, info3);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertTrue(info1.toString().contains("bundleA"));
    }

    @Test
    void testLoadExistingUtilityI18NInfo() throws IOException {
        Optional<I18NInfo> infoOpt = I18NInfo.load(I18NInstance.class);
        assertTrue(infoOpt.isPresent());
        I18NInfo info = infoOpt.get();
        assertEquals("messages", info.bundle());
        assertTrue(info.languageTags().contains("en"));
        assertTrue(info.languageTags().contains("de"));
    }

    @Test
    void testLoadNonExistingResource() throws IOException {
        Optional<I18NInfo> infoOpt = I18NInfo.load(String.class);
        assertFalse(infoOpt.isPresent());
    }

    @Test
    void testLoadValidFixture() throws IOException {
        Optional<I18NInfo> infoOpt = I18NInfo.load(ValidFixture.class);
        assertTrue(infoOpt.isPresent());
        I18NInfo info = infoOpt.get();
        assertEquals("fixture_messages", info.bundle());
        assertEquals(Set.of("en", "de", "fr-CA"), info.languageTags());
    }

    @Test
    void testLoadMissingBundleThrows() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> I18NInfo.load(MissingBundleFixture.class));
        assertTrue(ex.getMessage().contains("Missing 'bundle' property"));
    }

    @Test
    void testLoadMissingLanguagesThrows() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> I18NInfo.load(MissingLanguagesFixture.class));
        assertTrue(ex.getMessage().contains("Missing 'languageTags' property"));
    }

    @Test
    void testLoadInvalidTagsFiltersOutInvalidTags() throws IOException {
        Optional<I18NInfo> infoOpt = I18NInfo.load(InvalidTagsFixture.class);
        assertTrue(infoOpt.isPresent());
        I18NInfo info = infoOpt.get();
        assertEquals("fixture_messages", info.bundle());
        assertEquals(Set.of("en", "de"), info.languageTags());
        assertFalse(info.languageTags().contains("invalid_tag"));
    }
}

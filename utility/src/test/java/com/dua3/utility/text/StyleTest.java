package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleTest {

    @SuppressWarnings("StringBufferReplaceableByString")
    @Test
    void testEquals() {
        Style s1 = Style.create("style",
                Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE),
                Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));

        String s2Name = new StringBuilder("st").append("yle").toString();
        Style s2 = Style.create(s2Name,
                Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD),
                Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE));

        // first make sure the names are equal but not identical 
        assertEquals(s2.name(), s1.name());
        assertNotSame(s1.name(), s2.name());

        // s1 and s2 do not possess any properties
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testPredefinedStyles() {
        assertEquals("bold", Style.BOLD.name());
        assertEquals("italic", Style.ITALIC.name());
        assertEquals("underline", Style.UNDERLINE.name());
        assertEquals("line-through", Style.LINE_THROUGH.name());
        assertEquals("red", Style.RED.name());
        assertEquals("blue", Style.BLUE.name());
        assertEquals("green", Style.GREEN.name());
    }

    @Test
    void testPropertiesAndGetters() {
        Style style = Style.create("custom", Map.of(Style.FONT_SIZE, 14.0f, Style.COLOR, Color.RED));

        assertEquals("custom", style.name());
        assertEquals(14.0f, style.get(Style.FONT_SIZE));
        assertEquals(Color.RED, style.getOrDefault(Style.COLOR, Color.BLACK));
        assertEquals(Color.BLUE, style.getOrDefault("non-existent", Color.BLUE));
        assertNull(style.get("non-existent"));
        assertTrue(style.toString().contains("custom"));
    }

    @Test
    void testIfPresentAndIfPresentOrElse() {
        Style style = Style.create("custom", Map.of(Style.FONT_SIZE, 14.0f));

        AtomicReference<Object> ref = new AtomicReference<>();
        style.ifPresent(Style.FONT_SIZE, ref::set);
        assertEquals(14.0f, ref.get());

        ref.set(null);
        style.ifPresent("missing", ref::set);
        assertNull(ref.get());

        style.ifPresentOrElse("missing", 20.0f, ref::set);
        assertEquals(20.0f, ref.get());

        style.ifPresentOrElseGet("missing", () -> 30.0f, ref::set);
        assertEquals(30.0f, ref.get());

        style.ifPresentOrElseGet(Style.FONT_SIZE, () -> 30.0f, ref::set);
        assertEquals(14.0f, ref.get());
    }

    @Test
    void testGetFontDefAndGetFont() {
        FontDef fd = new FontDef();
        fd.setFamily("serif");
        fd.setSize(18.0f);
        fd.setBold(true);
        fd.setColor(Color.BLUE);

        Style style = Style.create(fd);
        assertNotNull(style);

        FontDef retrievedFd = style.getFontDef();
        assertEquals(List.of("serif"), retrievedFd.getFamilies());
        assertEquals(18.0f, retrievedFd.getSize());
        assertEquals(Boolean.TRUE, retrievedFd.getBold());
        assertEquals(Color.BLUE, retrievedFd.getColor());

        Font defaultFont = FontUtil.getInstance().getDefaultFont();
        Font derivedFont = style.getFont(defaultFont);
        assertNotNull(derivedFont);
        assertEquals("serif", derivedFont.getFamily());
    }

    @Test
    void testStyleBackgroundAndAddFontProperties() {
        Style bg = Style.background(Color.YELLOW);
        assertEquals(Color.YELLOW, bg.get(Style.BACKGROUND_COLOR));

        FontDef fd = new FontDef();
        fd.setFamily("Dialog");
        fd.setSize(12.0f);

        Map<String, Object> props = new HashMap<>();
        Style.addFontProperties(props, fd);
        assertEquals(List.of("Dialog"), props.get(Style.FONT_FAMILIES));
        assertEquals(12.0f, props.get(Style.FONT_SIZE));
    }
}

package com.dua3.utility.fx.icons;

import javafx.application.Platform;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class IconTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    void testEmptyIcon() {
        Icon empty = IconUtil.emptyIcon();
        assertNotNull(empty);
        assertEquals("", empty.getIconIdentifier());

        assertEquals(0, empty.getIconSize());
        empty.setIconSize(16);
        assertEquals(16, empty.getIconSize());
        assertEquals(16, empty.iconSizeProperty().get());

        assertEquals(Paint.valueOf("BLACK"), empty.getIconColor());
        empty.setIconColor(Color.RED);
        assertEquals(Color.RED, empty.getIconColor());
        assertEquals(Color.RED, empty.iconColorProperty().get());

        assertNotNull(empty.node());
    }

    @Test
    void testIconFromNameAndProviderNames() {
        Optional<Icon> result = IconUtil.iconFromName("non_existent_icon_name_12345");
        assertTrue(result.isEmpty());

        Collection<String> providerNames = IconUtil.iconProviderNames();
        assertNotNull(providerNames);
    }

    @Test
    void testIconViewDefaultConstructor() {
        IconView iconView = new IconView();
        assertEquals("", iconView.getIconIdentifier());
        assertEquals(10, iconView.getIconSize());
        assertEquals(Paint.valueOf("BLACK"), iconView.getIconColor());
        assertEquals("", iconView.toString());

        iconView.setIconIdentifier("sample_icon");
        assertEquals("sample_icon", iconView.getIconIdentifier());
        assertEquals("sample_icon", iconView.toString());

        iconView.setIconSize(32);
        assertEquals(32, iconView.getIconSize());

        iconView.setIconColor(Color.BLUE);
        assertEquals(Color.BLUE, iconView.getIconColor());
    }

    @Test
    void testIconViewParameterizedConstructor() {
        IconView iconView = new IconView("sample_icon", 24, Color.GREEN);
        assertEquals("sample_icon", iconView.getIconIdentifier());
        assertEquals(24, iconView.getIconSize());
        assertEquals(Color.GREEN, iconView.getIconColor());
    }

    @Test
    void testIconViewCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> classMeta = IconView.getClassCssMetaData();
        assertNotNull(classMeta);
        assertFalse(classMeta.isEmpty());

        IconView iconView = new IconView();
        List<CssMetaData<? extends Styleable, ?>> controlMeta = iconView.getControlCssMetaData();
        assertEquals(classMeta, controlMeta);

        boolean foundSize = false;
        boolean foundColor = false;
        boolean foundIdentifier = false;

        for (CssMetaData<? extends Styleable, ?> meta : controlMeta) {
            @SuppressWarnings("unchecked")
            CssMetaData<Styleable, Object> m = (CssMetaData<Styleable, Object>) meta;
            String property = meta.getProperty();
            assertTrue(m.isSettable(iconView));
            assertNotNull(m.getStyleableProperty(iconView));

            if ("-fx-icon-size".equals(property)) {
                foundSize = true;
                assertEquals(16.0, ((Number) m.getInitialValue(iconView)).doubleValue());
            } else if ("-fx-icon-color".equals(property)) {
                foundColor = true;
                assertEquals(Color.BLACK, m.getInitialValue(iconView));
            } else if ("-fx-icon-identifier".equals(property)) {
                foundIdentifier = true;
                assertEquals("", m.getInitialValue(iconView));
            }
        }

        assertTrue(foundSize, "-fx-icon-size metadata missing");
        assertTrue(foundColor, "-fx-icon-color metadata missing");
        assertTrue(foundIdentifier, "-fx-icon-identifier metadata missing");
    }

    @Test
    void testIconViewSkin() {
        IconView iconView = new IconView("test_icon", 16, Color.RED);
        new javafx.scene.Scene(new javafx.scene.Group(iconView));
        iconView.applyCss();
        assertNotNull(iconView.getSkin());
    }
}

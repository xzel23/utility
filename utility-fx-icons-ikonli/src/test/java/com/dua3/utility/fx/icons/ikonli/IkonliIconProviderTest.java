package com.dua3.utility.fx.icons.ikonli;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.Ikon;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class IkonliIconProviderTest {

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
    void testIkonliIconProvider() {
        IkonliIconProvider provider = new IkonliIconProvider();
        assertEquals("IkonliIconProvider", provider.name());
        assertTrue(provider.forName("unknown-ikon-xyz").isEmpty());
    }

    @Test
    void testIkonliIcon() {
        Ikon dummyIkon = new Ikon() {
            @Override
            public String getDescription() {
                return "dummy-desc";
            }

            @Override
            public int getCode() {
                return 100;
            }
        };

        IkonliIconProvider.IkonliIcon icon = new IkonliIconProvider.IkonliIcon(dummyIkon, "test-icon-name");
        assertEquals("test-icon-name", icon.getIconIdentifier());
        assertSame(icon, icon.node());
        assertNotNull(icon.iconSizeProperty());
        assertNotNull(icon.getIconColor());
    }
}

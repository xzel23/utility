package com.dua3.utility.fx;

import com.dua3.utility.data.ImageBuffer;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for {@link FxBufferedImage}.
 */
class FxBufferedImageTest extends FxTestBase {

    /**
     * Test creating a new FxBufferedImage instance.
     */
    @Test
    void testCreateFxBufferedImage() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            FxBufferedImage image = new FxBufferedImage(100, 150);
            assertNotNull(image, "FxBufferedImage should not be null");
            assertEquals(100, image.width(), "Width should be 100");
            assertEquals(150, image.height(), "Height should be 150");
        });
    }

    /**
     * Test getting the width and height of the image.
     */
    @Test
    void testWidthAndHeight() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            FxBufferedImage image = new FxBufferedImage(200, 300);
            assertEquals(200, image.width(), "Width should be 200");
            assertEquals(300, image.height(), "Height should be 300");
            assertEquals(200, image.getWidth(), "getWidth() should return 200");
            assertEquals(300, image.getHeight(), "getHeight() should return 300");
        });
    }

    /**
     * Test getting the ARGB array.
     */
    @Test
    void testGetArgb() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            int width = 50;
            int height = 60;
            FxBufferedImage image = new FxBufferedImage(width, height);

            int[] argb = image.getArgb();
            assertNotNull(argb, "ARGB array should not be null");
            assertEquals(width * height, argb.length, "ARGB array length should be width * height");
        });
    }

    /**
     * Test getting the JavaFX Image.
     */
    @Test
    void testFxImage() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            FxBufferedImage image = new FxBufferedImage(100, 100);

            Image fxImage = image.fxImage();
            assertNotNull(fxImage, "JavaFX Image should not be null");
            assertEquals(100, fxImage.getWidth(), "JavaFX Image width should be 100");
            assertEquals(100, fxImage.getHeight(), "JavaFX Image height should be 100");
        });
    }

    /**
     * Test getting the ImageBuffer.
     */
    @Test
    void testGetBuffer() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            int width = 75;
            int height = 85;
            FxBufferedImage image = new FxBufferedImage(width, height);

            ImageBuffer buffer = image.getBuffer();
            assertNotNull(buffer, "ImageBuffer should not be null");
            assertEquals(width, buffer.width(), "ImageBuffer width should be " + width);
            assertEquals(height, buffer.height(), "ImageBuffer height should be " + height);
            assertEquals(width * height, buffer.getArgb().length, "ImageBuffer ARGB array length should be width * height");
        });
    }

    /**
     * Test that the ARGB array from getArgb() and the one from getBuffer() are the same.
     */
    @Test
    void testArgbArraysAreSame() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            FxBufferedImage image = new FxBufferedImage(50, 50);

            int[] argbDirect = image.getArgb();
            int[] argbFromBuffer = image.getBuffer().getArgb();

            assertSame(argbDirect, argbFromBuffer, "ARGB arrays from getArgb() and getBuffer() should be the same instance");
        });
    }

    /**
     * Test modifying the image by setting pixel values.
     */
    @Test
    void testModifyImage() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            int width = 10;
            int height = 10;
            FxBufferedImage image = new FxBufferedImage(width, height);

            // Set all pixels to red (ARGB: 0xFFFF0000)
            int[] argb = image.getArgb();
            for (int i = 0; i < argb.length; i++) {
                argb[i] = 0xFFFF0000;
            }

            // Check that all pixels are red
            for (int i = 0; i < argb.length; i++) {
                assertEquals(0xFFFF0000, argb[i], "Pixel should be red (0xFFFF0000)");
            }
        });
    }
}
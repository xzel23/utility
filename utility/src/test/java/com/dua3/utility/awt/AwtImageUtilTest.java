package com.dua3.utility.awt;

import com.dua3.utility.data.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the AwtImageUtil class.
 */
class AwtImageUtilTest {

    private AwtImageUtil imageUtil;

    @BeforeEach
    void setUp() {
        imageUtil = AwtImageUtil.getInstance();
    }

    @Test
    void testGetInstance() {
        assertNotNull(imageUtil);
        // Test singleton pattern
        assertSame(imageUtil, AwtImageUtil.getInstance());
    }

    @Test
    void testCreateBufferedImage() {
        int width = 100;
        int height = 80;

        AwtImage image = imageUtil.createBufferedImage(width, height);

        assertNotNull(image);
        assertEquals(width, image.width());
        assertEquals(height, image.height());
    }

    @Test
    void testCreateWithData() {
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];

        // Fill with some test data
        for (int i = 0; i < data.length; i++) {
            data[i] = 0xFF000000 | (i & 0xFFFFFF); // Set alpha to 255 and use index as color
        }

        Image image = imageUtil.create(width, height, data);

        assertNotNull(image);
        assertEquals(width, image.width());
        assertEquals(height, image.height());

        // Verify the data was properly set
        int[] imageData = image.getArgb();
        assertArrayEquals(data, imageData);
    }

    @Test
    void testConvertFromImage() {
        // Create a test image
        int width = 20;
        int height = 15;
        int[] data = new int[width * height];

        // Fill with some test data
        for (int i = 0; i < data.length; i++) {
            data[i] = 0xFF000000 | (i & 0xFFFFFF);
        }

        // Create a non-AwtImage implementation of Image for testing
        Image testImage = new Image() {
            @Override
            public int width() {
                return width;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public int[] getArgb() {
                return data;
            }
        };

        // Convert the test image to AwtImage
        AwtImage convertedImage = imageUtil.convert(testImage);

        assertNotNull(convertedImage);
        assertEquals(width, convertedImage.width());
        assertEquals(height, convertedImage.height());
        assertArrayEquals(data, convertedImage.getArgb());
    }

    @Test
    void testConvertFromAwtImage() {
        // Create an AwtImage
        int width = 30;
        int height = 25;
        AwtImage originalImage = AwtImage.create(width, height);

        // Convert the AwtImage to Image (should return the same instance)
        Image convertedImage = imageUtil.convert(originalImage);

        assertNotNull(convertedImage);
        assertSame(originalImage, convertedImage);
    }

    @Test
    void testConvertFromBufferedImage() {
        // Create a BufferedImage that is not an AwtImage
        int width = 40;
        int height = 30;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Draw something on the image to test conversion
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, 0xFF000000 | ((x + y) & 0xFFFFFF));
            }
        }

        // Convert the BufferedImage to AwtImage
        AwtImage convertedImage = imageUtil.convert(bufferedImage);

        assertNotNull(convertedImage);
        assertEquals(width, convertedImage.width());
        assertEquals(height, convertedImage.height());

        // Verify the pixel data was properly converted
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                assertEquals(bufferedImage.getRGB(x, y), convertedImage.getRGB(x, y));
            }
        }
    }

    @Test
    void testLoad() throws Exception{
        try (InputStream in = AwtImageTest.class.getResourceAsStream("image.jpg") ) {
            AwtImage image = imageUtil.load(in);
            assertNotNull(image);
            assertEquals(1024, image.getWidth());
            assertEquals(1024, image.getHeight());
        }
    }
}
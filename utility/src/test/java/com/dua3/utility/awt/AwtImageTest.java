package com.dua3.utility.awt;

import com.dua3.utility.data.ImageBuffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AwtImage class.
 */
class AwtImageTest {

    @Test
    void testCreateWithDimensions() {
        int width = 100;
        int height = 80;

        AwtImage image = AwtImage.create(width, height);

        assertNotNull(image);
        assertEquals(width, image.width());
        assertEquals(height, image.height());
        assertEquals(width * height, image.getArgb().length);
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

        AwtImage image = AwtImage.create(width, height, data);

        assertNotNull(image);
        assertEquals(width, image.width());
        assertEquals(height, image.height());

        // Verify the data was properly set
        int[] imageData = image.getArgb();
        assertArrayEquals(data, imageData);
    }

    @Test
    void testGetBuffer() {
        int width = 20;
        int height = 15;
        int[] data = new int[width * height];

        // Fill with some test data
        for (int i = 0; i < data.length; i++) {
            data[i] = 0xFF000000 | (i & 0xFFFFFF);
        }

        AwtImage image = AwtImage.create(width, height, data);
        ImageBuffer buffer = image.getBuffer();

        assertNotNull(buffer);
        assertEquals(width, buffer.width());
        assertEquals(height, buffer.height());
        assertArrayEquals(data, buffer.getArgb());
    }

    @Test
    void testWidthAndHeight() {
        int width = 200;
        int height = 150;

        AwtImage image = AwtImage.create(width, height);

        assertEquals(width, image.width());
        assertEquals(height, image.height());
        assertEquals(width, image.getWidth());  // From BufferedImage
        assertEquals(height, image.getHeight()); // From BufferedImage
    }

    @Test
    void testGetArgb() {
        int width = 5;
        int height = 5;
        int[] data = new int[width * height];

        // Fill with some test data
        for (int i = 0; i < data.length; i++) {
            data[i] = 0xFF000000 | (i & 0xFFFFFF);
        }

        AwtImage image = AwtImage.create(width, height, data);
        int[] retrievedData = image.getArgb();

        assertNotNull(retrievedData);
        assertEquals(data.length, retrievedData.length);
        assertArrayEquals(data, retrievedData);
    }

    @Test
    void testLoad() throws IOException {
        try (InputStream in = AwtImageTest.class.getResourceAsStream("image.jpg") ) {
            AwtImage image = AwtImage.load(in);
            assertNotNull(image);
            assertEquals(1024, image.getWidth());
            assertEquals(1024, image.getHeight());
        }
    }
}

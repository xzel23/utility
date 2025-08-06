package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link ImageBuffer}.
 */
class ImageBufferTest {

    @Test
    void testConstructorAndGetters() {
        // Create an image buffer
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Test getters
        assertEquals(width, imageBuffer.width());
        assertEquals(height, imageBuffer.height());
        assertSame(data, imageBuffer.getArgb());
    }

    @Test
    void testGetAndSet() {
        // Create an image buffer
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Set a pixel
        int x = 3;
        int y = 5;
        int color = 0xFF00FF00; // Green
        imageBuffer.set(x, y, color);

        // Get the pixel
        assertEquals(color, imageBuffer.get(x, y));

        // Check the data array
        assertEquals(color, data[y * width + x]);
    }

    @Test
    void testHline() {
        // Create an image buffer
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Draw a horizontal line
        int x = 2;
        int y = 3;
        int w = 5;
        int color = 0xFF0000FF; // Blue
        imageBuffer.hline(x, y, w, color);

        // Check the pixels
        for (int i = 0; i < w; i++) {
            assertEquals(color, imageBuffer.get(x + i, y));
        }

        // Check pixels outside the line
        if (x > 0) {
            assertNotEquals(color, imageBuffer.get(x - 1, y));
        }
        if (x + w < width) {
            assertNotEquals(color, imageBuffer.get(x + w, y));
        }
    }

    @Test
    void testVline() {
        // Create an image buffer
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Draw a vertical line
        int x = 4;
        int y = 1;
        int h = 6;
        int color = 0xFFFF0000; // Red
        imageBuffer.vline(x, y, h, color);

        // Check the pixels
        for (int i = 0; i < h; i++) {
            assertEquals(color, imageBuffer.get(x, y + i));
        }
    }

    @Test
    void testFill() {
        // Create an image buffer
        int width = 10;
        int height = 8;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Fill a rectangle
        int x = 2;
        int y = 3;
        int w = 4;
        int h = 2;
        int color = 0xFFFFFF00; // Yellow
        imageBuffer.fill(x, y, w, h, color);

        // Check the pixels inside the rectangle
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                assertEquals(color, imageBuffer.get(x + i, y + j));
            }
        }
    }

    @Test
    void testEquals() {
        // Create two image buffers with the same content
        int width = 5;
        int height = 4;
        int[] data1 = new int[width * height];
        int[] data2 = new int[width * height];

        // Fill with some data
        for (int i = 0; i < data1.length; i++) {
            data1[i] = i * 100;
            data2[i] = i * 100;
        }

        ImageBuffer buffer1 = new ImageBuffer(data1, width, height);
        ImageBuffer buffer2 = new ImageBuffer(data2, width, height);

        // Test equality
        assertEquals(buffer1, buffer1); // Same instance
        assertEquals(buffer1, buffer2); // Equal content
        assertEquals(buffer2, buffer1); // Symmetry

        // Test inequality
        assertNotEquals(buffer1, null); // Null check
        assertNotEquals(buffer1, "not an ImageBuffer"); // Type check

        // Different width
        ImageBuffer buffer3 = new ImageBuffer(data1, width + 1, height);
        assertNotEquals(buffer1, buffer3);

        // Different height
        ImageBuffer buffer4 = new ImageBuffer(data1, width, height + 1);
        assertNotEquals(buffer1, buffer4);

        // Different data
        int[] data3 = new int[width * height];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        data3[0] = 999; // Change one element
        ImageBuffer buffer5 = new ImageBuffer(data3, width, height);
        assertNotEquals(buffer1, buffer5);
    }

    @Test
    void testHashCode() {
        // Create two image buffers with the same content
        int width = 5;
        int height = 4;
        int[] data1 = new int[width * height];
        int[] data2 = new int[width * height];

        // Fill with some data
        for (int i = 0; i < data1.length; i++) {
            data1[i] = i * 100;
            data2[i] = i * 100;
        }

        ImageBuffer buffer1 = new ImageBuffer(data1, width, height);
        ImageBuffer buffer2 = new ImageBuffer(data2, width, height);

        // Equal objects should have equal hash codes
        assertEquals(buffer1.hashCode(), buffer2.hashCode());

        // Different data should likely have different hash codes
        int[] data3 = new int[width * height];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        data3[0] = 999; // Change one element
        ImageBuffer buffer3 = new ImageBuffer(data3, width, height);

        // Note: This is not guaranteed by the hashCode contract, but it's a good property to have
        // and likely to be true for our implementation
        assertNotEquals(buffer1.hashCode(), buffer3.hashCode());
    }

    @Test
    void testToString() {
        int width = 3;
        int height = 2;
        int[] data = {1, 2, 3, 4, 5, 6};
        ImageBuffer buffer = new ImageBuffer(data, width, height);

        String toString = buffer.toString();

        // Check that the toString contains the essential information
        assertTrue(toString.contains("ImageBuffer"));
        assertTrue(toString.contains("width=" + width));
        assertTrue(toString.contains("height=" + height));
        assertTrue(toString.contains("data=" + java.util.Arrays.toString(data)));
    }
}
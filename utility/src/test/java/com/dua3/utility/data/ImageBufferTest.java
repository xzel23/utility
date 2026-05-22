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
        assertNotEquals(null, buffer1); // Null check
        assertNotEquals("not an ImageBuffer", buffer1); // Type check

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

    @Test
    void testRect() {
        // Create an image buffer
        int width = 10;
        int height = 10;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);

        // Draw a rectangle
        int x = 2;
        int y = 2;
        int w = 6;
        int h = 6;
        int color = 0xFFFF00FF; // Magenta
        imageBuffer.rect(x, y, w, h, color);

        // Check top and bottom edges
        for (int i = 0; i < w; i++) {
            assertEquals(color, imageBuffer.get(x + i, y), "Top edge at " + (x + i) + "," + y);
            assertEquals(color, imageBuffer.get(x + i, y + h - 1), "Bottom edge at " + (x + i) + "," + (y + h - 1));
        }

        // Check left and right edges (excluding corners already checked)
        for (int j = 1; j < h - 1; j++) {
            assertEquals(color, imageBuffer.get(x, y + j), "Left edge at " + x + "," + (y + j));
            assertEquals(color, imageBuffer.get(x + w - 1, y + j), "Right edge at " + (x + w - 1) + "," + (y + j));
        }

        // Check a pixel inside the rectangle
        assertNotEquals(color, imageBuffer.get(x + 1, y + 1), "Inside pixel");
    }

    @Test
    void testArgumentChecks() {
        int width = 10;
        int height = 10;
        int[] data = new int[width * height];
        ImageBuffer imageBuffer = new ImageBuffer(data, width, height);
        int color = 0xFFFFFFFF;

        // get/set
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.get(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.get(width, 0));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.get(0, -1));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.get(0, height));

        assertThrows(IllegalArgumentException.class, () -> imageBuffer.set(-1, 0, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.set(width, 0, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.set(0, -1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.set(0, height, color));

        // hline
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(-1, 0, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(width, 0, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(0, -1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(0, height, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(5, 0, 6, color)); // x+w >= width
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.hline(0, 0, -1, color));

        // vline
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(-1, 0, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(width, 0, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(0, -1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(0, height, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(0, 5, 6, color)); // y+h >= height
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.vline(0, 0, -1, color));

        // fill
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(-1, 0, 1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(0, -1, 1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(5, 0, 6, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(0, 5, 1, 6, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(0, 0, -1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.fill(0, 0, 1, -1, color));

        // rect
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(-1, 0, 1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(0, -1, 1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(5, 0, 6, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(0, 5, 1, 6, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(0, 0, -1, 1, color));
        assertThrows(IllegalArgumentException.class, () -> imageBuffer.rect(0, 0, 1, -1, color));
    }
}
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
        
        // Check pixels outside the line
        if (y > 0) {
            assertNotEquals(color, imageBuffer.get(x, y - 1));
        }
        if (y + h < height) {
            assertNotEquals(color, imageBuffer.get(x, y + h));
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
        
        // Check pixels outside the rectangle
        if (x > 0) {
            assertNotEquals(color, imageBuffer.get(x - 1, y));
        }
        if (y > 0) {
            assertNotEquals(color, imageBuffer.get(x, y - 1));
        }
        if (x + w < width) {
            assertNotEquals(color, imageBuffer.get(x + w, y));
        }
        if (y + h < height) {
            assertNotEquals(color, imageBuffer.get(x, y + h));
        }
    }
}
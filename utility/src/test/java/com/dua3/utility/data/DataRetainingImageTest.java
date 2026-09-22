package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataRetainingImageTest {

    static class DummyDataRetainingImage implements DataRetainingImage {
        private final byte[] data;

        DummyDataRetainingImage(byte[] data) {
            this.data = data;
        }

        @Override
        public Object source() {
            return data;
        }

        @Override
        public int width() {
            return 1;
        }

        @Override
        public int height() {
            return 1;
        }

        @Override
        public int[] getArgb() {
            return new int[]{0xFF000000};
        }
    }

    @Test
    void testDataRetainingImage() throws IOException {
        byte[] expected = new byte[]{1, 2, 3, 4, 5};
        DummyDataRetainingImage img = new DummyDataRetainingImage(expected);

        assertEquals("png", img.defaultExtension());
        assertEquals("image/png", img.mimeType());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.write(baos);
        assertArrayEquals(expected, baos.toByteArray());
    }
}

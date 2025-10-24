package com.dua3.utility.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    private Path tmpPng;

    @BeforeEach
    void setUp() throws IOException {
        // create a tiny 2x2 PNG with opaque colors to avoid premultiplied alpha surprises
        BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        // ARGB values (opaque)
        int R = 0xFFFF0000; // red
        int G = 0xFF00FF00; // green
        int B = 0xFF0000FF; // blue
        int W = 0xFFFFFFFF; // white
        bi.setRGB(0, 0, R);
        bi.setRGB(1, 0, G);
        bi.setRGB(0, 1, B);
        bi.setRGB(1, 1, W);

        tmpPng = Files.createTempFile("imageutil-test-", ".png");
        // write as PNG
        assertTrue(ImageIO.write(bi, "png", tmpPng.toFile()), "PNG writer not available");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tmpPng != null) {
            Files.deleteIfExists(tmpPng);
        }
    }

    @Test
    void testToImageBuffer_and_FromImageBuffer_roundTrip() {
        ImageUtil util = ImageUtil.getInstance();

        int w = 3, h = 2;
        int[] argb = new int[]{
                0x11223344, 0x55667788, 0x99AABBCC,
                0xFFFFFFFF, 0x00000000, 0x7F010203
        };

        Image img = util.create(w, h, argb.clone());
        // default method toImageBuffer()
        ImageBuffer buf = util.toImageBuffer(img);
        assertEquals(w, buf.width());
        assertEquals(h, buf.height());
        assertArrayEquals(argb, buf.getArgb());

        // default method fromImageBuffer()
        Image img2 = util.fromImageBuffer(buf);
        assertEquals(w, img2.width());
        assertEquals(h, img2.height());
        assertArrayEquals(argb, img2.getArgb());
    }

    @Test
    void testLoad_from_Path() throws IOException {
        ImageUtil util = ImageUtil.getInstance();
        Image img = util.load(tmpPng);
        assertLoadedTinyImage(img);
    }

    @Test
    void testLoad_from_URI() throws IOException {
        ImageUtil util = ImageUtil.getInstance();
        URI uri = tmpPng.toUri();
        Image img = util.load(uri);
        assertLoadedTinyImage(img);
    }

    @Test
    void testLoad_from_URL() throws IOException {
        ImageUtil util = ImageUtil.getInstance();
        URL url = tmpPng.toUri().toURL();
        Image img = util.load(url);
        assertLoadedTinyImage(img);
    }

    private static void assertLoadedTinyImage(Image img) {
        assertNotNull(img);
        assertEquals(2, img.width());
        assertEquals(2, img.height());
        int[] data = img.getArgb();
        assertEquals(4, data.length);
        // expect the same opaque colors (order row-major)
        assertEquals(0xFFFF0000, data[0]); // (0,0) R
        assertEquals(0xFF00FF00, data[1]); // (1,0) G
        assertEquals(0xFF0000FF, data[2]); // (0,1) B
        assertEquals(0xFFFFFFFF, data[3]); // (1,1) W
    }
}

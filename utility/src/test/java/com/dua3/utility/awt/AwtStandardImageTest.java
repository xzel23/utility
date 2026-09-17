package com.dua3.utility.awt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class AwtStandardImageTest {

    private int width;
    private int height;
    private int[] data;
    private AwtMutableImage mutableImage;
    private AwtStandardImage standardImage;

    @BeforeEach
    void setUp() {
        width = 10;
        height = 8;
        data = new int[width * height];
        for (int i = 0; i < data.length; i++) {
            data[i] = 0xFF000000 | (i & 0xFFFFFF);
        }
        mutableImage = new AwtMutableImage(width, height, data);
        standardImage = new AwtStandardImage(mutableImage);
    }

    @Test
    void testDimensions() {
        assertEquals(width, standardImage.width());
        assertEquals(height, standardImage.height());
        assertEquals(width, standardImage.getWidth());
        assertEquals(height, standardImage.getHeight());
    }

    @Test
    void testGetArgbReturnsClonedArray() {
        int[] argb = standardImage.getArgb();
        assertArrayEquals(data, argb);
        assertNotSame(data, argb);

        // Mutating returned array should not affect original data
        argb[0] = 0x12345678;
        assertEquals(data[0], standardImage.getArgb()[0]);
    }

    @Test
    void testPropertiesAndSources() {
        assertNull(standardImage.getSources());
        assertEquals(java.awt.Image.UndefinedProperty, standardImage.getProperty("dummy"));
        assertNull(standardImage.getPropertyNames());
    }

    @Test
    void testColorAndSampleModel() {
        assertNotNull(standardImage.getColorModel());
        assertNotNull(standardImage.getSampleModel());
    }

    @Test
    void testTileInformation() {
        assertEquals(0, standardImage.getMinX());
        assertEquals(0, standardImage.getMinY());
        assertEquals(1, standardImage.getNumXTiles());
        assertEquals(1, standardImage.getNumYTiles());
        assertEquals(0, standardImage.getMinTileX());
        assertEquals(0, standardImage.getMinTileY());
        assertEquals(width, standardImage.getTileWidth());
        assertEquals(height, standardImage.getTileHeight());
        assertEquals(0, standardImage.getTileGridXOffset());
        assertEquals(0, standardImage.getTileGridYOffset());

        Raster tile = standardImage.getTile(0, 0);
        assertNotNull(tile);
        assertEquals(width, tile.getWidth());
        assertEquals(height, tile.getHeight());
    }

    @Test
    void testDataAndCopyData() {
        Raster fullData = standardImage.getData();
        assertNotNull(fullData);
        assertEquals(width, fullData.getWidth());
        assertEquals(height, fullData.getHeight());

        Rectangle subRect = new Rectangle(2, 2, 4, 3);
        Raster subData = standardImage.getData(subRect);
        assertNotNull(subData);
        assertEquals(4, subData.getWidth());
        assertEquals(3, subData.getHeight());

        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster targetRaster = target.getRaster();
        WritableRaster copied = standardImage.copyData(targetRaster);
        assertNotNull(copied);
    }
}

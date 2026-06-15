package com.dua3.utility.awt;

import com.dua3.utility.data.MutableImage;
import org.jspecify.annotations.Nullable;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;

/**
 * A class that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public final class AwtStandardImage implements AwtImage {

    private final MutableImage image;

    AwtStandardImage(AwtMutableImage image) {
        this.image = image;
    }

    @Override
    public int width() {
        return image.width();
    }

    @Override
    public int height() {
        return image.height();
    }

    @Override
    public int[] getArgb() {
        return image.getArgb().clone();
    }

    @Override
    public Vector<RenderedImage> getSources() {
        return image.getSources();
    }

    @Override
    public Object getProperty(String name) {
        return image.getProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
        return image.getPropertyNames();
    }

    @Override
    public ColorModel getColorModel() {
        return image.getColorModel();
    }

    @Override
    public SampleModel getSampleModel() {
        return image.getSampleModel();
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int getMinX() {
        return image.getMinX();
    }

    @Override
    public int getMinY() {
        return image.getMinY();
    }

    @Override
    public int getNumXTiles() {
        return image.getNumXTiles();
    }

    @Override
    public int getNumYTiles() {
        return image.getNumYTiles();
    }

    @Override
    public int getMinTileX() {
        return image.getMinTileX();
    }

    @Override
    public int getMinTileY() {
        return image.getMinTileY();
    }

    @Override
    public int getTileWidth() {
        return image.getTileWidth();
    }

    @Override
    public int getTileHeight() {
        return image.getTileHeight();
    }

    @Override
    public int getTileGridXOffset() {
        return image.getTileGridXOffset();
    }

    @Override
    public int getTileGridYOffset() {
        return image.getTileGridYOffset();
    }

    @Override
    public Raster getTile(int tileX, int tileY) {
        return image.getTile(tileX, tileY);
    }

    @Override
    public Raster getData() {
        return image.getData();
    }

    @Override
    public Raster getData(Rectangle rect) {
        return image.getData(rect);
    }

    @Override
    public WritableRaster copyData(WritableRaster raster) {
        return image.copyData(raster);
    }
}

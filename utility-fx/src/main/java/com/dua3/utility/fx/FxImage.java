package com.dua3.utility.fx;

import com.dua3.utility.data.Image;
import javafx.scene.image.PixelFormat;

/**
 * A record representing an image in JavaFX.
 * This class implements the {@link Image} interface.
 * @param fxImage the {@link javafx.scene.image.Image} instance to wrap
 */
public record FxImage(javafx.scene.image.Image fxImage) implements Image {

    @Override
    public int width() {
        return (int) Math.round(fxImage.getWidth());
    }

    @Override
    public int height() {
        return (int) Math.round(fxImage.getHeight());
    }

    @Override
    public int[] getArgb() {
        int w = width();
        int h = height();
        int[] data = new int[w * h];
        fxImage.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), data, 0, w);
        return data;
    }

}

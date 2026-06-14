package com.dua3.utility.fx;

import com.dua3.utility.data.Image;
import com.dua3.utility.math.MathUtil;
import javafx.scene.image.PixelFormat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A record representing an image in JavaFX.
 * This class implements the {@link Image} interface.
 * @param fxImage the {@link javafx.scene.image.Image} instance to wrap
 */
public record FxLosslessImage(javafx.scene.image.Image fxImage) implements FxStorableImage {
    @Override
    public int width() {
        return MathUtil.roundToInt(fxImage.getWidth());
    }

    @Override
    public int height() {
        return MathUtil.roundToInt(fxImage.getHeight());
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

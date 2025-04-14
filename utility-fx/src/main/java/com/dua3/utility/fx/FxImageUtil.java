package com.dua3.utility.fx;

import com.dua3.utility.data.ImageUtil;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.InputStream;

/**
 * Utility class for manipulating JavaFX images.
 */
public final class FxImageUtil implements ImageUtil<Image> {

    private static final class SingletonHolder {
        private static final FxImageUtil INSTANCE = new FxImageUtil();
    }

    private FxImageUtil() {} // utility class constructor

    /**
     * Returns the instance of FxImageUtil. Only use this if your program uses different implementations of
     * {@link ImageUtil} and you specifically need the implementation that is based on the JavaFX
     * {@link Image} class. In general, {@link #getInstance()} should be used.
     *
     * @return the instance of FxImageUtil
     */
    public static FxImageUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public FxImage load(InputStream inputStream) {
        return new FxImage(new Image(inputStream));
    }

    @Override
    public FxImage create(int w, int h, int[] argb) {
        WritableImage wr = new WritableImage(w, h);
        PixelWriter pw = wr.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), argb, 0, w);
        return new FxImage(wr);
    }

    @Override
    public Image convert(com.dua3.utility.data.Image img) {
        if (!(img instanceof FxImage)) {
            throw new UnsupportedOperationException("unsupported image class: " + img.getClass());
        }
        return ((FxImage) img).fxImage();
    }

    @Override
    public FxImage convert(Image img) {
        return new FxImage(img);
    }
}

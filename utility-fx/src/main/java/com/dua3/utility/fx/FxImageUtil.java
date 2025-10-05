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

    private FxImageUtil() { /* utility class */ }

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
        return new FxStandardImage(new Image(inputStream));
    }

    @Override
    public FxImage create(int w, int h, int[] argb) {
        WritableImage wr = new WritableImage(w, h);
        PixelWriter pw = wr.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), argb, 0, w);
        return new FxStandardImage(wr);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FxBufferedImage createBufferedImage(int w, int h) {
        return new FxBufferedImage(w, h);
    }

    @Override
    public Image convert(com.dua3.utility.data.Image img) {
        if (img instanceof FxImage fxImage) {
            return fxImage.fxImage();
        }
        return convert(create(img.width(), img.height(), img.getArgb()));
    }

    @Override
    public FxImage convert(Image img) {
        if (img instanceof FxImage fxImage) {
            return fxImage;
        }
        return new FxStandardImage(img);
    }
}

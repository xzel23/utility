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
public final class FxImageUtil implements ImageUtil {

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

    /**
     * Converts a general {@link com.dua3.utility.data.Image} to a JavaFX {@link Image}.
     * If the provided image is already an instance of {@link FxImage},
     * the underlying JavaFX Image will be returned.
     * Otherwise, a new JavaFX Image will be created from the input image's
     * width, height, and ARGB data.
     *
     * @param img the input image to be converted
     * @return the converted JavaFX {@link Image}
     */
    public Image convert(com.dua3.utility.data.Image img) {
        if (img instanceof FxImage fxImage) {
            return fxImage.fxImage();
        }
        return convert(create(img.width(), img.height(), img.getArgb()));
    }

    /**
     * Converts the given {@link Image} instance to an {@link FxImage}.
     * If the input image is already an instance of {@link FxImage}, it is returned as is.
     * Otherwise, a new {@link FxStandardImage} is created to wrap the given image.
     *
     * @param img the {@link Image} to be converted
     * @return the converted {@link FxImage} instance
     */
    public FxImage convert(Image img) {
        if (img instanceof FxImage fxImage) {
            return fxImage;
        }
        return new FxStandardImage(img);
    }
}

package com.dua3.utility.data;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.spi.SpiLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for Image handling utility classes. The concrete implementation is automatically chosen at runtime.
 *
 * @param <I> the implementation's underlying Image class
 */
public interface ImageUtil<I> {

    /**
     * Get FontUtil instance.
     *
     * @return the default FontUtil instance
     */
    static ImageUtil<?> getInstance() {
        final class SingletonHolder {
            static final ImageUtil<?> INSTANCE = SpiLoader.builder(ImageUtilProvider.class)
                    .defaultSupplier(() -> AwtImageUtil::getInstance)
                    .build()
                    .load()
                    .get();
        }

        return SingletonHolder.INSTANCE;
    }

    /**
     * Load image.
     *
     * @param in the stream to load the image from
     * @return the image
     * @throws IOException if loading fails
     */
    Image load(InputStream in) throws IOException;

    /**
     * Create image from pixel data.
     *
     * @param w    the image width
     * @param h    the image height
     * @param data the pixel data as int values containing ARGB values
     * @return the image
     */
    Image create(int w, int h, int[] data);

    /**
     * Create an empty {@link BufferedImage}.
     *
     * @param <BI> the generic type of the {@link BufferedImage} subclass that is returned
     * @param w the image width
     * @param h the image height
     * @return new {@link BufferedImage}
     */
    <BI extends BufferedImage & Image> BI createBufferedImage(int w, int h);

    /**
     * Convert {@link Image} instance to underlying implementation.
     *
     * @param img the image
     * @return implementation dependent image class
     */
    I convert(Image img);

    /**
     * Convert image from underlying implementation to {@link Image} instance.
     *
     * @param img the implementation dependent image
     * @return image
     */
    Image convert(I img);

    /**
     * Convert {@link Image} to {@link ImageBuffer}.
     *
     * @param img the image
     * @return the ARGBImage
     */
    default ImageBuffer toImageBuffer(Image img) {
        return new ImageBuffer(img.getArgb(), img.width(), img.height());
    }

    /**
     * Convert {@link ImageBuffer} to {@link Image}.
     *
     * @param img the ARGBImage
     * @return the image
     */
    default Image fromImageBuffer(ImageBuffer img) {
        return create(img.width(), img.height(), img.getArgb());
    }
}

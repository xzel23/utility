package com.dua3.utility.data;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.spi.Loader;

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
        class SingletonHolder {
            static final ImageUtil<?> INSTANCE = Loader.builder(ImageUtilProvider.class)
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

}

package com.dua3.utility.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Interface for Image handling utility classes. The concrete implementation is automatically chosen at runtime.
 *
 * @param <I> the implementation's underlying Image class
 */
public interface ImageUtil<I> {

    /**
     * Return the default ImageUtil instance.
     *
     * @return default instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static ImageUtil<? extends Image> getInstance() {
        Iterator<ImageUtil> serviceIterator = ServiceLoader
                .load(ImageUtil.class)
                .iterator();

        ImageUtil<? extends Image> iu;
        if (serviceIterator.hasNext()) {
            iu = serviceIterator.next();
        } else {
            iu = new ImageUtil<>() {
                @SuppressWarnings("MethodMayBeStatic")
                private <T> T noImplementation() {
                    throw new UnsupportedOperationException("no ImageUtil implementation present");
                }

                @Override
                public Image load(InputStream in) {
                    return noImplementation();
                }

                @Override
                public Image create(int w, int h, int[] data) {
                    return noImplementation();
                }

                @Override
                public Image convert(Image img) {
                    return noImplementation();
                }
            };
        }

        return iu;
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

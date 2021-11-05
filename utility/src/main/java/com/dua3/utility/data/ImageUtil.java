package com.dua3.utility.data;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Interface for Image handling utility classes. The concrete implementation is automatically chosen at runtime.
 * @param <I> the implementation's underlying Image class
 */
public interface ImageUtil<I> {

    String NO_IMPLEMENTATION = "no ImageUtil implementation present";

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
                @Override
                public @NotNull Image load(@NotNull InputStream in) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public @NotNull Image create(int w, int h, int[] data) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public @NotNull Image convert(@NotNull Image img) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }
            };
        }

        return iu;
    }

    /**
     * Load image.
     * @param in the stream to load the image from
     * @return the image
     * @throws IOException if loading fails
     */
    @NotNull Image load(@NotNull InputStream in) throws IOException;

    /**
     * Create image from pixel data.
     * @param w the image width
     * @param h the image height
     * @param data the pixeal data as int values containing ARGB values
     * @return the image
     */
    @NotNull Image create(int w, int h, int[] data);

    /**
     * Convert image to underlying implementation.
     * @param img the image
     * @return implementation dependent image class
     */
    @NotNull I convert(@NotNull Image img);

    /**
     * Convert image underlying implementation. to image.
     * @param img the implementation dependent image
     * @return image
     */
    @NotNull Image convert(@NotNull I img);
    
}

package com.dua3.utility.data;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.spi.SpiLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/**
 * Interface for Image handling utility classes. The concrete implementation is automatically chosen at runtime.
 */
public interface ImageUtil {

    /**
     * Get FontUtil instance.
     *
     * @return the default FontUtil instance
     */
    static ImageUtil getInstance() {
        final class SingletonHolder {
            static final ImageUtil INSTANCE = SpiLoader.builder(ImageUtilProvider.class)
                    .defaultSupplier(() -> AwtImageUtil::getInstance)
                    .build()
                    .load()
                    .get();

            private SingletonHolder() {}
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
     * Load image.
     *
     * @param path the image path
     * @return the image
     * @throws IOException if loading fails
     */
    default Image load(Path path) throws IOException {
        try (InputStream in = java.nio.file.Files.newInputStream(path)) {
            return load(in);
        }
    }

    /**
     * Load image.
     *
     * @param uri the image URI
     * @return the image
     * @throws IOException if loading fails
     */
    default Image load(URI uri) throws IOException {
        try (InputStream in = IoUtil.openInputStream(uri)) {
            return load(in);
        }
    }

    /**
     * Load image.
     *
     * @param url the image URL
     * @return the image
     * @throws IOException if loading fails
     */
    default Image load(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return load(in);
        }
    }

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
     * @param w the image width
     * @param h the image height
     * @return new {@link BufferedImage}
     */
    BufferedImage createBufferedImage(int w, int h);

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

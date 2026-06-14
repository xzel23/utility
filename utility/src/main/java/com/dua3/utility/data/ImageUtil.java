package com.dua3.utility.data;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.io.Payload;
import com.dua3.utility.spi.SpiLoader;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Interface for Image handling utility classes. The concrete implementation is automatically chosen at runtime.
 * @param <I> the image type returen by methods that create new images, i.e., {@link #load(Payload)},
 * {@link #create(int, int, int[])}, etc.
 */
public interface ImageUtil<I extends Image> {

    /**
     * Defines the options available for loading images. These options determine
     * how the image loading process handles memory and data retention.
     */
    enum LoadOption {
        /**
         * An option in the {@link LoadOption} enum that indicates automatic handling
         * of memory and data retention when loading images. Images loaded using a lossless format will be written out
         * as PNG, JPEG images will wite out the original data the imaae was created from.
         */
        AUTOMATIC,
        /**
         * Retain the original image data to be written out again later in {@link #write(Image, OutputStream, String)}.
         */
        RETAIN_DATA,
        /**
         * Do not retain the original image data. In this mode, the image is loaded and the internal representation
         * created without any reference to the original data. Images loaded in this mode cannot will be written out
         * in a standard format.
         */
        DONT_RETAIN_DATA;
    }

    /**
     * A constant {@link Magic} instance that defines the magic numbers for the know image formats.
     */
    Magic MAGIC = new Magic(List.of(
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            Magic.MagicNumber.of("image/png", 0x89504E470D0A1A0AL, 0xFFFFFFFFFFFFFFFFL),

            // JPEG: FF D8 FF (Masks only the first 3 bytes, ignores the remaining 5)
            Magic.MagicNumber.of("image/jpeg", 0xFFD8FF0000000000L, 0xFFFFFF0000000000L),

            // GIF89a: 47 49 46 38 39 61 (GIF89a -> G I F 8 9 a)
            Magic.MagicNumber.of("image/gif", 0x4749463839610000L, 0xFFFFFFFFFFFF0000L),

            // GIF87a: 47 49 46 38 37 61 (GIF87a -> G I F 8 7 a)
            Magic.MagicNumber.of("image/gif", 0x4749463837610000L, 0xFFFFFFFFFFFF0000L),

            // BMP: 42 4D (BM - Masks first 2 bytes)
            Magic.MagicNumber.of("image/bmp", 0x424D000000000000L, 0xFFFF000000000000L),

            // WebP: 52 49 46 46 xx xx xx xx (RIFF header - Masks first 4 bytes)
            // Note: Full WebP validation checks bytes 8-11 for "WEBP", which falls outside
            // of a single 8-byte long. This detects the RIFF container commonly used for WebP.
            Magic.MagicNumber.of("image/webp", 0x5249464600000000L, 0xFFFFFFFF00000000L),

            // TIFF (Little Endian): 49 49 2A 00
            Magic.MagicNumber.of("image/tiff", 0x49492A0000000000L, 0xFFFFFFFF00000000L),

            // TIFF (Big Endian): 4D 4D 00 2A
            Magic.MagicNumber.of("image/tiff", 0x4D4D002A00000000L, 0xFFFFFFFF00000000L),

            // ICO (Windows Icon): 00 00 01 00
            Magic.MagicNumber.of("image/x-icon", 0x0000010000000000L, 0xFFFFFFFF00000000L)
    ));

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
     * @param payload the {@link Payload} to load the image from
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(Payload payload) throws IOException {
        return load(payload, LoadOption.AUTOMATIC);
    }

    /**
     * Load image.
     *
     * @param in the stream to load the image from
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(InputStream in) throws IOException {
        return load(Payload.fromInputStream(in), LoadOption.AUTOMATIC);
    }

    /**
     * Load image.
     *
     * @param uri the image URI
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(URI uri) throws IOException {
        return load(Payload.fromUri(uri), LoadOption.AUTOMATIC);
    }

    /**
     * Load image.
     *
     * @param path the image path
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(Path path) throws IOException {
        return load(Payload.fromPath(path), LoadOption.AUTOMATIC);
    }


    /**
     * Load image.
     *
     * @param url the image URL
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(URL url) throws IOException {
        return load(Payload.fromUrl(url));
    }

    /**
     * Load image.
     *
     * @param payload the {@link Payload} to load the image from
     * @param loadOption the load option to apply
     * @return the image
     * @throws IOException if loading fails
     */
    I load(Payload payload, LoadOption loadOption) throws IOException;


    /**
     * Load image.
     *
     * @param in the stream to load the image from
     * @param loadOption the load option to apply
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(InputStream in, LoadOption loadOption) throws IOException {
        return load(Payload.fromInputStream(in), loadOption);
    }


    /**
     * Load image.
     *
     * @param uri the image URI
     * @param loadOption the load option to apply
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(URI uri, LoadOption loadOption) throws IOException {
        return load(Payload.fromUri(uri), loadOption);
    }

    /**
     * Load image.
     *
     * @param path the image path
     * @param loadOption the load option to apply
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(Path path, LoadOption loadOption) throws IOException {
        return load(path.toUri(), loadOption);
    }


    /**
     * Load image.
     *
     * @param url the image URL
     * @param loadOption the load option to apply
     * @return the image
     * @throws IOException if loading fails
     */
    default I load(URL url, LoadOption loadOption) throws IOException {
        return load(Payload.fromUrl(url), loadOption);
    }

    /**
     * Create image from pixel data.
     *
     * @param w    the image width
     * @param h    the image height
     * @param data the pixel data as int values containing ARGB values
     * @return the image
     */
    I create(int w, int h, int[] data);

    /**
     * Create an empty {@link BufferedImage}.
     *
     * @param <J> the image type
     * @param w   the image width
     * @param h   the image height
     * @return new {@link BufferedImage}
     */
    <J extends BufferedImage & Image> J createBufferedImage(int w, int h);

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
    default I fromImageBuffer(ImageBuffer img) {
        return create(img.width(), img.height(), img.getArgb());
    }

    /**
     * Converts the given {@link Image} to a {@link BufferedImage}.
     * If the input image is already an instance of BufferedImage, it is returned as-is.
     * Otherwise, a new BufferedImage is created and populated with the ARGB data from the input image.
     *
     * @param image the image to convert; must not be null
     * @return the converted BufferedImage
     */
    default BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bi) {
            return bi;
        }

        BufferedImage bufferedImage = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
        int[] buffer = image.getArgb();

        bufferedImage.setRGB(0, 0, image.width(), image.height(), buffer, 0, image.width());
        return bufferedImage;
    }

    /**
     * Writes the given {@link Image} to the specified {@link OutputStream}
     * in the specified image format.
     *
     * @param image    the image to write; must not be null
     * @param mimeType the MIME type of the image format (e.g., "image/png", "image/jpeg"); must not be null
     * @param out      the output stream to write the image data to; must not be null
     * @throws IOException if writing the image fails, including issues with the output stream or unsupported MIME type
     */
    default void write(Image image, OutputStream out, String mimeType) throws IOException {
        ImageWriter writer = ImageTypeData.getByMimeType(mimeType).writer().get();

        try (ImageOutputStream imageOut = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(imageOut);
            RenderedImage renderedImage = toBufferedImage(image);
            writer.write(renderedImage);
        } finally {
            writer.dispose(); // Always clear native resources held by ImageIO
        }
    }
}

record ImageTypeData(Supplier<ImageWriter> writer, Supplier<ImageReader> reader) {

    private static final Map<String, ImageTypeData> TYPES = new ConcurrentHashMap<>();

    static ImageTypeData getByMimeType(String mimeType) {
        return TYPES.computeIfAbsent(mimeType, mime -> {
            Supplier<ImageReader> readerSupplier = () -> {
                try {
                    return ImageIO.getImageReadersByMIMEType(mimeType).next();
                } catch (NoSuchElementException e) {
                    throw new IllegalStateException("No ImageReader found for type: " + mimeType, e);
                }
            };

            Supplier<ImageWriter> writerSupplier = () -> {
                try {
                    return ImageIO.getImageWritersByMIMEType(mimeType).next();
                } catch (NoSuchElementException e) {
                    throw new IllegalStateException("No ImageWriter found for type: " + mimeType, e);
                }
            };

            return new ImageTypeData(writerSupplier, readerSupplier);
        });
    }
}


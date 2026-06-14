package com.dua3.utility.fx;

import com.dua3.utility.data.DataRetainingImage;
import com.dua3.utility.math.MathUtil;
import javafx.scene.image.PixelFormat;

/**
 * Represents a lossy image in the JavaFX context with associated metadata.
 * This class provides methods to retrieve image dimensions, ARGB pixel data,
 * and to write the unchanged image data to an output stream.
 * <p>
 * The lossy image is stored in conjunction with its mime type, default file extension,
 * and original binary data that represents the compressed image format.
 *
 * @param fxImage           The JavaFX image object representing the visual data.
 * @param mimeType          The MIME type of the image format (e.g., "image/jpeg").
 * @param defaultExtension  The default file extension associated with the image format (e.g., "jpg").
 * @param source            The original binary data of the lossy-compressed image, stored as Object.
 */
public record FxDataRetainingStorableImage(javafx.scene.image.Image fxImage, String mimeType, String defaultExtension,
                                           Object source) implements FxImage, DataRetainingImage {

    /**
     * Validates the input parameters for the compact constructor of the {@code FxDataRetainingStorableImage} record.
     * Ensures that the {@code source} parameter is of type {@code byte[]}, which represents the binary data of the
     * lossy-compressed image.
     *
     * @param fxImage          The JavaFX image object representing the image's visual data.
     * @param mimeType         The MIME type of the image format (e.g., "image/png", "image/jpeg").
     * @param defaultExtension The default file extension associated with the image format (e.g., "png", "jpg").
     * @param source           The original binary data of the image. Must be an instance of {@code byte[]}.
     * @throws IllegalArgumentException If {@code source} is not of type {@code byte[]}.
     */
    public FxDataRetainingStorableImage {
        if (!(source instanceof byte[])) {
            throw new IllegalArgumentException("source must be byte[]");
        }
    }

    /**
     * Constructs an instance of FxDataRetainingStorableImage using a JavaFX image,
     * MIME type, default extension, and the original source byte array representing
     * the lossy-compressed image data.
     *
     * @param fxImage          The JavaFX image object representing the image's visual data.
     * @param mimeType         The MIME type of the image format (e.g., "image/png", "image/jpeg").
     * @param defaultExtension The default file extension associated with the image format (e.g., "png", "jpg").
     * @param source           The original binary data of the image represented as a byte array.
     */
    public FxDataRetainingStorableImage(javafx.scene.image.Image fxImage, String mimeType, String defaultExtension,
                                        byte[] source) {
        this(fxImage, mimeType, defaultExtension, (Object) source);
    }

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

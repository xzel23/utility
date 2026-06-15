package com.dua3.utility.fx;

import com.dua3.utility.data.ImageBuffer;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.Payload;
import javafx.scene.image.Image;

import java.io.IOException;

/**
 * Utility class for manipulating JavaFX images.
 */
public final class FxImageUtil implements ImageUtil<FxImage, FxMutableImage> {

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
    public FxImage load(Payload payload, LoadOption loadOption) throws IOException {
        try (payload) {
            String mime = MAGIC.getMimeType(payload.magic8Bytes());
            LoadOption activeLoadOption = loadOption != LoadOption.AUTOMATIC
                    ? loadOption
                    : (mime.equals("image/jpeg") ? LoadOption.RETAIN_DATA : LoadOption.DONT_RETAIN_DATA);

            return switch (activeLoadOption) {
                case RETAIN_DATA -> {
                    // for jpeg, we want to keep the original data in order not to introduce new artifacts
                    try (var imageIn = IoUtil.getInputStream(payload.stream().readAllBytes())) {
                        yield new FxDataRetainingImage(new Image(imageIn), "image/jpeg", "jpg", payload.stream().readAllBytes());
                    }
                }
                default -> new FxWrappedImage(new Image(payload.stream()));
            };
        }
    }

    @Override
    public FxMutableImage loadMutable(Payload payload) throws IOException {
        return FxMutableImage.loadImage(payload);
    }

    @Override
    public FxMutableImage createImage(int w, int h, int[] data) {
        return new FxMutableImage(w, h, data);
    }

    @Override
    public FxMutableImage createImage(int w, int h) {
        return new FxMutableImage(w, h, null);
    }

    /**
     * Converts a given {@link com.dua3.utility.data.Image} to an {@link FxImage}.
     * If the provided image is already an instance of {@link FxImage}, it is returned as-is.
     * Otherwise, it is converted using an intermediate {@link ImageBuffer}.
     *
     * @param image the input image to convert. Must not be null.
     * @return an {@link FxImage} instance representing the converted image.
     */
    public FxImage toImage(com.dua3.utility.data.Image image) {
        if (image instanceof FxImage fxi) {
            return fxi;
        }

        return fromImageBuffer(new ImageBuffer(image.getArgb(), image.width(), image.height()));
    }
}

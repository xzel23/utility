package com.dua3.utility.fx;

import com.dua3.utility.data.ImageBuffer;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.Payload;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Utility class for manipulating JavaFX images.
 */
public final class FxImageUtil implements ImageUtil<FxImage> {

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
                default -> new FxBufferedImage(new Image(payload.stream()));
            };
        }
    }

    @Override
    public FxImage create(int w, int h, int[] data) {
        return new FxBufferedImage(w, h);
    }

    @Override
    public FxBufferedImage createBufferedImage(int w, int h) {
        return new FxBufferedImage(w, h);
    }

}

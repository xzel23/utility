package com.dua3.utility.awt;

import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.Payload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

/**
 * An implementation of the ImageUtil interface for working with awt images.
 */
public final class AwtImageUtil implements ImageUtil<AwtImage, AwtMutableImage> {

    private static final class SingletonHolder {
        private static final AwtImageUtil INSTANCE = new AwtImageUtil();
    }

    /**
     * Private constructor to prevent direct instantiation of the AwtImageUtil class.
     * <p>
     * Use {@link #getInstance()} to access the singleton instance of AwtImageUtil.
     */
    private AwtImageUtil() {}

    /**
     * Returns an instance of AwtImageUtil.
     *
     * @return the instance of AwtImageUtil
     */
    public static AwtImageUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public AwtImage load(Payload payload, LoadOption loadOption) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(payload.stream())) {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            if (!iter.hasNext()) {
                throw new IOException("no matching ImageReader found");
            }

            ImageReader reader = iter.next();
            try {
                reader.setInput(iis);
                BufferedImage image = reader.read(0);
                AwtMutableImage awtImage = new AwtMutableImage(image.getWidth(), image.getHeight(), null);
                awtImage.getGraphics().drawImage(image, 0, 0, null);
                return awtImage;
            } finally {
                reader.dispose();  // Clean up the reader
            }
        }
    }

    @Override
    public AwtMutableImage loadMutable(Payload payload) throws IOException {
        return AwtMutableImage.loadImage(payload);
    }

    @Override
    public AwtMutableImage createImage(int w, int h, int[] data) {
        return new AwtMutableImage(w, h, data);
    }

    @Override
    public AwtMutableImage createImage(int w, int h) {
        return new AwtMutableImage(w, h, null);
    }

    /**
     * Converts an instance of {@code com.dua3.utility.data.Image} into an {@code AwtImage}.
     * If the provided image is already an instance of {@code AwtImage}, it is returned directly.
     * Otherwise, a new {@code AwtImage} is created based on the dimensions and pixel data of the input image.
     *
     * @param img the {@code Image} to be converted
     * @return the converted {@code AwtImage} instance
     */
    @Override
    public AwtMutableImage toImage(com.dua3.utility.data.Image img) {
        if (img instanceof AwtMutableImage awtImage) {
            return awtImage;
        }

        return createImage(img.width(), img.height(), img.getArgb());
    }

    /**
     * Converts a {@link BufferedImage} to an {@link AwtMutableImage}.
     *
     * @param img the BufferedImage to be converted
     * @return the converted AwtImage
     */
    public AwtMutableImage toMutableImage(BufferedImage img) {
        if (img instanceof AwtMutableImage awtImage) {
            return awtImage;
        }

        AwtMutableImage awtImage = createImage(img.getWidth(), img.getHeight());
        awtImage.getGraphics().drawImage(img, 0, 0, null);
        return awtImage;
    }
}

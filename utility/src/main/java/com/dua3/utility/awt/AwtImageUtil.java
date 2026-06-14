package com.dua3.utility.awt;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.Payload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

/**
 * An implementation of the ImageUtil interface for working with awt images.
 */
public final class AwtImageUtil implements ImageUtil<AwtImage> {

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
                AwtImage awtImage = AwtImage.create(image.getWidth(), image.getHeight());
                awtImage.getGraphics().drawImage(image, 0, 0, null);
                return awtImage;
            } finally {
                reader.dispose();  // Clean up the reader
            }
        }
    }

    @Override
    public AwtImage create(int w, int h, int[] data) {
        return AwtImage.create(w, h, data);
    }

    @Override
    public AwtImage createBufferedImage(int w, int h) {
        return AwtImage.create(w, h);
    }

    /**
     * Converts an instance of {@code com.dua3.utility.data.Image} into an {@code AwtImage}.
     * If the provided image is already an instance of {@code AwtImage}, it is returned directly.
     * Otherwise, a new {@code AwtImage} is created based on the dimensions and pixel data of the input image.
     *
     * @param img the {@code Image} to be converted
     * @return the converted {@code AwtImage} instance
     */
    public AwtImage convert(com.dua3.utility.data.Image img) {
        if (img instanceof AwtImage awtImage) {
            return awtImage;
        } else {
            return AwtImage.create(img.width(), img.height(), img.getArgb());
        }
    }

    /**
     * Converts an AwtImage to a generic Image.
     * This method accepts an instance of AwtImage and directly returns it as a generic Image.
     *
     * @param img the AwtImage to be converted
     * @return the provided AwtImage cast as a generic Image
     */
    public Image convert(AwtImage img) {
        return img;
    }

    /**
     * Converts a {@link BufferedImage} to an {@link AwtImage}.
     *
     * @param img the BufferedImage to be converted
     * @return the converted AwtImage
     */
    public AwtImage convert(BufferedImage img) {
        if (img instanceof AwtImage awtImage) {
            return awtImage;
        } else {
            AwtImage awtImage = AwtImage.create(img.getWidth(), img.getHeight());
            awtImage.getGraphics().drawImage(img, 0, 0, null);
            return awtImage;
        }
    }
}
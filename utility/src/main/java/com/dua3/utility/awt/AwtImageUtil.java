package com.dua3.utility.awt;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

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
    public AwtImage load(InputStream in) throws IOException {
        return AwtImage.load(in);
    }

    @Override
    public Image create(int w, int h, int[] data) {
        return AwtImage.create(w, h, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AwtImage createBufferedImage(int w, int h) {
        return AwtImage.create(w, h);
    }

    @Override
    public AwtImage convert(com.dua3.utility.data.Image img) {
        if (img instanceof AwtImage awtImage) {
            return awtImage;
        } else {
            return AwtImage.create(img.width(), img.height(), img.getArgb());
        }
    }

    @Override
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
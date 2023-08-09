package com.dua3.utility.swing;

import com.dua3.utility.data.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

/**
 * A class that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public record SwingImage(BufferedImage bufferedImage) implements Image {

    /**
     * Constructs a SwingImage object with the given BufferedImage.
     *
     * @param bufferedImage the BufferedImage to be used for the SwingImage
     * @throws NullPointerException if the bufferedImage is null
     */
    public SwingImage {
        Objects.requireNonNull(bufferedImage);
    }

    /**
     * Creates a SwingImage object with the given width, height, and pixel data.
     *
     * @param w the width of the image
     * @param h the height of the image
     * @param data the pixel data to be used for the image
     * @return a new SwingImage object with the specified width, height, and pixel data
     */
    public static SwingImage create(int w, int h, int[] data) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, data, 0, w);
        return new SwingImage(image);
    }

    /**
     * Loads an image from the specified input stream and creates a SwingImage object.
     *
     * @param in the input stream containing the image data
     * @return a new SwingImage object created from the loaded image
     * @throws IOException if an I/O error occurs while reading the image
     */
    public static SwingImage load(InputStream in) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            if (!iter.hasNext()) {
                throw new IOException("no matching ImageReader found");
            }

            ImageReader reader = iter.next();
            BufferedImage bufferedImage = reader.read(0);
            return new SwingImage(bufferedImage);
        }
    }

    @Override
    public int width() {
        return bufferedImage().getWidth();
    }

    @Override
    public int height() {
        return bufferedImage().getHeight();
    }

    @Override
    public int[] getArgb() {
        return bufferedImage().getRaster().getPixels(0, 0, width(), height(), (int[]) null);
    }

}

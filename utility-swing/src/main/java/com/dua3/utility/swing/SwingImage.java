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

public record SwingImage(BufferedImage bufferedImage) implements Image {

    public SwingImage {
        Objects.requireNonNull(bufferedImage);
    }

    public static SwingImage create(int w, int h, int[] data) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, data, 0, w);
        return new SwingImage(image);
    }

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

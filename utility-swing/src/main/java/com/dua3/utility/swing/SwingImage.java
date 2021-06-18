package com.dua3.utility.swing;

import com.dua3.utility.data.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class SwingImage extends Image {
    
    private final String format;
    private final BufferedImage bufferedImage;

    public static SwingImage create(int w, int h, int[] data) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, data, 0, w);
        return new SwingImage(image, "");
    }

    public static SwingImage load(InputStream in) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            if (!iter.hasNext()) {
                throw new IOException("no matching ImageReader found");
            }
            
            ImageReader reader = iter.next();
            String format = reader.getFormatName();
            BufferedImage bufferedImage = reader.read(0);
            return new SwingImage(bufferedImage, format);
        }
    }
    
    SwingImage(BufferedImage bufferedImage, String format) {
        this.bufferedImage = Objects.requireNonNull(bufferedImage);
        this.format = Objects.requireNonNull(format);
    }
    
    @Override
    public void write(OutputStream out) throws IOException {
        ImageIO.write(bufferedImage, format, out);
    }

    @Override
    public int width() {
        return bufferedImage.getWidth();
    }

    @Override
    public int height() {
        return bufferedImage.getHeight();
    }
    
    BufferedImage bufferedImage() {
        return bufferedImage;
    }
}

package com.dua3.utility.swing;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SwingImageUtil implements ImageUtil<BufferedImage> {
    @Override
    public SwingImage load(InputStream in) throws IOException {
        return SwingImage.load(in);
    }

    @Override
    public Image create(int w, int h, int[] data) {
        return SwingImage.create(w, h, data);
    }

    @Override
    public BufferedImage convert(com.dua3.utility.data.Image img) {
        if (!(img instanceof SwingImage)) {
            throw new UnsupportedOperationException("unsupported image class: "+img.getClass());
        }
        return ((SwingImage) img).bufferedImage();
    }

    @Override
    public SwingImage convert(BufferedImage img) {
        return new SwingImage(img);
    }
}

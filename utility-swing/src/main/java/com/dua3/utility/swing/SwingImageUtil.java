package com.dua3.utility.swing;

import com.dua3.utility.data.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class SwingImageUtil implements ImageUtil<BufferedImage> {
    @Override
    public Optional<SwingImage> load(InputStream in) throws IOException {
        return SwingImage.load(in);
    }

    @Override
    public BufferedImage convert(com.dua3.utility.data.Image img) {
        if (!(img instanceof SwingImage)) {
            throw new UnsupportedOperationException("unsupported image class: "+img.getClass());
        }
        return ((SwingImage) img).bufferedImage();
    }

    @Override
    public com.dua3.utility.data.Image convert(BufferedImage img) {
        return new SwingImage(img, "");
    }
}

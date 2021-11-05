package com.dua3.utility.swing;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SwingImageUtil implements ImageUtil<BufferedImage> {
    @Override
    public @NotNull SwingImage load(@NotNull InputStream in) throws IOException {
        return SwingImage.load(in);
    }

    @Override
    public @NotNull Image create(int w, int h, int[] data) {
        return SwingImage.create(w, h, data);
    }

    @Override
    public @NotNull BufferedImage convert(com.dua3.utility.data.@NotNull Image img) {
        if (!(img instanceof SwingImage)) {
            throw new UnsupportedOperationException("unsupported image class: "+img.getClass());
        }
        return ((SwingImage) img).bufferedImage();
    }

    @Override
    public @NotNull SwingImage convert(@NotNull BufferedImage img) {
        return new SwingImage(img);
    }
}

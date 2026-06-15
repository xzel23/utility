package com.dua3.utility.data;

import com.dua3.utility.io.Payload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MutableImage extends BufferedImage implements Image {

    private static final DirectColorModel DIRECT_COLOR_MODEL = new DirectColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            32,    // Bits (ARGB: 8+8+8+8)
            0x00FF0000, // Red mask
            0x0000FF00, // Green mask
            0x000000FF, // Blue mask
            0xFF000000, // Alpha mask
            true,       // premultiplied alpha
            DataBuffer.TYPE_INT
    );

    private static WritableRaster createRaster(int w, int h, int[] data) {
        // create the buffer
        int size = w * h;

        if (data.length != size) {
            throw new IllegalArgumentException("data.length != w * h");
        }

        DataBuffer dataBuffer = new DataBufferInt(data, size);

        // Create the sample model
        SampleModel sampleModel = DIRECT_COLOR_MODEL.createCompatibleSampleModel(w, h);

        // Create the WritableRaster
        return Raster.createWritableRaster(sampleModel, dataBuffer, null);
    }

    private final ImageBuffer buffer;

    protected MutableImage(int width, int height, int[] data) {
        super(DIRECT_COLOR_MODEL, createRaster(width, height, data), false, null);
        this.buffer = new ImageBuffer(data, width, height);
    }

    protected static <I extends MutableImage> I loadImage(Payload payload, BiFunction<Integer, Integer, I> factory) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(payload.stream())) {
            ImageReader reader = ImageUtil.getInstance().getImageReader(payload);
            try {
                reader.setInput(iis, true);

                BufferedImage decodedImage = reader.read(0, reader.getDefaultReadParam());

                int w = decodedImage.getWidth();
                int h = decodedImage.getHeight();

                I targetImage = factory.apply(w, h);
                java.awt.Graphics2D g2d = targetImage.createGraphics();
                try {
                    g2d.drawImage(decodedImage, 0, 0, null);
                } finally {
                    g2d.dispose();
                }

                return targetImage;
            } finally {
                reader.dispose();
            }
        }
    }

    @Override
    public int width() {
        return getWidth();
    }

    @Override
    public int height() {
        return getHeight();
    }

    @Override
    public final int[] getArgb() {
        return buffer.data();
    }

    /**
     * Creates and returns a new {@link ImageBuffer} instance using the pixel data, width,
     * and height of this {@code FxBufferedImage}. This buffer can be used to manipulate the
     * image data.
     *
     * @return a new {@link ImageBuffer} containing the image data, width, and height
     */
    public final ImageBuffer getBuffer() {
        return new ImageBuffer(getArgb(), width(), height());
    }

}

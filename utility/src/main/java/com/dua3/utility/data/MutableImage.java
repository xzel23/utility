package com.dua3.utility.data;

import com.dua3.utility.io.Payload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
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

/**
 * Represents a mutable image that extends the functionality of a {@link BufferedImage}
 * and implements the {@link Image} interface. This abstract class provides a base
 * for image representations that allow direct manipulation of pixel data.
 */
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

    /**
     * Creates a {@link WritableRaster} from the provided dimensions and pixel data.
     *
     * @param w    the width of the raster
     * @param h    the height of the raster
     * @param data the pixel data for the raster, expected to have a length of {@code w * h}
     * @return a writable raster created using the specified dimensions and pixel data
     * @throws IllegalArgumentException if the length of {@code data} is not equal to {@code w * h}
     */
    private static WritableRaster createRaster(int w, int h, int[] data) {
        int size = w * h;

        if (data.length != size) {
            throw new IllegalArgumentException("data.length != w * h");
        }

        DataBuffer dataBuffer = new DataBufferInt(data, size);
        SampleModel sampleModel = DIRECT_COLOR_MODEL.createCompatibleSampleModel(w, h);

        return Raster.createWritableRaster(sampleModel, dataBuffer, null);
    }

    private final ImageBuffer buffer;

    /**
     * Constructs a {@code MutableImage} instance with the specified dimensions and pixel data.
     *
     * @param width  the width of the image, in pixels
     * @param height the height of the image, in pixels
     * @param data   the pixel data for the image in ARGB format. The array should have a length
     *               of {@code width * height}.
     * @throws IllegalArgumentException if the length of {@code data} does not match {@code width * height}
     */
    protected MutableImage(int width, int height, int[] data) {
        super(DIRECT_COLOR_MODEL, createRaster(width, height, data), false, null);
        this.buffer = new ImageBuffer(data, width, height);
    }

    /**
     * Loads an image from the provided payload and creates a new instance of the specified MutableImage type.
     * The dimensions of the loaded image are used to initialize the new instance, and the pixel data is copied.
     *
     * @param <I>      the specific type of {@code MutableImage} to be returned
     * @param payload  the payload containing the image data to be loaded
     * @param factory  a factory function to create a new instance of {@code MutableImage}, using the image's width and height
     * @return an instance of the specified {@code MutableImage} type, initialized with the loaded image data
     * @throws IOException if an I/O error occurs during loading of the image
     */
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

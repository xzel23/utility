package com.dua3.utility.awt;

import com.dua3.utility.data.ImageBuffer;
import com.dua3.utility.data.MutableImage;

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
import java.io.InputStream;
import java.util.Iterator;

/**
 * A class that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public class AwtImage extends BufferedImage implements MutableImage {

    private final int[] data;

    /**
     * Creates a SwingImage object with the given width, height, and pixel data.
     *
     * @param w the width of the image
     * @param h the height of the image
     * @param data the pixel data to be used for the image
     * @return a new SwingImage object with the specified width, height, and pixel data
     */
    public static AwtImage create(int w, int h, int[] data) {
        DataBuffer dataBuffer = new DataBufferInt(data, data.length);

        // Define the DirectColorModel for non-premultiplied ARGB
        DirectColorModel colorModel = new DirectColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                32,    // Bits (ARGB: 8+8+8+8)
                0x00FF0000, // Red mask
                0x0000FF00, // Green mask
                0x000000FF, // Blue mask
                0xFF000000, // Alpha mask
                true,       // premultiplied alpha
                DataBuffer.TYPE_INT);

        // Create the sample model
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(w, h);

        // Create the WritableRaster
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        // Create the BufferedImage
        return new AwtImage(data, colorModel, raster);
    }

    /**
     * Creates a SwingImage object with the given width, height, and pixel data.
     *
     * @param w the width of the image
     * @param h the height of the image
     * @return a new SwingImage object with the specified width and height
     */
    public static AwtImage create(int w, int h) {
        return create(w, h, new int[w * h]);
    }

    /**
     * Loads an image from the specified input stream and creates a SwingImage object.
     *
     * @param in the input stream containing the image data
     * @return a new SwingImage object created from the loaded image
     * @throws IOException if an I/O error occurs while reading the image
     */
    public static AwtImage load(InputStream in) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            if (!iter.hasNext()) {
                throw new IOException("no matching ImageReader found");
            }

            ImageReader reader = iter.next();
            BufferedImage image = reader.read(0);
            AwtImage awtImage = create(image.getWidth(), image.getHeight());
            awtImage.getGraphics().drawImage(image, 0, 0, null);
            return awtImage;
        }
    }

    private AwtImage(int[] data, DirectColorModel colorModel, WritableRaster raster) {
        super(colorModel, raster, true, null);
        this.data = data;
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
    public int[] getArgb() {
        return data;
    }

    /**
     * Creates and returns a new {@link ImageBuffer} instance using the pixel data, width,
     * and height of this {@code FxBufferedImage}. This buffer can be used to manipulate the
     * image data.
     *
     * @return a new {@link ImageBuffer} containing the image data, width, and height
     */
    @Override
    public ImageBuffer getBuffer() {
        return new ImageBuffer(getArgb(), width(), height());
    }
}

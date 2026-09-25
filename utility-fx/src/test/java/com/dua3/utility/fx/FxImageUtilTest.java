package com.dua3.utility.fx;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.Payload;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class FxImageUtilTest extends FxTestBase {

    @Test
    void testFxImageUtilAndProvider() {
        FxImageUtil instance = FxImageUtil.getInstance();
        assertNotNull(instance);

        FxImageUtilProvider provider = new FxImageUtilProvider();
        assertEquals(instance, provider.get());
    }

    @Test
    void testCreateAndConvertImage() throws Throwable {
        runOnFxThreadAndWait(() -> {
            FxImageUtil util = FxImageUtil.getInstance();

            FxMutableImage emptyImg = util.createImage(10, 20);
            assertEquals(10, emptyImg.width());
            assertEquals(20, emptyImg.height());

            int[] pixels = new int[4];
            pixels[0] = 0xFF112233;
            pixels[1] = 0xFF445566;
            pixels[2] = 0xFF778899;
            pixels[3] = 0xFFAABBCC;
            FxMutableImage pixelImg = util.createImage(2, 2, pixels);
            assertEquals(2, pixelImg.width());
            assertEquals(2, pixelImg.height());
            assertArrayEquals(pixels, pixelImg.getArgb());

            // toImage with FxMutableImage returns same instance
            assertSame(pixelImg, util.toImage(pixelImg));

            // toImage with custom Image
            Image customImage = new Image() {
                @Override
                public int width() {
                    return 2;
                }

                @Override
                public int height() {
                    return 2;
                }

                @Override
                public int[] getArgb() {
                    return pixels;
                }
            };

            FxMutableImage converted = util.toImage(customImage);
            assertEquals(2, converted.width());
            assertEquals(2, converted.height());
            assertArrayEquals(pixels, converted.getArgb());
        });
    }

    @Test
    void testFxDataRetainingImage() throws Throwable {
        runOnFxThreadAndWait(() -> {
            WritableImage fxImg = new WritableImage(2, 2);
            byte[] sourceData = new byte[]{1, 2, 3, 4};

            FxDataRetainingImage img = new FxDataRetainingImage(fxImg, "image/jpeg", "jpg", sourceData);
            assertEquals(2, img.width());
            assertEquals(2, img.height());
            assertEquals("image/jpeg", img.mimeType());
            assertEquals("jpg", img.defaultExtension());
            assertArrayEquals(sourceData, (byte[]) img.source());
            assertNotNull(img.getArgb());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> {
                img.write(baos);
                assertArrayEquals(sourceData, baos.toByteArray());
            });

            // Compact constructor validation
            assertThrows(IllegalArgumentException.class, () -> new FxDataRetainingImage(fxImg, "image/jpeg", "jpg", "not-a-byte-array"));
        });
    }

    @Test
    void testLoadDataRetainingImage() throws Throwable {
        runOnFxThreadAndWait(() -> {
            assertDoesNotThrow(() -> {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    FxMutableImage src = FxImageUtil.getInstance().createImage(2, 2, new int[]{0xFF112233, 0xFF445566, 0xFF778899, 0xFFAABBCC});
                    src.write(baos);
                    byte[] pngBytes = baos.toByteArray();

                    FxImageUtil util = FxImageUtil.getInstance();
                    FxImage loaded = util.load(Payload.fromInputStream(new java.io.ByteArrayInputStream(pngBytes)), ImageUtil.LoadOption.DONT_RETAIN_DATA);
                    assertNotNull(loaded);
                    assertEquals(2, loaded.width());
                    assertEquals(2, loaded.height());

                    FxImage loadedRetain = util.load(Payload.fromInputStream(new java.io.ByteArrayInputStream(pngBytes)), ImageUtil.LoadOption.RETAIN_DATA);
                    assertInstanceOf(FxDataRetainingImage.class, loadedRetain);
                    assertEquals(2, loadedRetain.width());
                    assertEquals(2, loadedRetain.height());
                }
            });
        });
    }

    @Test
    void testI18NInstance() {
        assertNotNull(I18NInstance.get());
    }
}

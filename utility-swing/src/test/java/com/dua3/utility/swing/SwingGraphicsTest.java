package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.data.Image;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.ui.AbstractGraphicsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * Tests for the SwingGraphics class.
 * <p>
 * These tests run in headless mode and use software rendering to draw into a BufferedImage.
 * The generated image is compared to a reference image stored as a test resource.
 */
class SwingGraphicsTest extends AbstractGraphicsTest {

    private static final int IMAGE_WIDTH = 1000;
    private static final int IMAGE_HEIGHT = 800;
    private static final Path REFERENCE_IMAGE_PATH = Objects.requireNonNull(
            Paths.get(TextUtil.transform(
                    "src/test/resources/com/dua3/utility/swing/reference_image-${OS}.png",
                    Map.of("OS", Platform.currentPlatform().name())
            ))
    );

    private BufferedImage image;
    private Graphics2D g2d;

    @Override
    protected Path getReferenceImagePath() {
        return REFERENCE_IMAGE_PATH;
    }

    @Override
    protected FontUtil<?> getFontUtil() {
        return AwtFontUtil.getInstance();
    }

    @Override
    protected BufferedImage getRenderedImage() {
        return image;
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        // Ensure we're in headless mode
        System.setProperty("java.awt.headless", "true");

        // Create a BufferedImage with software rendering
        image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();

        // Enable high quality rendering
        SwingUtil.setRenderingQualityHigh(g2d);

        // Create the SwingGraphics instance
        Rectangle bounds = new Rectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        setGraphics(new SwingGraphics(g2d, bounds));

        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @Override
    protected Image convertImage(BufferedImage bi) {
        return AwtImageUtil.getInstance().convert(bi);
    }

}

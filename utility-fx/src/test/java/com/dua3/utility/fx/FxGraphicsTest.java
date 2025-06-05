package com.dua3.utility.fx;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.data.Image;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.ui.AbstractGraphicsTest;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the FxGraphics class.
 * <p>
 * These tests run in headless mode and use software rendering to draw into a WritableImage.
 * The generated image is compared to a reference image stored as a test resource.
 */
class FxGraphicsTest extends AbstractGraphicsTest {

    private static boolean platformInitialized = false;
    private static final Object lock = new Object();

    /**
     * Initialize the JavaFX platform if it's not already initialized.
     * This method is synchronized to prevent multiple concurrent initializations.
     */
    @BeforeAll
    static void initializePlatform() {
        synchronized (lock) {
            if (!platformInitialized) {
                try {
                    Platform.startup(() -> {
                        System.out.println("JavaFX Platform initialized");
                    });
                    platformInitialized = true;
                } catch (IllegalStateException e) {
                    // Platform already running, which is fine
                    System.out.println("JavaFX Platform was already running");
                    platformInitialized = true;
                }
            }
        }
    }
    private static final Logger LOG = LogManager.getLogger(FxGraphicsTest.class);

    private static final Path REFERENCE_IMAGE_PATH = Objects.requireNonNull(
            Paths.get(TextUtil.transform(
                    "src/test/resources/com/dua3/utility/fx/reference_image-${OS}.png",
                    Map.of("OS", com.dua3.utility.lang.Platform.currentPlatform().name())
            ))
    );

    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage writableImage;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        // JavaFX operations must be performed on the JavaFX Application Thread
        CountDownLatch latch = new CountDownLatch(1);

        PlatformHelper.runLater(() -> {
            try {
                // Create a Canvas with software rendering
                canvas = new Canvas(IMAGE_WIDTH, IMAGE_HEIGHT);
                gc = canvas.getGraphicsContext2D();

                // Create the FxGraphics instance
                setGraphics(new FxGraphics(gc, IMAGE_WIDTH, IMAGE_HEIGHT));

                LangUtil.uncheckedRunnable(super::setUp).run();
            } finally {
                latch.countDown();
            }
        });

        // Wait for JavaFX initialization to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX initialization timed out");
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Override
    protected void logInfo(String message) {
        System.out.println(message);
        LOG.info("{}", message);
    }

    @Override
    protected void logWarning(String message) {
        System.err.println(message);
        LOG.warn("{}", message);
    }

    @Override
    protected Path getReferenceImagePath() {
        return REFERENCE_IMAGE_PATH;
    }

    @Override
    protected FontUtil<?> getFontUtil() {
        return FxFontUtil.getInstance();
    }

    @Override
    protected BufferedImage getRenderedImage() {
        CountDownLatch latch = new CountDownLatch(1);

        PlatformHelper.runLater(() -> {
            try {
                // Capture the rendered image
                writableImage = new WritableImage(IMAGE_WIDTH, IMAGE_HEIGHT);
                canvas.snapshot(null, writableImage);
            } finally {
                latch.countDown();
            }
        });

        try {
            // Wait for rendering to complete
            assertTrue(latch.await(30, TimeUnit.SECONDS), "Rendering timed out");

            // Convert WritableImage to BufferedImage for comparison
            return AwtImageUtil.getInstance().convert(FxImageUtil.getInstance().convert(writableImage));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rendering to complete", e);
        }
    }

    @Override
    protected Image convertImage(BufferedImage bi) {
        return AwtImageUtil.getInstance().convert(bi);
    }
}

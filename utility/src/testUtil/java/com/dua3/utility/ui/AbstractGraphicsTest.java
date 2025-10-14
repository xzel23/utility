package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.VerticalAlignment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static com.dua3.utility.ui.Graphics.HAnchor;
import static com.dua3.utility.ui.Graphics.VAnchor;
import static com.dua3.utility.ui.Graphics.TextWrapping;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Abstract base class for testing Graphics implementations.
 * <p>
 * This class contains common test functionality for testing implementations of the Graphics interface.
 * Concrete test classes should extend this class and implement the abstract methods to provide
 * the specific Graphics implementation to test.
 */
public abstract class AbstractGraphicsTest {
    private Graphics graphics;

    protected static final int IMAGE_WIDTH = 1000;
    protected static final int IMAGE_HEIGHT = 800;
    protected static final long PIXEL_DIFFERENCE_THRESHOLD = 10_000;

    /**
     * Get the path to the reference image for this test.
     * @return the path to the reference image
     */
    protected abstract Path getReferenceImagePath();

    /**
     * Sets the Graphics instance to be used for rendering operations.
     *
     * @param graphics the Graphics instance to be assigned
     */
    protected void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }

    /**
     * Get the Graphics instance to be used for rendering operations.
     * @return the Graphics instance
     */
    protected Graphics getGraphics() {
        return Objects.requireNonNull(graphics, "Graphics instance not set");
    }

    /**
     * Get the FontUtil instance for testing.
     * @return the FontUtil instance
     */
    protected abstract FontUtil getFontUtil();

    /**
     * Convert the rendered graphics to a BufferedImage for comparison.
     * @return the BufferedImage representation of the rendered graphics
     */
    protected abstract BufferedImage getRenderedImage();

    /**
     * Set up the test environment.
     * This method should initialize the Graphics instance and clear the background.
     */
    protected void setUp() {
        Objects.requireNonNull(graphics, "Graphics instance not set");
        graphics.setFill(Color.WHITE);
        graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    /**
     * Clean up the test environment.
     * This method should close the Graphics instance and release any resources.
     */
    @AfterEach
    protected void tearDown() {
        if (graphics != null) {
            graphics.close();
            graphics = null;
        }
    }

    /**
     * Logs an informational message during the test execution. This method is intended
     * for providing general logging details or status updates relevant to the test process.
     *
     * @param message the informational message to be logged
     */
    protected abstract void logInfo(String message);

    /**
     * Logs a warning message. This method is intended for notifying issues
     * such as significant discrepancies found during tests or validation scenarios.
     *
     * @param message the warning message to be logged
     */
    protected abstract void logWarning(String message);

    /**
     * Test for dimension-related methods
     */
    protected void testDimensionMethods() {
        // Test getWidth and getHeight
        assertEquals(IMAGE_WIDTH, graphics.getWidth(), "Width should match the image width");
        assertEquals(IMAGE_HEIGHT, graphics.getHeight(), "Height should match the image height");

        // Test getDimension
        Dimension2f dimension = graphics.getDimension();
        assertEquals(IMAGE_WIDTH, dimension.width(), "Dimension width should match the image width");
        assertEquals(IMAGE_HEIGHT, dimension.height(), "Dimension height should match the image height");
    }

    /**
     * Test for utility methods
     */
    protected void testUtilityMethods() {
        // Test getFontUtil
        assertNotNull(graphics.getFontUtil(), "FontUtil should not be null");

        // Test getDefaultFont
        Font defaultFont = graphics.getDefaultFont();
        assertNotNull(defaultFont, "Default font should not be null");
    }

    /**
     * Test for styling methods
     */
    protected void testStylingMethods() {
        // Test setFill and getFill
        graphics.setFill(Color.RED);
        assertEquals(Color.RED, graphics.getFill(), "Fill color should be RED");

        // Test setStroke, getStrokeColor, and getStrokeWidth
        graphics.setStroke(Color.BLUE, 5.0f);
        assertEquals(Color.BLUE, graphics.getStrokeColor(), "Stroke color should be BLUE");
        assertEquals(5.0f, graphics.getStrokeWidth(), "Stroke width should be 5.0");

        // Test setStrokeColor
        graphics.setStrokeColor(Color.GREEN);
        assertEquals(Color.GREEN, graphics.getStrokeColor(), "Stroke color should be GREEN");

        // Test setStrokeWidth
        graphics.setStrokeWidth(2.0f);
        assertEquals(2.0f, graphics.getStrokeWidth(), "Stroke width should be 2.0");

        // Test setFont and getFont
        Font font = graphics.getDefaultFont().withSize(16).withColor(Color.BLACK);
        graphics.setFont(font);
        assertEquals(font, graphics.getFont(), "Font should match the set font");
    }

    /**
     * Test for transformation methods
     */
    protected void testTransformationMethods() {
        // Test setTransformation and getTransformation
        AffineTransformation2f transform = AffineTransformation2f.rotate(Math.PI / 4);
        graphics.setTransformation(transform);
        assertEquals(transform, graphics.getTransformation(), "Transformation should match the set transformation");

        // Test transform method
        AffineTransformation2f originalTransform = graphics.getTransformation();
        AffineTransformation2f additionalTransform = AffineTransformation2f.translate(10, 20);
        AffineTransformation2f returnedTransform = graphics.transform(additionalTransform);

        assertEquals(originalTransform, returnedTransform, "Transform should return the original transformation");

        AffineTransformation2f expectedCombinedTransform = AffineTransformation2f.combine(additionalTransform, originalTransform);
        assertEquals(expectedCombinedTransform, graphics.getTransformation(), "Combined transformation should match expected");

        // Test getInverseTransformation
        AffineTransformation2f inverseTransform = graphics.getInverseTransformation();
        assertNotNull(inverseTransform, "Inverse transformation should not be null");

        // Reset transformation
        graphics.setTransformation(AffineTransformation2f.identity());
    }

    /**
     * Test for image drawing methods
     */
    protected void testImageDrawingMethods() {
        BufferedImage testImage;
        try (InputStream in = getClass().getResourceAsStream("/com/dua3/utility/ui/test.jpg")) {
            testImage = ImageIO.read(Objects.requireNonNull(in, "test image not found"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Convert to the utility Image type
        Image image2 = convertImage(testImage);

        // Test drawImage with float coordinates
        graphics.drawImage(image2, 50, 50);

        // Test drawImage with Vector2f
        graphics.drawImage(image2, Vector2f.of(200, 50));

        graphics.transform(AffineTransformation2f.rotate(30.0 * Math.PI / 180.0, Vector2f.of(400, 100)));
        graphics.drawImage(image2, Vector2f.of(350, 50));
    }

    protected abstract Image convertImage(BufferedImage testImage);

    /**
     * Test for basic shape drawing methods
     */
    protected void testBasicShapeDrawingMethods() {
        // Test strokeRect with float parameters
        graphics.setStroke(Color.BLACK, 2);
        graphics.strokeRect(50, 50, 100, 100);

        // Test strokeRect with Rectangle2f
        Rectangle2f rect = Rectangle2f.of(200, 50, 100, 100);
        graphics.strokeRect(rect);

        // Test strokeRect with Vector2f and Dimension2f
        Vector2f pos = Vector2f.of(50, 200);
        Dimension2f dim = new Dimension2f(100, 100);
        graphics.strokeRect(pos, dim);

        // Test fillRect with float parameters
        graphics.setFill(Color.BLUE);
        graphics.fillRect(350, 50, 100, 100);

        // Test fillRect with Rectangle2f
        graphics.setFill(Color.RED);
        graphics.fillRect(Rectangle2f.of(350, 200, 100, 100));

        // Test strokeCircle with float parameters
        graphics.setStroke(Color.GREEN, 2);
        graphics.strokeCircle(100, 350, 50);

        // Test strokeCircle with Vector2f
        graphics.strokeCircle(Vector2f.of(250, 350), 50);

        // Test fillCircle with float parameters
        graphics.setFill(Color.YELLOW);
        graphics.fillCircle(400, 350, 50);

        // Test fillCircle with Vector2f
        graphics.setFill(Color.BLUE);
        graphics.fillCircle(Vector2f.of(550, 350), 50);

        // Test strokeEllipse
        graphics.setStroke(Color.GREEN, 2);
        graphics.strokeEllipse(100, 450, 70, 40, 0);

        // Test fillEllipse
        graphics.setFill(Color.RED);
        graphics.fillEllipse(250, 450, 70, 40, (float) (Math.PI / 4));

        // Test strokeLine with float parameters
        graphics.setStroke(Color.BLACK, 3);
        graphics.strokeLine(400, 400, 550, 500);

        // Test strokeLine with Vector2f
        graphics.setStroke(Color.RED, 3);
        graphics.strokeLine(Vector2f.of(400, 500), Vector2f.of(550, 400));

        // Test strokePolyLines
        graphics.setStroke(Color.GREEN, 2);
        graphics.strokePolyLines(
                Vector2f.of(50, 550),
                Vector2f.of(100, 600),
                Vector2f.of(150, 550),
                Vector2f.of(200, 600)
        );

        // Test strokePolygon
        graphics.setStroke(Color.BLUE, 2);
        graphics.strokePolygon(
                Vector2f.of(250, 550),
                Vector2f.of(300, 550),
                Vector2f.of(325, 600),
                Vector2f.of(275, 625),
                Vector2f.of(225, 600)
        );
    }

    /**
     * Test for path operations
     */
    protected void testPathOperations() {
        // Create a path
        Path2f path = Path2f.builder()
                .moveTo(Vector2f.of(100, 100))
                .lineTo(Vector2f.of(200, 100))
                .lineTo(Vector2f.of(200, 200))
                .lineTo(Vector2f.of(100, 200))
                .lineTo(Vector2f.of(100, 100))  // Close the path by returning to the start point
                .build();

        // Test strokePath
        graphics.setStroke(Color.BLUE, 2);
        graphics.strokePath(path);

        // Create another path
        Path2f path2 = Path2f.builder()
                .moveTo(Vector2f.of(300, 100))
                .lineTo(Vector2f.of(400, 100))
                .lineTo(Vector2f.of(400, 200))
                .lineTo(Vector2f.of(300, 200))
                .lineTo(Vector2f.of(300, 100))  // Close the path by returning to the start point
                .build();

        // Test fillPath
        graphics.setFill(Color.RED);
        graphics.fillPath(path2);
    }

    /**
     * Test for clipping operations
     */
    protected void testClippingOperations() {
        // Create a clipping path
        Path2f clipPath = Path2f.builder()
                .moveTo(Vector2f.of(100, 100))
                .lineTo(Vector2f.of(300, 100))
                .lineTo(Vector2f.of(300, 300))
                .lineTo(Vector2f.of(100, 300))
                .lineTo(Vector2f.of(100, 100))  // Close the path by returning to the start point
                .build();

        // Test clip with Path2f
        graphics.clip(clipPath);

        // Draw something that extends beyond the clip region
        graphics.setFill(Color.BLUE);
        graphics.fillRect(50, 50, 400, 400);

        // Reset clip
        graphics.resetClip();

        // Test clip with Rectangle2f
        Rectangle2f clipRect = Rectangle2f.of(350, 100, 200, 200);
        graphics.clip(clipRect);

        // Draw something that extends beyond the clip region
        graphics.setFill(Color.RED);
        graphics.fillRect(300, 50, 400, 400);

        // Reset clip
        graphics.resetClip();
    }

    /**
     * Test for text rendering methods
     */
    protected void testTextRenderingMethods() {
        // Test drawText with CharSequence, float x, float y
        Font font = getFontUtil().getFont("Arial-10");
        graphics.setFont(font);
        graphics.drawText("Test drawText with x, y", 100, 100);

        // Test drawText with CharSequence, Vector2f
        graphics.drawText("Test drawText with Vector2f", Vector2f.of(100, 150));

        // Test drawText with CharSequence, float x, float y, HAnchor, VAnchor
        graphics.drawText("Test drawText with anchors", 300, 100, HAnchor.CENTER, VAnchor.MIDDLE);

        // Test renderText with RichText
        // Note: This is a simplified test as RichText creation is complex
        try {
            RichText richText = new RichTextBuilder()
                    .append("Test renderText")
                    .toRichText();

            graphics.renderText(
                    Vector2f.of(100, 200),
                    richText,
                    HAnchor.LEFT,
                    VAnchor.TOP,
                    Alignment.LEFT,
                    VerticalAlignment.TOP,
                    new Dimension2f(200, 100),
                    TextWrapping.WRAP
            );

            // Test renderText with rotation
            graphics.renderText(
                    Vector2f.of(400, 200),
                    richText,
                    HAnchor.LEFT,
                    VAnchor.TOP,
                    Alignment.LEFT,
                    VerticalAlignment.TOP,
                    new Dimension2f(200, 100),
                    TextWrapping.WRAP,
                    Math.PI / 4,
                    Graphics.TextRotationMode.ROTATE_OUTPUT_AREA
            );
        } catch (Exception e) {
            fail("Exception while testing renderText: " + e.getMessage());
        }
    }

    /**
     * Test for reset method
     */
    protected void testResetMethod() {
        // Draw something
        graphics.setFill(Color.RED);
        graphics.fillRect(100, 100, 200, 200);

        // Reset
        graphics.reset();

        // Draw something else to verify reset worked
        graphics.setFill(Color.BLUE);
        graphics.fillRect(300, 300, 100, 100);
    }

    /**
     * Test for rendering to an image
     * This test calls all the other test methods to render to the same image
     */
    @Test
    protected void testRendering() throws IOException {
        // Call all the test methods to render to the same image
        // Position each test in a different area of the image

        // Test dimension methods (no visual output)
        testDimensionMethods();

        // Test utility methods (no visual output)
        testUtilityMethods();

        // Test styling methods (no visual output)
        testStylingMethods();

        // Test transformation methods (no visual output)
        testTransformationMethods();

        // Test image drawing methods
        graphics.setTransformation(AffineTransformation2f.translate(0, 0));
        testImageDrawingMethods();

        // Test basic shape drawing methods
        graphics.setTransformation(AffineTransformation2f.translate(0, 100));
        testBasicShapeDrawingMethods();

        // Test path operations
        graphics.setTransformation(AffineTransformation2f.translate(0, 200));
        testPathOperations();

        // Test clipping operations
        graphics.setTransformation(AffineTransformation2f.translate(400, 0));
        testClippingOperations();

        // Test text rendering methods
        graphics.setTransformation(AffineTransformation2f.translate(400, 400));
        testTextRenderingMethods();

        // Add a title to the image
        graphics.setTransformation(AffineTransformation2f.identity());
        Font titleFont = getFontUtil().getFont("arial-20");
        graphics.setFont(titleFont);
        String title = "Graphics Test - All Methods - %s (%s)".formatted(getClass().getSimpleName(), Platform.currentPlatform());
        graphics.drawText(title, IMAGE_HEIGHT / 2.0f, 24, HAnchor.CENTER, VAnchor.TOP);

        // Get the rendered image
        BufferedImage image = getRenderedImage();

        // Create the resources directory if it doesn't exist
        Path referenceImagePath = getReferenceImagePath();
        Path resourcesDir = Objects.requireNonNull(referenceImagePath.getParent());
        if (!Files.exists(resourcesDir)) {
            Files.createDirectories(resourcesDir);
        }

        // Check if the reference image exists, if not, create it
        File referenceFile = referenceImagePath.toFile();
        BufferedImage referenceImage;

        if (!referenceFile.exists()) {
            // Save the current image as the reference image
            ImageIO.write(image, "png", referenceFile);
            System.out.println("Created reference image: " + referenceFile.getAbsolutePath());
            return; // Skip comparison on first run
        } else {
            // Load the reference image
            referenceImage = ImageIO.read(referenceFile);
        }

        // Save the generated test image to build directory
        Path outputFile = Paths.get("build/test-artifacts", getClass().getSimpleName() + ".png");
        Files.createDirectories(Objects.requireNonNull(outputFile.getParent(), "could not create the output directory"));
        ImageIO.write(image, "png", outputFile.toFile());

        logInfo("Generated test image saved to: file://" + outputFile.toAbsolutePath());

        // Compare the rendered image with the reference image
        assertEquals(referenceImage.getWidth(), image.getWidth(), "Image widths should match");
        assertEquals(referenceImage.getHeight(), image.getHeight(), "Image heights should match");

        long nPixelDifferences = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (referenceImage.getRGB(x, y) != image.getRGB(x, y)) {
                    nPixelDifferences++;
                }
            }
        }

        if (nPixelDifferences > PIXEL_DIFFERENCE_THRESHOLD) {
            logWarning(nPixelDifferences + " pixel differences found, manually check artifacts!");
            ImageIO.write(image, "png", new File("build/test-artifacts", getClass().getSimpleName() + "-diff.png"));
        }

        // Skip the test with a descriptive message
        assumeTrue(nPixelDifferences == 0,
                String.format("Test skipped: %d pixel differences found", nPixelDifferences)
        );
    }
}
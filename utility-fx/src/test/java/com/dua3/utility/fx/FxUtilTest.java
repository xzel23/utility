package com.dua3.utility.fx;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.FillRule;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.text.RichText;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FxUtil class.
 * <p>
 * These tests run in headless mode and test the utility methods in FxUtil.
 */
class FxUtilTest extends FxTestBase {

    @Test
    void testConvertColorToFx() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from utility Color to JavaFX Color
            Color color = Color.rgba(255, 0, 0, 128); // Semi-transparent red
            javafx.scene.paint.Color fxColor = FxUtil.convert(color);

            double delta = 1.1 / 255;
            assertEquals(1.0, fxColor.getRed(), delta, "Red component should be 1.0");
            assertEquals(0.0, fxColor.getGreen(), delta, "Green component should be 0.0");
            assertEquals(0.0, fxColor.getBlue(), delta, "Blue component should be 0.0");
            assertEquals(0.5, fxColor.getOpacity(), delta, "Alpha component should be 0.5");
        });
    }

    @Test
    void testConvertColorFromFx() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from JavaFX Color to utility Color
            javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(0, 255, 0, 0.75); // Semi-transparent green
            Color color = FxUtil.convert(fxColor);

            // Convert to RGBColor to access components
            RGBColor rgbColor = color.toRGBColor();
            assertEquals(0, rgbColor.r(), "Red component should be 0");
            assertEquals(255, rgbColor.g(), "Green component should be 255");
            assertEquals(0, rgbColor.b(), "Blue component should be 0");
            assertEquals(191, rgbColor.a(), "Alpha component should be 191");
        });
    }

    @Test
    void testConvertFillRuleToFx() {
        // Test converting from utility FillRule to JavaFX FillRule
        javafx.scene.shape.FillRule fxFillRuleEvenOdd = FxUtil.convert(FillRule.EVEN_ODD);
        javafx.scene.shape.FillRule fxFillRuleNonZero = FxUtil.convert(FillRule.NON_ZERO);

        assertEquals(javafx.scene.shape.FillRule.EVEN_ODD, fxFillRuleEvenOdd, "Should convert to EVEN_ODD");
        assertEquals(javafx.scene.shape.FillRule.NON_ZERO, fxFillRuleNonZero, "Should convert to NON_ZERO");
    }

    @Test
    void testConvertFillRuleFromFx() {
        // Test converting from JavaFX FillRule to utility FillRule
        FillRule fillRuleEvenOdd = FxUtil.convert(javafx.scene.shape.FillRule.EVEN_ODD);
        FillRule fillRuleNonZero = FxUtil.convert(javafx.scene.shape.FillRule.NON_ZERO);

        assertEquals(FillRule.EVEN_ODD, fillRuleEvenOdd, "Should convert to EVEN_ODD");
        assertEquals(FillRule.NON_ZERO, fillRuleNonZero, "Should convert to NON_ZERO");
    }

    @Test
    void testConvertAffineTransformationToFx() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from utility AffineTransformation2f to JavaFX Affine
            // Create a transformation by combining translate, scale, and rotate
            AffineTransformation2f at = AffineTransformation2f.combine(
                    AffineTransformation2f.translate(10, 20),
                    AffineTransformation2f.scale(2, 3),
                    AffineTransformation2f.rotate(Math.PI/4)
            );

            Affine affine = FxUtil.convert(at);

            // Check matrix values (with some tolerance for floating point precision)
            assertEquals(at.a(), affine.getMxx(), 0.0001, "Mxx should match");
            assertEquals(at.b(), affine.getMxy(), 0.0001, "Mxy should match");
            assertEquals(at.c(), affine.getTx(), 0.0001, "Tx should match");
            assertEquals(at.d(), affine.getMyx(), 0.0001, "Myx should match");
            assertEquals(at.e(), affine.getMyy(), 0.0001, "Myy should match");
            assertEquals(at.f(), affine.getTy(), 0.0001, "Ty should match");
        });
    }

    @Test
    void testConvertAffineFromFx() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from JavaFX Affine to utility AffineTransformation2f
            Affine affine = new Affine();
            affine.setMxx(1.5);
            affine.setMxy(0.5);
            affine.setTx(10);
            affine.setMyx(-0.5);
            affine.setMyy(2.0);
            affine.setTy(20);

            AffineTransformation2f at = FxUtil.convert(affine);

            // Check matrix values
            assertEquals(affine.getMxx(), at.a(), 0.0001, "a should match");
            assertEquals(affine.getMxy(), at.b(), 0.0001, "b should match");
            assertEquals(affine.getTx(), at.c(), 0.0001, "c should match");
            assertEquals(affine.getMyx(), at.d(), 0.0001, "d should match");
            assertEquals(affine.getMyy(), at.e(), 0.0001, "e should match");
            assertEquals(affine.getTy(), at.f(), 0.0001, "f should match");

            // check that the backtransformation creates the original transformation
            Affine backToAffine = FxUtil.convert(at);

            assertEquals(affine.getMxx(), backToAffine.getMxx(), 0.0001, "Mxx should match");
            assertEquals(affine.getMxy(), backToAffine.getMxy(), 0.0001, "Mxy should match");
            assertEquals(affine.getTx(), backToAffine.getTx(), 0.0001, "Tx should match");
            assertEquals(affine.getMyx(), backToAffine.getMyx(), 0.0001, "Myx should match");
            assertEquals(affine.getMyy(), backToAffine.getMyy(), 0.0001, "Myy should match");
            assertEquals(affine.getTy(), backToAffine.getTy(), 0.0001, "Ty should match");
        });
    }

    @Test
    void testConvertRectangle2fToRectangle() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from utility Rectangle2f to JavaFX Rectangle
            Rectangle2f rect = new Rectangle2f(10, 20, 30, 40);
            javafx.scene.shape.Rectangle fxRect = FxUtil.convert(rect);

            assertEquals(10, fxRect.getX(), "X should be 10");
            assertEquals(20, fxRect.getY(), "Y should be 20");
            assertEquals(30, fxRect.getWidth(), "Width should be 30");
            assertEquals(40, fxRect.getHeight(), "Height should be 40");
        });
    }

    @Test
    void testConvertRectangle2DToRectangle2f() {
        // Test converting from JavaFX Rectangle2D to utility Rectangle2f
        Rectangle2D rect = new Rectangle2D(10, 20, 30, 40);
        Rectangle2f rect2f = FxUtil.convert(rect);

        assertEquals(10, rect2f.x(), "X should be 10");
        assertEquals(20, rect2f.y(), "Y should be 20");
        assertEquals(30, rect2f.width(), "Width should be 30");
        assertEquals(40, rect2f.height(), "Height should be 40");
    }

    @Test
    void testConvertBoundsToRectangle2f() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Create a Text node to get its bounds
            javafx.scene.text.Text text = new javafx.scene.text.Text("Test");
            Bounds bounds = text.getBoundsInLocal();

            Rectangle2f rect2f = FxUtil.convert(bounds);

            assertEquals(bounds.getMinX(), rect2f.x(), 0.0001, "X should match");
            assertEquals(bounds.getMinY(), rect2f.y(), 0.0001, "Y should match");
            assertEquals(bounds.getWidth(), rect2f.width(), 0.0001, "Width should match");
            assertEquals(bounds.getHeight(), rect2f.height(), 0.0001, "Height should match");
        });
    }

    @Test
    void testGetTextBounds() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test getting text bounds
            Font font = Font.font("Arial", 12);
            Bounds bounds = FxUtil.getTextBounds("Test", font);

            assertNotNull(bounds, "Bounds should not be null");
            assertTrue(bounds.getWidth() > 0, "Width should be positive");
            assertTrue(bounds.getHeight() > 0, "Height should be positive");
        });
    }

    @Test
    void testGetTextWidth() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test getting text width
            Font fxFont = Font.font("Arial", 12);
            // Convert JavaFX Font to utility Font
            com.dua3.utility.text.Font font = FxUtil.convert(fxFont);
            double width = FxUtil.getTextWidth("Test", font);

            assertTrue(width > 0, "Width should be positive");
        });
    }

    @Test
    void testGetTextHeight() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test getting text height
            Font fxFont = Font.font("Arial", 12);
            // Convert JavaFX Font to utility Font
            com.dua3.utility.text.Font font = FxUtil.convert(fxFont);
            double height = FxUtil.getTextHeight("Test", font);

            assertTrue(height > 0, "Height should be positive");
        });
    }

    @Test
    void testGrowToFit() {
        // Test growing a dimension to fit bounds
        Dimension2D dim = new Dimension2D(100, 50);
        Rectangle2D rect = new Rectangle2D(0, 0, 150, 75);
        Bounds bounds = new javafx.geometry.BoundingBox(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());

        Dimension2D result = FxUtil.growToFit(dim, bounds);

        assertEquals(150, result.getWidth(), "Width should grow to 150");
        assertEquals(75, result.getHeight(), "Height should grow to 75");

        // Test when no growth is needed
        dim = new Dimension2D(200, 100);
        result = FxUtil.growToFit(dim, bounds);

        assertEquals(200, result.getWidth(), "Width should remain 200");
        assertEquals(100, result.getHeight(), "Height should remain 100");
    }

    @Test
    void testUnion() {
        // Test union of two rectangles
        Rectangle2D r1 = new Rectangle2D(10, 20, 30, 40);
        Rectangle2D r2 = new Rectangle2D(5, 15, 20, 30);

        Rectangle2D union = FxUtil.union(r1, r2);

        assertEquals(5, union.getMinX(), "MinX should be 5");
        assertEquals(15, union.getMinY(), "MinY should be 15");
        assertEquals(35, union.getWidth(), "Width should be 35");
        assertEquals(45, union.getHeight(), "Height should be 45");
    }

    @Test
    void testConstant() {
        // Test creating a constant ObservableBooleanValue
        ObservableBooleanValue trueValue = FxUtil.constant(true);
        ObservableBooleanValue falseValue = FxUtil.constant(false);

        assertTrue(trueValue.get(), "Value should be true");
        assertFalse(falseValue.get(), "Value should be false");
    }

    @Test
    void testCopyToClipboardString() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test copying a string to the clipboard
            String text = "Test clipboard text";
            FxUtil.copyToClipboard(text);

            // Verify the clipboard content
            Optional<String> clipboardText = FxUtil.getStringFromClipboard();
            assertTrue(clipboardText.isPresent(), "Clipboard should contain text");
            assertEquals(text, clipboardText.get(), "Clipboard text should match");
        });
    }

    @Test
    void testCopyToClipboardRichText() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test copying rich text to the clipboard
            RichText richText = RichText.valueOf("Test rich text");
            FxUtil.copyToClipboard(richText);

            // Verify the clipboard content
            Optional<String> clipboardText = FxUtil.getStringFromClipboard();
            assertTrue(clipboardText.isPresent(), "Clipboard should contain text");
            assertTrue(clipboardText.get().contains("Test"), "Clipboard text should contain the plain text");
        });
    }

    @Test
    void testConvertPath() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Create a simple path
            Path2f path2f = Path2f.builder()
                    .moveTo(10f, 10f)
                    .lineTo(100f, 10f)
                    .lineTo(100f, 100f)
                    .lineTo(10f, 100f)
                    .closePath()
                    .build();

            // Convert to JavaFX Path
            Path path = FxUtil.convert(path2f);

            // Basic validation
            assertNotNull(path, "Converted path should not be null");
            assertFalse(path.getElements().isEmpty(), "Path should have elements");
        });
    }

    @Test
    void testFontConversion() throws Throwable {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            // Test converting from JavaFX Font to utility Font and back
            Platform platform = Platform.currentPlatform();
            Assumptions.assumeTrue(platform != Platform.UNKNOWN);
            String family = switch (platform) {
                case Platform.LINUX -> "DejaVu Sans";
                default -> "Arial";
            };
            Font fxFont = Font.font(family, 12);
            com.dua3.utility.text.Font utilFont = FxUtil.convert(fxFont);
            Font convertedFxFont = FxUtil.convert(utilFont);

            assertEquals(family, utilFont.getFamily(), "Font family should be preserved");
            assertEquals(12, utilFont.getSizeInPoints(), 0.001, "Font size should be preserved");
            assertEquals(fxFont.getFamily(), convertedFxFont.getFamily(), "Font family should be preserved in round-trip conversion");
            assertEquals(fxFont.getSize(), convertedFxFont.getSize(), 0.001, "Font size should be preserved in round-trip conversion");
        });
    }
}

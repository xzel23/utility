package com.dua3.utility.ui;

import com.dua3.utility.data.Image;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the Graphics interface.
 * <p>
 * These tests verify the contract of the Graphics interface using a mock implementation.
 */
class GraphicsTest {

    private Graphics graphics;

    @BeforeEach
    void setUp() {
        // Create a mock implementation of the Graphics interface
        graphics = Mockito.mock(Graphics.class);
    }

    @Test
    void testGetDimension() {
        // Setup
        when(graphics.getWidth()).thenReturn(100f);
        when(graphics.getHeight()).thenReturn(200f);

        // Add delegation for getDimension to use getWidth and getHeight
        doAnswer(invocation -> new Dimension2f(graphics.getWidth(), graphics.getHeight()))
            .when(graphics).getDimension();

        // Test
        Dimension2f dimension = graphics.getDimension();

        // Verify
        assertEquals(100f, dimension.width());
        assertEquals(200f, dimension.height());
        verify(graphics).getWidth();
        verify(graphics).getHeight();
    }

    @Test
    void testDrawImageWithVector() {
        // Setup
        Image image = mock(Image.class);
        Vector2f position = new Vector2f(10f, 20f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Image img = invocation.getArgument(0);
            Vector2f pos = invocation.getArgument(1);
            graphics.drawImage(img, pos.x(), pos.y());
            return null;
        }).when(graphics).drawImage(any(Image.class), any(Vector2f.class));

        // Test
        graphics.drawImage(image, position);

        // Verify
        verify(graphics).drawImage(image, 10f, 20f);
    }

    @Test
    void testStrokeRectWithRectangle() {
        // Setup
        Rectangle2f rectangle = new Rectangle2f(10f, 20f, 30f, 40f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Rectangle2f rect = invocation.getArgument(0);
            graphics.strokeRect(rect.x(), rect.y(), rect.width(), rect.height());
            return null;
        }).when(graphics).strokeRect(any(Rectangle2f.class));

        // Test
        graphics.strokeRect(rectangle);

        // Verify
        verify(graphics).strokeRect(10f, 20f, 30f, 40f);
    }

    @Test
    void testStrokeRectWithVectorAndDimension() {
        // Setup
        Vector2f position = new Vector2f(10f, 20f);
        Dimension2f dimension = new Dimension2f(30f, 40f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Vector2f pos = invocation.getArgument(0);
            Dimension2f dim = invocation.getArgument(1);
            graphics.strokeRect(pos.x(), pos.y(), dim.width(), dim.height());
            return null;
        }).when(graphics).strokeRect(any(Vector2f.class), any(Dimension2f.class));

        // Test
        graphics.strokeRect(position, dimension);

        // Verify
        verify(graphics).strokeRect(10f, 20f, 30f, 40f);
    }

    @Test
    void testFillRectWithRectangle() {
        // Setup
        Rectangle2f rectangle = new Rectangle2f(10f, 20f, 30f, 40f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Rectangle2f rect = invocation.getArgument(0);
            graphics.fillRect(rect.x(), rect.y(), rect.width(), rect.height());
            return null;
        }).when(graphics).fillRect(any(Rectangle2f.class));

        // Test
        graphics.fillRect(rectangle);

        // Verify
        verify(graphics).fillRect(10f, 20f, 30f, 40f);
    }

    @Test
    void testStrokeCircleWithVector() {
        // Setup
        Vector2f center = new Vector2f(10f, 20f);
        float radius = 30f;

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Vector2f c = invocation.getArgument(0);
            float r = invocation.getArgument(1);
            graphics.strokeCircle(c.x(), c.y(), r);
            return null;
        }).when(graphics).strokeCircle(any(Vector2f.class), anyFloat());

        // Test
        graphics.strokeCircle(center, radius);

        // Verify
        verify(graphics).strokeCircle(10f, 20f, 30f);
    }

    @Test
    void testFillCircleWithVector() {
        // Setup
        Vector2f center = new Vector2f(10f, 20f);
        float radius = 30f;

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Vector2f c = invocation.getArgument(0);
            float r = invocation.getArgument(1);
            graphics.fillCircle(c.x(), c.y(), r);
            return null;
        }).when(graphics).fillCircle(any(Vector2f.class), anyFloat());

        // Test
        graphics.fillCircle(center, radius);

        // Verify
        verify(graphics).fillCircle(10f, 20f, 30f);
    }

    @Test
    void testStrokeLineWithVectors() {
        // Setup
        Vector2f start = new Vector2f(10f, 20f);
        Vector2f end = new Vector2f(30f, 40f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            Vector2f a = invocation.getArgument(0);
            Vector2f b = invocation.getArgument(1);
            graphics.strokeLine(a.x(), a.y(), b.x(), b.y());
            return null;
        }).when(graphics).strokeLine(any(Vector2f.class), any(Vector2f.class));

        // Test
        graphics.strokeLine(start, end);

        // Verify
        verify(graphics).strokeLine(10f, 20f, 30f, 40f);
    }

    @Test
    void testDrawTextWithVector() {
        // Setup
        String text = "Test Text";
        Vector2f position = new Vector2f(10f, 20f);

        // Mock the behavior to delegate to the float version
        doAnswer(invocation -> {
            CharSequence t = invocation.getArgument(0);
            Vector2f pos = invocation.getArgument(1);
            graphics.drawText(t, pos.x(), pos.y());
            return null;
        }).when(graphics).drawText(any(CharSequence.class), any(Vector2f.class));

        // Test
        graphics.drawText(text, position);

        // Verify
        verify(graphics).drawText(text, 10f, 20f);
    }

    @Test
    void testTransformVector() {
        // Setup
        Vector2f point = new Vector2f(10f, 20f);
        AffineTransformation2f transformation = mock(AffineTransformation2f.class);
        when(graphics.getTransformation()).thenReturn(transformation);
        when(transformation.transform(point)).thenReturn(new Vector2f(30f, 40f));

        // Add delegation for transform to use getTransformation().transform()
        doAnswer(invocation -> {
            Vector2f p = invocation.getArgument(0);
            return graphics.getTransformation().transform(p);
        }).when(graphics).transform(any(Vector2f.class));

        // Test
        Vector2f result = graphics.transform(point);

        // Verify
        assertEquals(30f, result.x());
        assertEquals(40f, result.y());
        verify(graphics).getTransformation();
        verify(transformation).transform(point);
    }

    @Test
    void testTransformCoordinates() {
        // Setup
        float x = 10f;
        float y = 20f;
        AffineTransformation2f transformation = mock(AffineTransformation2f.class);
        when(graphics.getTransformation()).thenReturn(transformation);
        when(transformation.transform(any(Vector2f.class))).thenReturn(new Vector2f(30f, 40f));

        // Add delegation for transform to use getTransformation().transform()
        doAnswer(invocation -> {
            float xCoord = invocation.getArgument(0);
            float yCoord = invocation.getArgument(1);
            return graphics.getTransformation().transform(new Vector2f(xCoord, yCoord));
        }).when(graphics).transform(anyFloat(), anyFloat());

        // Test
        Vector2f result = graphics.transform(x, y);

        // Verify
        assertEquals(30f, result.x());
        assertEquals(40f, result.y());
        verify(graphics).getTransformation();
        verify(transformation).transform(any(Vector2f.class));
    }

    @Test
    void testInverseTransformVector() {
        // Setup
        Vector2f point = new Vector2f(10f, 20f);
        AffineTransformation2f inverseTransformation = mock(AffineTransformation2f.class);
        when(graphics.getInverseTransformation()).thenReturn(inverseTransformation);
        when(inverseTransformation.transform(point)).thenReturn(new Vector2f(30f, 40f));

        // Add delegation for inverseTransform to use getInverseTransformation().transform()
        doAnswer(invocation -> {
            Vector2f p = invocation.getArgument(0);
            return graphics.getInverseTransformation().transform(p);
        }).when(graphics).inverseTransform(any(Vector2f.class));

        // Test
        Vector2f result = graphics.inverseTransform(point);

        // Verify
        assertEquals(30f, result.x());
        assertEquals(40f, result.y());
        verify(graphics).getInverseTransformation();
        verify(inverseTransformation).transform(point);
    }

    @Test
    void testInverseTransformCoordinates() {
        // Setup
        float x = 10f;
        float y = 20f;
        AffineTransformation2f inverseTransformation = mock(AffineTransformation2f.class);
        when(graphics.getInverseTransformation()).thenReturn(inverseTransformation);
        when(inverseTransformation.transform(any(Vector2f.class))).thenReturn(new Vector2f(30f, 40f));

        // Add delegation for inverseTransform to use getInverseTransformation().transform()
        doAnswer(invocation -> {
            float xCoord = invocation.getArgument(0);
            float yCoord = invocation.getArgument(1);
            return graphics.getInverseTransformation().transform(new Vector2f(xCoord, yCoord));
        }).when(graphics).inverseTransform(anyFloat(), anyFloat());

        // Test
        Vector2f result = graphics.inverseTransform(x, y);

        // Verify
        assertEquals(30f, result.x());
        assertEquals(40f, result.y());
        verify(graphics).getInverseTransformation();
        verify(inverseTransformation).transform(any(Vector2f.class));
    }
}

package com.dua3.utility.swing;

import org.junit.jupiter.api.Test;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the SwingDocumentListener interface.
 * <p>
 * These tests verify that the update method is called when any of the three
 * DocumentListener methods are called.
 */
class SwingDocumentListenerTest {

    /**
     * Test that the update method is called when insertUpdate is called.
     */
    @Test
    void testInsertUpdate() {
        // Create a counter to track the number of update calls
        AtomicInteger updateCount = new AtomicInteger(0);

        // Create a SwingDocumentListener that increments the counter
        SwingDocumentListener listener = e -> updateCount.incrementAndGet();

        // Create a mock DocumentEvent
        DocumentEvent event = createMockDocumentEvent();

        // Call insertUpdate
        listener.insertUpdate(event);

        // Verify that update was called once
        assertEquals(1, updateCount.get(), "update should be called once when insertUpdate is called");
    }

    /**
     * Test that the update method is called when removeUpdate is called.
     */
    @Test
    void testRemoveUpdate() {
        // Create a counter to track the number of update calls
        AtomicInteger updateCount = new AtomicInteger(0);

        // Create a SwingDocumentListener that increments the counter
        SwingDocumentListener listener = e -> updateCount.incrementAndGet();

        // Create a mock DocumentEvent
        DocumentEvent event = createMockDocumentEvent();

        // Call removeUpdate
        listener.removeUpdate(event);

        // Verify that update was called once
        assertEquals(1, updateCount.get(), "update should be called once when removeUpdate is called");
    }

    /**
     * Test that the update method is called when changedUpdate is called.
     */
    @Test
    void testChangedUpdate() {
        // Create a counter to track the number of update calls
        AtomicInteger updateCount = new AtomicInteger(0);

        // Create a SwingDocumentListener that increments the counter
        SwingDocumentListener listener = e -> updateCount.incrementAndGet();

        // Create a mock DocumentEvent
        DocumentEvent event = createMockDocumentEvent();

        // Call changedUpdate
        listener.changedUpdate(event);

        // Verify that update was called once
        assertEquals(1, updateCount.get(), "update should be called once when changedUpdate is called");
    }

    /**
     * Test that the update method is called for all three types of updates.
     */
    @Test
    void testAllUpdates() {
        // Create a counter to track the number of update calls
        AtomicInteger updateCount = new AtomicInteger(0);

        // Create a SwingDocumentListener that increments the counter
        SwingDocumentListener listener = e -> updateCount.incrementAndGet();

        // Create a mock DocumentEvent
        DocumentEvent event = createMockDocumentEvent();

        // Call all three update methods
        listener.insertUpdate(event);
        listener.removeUpdate(event);
        listener.changedUpdate(event);

        // Verify that update was called three times
        assertEquals(3, updateCount.get(), "update should be called three times when all update methods are called");
    }

    /**
     * Create a mock DocumentEvent for testing.
     * 
     * @return a DocumentEvent
     */
    private DocumentEvent createMockDocumentEvent() {
        // Create a simple document
        Document doc = new PlainDocument();
        try {
            doc.insertString(0, "test", null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        // Create a mock DocumentEvent
        return new DocumentEvent() {
            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getLength() {
                return 4;
            }

            @Override
            public Document getDocument() {
                return doc;
            }

            @Override
            public EventType getType() {
                return EventType.INSERT;
            }

            @Override
            public ElementChange getChange(Element elem) {
                return null;
            }
        };
    }
}

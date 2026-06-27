package com.dua3.utility.ui;

import com.dua3.utility.text.Font;

import java.util.List;

/**
 * Cached visual line layout keyed by width and base font.
 *
 * @param widthKey cached width key
 * @param font cached base font
 * @param lines cached lines
 */
public record VisualLineCache(double widthKey, Font font, List<VisualLine> lines) {
    /**
     * Constructor.
     *
     * @param widthKey width cache key
     * @param font base font
     * @param lines cached lines
     */
    public VisualLineCache {
        lines = List.copyOf(lines);
    }
}

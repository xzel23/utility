package com.dua3.utility.ui;

import com.dua3.utility.text.Font;

import java.util.List;
import java.util.Objects;

/**
 * Cached visual line layout keyed by width and base font.
 *
 * @param widthKey cached width key
 * @param font cached base font
 * @param lines cached lines
 * @param <T> visual line type
 */
public record VisualLineCache<T>(double widthKey, Font font, List<T> lines) {
    /**
     * Constructor.
     *
     * @param widthKey width cache key
     * @param font base font
     * @param lines cached lines
     */
    public VisualLineCache {
        Objects.requireNonNull(font, "font");
        lines = List.copyOf(lines);
    }
}

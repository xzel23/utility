package com.dua3.utility.ui;

import com.dua3.utility.text.FragmentedText;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Shared rich-text rendering helpers for toolkit-specific controls.
 */
public final class RichTextRenderer {

    private RichTextRenderer() {
        // utility class
    }

    /**
     * Renders fragment lines using the current {@link Graphics} state.
     *
     * @param graphics graphics target
     * @param lines fragmented lines
     */
    public static void renderFragmentLines(Graphics graphics, List<List<FragmentedText.Fragment>> lines) {
        renderFragmentLines(graphics, lines, fragment -> false);
    }

    /**
     * Renders fragment lines using the current {@link Graphics} state.
     *
     * <p>Fragments matching {@code excludedFromBaseline} are still rendered, but are ignored when
     * computing the shared line baseline.
     *
     * @param graphics graphics target
     * @param lines fragmented lines
     * @param excludedFromBaseline predicate selecting fragments to exclude from baseline computation
     */
    public static void renderFragmentLines(
            Graphics graphics,
            List<List<FragmentedText.Fragment>> lines,
            Predicate<FragmentedText.Fragment> excludedFromBaseline
    ) {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(lines, "lines");
        Objects.requireNonNull(excludedFromBaseline, "excludedFromBaseline");

        for (List<FragmentedText.Fragment> line : lines) {
            double lineBaseline = line.stream()
                    .filter(excludedFromBaseline.negate())
                    .mapToDouble(fragment -> fragment.font().getAscent())
                    .max()
                    .orElseGet(() -> line.stream()
                            .mapToDouble(fragment -> fragment.font().getAscent())
                            .max()
                            .orElse(0.0));

            for (FragmentedText.Fragment fragment : line) {
                graphics.setFont(fragment.font());
                graphics.drawText(
                        fragment.text().toString(),
                        fragment.x(),
                        (float) (fragment.y() + lineBaseline),
                        HAnchor.LEFT,
                        VAnchor.BASELINE
                );
            }
        }
    }
}

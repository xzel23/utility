package com.dua3.utility.fx;

import com.dua3.utility.text.FontData;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;

import java.util.List;
import java.util.Objects;

/**
 * This class represents an embedded font in JavaFX.
 */
public final class FxFontEmbedded extends Font {

    private final javafx.scene.text.Font fxFont;

    FxFontEmbedded(javafx.scene.text.Font fxFont, List<String> families, Color color, boolean bold, boolean italic, boolean underline, boolean strikeThrough) {
        super(prepareEmbeddedFontData(fxFont, families, bold, italic, underline, strikeThrough), color);
        this.fxFont = fxFont;
    }

    private static FontData prepareEmbeddedFontData(javafx.scene.text.Font fxFont, List<String> families, boolean bold, boolean italic, boolean underline, boolean strikeThrough) {
        FontData fxFontData = FxFontUtil.getFontData(fxFont);

        return FontData.get(
                families,
                fxFontData.size(),
                fxFontData.monospaced(),
                bold,
                italic,
                underline,
                strikeThrough,
                fxFontData.ascent(),
                fxFontData.descent(),
                fxFontData.height(),
                fxFontData.spaceWidth()
        );
    }

    /**
     * Retrieves the JavaFX {@link javafx.scene.text.Font} associated with this {@code FxFontEmbedded}.
     *
     * @return The JavaFX {@link javafx.scene.text.Font}.
     */
    public javafx.scene.text.Font fxFont() {
        return fxFont;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fxFont);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof FxFontEmbedded ffe && ffe.fxFont.equals(fxFont) && super.equals(ffe);
    }

}

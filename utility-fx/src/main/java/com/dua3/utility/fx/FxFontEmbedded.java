package com.dua3.utility.fx;

import com.dua3.utility.text.FontData;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;

import java.util.Objects;

/**
 * This class represents an embedded font in JavaFX.
 */
public final class FxFontEmbedded extends Font {

    private final javafx.scene.text.Font fxFont;

    FxFontEmbedded(javafx.scene.text.Font fxFont, String family, float size, Color color, boolean bold, boolean italic, boolean underline, boolean strikeThrough) {
        super(prepareEmbeddedFontData(fxFont, family, color, bold, italic, underline, strikeThrough), color);
        this.fxFont = fxFont;
    }

    private static FontData prepareEmbeddedFontData(javafx.scene.text.Font fxFont, String family, Color color, boolean bold, boolean italic, boolean underline, boolean strikeThrough) {
        FontData fxFontData = FxFontUtil.getFontData(fxFont);

        FontDef fontDef = new FontDef();
        fontDef.setFamily(family);
        fontDef.setSize(fxFontData.size());
        fontDef.setBold(bold);
        fontDef.setItalic(italic);
        fontDef.setColor(color);
        fontDef.setUnderline(underline);
        fontDef.setStrikeThrough(strikeThrough);

        return new FontData(
                family,
                fxFontData.size(),
                bold,
                italic,
                underline,
                strikeThrough,
                fontDef,
                fontDef.fontspec(),
                fontDef.getCssStyle(),
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
        if (!super.equals(obj)) {
            return false;
        }
        return ((FxFontEmbedded) obj).fxFont.equals(fxFont);
    }

}

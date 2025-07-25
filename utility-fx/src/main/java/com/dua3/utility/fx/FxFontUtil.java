// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.FontData;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontType;
import com.dua3.utility.text.FontUtil;
import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for working with fonts in JavaFX.
 */
public final class FxFontUtil implements FontUtil<Font> {

    private static final String DEFAULT_FAMILY;
    private static final float DEFAULT_SIZE;

    static {
        Font font = Font.getDefault();
        // on macOS use Verdana instead of the default font because the default font
        // does not support styling (bold, italics) in JavaFX..
        DEFAULT_FAMILY = Platform.isMacOS() ? "Verdana" : font.getFamily();
        DEFAULT_SIZE = (float) font.getSize();
    }

    private static final class SingletonHolder {
        private static final FxFontUtil INSTANCE = new FxFontUtil();
    }

    private final Map<FontData, Font> fontData2fxFont = new ConcurrentHashMap<>();
    private final Map<Font, FontData> fxFont2FontData = new ConcurrentHashMap<>();

    private record FxFontData(String family, FontWeight weight, FontPosture posture, double size) {}

    private final Map<FxFontData, Font> fxFontData2fxFont = new ConcurrentHashMap<>();

    private final com.dua3.utility.text.Font defaultFont;

    /**
     * Returns an instance of FxUtil.
     *
     * @return the instance of FxUtil
     */
    public static FxFontUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Public constructor. Needed fo SPI.
     */
    private FxFontUtil() {
        defaultFont = convert(new Font(DEFAULT_FAMILY, DEFAULT_SIZE));
    }

    /**
     * Converts a com.dua3.utility.text.Font object to a JavaFX Font object.
     *
     * @param font the com.dua3.utility.text.Font object to be converted
     * @return the converted JavaFX Font object
     */
    @Override
    public Font convert(com.dua3.utility.text.Font font) {
        if (font instanceof FxFontEmbedded fxf) {
            return fxf.fxFont();
        }

        Font fxFont = fontData2fxFont.computeIfAbsent(font.getFontData(), fd -> getFxFont(fd.families(), fd.size(), fd.bold(), fd.italic()));
        fxFont2FontData.putIfAbsent(fxFont, font.getFontData());
        return fxFont;
    }

    /**
     * Converts a JavaFX Font object to a com.dua3.utility.text.Font object.
     *
     * @param fxFont the JavaFX Font object to be converted
     * @return the converted com.dua3.utility.text.Font object
     */
    public com.dua3.utility.text.Font convert(Font fxFont) {
        FontData fontData = fxFont2FontData.computeIfAbsent(fxFont, FxFontUtil::getFontData);
        fontData2fxFont.putIfAbsent(fontData, fxFont);
        return com.dua3.utility.text.Font.getFont(fontData, Color.BLACK);
    }

    /**
     * Extracts font data from a JavaFX Font object.
     *
     * @param fxFont the JavaFX Font object from which the font data is extracted
     * @return a FontData object containing detailed information about the font, including its family, size,
     *         style attributes such as bold, italic, underline, and strike-through, and additional
     *         metrics like ascent, descent, height, and space width
     */
    public static FontData getFontData(Font fxFont) {
        Text text = new Text("Xg|█");
        text.setFont(fxFont);
        Bounds bounds = text.getBoundsInLocal();
        float ascent = (float) text.getBaselineOffset();
        float height = (float) bounds.getHeight();
        float descent = height - ascent;
        boolean monospaced = isMonospaced(fxFont);

        text.setText(" ");
        bounds = text.getBoundsInLocal();
        float spaceWidth = (float) bounds.getWidth();

        Set<String> styles = Set.of(fxFont.getStyle().toLowerCase(Locale.ROOT).split(" "));

        return FontData.get(
                List.of(fxFont.getFamily()),
                (float) fxFont.getSize(),
                monospaced,
                styles.contains("bold"),
                styles.contains("italic") || styles.contains("oblique"),
                styles.contains("line-under"),
                styles.contains("line-through"),
                ascent,
                descent,
                height,
                spaceWidth
        );
    }

    /**
     * Extracts font characteristics from a JavaFX Font object and creates a corresponding FontDef object.
     *
     * @param fxFont the JavaFX Font object to be analyzed
     * @return a FontDef object containing the extracted font properties such as family, size, bold,
     *         italic, underline, and strikethrough
     */
    public static FontDef getFontDef(Font fxFont) {
        Set<String> styles = Set.of(fxFont.getStyle().toLowerCase(Locale.ROOT).split(" "));
        FontDef fontDef = new FontDef();
        fontDef.setFamilies(List.of(fxFont.getFamily()));
        fontDef.setSize((float) fxFont.getSize());
        fontDef.setBold(styles.contains("bold"));
        fontDef.setItalic(styles.contains("italic") || styles.contains("oblique"));
        fontDef.setUnderline(styles.contains("line-under"));
        fontDef.setStrikeThrough(styles.contains("line-through"));
        return fontDef;
    }

    @Override
    public Rectangle2f getTextDimension(CharSequence s, com.dua3.utility.text.Font f) {
        var bounds = FxUtil.getTextBounds(s, f);
        return Rectangle2f.of((float) bounds.getMinX(), (float) bounds.getMinY(), (float) bounds.getWidth(), (float) bounds.getHeight());
    }

    /**
     * Calculates the dimensions of the given text when rendered with the specified font.
     *
     * @param s the text to measure
     * @param f the font used to render the text
     * @return a {@link Rectangle2f} object representing the dimensions of the rendered text
     */
    public Rectangle2f getTextDimension(CharSequence s, Font f) {
        var bounds = FxUtil.getTextBounds(s, f);
        return Rectangle2f.of((float) bounds.getMinX(), (float) bounds.getMinY(), (float) bounds.getWidth(), (float) bounds.getHeight());
    }

    @Override
    public double getTextWidth(CharSequence s, com.dua3.utility.text.Font f) {
        return FxUtil.getTextWidth(s, f);
    }

    @Override
    public double getTextHeight(CharSequence s, com.dua3.utility.text.Font f) {
        return FxUtil.getTextHeight(s, f);
    }

    @Override
    public List<com.dua3.utility.text.Font> loadFonts(InputStream in) {
        Font[] fxFonts = Font.loadFonts(in, 0);
        if (fxFonts == null || fxFonts.length == 0) {
            return Collections.emptyList();
        }

        List<com.dua3.utility.text.Font> fonts = new ArrayList<>(fxFonts.length);
        for (Font fxFont : fxFonts) {
            fonts.add(convert(fxFont));
        }

        return Collections.unmodifiableList(fonts);
    }

    private static class FontList {
        // map containing all known font families as keys, with mapping family -> monospaced
        private static final Map<String, Boolean> AVAILABLE_FONTS;
        // alphabetically sorted list of all know font families
        private static final LinkedHashSet<String> ALL_FONTS;
        // alphabetically sorted list of all know monospaced font families
        private static final LinkedHashSet<String> MONOSPACE_FONTS;
        // alphabetically sorted list of all know proportional font families
        private static final LinkedHashSet<String> PROPORTIONAL_FONTS;

        static {
            AVAILABLE_FONTS = Font.getFamilies().stream().collect(Collectors.toUnmodifiableMap(Function.identity(), FontList::isMonospaced, (a, b) -> b));
            ALL_FONTS = AVAILABLE_FONTS.keySet().stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            MONOSPACE_FONTS = ALL_FONTS.stream().filter(AVAILABLE_FONTS::get).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            PROPORTIONAL_FONTS = ALL_FONTS.stream().filter(Predicate.not(AVAILABLE_FONTS::get)).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        }

        private static boolean isMonospaced(String family) {
            Font font = Font.font(family, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
            return FxFontUtil.isMonospaced(font);
        }
    }

    private static boolean isMonospaced(Font font) {
        Text thin = new Text("1 l");
        Text thick = new Text("M_W");
        thin.setFont(font);
        thick.setFont(font);
        return thin.getLayoutBounds().getWidth() == thick.getLayoutBounds().getWidth();
    }

    @Override
    public SequencedCollection<String> getFamilies(FontTypes types) {
        return switch (types) {
            case ALL -> FontList.ALL_FONTS;
            case MONOSPACED -> FontList.MONOSPACE_FONTS;
            case PROPORTIONAL -> FontList.PROPORTIONAL_FONTS;
        };
    }

    @Override
    public com.dua3.utility.text.Font loadFontAs(InputStream in, com.dua3.utility.text.Font font) throws IOException {
        Font fxFont = Font.loadFont(in, font.getSizeInPoints());
        LangUtil.check(fxFont != null, () -> new IOException("no font loaded"));
        return new com.dua3.utility.fx.FxFontEmbedded(fxFont, font.getFamilies(), font.getColor(), font.isBold(), font.isItalic(), font.isUnderline(), font.isStrikeThrough());
    }

    @Override
    public com.dua3.utility.text.Font getDefaultFont() {
        return defaultFont;
    }

    @Override
    public com.dua3.utility.text.Font deriveFont(com.dua3.utility.text.Font font, FontDef fontDef) {
        List<String> families = Objects.requireNonNullElse(fontDef.getFamilies(), font.getFamilies());
        float size = Objects.requireNonNullElse(fontDef.getSize(), font.getSizeInPoints());
        boolean bold = Objects.requireNonNullElse(fontDef.getBold(), font.isBold());
        boolean italic = Objects.requireNonNullElse(fontDef.getItalic(), font.isItalic());

        com.dua3.utility.text.Font baseFont = convert(getFxFont(
                families,
                size,
                bold,
                italic
        ));

        FontData fontData = FontData.get(
                families,
                size,
                baseFont.getType() == FontType.MONOSPACED,
                bold,
                italic,
                Objects.requireNonNullElse(fontDef.getUnderline(), font.isUnderline()),
                Objects.requireNonNullElse(fontDef.getStrikeThrough(), font.isStrikeThrough()),
                baseFont.getAscent(),
                baseFont.getDescent(),
                baseFont.getHeight(),
                baseFont.getSpaceWidth()
        );

        Color color = Objects.requireNonNullElse(fontDef.getColor(), font.getColor());
        if (fontData.equals(baseFont.getFontData()) && color.equals(baseFont.getColor())) {
            return baseFont; // avoid creating unnecessary instance
        } else {
            return com.dua3.utility.text.Font.getFont(fontData, color);
        }
    }

    private Font getFxFont(List<String> families, float size, boolean bold, boolean italic) {
        String family = families.stream().filter(FontList.ALL_FONTS::contains).findFirst().orElse(families.getFirst());
        FxFontData fxf = new FxFontData(
                family,
                bold ? FontWeight.BOLD : FontWeight.NORMAL,
                italic ? FontPosture.ITALIC : FontPosture.REGULAR,
                size);

        return fxFontData2fxFont.computeIfAbsent(fxf, k -> Font.font(fxf.family, fxf.weight(), fxf.posture(), fxf.size()));
    }

}

package com.dua3.utility.awt;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontData;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for getting font properties through AWT. This class should normally not be used directly by user code
 * as the functionality should be available in the {@link com.dua3.utility.text.TextUtil} utility class which
 * in turn uses this class via SPI (Java ServiceProvider interface).
 * See usage of {@link FontUtil} in {@link com.dua3.utility.text.TextUtil} for details.
 */
@SuppressWarnings("NumericCastThatLosesPrecision")
public class AwtFontUtil implements FontUtil<java.awt.Font> {
    private static final Logger LOG = LogManager.getLogger(AwtFontUtil.class);

    private static final String DEFAULT_FAMILY = "Arial";
    private static final float DEFAULT_SIZE = 10.0f;

    static {
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        boolean isJavaAwtHeadless = Boolean.getBoolean("java.awt.headless");
        if (isHeadless && isJavaAwtHeadless) {
            LOG.warn("The environment is headless, but the property java.awt.headless is not set to \"true\", expect problems!");
        }
        if (isJavaAwtHeadless) {
            LOG.info("headless mode is enabled");
        }
    }

    private final Map<FontData, java.awt.Font> fontData2awtFont = new ConcurrentHashMap<>();
    private final Map<java.awt.Font, FontData> awtFont2FontData = new ConcurrentHashMap<>();

    private final Font defaultFont;
    private final Graphics2D graphics;

    private static java.awt.Font getAwtFont(String family, float size, boolean bold, boolean italic) {
        int style = (bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN)
                | (italic ? java.awt.Font.ITALIC : java.awt.Font.PLAIN);
        return new java.awt.Font(family, style, Math.round(size));
    }

    private static class SingletonHolder {
        private static final AwtFontUtil INSTANCE = new AwtFontUtil();
    }

    private AwtFontUtil() {
        graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        defaultFont = convert(getAwtFont(DEFAULT_FAMILY, DEFAULT_SIZE, false, false));
    } // utility class constructor

    /**
     * Retrieves an instance of the AwtFontUtil class.
     *
     * @return an instance of AwtFontUtil
     */
    public static AwtFontUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     *
     * @param text the text
     * @param font the font
     * @return the text's bounds (positioned at the origin)
     */
    public Rectangle2D stringBounds(CharSequence text, Font font) {
        java.awt.Font awtFont = convert(font);
        return stringBounds(text, awtFont);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Rectangle2D stringBounds(CharSequence text, java.awt.Font awtFont) {
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return awtFont.getStringBounds(text.toString(), frc);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public Rectangle2f getTextDimension(CharSequence text, java.awt.Font awtFont) {
        Rectangle2D r = stringBounds(text, awtFont);
        return new Rectangle2f((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float) r.getHeight());
    }

    /**
     * Calculate the height of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextHeight(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getHeight();
    }

    /**
     * Calculate the width of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextWidth(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getWidth();
    }

    @Override
    public Rectangle2f getTextDimension(CharSequence s, Font f) {
        Rectangle2D r = stringBounds(s, f);
        return new Rectangle2f((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float) r.getHeight());
    }

    @Override
    public double getTextHeight(CharSequence s, Font f) {
        return stringBounds(s, f).getHeight();
    }

    @Override
    public List<Font> loadFonts(InputStream in) throws IOException {
        try {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            return Arrays.stream(awtFonts).map(this::convert).toList();
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }

    private static class FontList {
        // map containing all known font families as keys, with mapping family -> monospaced
        private static final Map<String, Boolean> AVAILABLE_FONTS;
        // alphabetically sorted list of all know font families
        private static final List<String> ALL_FONTS;
        // alphabetically sorted list of all know monospaced font families
        private static final List<String> MONOSPACE_FONTS;
        // alphabetically sorted list of all know proportional font families
        private static final List<String> PROPORTIONAL_FONTS;

        static {
            AVAILABLE_FONTS = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).collect(Collectors.toUnmodifiableMap(Function.identity(), AwtFontUtil::isMonospaced, (a, b) -> b));
            ALL_FONTS = AVAILABLE_FONTS.keySet().stream().sorted().toList();
            MONOSPACE_FONTS = ALL_FONTS.stream().filter(AVAILABLE_FONTS::get).toList();
            PROPORTIONAL_FONTS =  ALL_FONTS.stream().filter(Predicate.not(AVAILABLE_FONTS::get)).toList();
        }
    }

    @Override
    public List<String> getFamilies(FontTypes types) {
        return switch (types) {
            case ALL -> FontList.ALL_FONTS;
            case MONOSPACED -> FontList.MONOSPACE_FONTS;
            case PROPORTIONAL -> FontList.PROPORTIONAL_FONTS;
        };
    }

    private static boolean isMonospaced(String family) {
        java.awt.Font font = new java.awt.Font(family, java.awt.Font.PLAIN, 14);
        FontRenderContext frc = new FontRenderContext(font.getTransform(), false, true);
        return Objects.equals(font.getStringBounds("1 l", frc), font.getStringBounds("M_W", frc));
    }

    @Override
    public double getTextWidth(CharSequence s, Font f) {
        return stringBounds(s, f).getWidth();
    }

    @Override
    public java.awt.Font convert(Font font) {
        java.awt.Font awtFont = fontData2awtFont.computeIfAbsent(font.getFontData(), fd -> getAwtFont(fd.family(), fd.size(), fd.bold(), fd.italic()));
        awtFont2FontData.putIfAbsent(awtFont, font.getFontData());
        return awtFont;
    }

    @Override
    public Font convert(java.awt.Font awtFont) {
        FontData fontData = awtFont2FontData.computeIfAbsent(awtFont, this::getFontData);
        fontData2awtFont.putIfAbsent(fontData, awtFont);
        return new Font(fontData, Color.BLACK);
    }

    @Override
    public Font loadFontAs(InputStream in, Font font) throws IOException {
        try {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            LangUtil.check(awtFonts.length > 0, () -> new IOException("no font loaded"));
            java.awt.Font awtFont = awtFonts[0].deriveFont(font.getSizeInPoints());
            FontData loadedFont = getFontData(awtFont);
            fontData2awtFont.putIfAbsent(loadedFont, awtFont);
            awtFont2FontData.putIfAbsent(awtFont, loadedFont);
            return font;
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Font deriveFont(Font font, FontDef fontDef) {
        Font baseFont = convert(getAwtFont(
                Objects.requireNonNullElse(fontDef.getFamily(), font.getFamily()),
                Objects.requireNonNullElse(fontDef.getSize(), font.getSizeInPoints()),
                Objects.requireNonNullElse(fontDef.getBold(), font.isBold()),
                Objects.requireNonNullElse(fontDef.getItalic(), font.isItalic())
        ));

        FontData fontData = FontData.get(
                baseFont.getFamily(),
                baseFont.getSizeInPoints(),
                baseFont.isBold(),
                baseFont.isItalic(),
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
            return new Font(fontData, color);
        }
    }

    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    private FontData getFontData(java.awt.Font awtFont) {
        String family = awtFont.getName();
        float size = awtFont.getSize2D();
        boolean bold = awtFont.isBold();
        FontMetrics fontMetrics = graphics.getFontMetrics(awtFont);
        return FontData.get(
                family,
                size,
                bold,
                awtFont.isItalic(),
                false,
                false,
                fontMetrics.getAscent(),
                fontMetrics.getDescent(),
                fontMetrics.getHeight(),
                fontMetrics.stringWidth(" ")
        );
    }

}

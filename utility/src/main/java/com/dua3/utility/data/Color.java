package com.dua3.utility.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A common interface for different color implementations.
 */
public interface Color {
    /** Factor to apply when generating a brighter or darker version of a color. */
    float F_BRIGHTEN = 0.7f;

    /** The color ALICEBLUE. */
    Color ALICEBLUE = register("ALICEBLUE", 0xFFF0F8FF);
    /** The color ANTIQUEWHITE. */
    Color ANTIQUEWHITE = register("ANTIQUEWHITE", 0xFFFAEBD7);
    /** The color AQUA. */
    Color AQUA = register("AQUA", 0xFF00FFFF);
    /** The color AQUAMARINE. */
    Color AQUAMARINE = register("AQUAMARINE", 0xFF7FFFD4);
    /** The color AZURE. */
    Color AZURE = register("AZURE", 0xFFF0FFFF);
    /** The color BEIGE. */
    Color BEIGE = register("BEIGE", 0xFFF5F5DC);
    /** The color BISQUE. */
    Color BISQUE = register("BISQUE", 0xFFFFE4C4);
    /** The color BLACK. */
    RGBColor BLACK = register("BLACK", 0xFF000000);
    /** The color BLANCHEDALMOND. */
    Color BLANCHEDALMOND = register("BLANCHEDALMOND", 0xFFFFE4C4);
    /** The color BLUE. */
    Color BLUE = register("BLUE", 0xFF0000FF);
    /** The color BLUEVIOLET. */
    Color BLUEVIOLET = register("BLUEVIOLET", 0xFF8A2BE2);
    /** The color BROWN. */
    Color BROWN = register("BROWN", 0xFFA52A2A);
    /** The color BURLYWOOD. */
    Color BURLYWOOD = register("BURLYWOOD", 0xFFDEB887);
    /** The color CADETBLUE. */
    Color CADETBLUE = register("CADETBLUE", 0xFF5F9EA0);
    /** The color CHARTREUSE. */
    Color CHARTREUSE = register("CHARTREUSE", 0xFF7FFF00);
    /** The color CHOCOLATE. */
    Color CHOCOLATE = register("CHOCOLATE", 0xFFD2691E);
    /** The color CORAL. */
    Color CORAL = register("CORAL", 0xFFFF7F50);
    /** The color CORNFLOWERBLUE. */
    Color CORNFLOWERBLUE = register("CORNFLOWERBLUE", 0xFF6495ED);
    /** The color CORNSILK. */
    Color CORNSILK = register("CORNSILK", 0xFFFFF8DC);
    /** The color CRIMSON. */
    Color CRIMSON = register("CRIMSON", 0xFFDC143C);
    /** The color DARKBLUE. */
    Color DARKBLUE = register("DARKBLUE", 0xFF00008B);
    /** The color DARKCYAN. */
    Color DARKCYAN = register("DARKCYAN", 0xFF008B8B);
    /** The color DARKGOLDENROD. */
    Color DARKGOLDENROD = register("DARKGOLDENROD", 0xFFB8860B);
    /** The color DARKGRAY. */
    Color DARKGRAY = register("DARKGRAY", 0xFFA9A9A9);
    /** The color DARKGREEN. */
    Color DARKGREEN = register("DARKGREEN", 0xFF006400);
    /** The color DARKGREY. */
    Color DARKGREY = register("DARKGREY", 0xFFA9A9A9);
    /** The color DARKKHAKI. */
    Color DARKKHAKI = register("DARKKHAKI", 0xFFBDB76B);
    /** The color DARKMAGENTA. */
    Color DARKMAGENTA = register("DARKMAGENTA", 0xFF8B008B);
    /** The color DARKOLIVEGREEN. */
    Color DARKOLIVEGREEN = register("DARKOLIVEGREEN", 0xFF556B2F);
    /** The color DARKORANGE. */
    Color DARKORANGE = register("DARKORANGE", 0xFFFF8C00);
    /** The color DARKORCHID. */
    Color DARKORCHID = register("DARKORCHID", 0xFF9932CC);
    /** The color DARKRED. */
    Color DARKRED = register("DARKRED", 0xFF8B0000);
    /** The color DARKSALMON. */
    Color DARKSALMON = register("DARKSALMON", 0xFFE9967A);
    /** The color DARKSEAGREEN. */
    Color DARKSEAGREEN = register("DARKSEAGREEN", 0xFF8FBC8F);
    /** The color DARKSLATEBLUE. */
    Color DARKSLATEBLUE = register("DARKSLATEBLUE", 0xFF483D8B);
    /** The color DARKSLATEGRAY. */
    Color DARKSLATEGRAY = register("DARKSLATEGRAY", 0xFF2F4F4F);
    /** The color DARKSLATEGREY. */
    Color DARKSLATEGREY = register("DARKSLATEGREY", 0xFF2F4F4F);
    /** The color DARKTURQUOISE. */
    Color DARKTURQUOISE = register("DARKTURQUOISE", 0xFF00CED1);
    /** The color DARKVIOLET. */
    Color DARKVIOLET = register("DARKVIOLET", 0xFF9400D3);
    /** The color DEEPPINK. */
    Color DEEPPINK = register("DEEPPINK", 0xFFFF1493);
    /** The color DEEPSKYBLUE. */
    Color DEEPSKYBLUE = register("DEEPSKYBLUE", 0xFF00BFFF);
    /** The color DIMGRAY. */
    Color DIMGRAY = register("DIMGRAY", 0xFF696969);
    /** The color DIMGREY. */
    Color DIMGREY = register("DIMGREY", 0xFF696969);
    /** The color DODGERBLUE. */
    Color DODGERBLUE = register("DODGERBLUE", 0xFF1E90FF);
    /** The color FIREBRICK. */
    Color FIREBRICK = register("FIREBRICK", 0xFFB22222);
    /** The color FLORALWHITE. */
    Color FLORALWHITE = register("FLORALWHITE", 0xFFFFFAF0);
    /** The color FORESTGREEN. */
    Color FORESTGREEN = register("FORESTGREEN", 0xFF228B22);
    /** The color FUCHSIA. */
    Color FUCHSIA = register("FUCHSIA", 0xFFFF00FF);
    /** The color GAINSBORO. */
    Color GAINSBORO = register("GAINSBORO", 0xFFDCDCDC);
    /** The color GHOSTWHITE. */
    Color GHOSTWHITE = register("GHOSTWHITE", 0xFFF8F8FF);
    /** The color GOLD. */
    Color GOLD = register("GOLD", 0xFFFFD700);
    /** The color GOLDENROD. */
    Color GOLDENROD = register("GOLDENROD", 0xFFDAA520);
    /** The color GRAY. */
    Color GRAY = register("GRAY", 0xFF808080);
    /** The color GREEN. */
    Color GREEN = register("GREEN", 0xFF008000);
    /** The color GREENYELLOW. */
    Color GREENYELLOW = register("GREENYELLOW", 0xFFADFF2F);
    /** The color GREY. */
    Color GREY = register("GREY", 0xFF808080);
    /** The color HONEYDEW. */
    Color HONEYDEW = register("HONEYDEW", 0xFFF0FFF0);
    /** The color HOTPINK. */
    Color HOTPINK = register("HOTPINK", 0xFFFF69B4);
    /** The color INDIANRED. */
    Color INDIANRED = register("INDIANRED", 0xFFCD5C5C);
    /** The color INDIGO. */
    Color INDIGO = register("INDIGO", 0xFF4B0082);
    /** The color IVORY. */
    Color IVORY = register("IVORY", 0xFFFFFFF0);
    /** The color KHAKI. */
    Color KHAKI = register("KHAKI", 0xFFF0E68C);
    /** The color LAVENDER. */
    Color LAVENDER = register("LAVENDER", 0xFFE6E6FA);
    /** The color LAVENDERBLUSH. */
    Color LAVENDERBLUSH = register("LAVENDERBLUSH", 0xFFFFF0F5);
    /** The color LAWNGREEN. */
    Color LAWNGREEN = register("LAWNGREEN", 0xFF7CFC00);
    /** The color LEMONCHIFFON. */
    Color LEMONCHIFFON = register("LEMONCHIFFON", 0xFFFFFACD);
    /** The color LIGHTBLUE. */
    Color LIGHTBLUE = register("LIGHTBLUE", 0xFFADD8E6);
    /** The color LIGHTCORAL. */
    Color LIGHTCORAL = register("LIGHTCORAL", 0xFFF08080);
    /** The color LIGHTCYAN. */
    Color LIGHTCYAN = register("LIGHTCYAN", 0xFFE0FFFF);
    /** The color LIGHTGOLDENRODYELLOW. */
    Color LIGHTGOLDENRODYELLOW = register("LIGHTGOLDENRODYELLOW", 0xFFFAFAD2);
    /** The color LIGHTGRAY. */
    Color LIGHTGRAY = register("LIGHTGRAY", 0xFFD3D3D3);
    /** The color LIGHTGREEN. */
    Color LIGHTGREEN = register("LIGHTGREEN", 0xFF90EE90);
    /** The color LIGHTGREY. */
    Color LIGHTGREY = register("LIGHTGREY", 0xFFD3D3D3);
    /** The color LIGHTPINK. */
    Color LIGHTPINK = register("LIGHTPINK", 0xFFFFB6C1);
    /** The color LIGHTSALMON. */
    Color LIGHTSALMON = register("LIGHTSALMON", 0xFFFFA07A);
    /** The color LIGHTSEAGREEN. */
    Color LIGHTSEAGREEN = register("LIGHTSEAGREEN", 0xFF20B2AA);
    /** The color LIGHTSKYBLUE. */
    Color LIGHTSKYBLUE = register("LIGHTSKYBLUE", 0xFF87CEFA);
    /** The color LIGHTSLATEGRAY. */
    Color LIGHTSLATEGRAY = register("LIGHTSLATEGRAY", 0xFF778899);
    /** The color LIGHTSLATEGREY. */
    Color LIGHTSLATEGREY = register("LIGHTSLATEGREY", 0xFF778899);
    /** The color LIGHTSTEELBLUE. */
    Color LIGHTSTEELBLUE = register("LIGHTSTEELBLUE", 0xFFB0C4DE);
    /** The color LIGHTYELLOW. */
    Color LIGHTYELLOW = register("LIGHTYELLOW", 0xFFFFFFE0);
    /** The color LIME. */
    Color LIME = register("LIME", 0xFF00FF00);
    /** The color LIMEGREEN. */
    Color LIMEGREEN = register("LIMEGREEN", 0xFF32CD32);
    /** The color LINEN. */
    Color LINEN = register("LINEN", 0xFFFAF0E6);
    /** The color MAROON. */
    Color MAROON = register("MAROON", 0xFF800000);
    /** The color MEDIUMAQUAMARINE. */
    Color MEDIUMAQUAMARINE = register("MEDIUMAQUAMARINE", 0xFF66CDAA);
    /** The color MEDIUMBLUE. */
    Color MEDIUMBLUE = register("MEDIUMBLUE", 0xFF0000CD);
    /** The color MEDIUMORCHID. */
    Color MEDIUMORCHID = register("MEDIUMORCHID", 0xFFBA55D3);
    /** The color MEDIUMPURPLE. */
    Color MEDIUMPURPLE = register("MEDIUMPURPLE", 0xFF9370DB);
    /** The color MEDIUMSEAGREEN. */
    Color MEDIUMSEAGREEN = register("MEDIUMSEAGREEN", 0xFF3CB371);
    /** The color MEDIUMSLATEBLUE. */
    Color MEDIUMSLATEBLUE = register("MEDIUMSLATEBLUE", 0xFF7B68EE);
    /** The color MEDIUMSPRINGGREEN. */
    Color MEDIUMSPRINGGREEN = register("MEDIUMSPRINGGREEN", 0xFF00FA9A);
    /** The color MEDIUMTURQUOISE. */
    Color MEDIUMTURQUOISE = register("MEDIUMTURQUOISE", 0xFF48D1CC);
    /** The color MEDIUMVIOLETRED. */
    Color MEDIUMVIOLETRED = register("MEDIUMVIOLETRED", 0xFFC71585);
    /** The color MIDNIGHTBLUE. */
    Color MIDNIGHTBLUE = register("MIDNIGHTBLUE", 0xFF191970);
    /** The color MINTCREAM. */
    Color MINTCREAM = register("MINTCREAM", 0xFFF5FFFA);
    /** The color MISTYROSE. */
    Color MISTYROSE = register("MISTYROSE", 0xFFFFE4E1);
    /** The color MOCCASIN. */
    Color MOCCASIN = register("MOCCASIN", 0xFFFFE4B5);
    /** The color NAVAJOWHITE. */
    Color NAVAJOWHITE = register("NAVAJOWHITE", 0xFFFFDEAD);
    /** The color NAVY. */
    Color NAVY = register("NAVY", 0xFF000080);
    /** The color OLDLACE. */
    Color OLDLACE = register("OLDLACE", 0xFFFDF5E6);
    /** The color OLIVE. */
    Color OLIVE = register("OLIVE", 0xFF808000);
    /** The color OLIVEDRAB. */
    Color OLIVEDRAB = register("OLIVEDRAB", 0xFF6B8E23);
    /** The color ORANGE. */
    Color ORANGE = register("ORANGE", 0xFFFFA500);
    /** The color ORANGERED. */
    Color ORANGERED = register("ORANGERED", 0xFFFF4500);
    /** The color ORCHID. */
    Color ORCHID = register("ORCHID", 0xFFDA70D6);
    /** The color PALEGOLDENROD. */
    Color PALEGOLDENROD = register("PALEGOLDENROD", 0xFFEEE8AA);
    /** The color PALEGREEN. */
    Color PALEGREEN = register("PALEGREEN", 0xFF98FB98);
    /** The color PALETURQUOISE. */
    Color PALETURQUOISE = register("PALETURQUOISE", 0xFFAFEEEE);
    /** The color PALEVIOLETRED. */
    Color PALEVIOLETRED = register("PALEVIOLETRED", 0xFFDB7093);
    /** The color PAPAYAWHIP. */
    Color PAPAYAWHIP = register("PAPAYAWHIP", 0xFFFFEFD5);
    /** The color PEACHPUFF. */
    Color PEACHPUFF = register("PEACHPUFF", 0xFFFFDAB9);
    /** The color PERU. */
    Color PERU = register("PERU", 0xFFCD853F);
    /** The color PINK. */
    Color PINK = register("PINK", 0xFFFFC0CB);
    /** The color PLUM. */
    Color PLUM = register("PLUM", 0xFFDDA0DD);
    /** The color POWDERBLUE. */
    Color POWDERBLUE = register("POWDERBLUE", 0xFFB0E0E6);
    /** The color PURPLE. */
    Color PURPLE = register("PURPLE", 0xFF800080);
    /** The color REBECCAPURPLE. */
    Color REBECCAPURPLE = register("REBECCAPURPLE", 0xFF663399);
    /** The color RED. */
    Color RED = register("RED", 0xFFFF0000);
    /** The color ROSYBROWN. */
    Color ROSYBROWN = register("ROSYBROWN", 0xFFBC8F8F);
    /** The color ROYALBLUE. */
    Color ROYALBLUE = register("ROYALBLUE", 0xFF4169E1);
    /** The color SADDLEBROWN. */
    Color SADDLEBROWN = register("SADDLEBROWN", 0xFF8B4513);
    /** The color SALMON. */
    Color SALMON = register("SALMON", 0xFFFA8072);
    /** The color SANDYBROWN. */
    Color SANDYBROWN = register("SANDYBROWN", 0xFFF4A460);
    /** The color SEAGREEN. */
    Color SEAGREEN = register("SEAGREEN", 0xFF2E8B57);
    /** The color SEASHELL. */
    Color SEASHELL = register("SEASHELL", 0xFFFFF5EE);
    /** The color SIENNA. */
    Color SIENNA = register("SIENNA", 0xFFA0522D);
    /** The color SILVER. */
    Color SILVER = register("SILVER", 0xFFC0C0C0);
    /** The color SKYBLUE. */
    Color SKYBLUE = register("SKYBLUE", 0xFF87CEEB);
    /** The color SLATEBLUE. */
    Color SLATEBLUE = register("SLATEBLUE", 0xFF6A5ACD);
    /** The color SLATEGRAY. */
    Color SLATEGRAY = register("SLATEGRAY", 0xFF708090);
    /** The color SLATEGREY. */
    Color SLATEGREY = register("SLATEGREY", 0xFF708090);
    /** The color SNOW. */
    Color SNOW = register("SNOW", 0xFFFFFAFA);
    /** The color SPRINGGREEN. */
    Color SPRINGGREEN = register("SPRINGGREEN", 0xFF00FF7F);
    /** The color STEELBLUE. */
    Color STEELBLUE = register("STEELBLUE", 0xFF4682B4);
    /** The color TAN. */
    Color TAN = register("TAN", 0xFFD2B48C);
    /** The color TEAL. */
    Color TEAL = register("TEAL", 0xFF008080);
    /** The color THISTLE. */
    Color THISTLE = register("THISTLE", 0xFFD8BFD8);
    /** The color TOMATO. */
    Color TOMATO = register("TOMATO", 0xFFFF6347);
    /** The color TURQUOISE. */
    Color TURQUOISE = register("TURQUOISE", 0xFF40E0D0);
    /** The color VIOLET. */
    Color VIOLET = register("VIOLET", 0xFFEE82EE);
    /** The color WHEAT. */
    Color WHEAT = register("WHEAT", 0xFFF5DEB3);
    /** The color WHITE. */
    Color WHITE = register("WHITE", 0xFFFFFFFF);
    /** The color WHITESMOKE. */
    Color WHITESMOKE = register("WHITESMOKE", 0xFFF5F5F5);
    /** The color YELLOW. */
    Color YELLOW = register("YELLOW", 0xFFFFFF00);
    /** The color YELLOWGREEN. */
    Color YELLOWGREEN = register("YELLOWGREEN", 0xFF9ACD32);
    /** The color TRANSPARENT. */
    Color TRANSPARENT_WHITE = register("TRANSPARENT_WHITE", 0x00FFFFFF);
    /** The color TRANSPARENT. */
    Color TRANSPARENT_BLACK = register("TRANSPARENT_WHITE", 0x00000000);

    /**
     * Get a mapping from color name to Color instance.
     *
     * @return map containing the predefined colors
     */
    static Map<String, Color> palette() {
        return Collections.unmodifiableMap(Colors.COLORS);
    }

    /**
     * Convert String to Color. Lookup is tried i these steps:
     * <ol>
     * <li>directly look up the String in the map of predefined colors.
     * <li>if first character is '#', interpret s as hex representation of the
     * RGB value
     * <li>if s starts with rgb, s should be something like "rgb(123,210,120)"
     * <li>otherwise an exception is thrown
     * </ol>
     *
     * @param  s
     *           the text
     * @return result of conversion
     */
    static Color valueOf(String s) {
        // try named colors first
        Color color = Colors.COLORS.get(s);
        if (color != null) {
            return color;
        }

        // HEX colors
        if (s.startsWith("#")) {
            String v = s.substring(1);
            int i = Integer.parseUnsignedInt(v, 16);
            boolean hasAlpha = v.length() > 6; // use RGBA if alpha is present, else add opaque alpha
            return new RGBColor(hasAlpha ? rgba2argb(i) : i + 0xff000000);
        }

        // RGB colors. example: "rgb(255, 0, 0)"
        if (s.startsWith("rgb(")) {
            String s1 = s.substring(3).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s1.length() - 1) == ')') {
                String s2 = s1.substring(1, s1.length() - 1);
                String[] parts = s2.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    return new RGBColor(r, g, b);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgb color.");
        }

        // RGBA colors. example: "rgb(255, 0, 0, 0.3)"
        if (s.startsWith("rgba(")) {
            String s1 = s.substring(4).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s1.length() - 1) == ')') {
                String s2 = s1.substring(1, s1.length() - 1);
                String[] parts = s2.split(",");
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    int a = Integer.parseInt(parts[3]);
                    return new RGBColor(r, g, b, a);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgba color.");
        }

        // no luck so far
        throw new IllegalArgumentException("\"" + s + "\" is no valid color.");
    }

    /**
     * Create color from ARGB packed integer value.
     * @param argb the ARGB packed integer value
     * @return the color
     */
    static RGBColor argb(int argb) {
        return new RGBColor(argb);
    }

    /**
     * Create color from RGB values.
     * @param r the red component   [0 .. 255]
     * @param g the green component [0 .. 255]
     * @param b the blue component  [0 .. 255]
     * @return the color
     */
    static RGBColor rgb(int r, int g, int b) {
        return new RGBColor(r, g, b);
    }

    /**
     * Create color from RGB values.
     * @param r the red component   [0 .. 255]
     * @param g the green component [0 .. 255]
     * @param b the blue component  [0 .. 255]
     * @param a the alpha value     [0 .. 255]
     * @return the color
     */
    static RGBColor rgb(int r, int g, int b, int a) {
        return new RGBColor(r, g, b, a);
    }

    /**
     * Create color from RGB values.
     * @param r the red component   [0 .. 1]
     * @param g the green component [0 .. 1]
     * @param b the blue component  [0 .. 1]
     * @return the color
     */
    static RGBColor rgb(float r, float g, float b) {
        return new RGBColor(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
    }

    /**
     * Create color from RGB values.
     * @param r the red component   [0 .. 1]
     * @param g the green component [0 .. 1]
     * @param b the blue component  [0 .. 1]
     * @param alpha the alpha value [0 .. 1]
     * @return the color
     */
    static RGBColor rgb(float r, float g, float b, float alpha) {
        return new RGBColor(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255), Math.round(alpha * 255));
    }

    /**
     * Create color from HSV values.
     * @param h the hue        [0 .. 360]
     * @param s the saturation [0 .. 1]
     * @param v the brightness [0 .. 1]
     * @return the color
     */
    static HSVColor hsv(float h, float s, float v) {
        return new HSVColor(h, s, v, 1);
    }

    /**
     * Create color from HSV values.
     * @param h the hue        [0 .. 360]
     * @param s the saturation [0 .. 1]
     * @param v the brightness [0 .. 1]
     * @param alpha the alpha value     [0 .. 1]
     * @return the color
     */
    static HSVColor hsv(float h, float s, float v, float alpha) {
        return new HSVColor(h, s, v, alpha);
    }

    /**
     * Get Iterable over all declared color values.
     *
     * @return Iterable
     */
    static Iterable<Color> values() {
        return Colors.COLORS.values();
    }

    private static RGBColor register(String name, int code) {
        RGBColor c = new RGBColor(code);
        Colors.COLORS.put(name, c);
        return c;
    }

    /**
     * Get alpha component of color.
     *
     * @return alpha component
     */
    float alpha();

    /**
     * Get alpha component of color.
     *
     * @return alpha component
     */
    int a();

    /**
     * Test if color is opaque.
     *
     * @return true, if the color uses the maximum alpha value
     */
    boolean isOpaque();

    /**
     * Test if color is transparent.
     *
     * @return true, if the color uses an alpha value of 0
     */
    boolean isTransparent();

    /**
     * Get hex-string in ARGB form for color.
     *
     * @return hex-string
     */
    default String toArgb() {
        return String.format(Locale.ROOT, "#%08x", argb());
    }

    /**
     * Get hex-string in RGBA form for color.
     *
     * @return hex-string
     */
    default String toRgba() {
        return String.format(Locale.ROOT, "#%08x", rgba());
    }

    /**
     * Convert to {@link RGBColor}.
     * @return this color as RGBColor instance
     */
    default RGBColor toRGBColor() {
        return RGBColor.valueOf(argb());
    }

    /**
     * Convert to {@link HSVColor}.
     * @return this color as RGBColor instance
     */
    default HSVColor toHSVColor() {
        return HSVColor.valueOf(argb());
    }

    /**
     * Get CSS compatible string representation of color.
     * <p>
     * Opaque colors are represented as 3 component hex strings, i. e. "#ff0000" for red.
     * Colors using transparency are represented as 4 component hex strings in rrggbbaa format.
     *
     * @return this color as hex value (in rgb or rgba representation)
     */
    default String toCss() {
        if (!isOpaque()) {
            return toRgba();
        } else {
            return String.format(Locale.ROOT, "#%06x", argb() & 0x00ffffff);
        }
    }

    /**
     * Get color value.
     *
     * @return this color encoded as an integer value
     */
    int argb();

    /**
     * Get color value.
     *
     * @return this color encoded as an integer value
     */
    default int rgba() {
        return argb2rgba(argb());
    }

    private static int argb2rgba(int v) {
        return (v << 8) + (v >>> 24);
    }

    private static int rgba2argb(int v) {
        return (v << 24) + (v >>> 8);
    }

    /**
     * Get color components.
     *
     * @return byte array of size 4 containing this color's components in argb order
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    default byte[] toByteArray() {
        int argb = argb();

        byte a = (byte) ((argb >> RGBColor.SHIFT_A) & 0xff);
        byte r = (byte) ((argb >> RGBColor.SHIFT_R) & 0xff);
        byte g = (byte) ((argb >> RGBColor.SHIFT_G) & 0xff);
        byte b = (byte) ((argb >> RGBColor.SHIFT_B) & 0xff);

        return new byte[]{a, r, g, b};
    }

    /**
     * Get color components.
     *
     * @return byte array of size 3 containing this color's components in rgb order
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    default byte[] toByteArrayRGB() {
        int argb = argb();

        byte r = (byte) ((argb >> RGBColor.SHIFT_R) & 0xff);
        byte g = (byte) ((argb >> RGBColor.SHIFT_G) & 0xff);
        byte b = (byte) ((argb >> RGBColor.SHIFT_B) & 0xff);

        return new byte[]{r, g, b};
    }

    /**
     * Get a brighter color.
     *
     * @return a brighter version of this color
     */
    Color brighter();

    /**
     * Get a darker color.
     *
     * @return a darker version of this color
     */
    Color darker();
}


/**
 * Utility class that holds the predefined color constants.
 */
@SuppressWarnings("ClassNameDiffersFromFileName")
final class Colors {
    private Colors() {
        // utility class
    }

    static final Map<String, Color> COLORS = new LinkedHashMap<>();
}

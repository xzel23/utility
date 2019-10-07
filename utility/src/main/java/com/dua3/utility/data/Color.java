// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dua3.utility.lang.LangUtil;

/**
 * Color in ARGB format.
 */
public final class Color {
    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    // predefined Color constants
    /** The color ALICEBLUE. */
    public static final Color ALICEBLUE = register("ALICEBLUE", 0xFFF0F8FF);
    /** The color ANTIQUEWHITE. */
    public static final Color ANTIQUEWHITE = register("ANTIQUEWHITE", 0xFFFAEBD7);
    /** The color AQUA. */
    public static final Color AQUA = register("AQUA", 0xFF00FFFF);
    /** The color AQUAMARINE. */
    public static final Color AQUAMARINE = register("AQUAMARINE", 0xFF7FFFD4);
    /** The color AZURE. */
    public static final Color AZURE = register("AZURE", 0xFFF0FFFF);
    /** The color BEIGE. */
    public static final Color BEIGE = register("BEIGE", 0xFFF5F5DC);
    /** The color BISQUE. */
    public static final Color BISQUE = register("BISQUE", 0xFFFFE4C4);
    /** The color BLACK. */
    public static final Color BLACK = register("BLACK", 0xFF000000);
    /** The color BLANCHEDALMOND. */
    public static final Color BLANCHEDALMOND = register("BLANCHEDALMOND", 0xFFFFE4C4);
    /** The color BLUE. */
    public static final Color BLUE = register("BLUE", 0xFF0000FF);
    /** The color BLUEVIOLET. */
    public static final Color BLUEVIOLET = register("BLUEVIOLET", 0xFF8A2BE2);
    /** The color BROWN. */
    public static final Color BROWN = register("BROWN", 0xFFA52A2A);
    /** The color BURLYWOOD. */
    public static final Color BURLYWOOD = register("BURLYWOOD", 0xFFDEB887);
    /** The color CADETBLUE. */
    public static final Color CADETBLUE = register("CADETBLUE", 0xFF5F9EA0);
    /** The color CHARTREUSE. */
    public static final Color CHARTREUSE = register("CHARTREUSE", 0xFF7FFF00);
    /** The color CHOCOLATE. */
    public static final Color CHOCOLATE = register("CHOCOLATE", 0xFFD2691E);
    /** The color CORAL. */
    public static final Color CORAL = register("CORAL", 0xFFFF7F50);
    /** The color CORNFLOWERBLUE. */
    public static final Color CORNFLOWERBLUE = register("CORNFLOWERBLUE", 0xFF6495ED);
    /** The color CORNSILK. */
    public static final Color CORNSILK = register("CORNSILK", 0xFFFFF8DC);
    /** The color CRIMSON. */
    public static final Color CRIMSON = register("CRIMSON", 0xFFDC143C);
    /** The color DARKBLUE. */
    public static final Color DARKBLUE = register("DARKBLUE", 0xFF00008B);
    /** The color DARKCYAN. */
    public static final Color DARKCYAN = register("DARKCYAN", 0xFF008B8B);
    /** The color DARKGOLDENROD. */
    public static final Color DARKGOLDENROD = register("DARKGOLDENROD", 0xFFB8860B);
    /** The color DARKGRAY. */
    public static final Color DARKGRAY = register("DARKGRAY", 0xFFA9A9A9);
    /** The color DARKGREEN. */
    public static final Color DARKGREEN = register("DARKGREEN", 0xFF006400);
    /** The color DARKGREY. */
    public static final Color DARKGREY = register("DARKGREY", 0xFFA9A9A9);
    /** The color DARKKHAKI. */
    public static final Color DARKKHAKI = register("DARKKHAKI", 0xFFBDB76B);
    /** The color DARKMAGENTA. */
    public static final Color DARKMAGENTA = register("DARKMAGENTA", 0xFF8B008B);
    /** The color DARKOLIVEGREEN. */
    public static final Color DARKOLIVEGREEN = register("DARKOLIVEGREEN", 0xFF556B2F);
    /** The color DARKORANGE. */
    public static final Color DARKORANGE = register("DARKORANGE", 0xFFFF8C00);
    /** The color DARKORCHID. */
    public static final Color DARKORCHID = register("DARKORCHID", 0xFF9932CC);
    /** The color DARKRED. */
    public static final Color DARKRED = register("DARKRED", 0xFF8B0000);
    /** The color DARKSALMON. */
    public static final Color DARKSALMON = register("DARKSALMON", 0xFFE9967A);
    /** The color DARKSEAGREEN. */
    public static final Color DARKSEAGREEN = register("DARKSEAGREEN", 0xFF8FBC8F);
    /** The color DARKSLATEBLUE. */
    public static final Color DARKSLATEBLUE = register("DARKSLATEBLUE", 0xFF483D8B);
    /** The color DARKSLATEGRAY. */
    public static final Color DARKSLATEGRAY = register("DARKSLATEGRAY", 0xFF2F4F4F);
    /** The color DARKSLATEGREY. */
    public static final Color DARKSLATEGREY = register("DARKSLATEGREY", 0xFF2F4F4F);
    /** The color DARKTURQUOISE. */
    public static final Color DARKTURQUOISE = register("DARKTURQUOISE", 0xFF00CED1);
    /** The color DARKVIOLET. */
    public static final Color DARKVIOLET = register("DARKVIOLET", 0xFF9400D3);
    /** The color DEEPPINK. */
    public static final Color DEEPPINK = register("DEEPPINK", 0xFFFF1493);
    /** The color DEEPSKYBLUE. */
    public static final Color DEEPSKYBLUE = register("DEEPSKYBLUE", 0xFF00BFFF);
    /** The color DIMGRAY. */
    public static final Color DIMGRAY = register("DIMGRAY", 0xFF696969);
    /** The color DIMGREY. */
    public static final Color DIMGREY = register("DIMGREY", 0xFF696969);
    /** The color DODGERBLUE. */
    public static final Color DODGERBLUE = register("DODGERBLUE", 0xFF1E90FF);
    /** The color FIREBRICK. */
    public static final Color FIREBRICK = register("FIREBRICK", 0xFFB22222);
    /** The color FLORALWHITE. */
    public static final Color FLORALWHITE = register("FLORALWHITE", 0xFFFFFAF0);
    /** The color FORESTGREEN. */
    public static final Color FORESTGREEN = register("FORESTGREEN", 0xFF228B22);
    /** The color FUCHSIA. */
    public static final Color FUCHSIA = register("FUCHSIA", 0xFFFF00FF);
    /** The color GAINSBORO. */
    public static final Color GAINSBORO = register("GAINSBORO", 0xFFDCDCDC);
    /** The color GHOSTWHITE. */
    public static final Color GHOSTWHITE = register("GHOSTWHITE", 0xFFF8F8FF);
    /** The color GOLD. */
    public static final Color GOLD = register("GOLD", 0xFFFFD700);
    /** The color GOLDENROD. */
    public static final Color GOLDENROD = register("GOLDENROD", 0xFFDAA520);
    /** The color GRAY. */
    public static final Color GRAY = register("GRAY", 0xFF808080);
    /** The color GREEN. */
    public static final Color GREEN = register("GREEN", 0xFF008000);
    /** The color GREENYELLOW. */
    public static final Color GREENYELLOW = register("GREENYELLOW", 0xFFADFF2F);
    /** The color GREY. */
    public static final Color GREY = register("GREY", 0xFF808080);
    /** The color HONEYDEW. */
    public static final Color HONEYDEW = register("HONEYDEW", 0xFFF0FFF0);
    /** The color HOTPINK. */
    public static final Color HOTPINK = register("HOTPINK", 0xFFFF69B4);
    /** The color INDIANRED. */
    public static final Color INDIANRED = register("INDIANRED", 0xFFCD5C5C);
    /** The color INDIGO. */
    public static final Color INDIGO = register("INDIGO", 0xFF4B0082);
    /** The color IVORY. */
    public static final Color IVORY = register("IVORY", 0xFFFFFFF0);
    /** The color KHAKI. */
    public static final Color KHAKI = register("KHAKI", 0xFFF0E68C);
    /** The color LAVENDER. */
    public static final Color LAVENDER = register("LAVENDER", 0xFFE6E6FA);
    /** The color LAVENDERBLUSH. */
    public static final Color LAVENDERBLUSH = register("LAVENDERBLUSH", 0xFFFFF0F5);
    /** The color LAWNGREEN. */
    public static final Color LAWNGREEN = register("LAWNGREEN", 0xFF7CFC00);
    /** The color LEMONCHIFFON. */
    public static final Color LEMONCHIFFON = register("LEMONCHIFFON", 0xFFFFFACD);
    /** The color LIGHTBLUE. */
    public static final Color LIGHTBLUE = register("LIGHTBLUE", 0xFFADD8E6);
    /** The color LIGHTCORAL. */
    public static final Color LIGHTCORAL = register("LIGHTCORAL", 0xFFF08080);
    /** The color LIGHTCYAN. */
    public static final Color LIGHTCYAN = register("LIGHTCYAN", 0xFFE0FFFF);
    /** The color LIGHTGOLDENRODYELLOW. */
    public static final Color LIGHTGOLDENRODYELLOW = register("LIGHTGOLDENRODYELLOW", 0xFFFAFAD2);
    /** The color LIGHTGRAY. */
    public static final Color LIGHTGRAY = register("LIGHTGRAY", 0xFFD3D3D3);
    /** The color LIGHTGREEN. */
    public static final Color LIGHTGREEN = register("LIGHTGREEN", 0xFF90EE90);
    /** The color LIGHTGREY. */
    public static final Color LIGHTGREY = register("LIGHTGREY", 0xFFD3D3D3);
    /** The color LIGHTPINK. */
    public static final Color LIGHTPINK = register("LIGHTPINK", 0xFFFFB6C1);
    /** The color LIGHTSALMON. */
    public static final Color LIGHTSALMON = register("LIGHTSALMON", 0xFFFFA07A);
    /** The color LIGHTSEAGREEN. */
    public static final Color LIGHTSEAGREEN = register("LIGHTSEAGREEN", 0xFF20B2AA);
    /** The color LIGHTSKYBLUE. */
    public static final Color LIGHTSKYBLUE = register("LIGHTSKYBLUE", 0xFF87CEFA);
    /** The color LIGHTSLATEGRAY. */
    public static final Color LIGHTSLATEGRAY = register("LIGHTSLATEGRAY", 0xFF778899);
    /** The color LIGHTSLATEGREY. */
    public static final Color LIGHTSLATEGREY = register("LIGHTSLATEGREY", 0xFF778899);
    /** The color LIGHTSTEELBLUE. */
    public static final Color LIGHTSTEELBLUE = register("LIGHTSTEELBLUE", 0xFFB0C4DE);
    /** The color LIGHTYELLOW. */
    public static final Color LIGHTYELLOW = register("LIGHTYELLOW", 0xFFFFFFE0);
    /** The color LIME. */
    public static final Color LIME = register("LIME", 0xFF00FF00);
    /** The color LIMEGREEN. */
    public static final Color LIMEGREEN = register("LIMEGREEN", 0xFF32CD32);
    /** The color LINEN. */
    public static final Color LINEN = register("LINEN", 0xFFFAF0E6);
    /** The color MAROON. */
    public static final Color MAROON = register("MAROON", 0xFF800000);
    /** The color MEDIUMAQUAMARINE. */
    public static final Color MEDIUMAQUAMARINE = register("MEDIUMAQUAMARINE", 0xFF66CDAA);
    /** The color MEDIUMBLUE. */
    public static final Color MEDIUMBLUE = register("MEDIUMBLUE", 0xFF0000CD);
    /** The color MEDIUMORCHID. */
    public static final Color MEDIUMORCHID = register("MEDIUMORCHID", 0xFFBA55D3);
    /** The color MEDIUMPURPLE. */
    public static final Color MEDIUMPURPLE = register("MEDIUMPURPLE", 0xFF9370DB);
    /** The color MEDIUMSEAGREEN. */
    public static final Color MEDIUMSEAGREEN = register("MEDIUMSEAGREEN", 0xFF3CB371);
    /** The color MEDIUMSLATEBLUE. */
    public static final Color MEDIUMSLATEBLUE = register("MEDIUMSLATEBLUE", 0xFF7B68EE);
    /** The color MEDIUMSPRINGGREEN. */
    public static final Color MEDIUMSPRINGGREEN = register("MEDIUMSPRINGGREEN", 0xFF00FA9A);
    /** The color MEDIUMTURQUOISE. */
    public static final Color MEDIUMTURQUOISE = register("MEDIUMTURQUOISE", 0xFF48D1CC);
    /** The color MEDIUMVIOLETRED. */
    public static final Color MEDIUMVIOLETRED = register("MEDIUMVIOLETRED", 0xFFC71585);
    /** The color MIDNIGHTBLUE. */
    public static final Color MIDNIGHTBLUE = register("MIDNIGHTBLUE", 0xFF191970);
    /** The color MINTCREAM. */
    public static final Color MINTCREAM = register("MINTCREAM", 0xFFF5FFFA);
    /** The color MISTYROSE. */
    public static final Color MISTYROSE = register("MISTYROSE", 0xFFFFE4E1);
    /** The color MOCCASIN. */
    public static final Color MOCCASIN = register("MOCCASIN", 0xFFFFE4B5);
    /** The color NAVAJOWHITE. */
    public static final Color NAVAJOWHITE = register("NAVAJOWHITE", 0xFFFFDEAD);
    /** The color NAVY. */
    public static final Color NAVY = register("NAVY", 0xFF000080);
    /** The color OLDLACE. */
    public static final Color OLDLACE = register("OLDLACE", 0xFFFDF5E6);
    /** The color OLIVE. */
    public static final Color OLIVE = register("OLIVE", 0xFF808000);
    /** The color OLIVEDRAB. */
    public static final Color OLIVEDRAB = register("OLIVEDRAB", 0xFF6B8E23);
    /** The color ORANGE. */
    public static final Color ORANGE = register("ORANGE", 0xFFFFA500);
    /** The color ORANGERED. */
    public static final Color ORANGERED = register("ORANGERED", 0xFFFF4500);
    /** The color ORCHID. */
    public static final Color ORCHID = register("ORCHID", 0xFFDA70D6);
    /** The color PALEGOLDENROD. */
    public static final Color PALEGOLDENROD = register("PALEGOLDENROD", 0xFFEEE8AA);
    /** The color PALEGREEN. */
    public static final Color PALEGREEN = register("PALEGREEN", 0xFF98FB98);
    /** The color PALETURQUOISE. */
    public static final Color PALETURQUOISE = register("PALETURQUOISE", 0xFFAFEEEE);
    /** The color PALEVIOLETRED. */
    public static final Color PALEVIOLETRED = register("PALEVIOLETRED", 0xFFDB7093);
    /** The color PAPAYAWHIP. */
    public static final Color PAPAYAWHIP = register("PAPAYAWHIP", 0xFFFFEFD5);
    /** The color PEACHPUFF. */
    public static final Color PEACHPUFF = register("PEACHPUFF", 0xFFFFDAB9);
    /** The color PERU. */
    public static final Color PERU = register("PERU", 0xFFCD853F);
    /** The color PINK. */
    public static final Color PINK = register("PINK", 0xFFFFC0CB);
    /** The color PLUM. */
    public static final Color PLUM = register("PLUM", 0xFFDDA0DD);
    /** The color POWDERBLUE. */
    public static final Color POWDERBLUE = register("POWDERBLUE", 0xFFB0E0E6);
    /** The color PURPLE. */
    public static final Color PURPLE = register("PURPLE", 0xFF800080);
    /** The color REBECCAPURPLE. */
    public static final Color REBECCAPURPLE = register("REBECCAPURPLE", 0xFF663399);
    /** The color RED. */
    public static final Color RED = register("RED", 0xFFFF0000);
    /** The color ROSYBROWN. */
    public static final Color ROSYBROWN = register("ROSYBROWN", 0xFFBC8F8F);
    /** The color ROYALBLUE. */
    public static final Color ROYALBLUE = register("ROYALBLUE", 0xFF4169E1);
    /** The color SADDLEBROWN. */
    public static final Color SADDLEBROWN = register("SADDLEBROWN", 0xFF8B4513);
    /** The color SALMON. */
    public static final Color SALMON = register("SALMON", 0xFFFA8072);
    /** The color SANDYBROWN. */
    public static final Color SANDYBROWN = register("SANDYBROWN", 0xFFF4A460);
    /** The color SEAGREEN. */
    public static final Color SEAGREEN = register("SEAGREEN", 0xFF2E8B57);
    /** The color SEASHELL. */
    public static final Color SEASHELL = register("SEASHELL", 0xFFFFF5EE);
    /** The color SIENNA. */
    public static final Color SIENNA = register("SIENNA", 0xFFA0522D);
    /** The color SILVER. */
    public static final Color SILVER = register("SILVER", 0xFFC0C0C0);
    /** The color SKYBLUE. */
    public static final Color SKYBLUE = register("SKYBLUE", 0xFF87CEEB);
    /** The color SLATEBLUE. */
    public static final Color SLATEBLUE = register("SLATEBLUE", 0xFF6A5ACD);
    /** The color SLATEGRAY. */
    public static final Color SLATEGRAY = register("SLATEGRAY", 0xFF708090);
    /** The color SLATEGREY. */
    public static final Color SLATEGREY = register("SLATEGREY", 0xFF708090);
    /** The color SNOW. */
    public static final Color SNOW = register("SNOW", 0xFFFFFAFA);
    /** The color SPRINGGREEN. */
    public static final Color SPRINGGREEN = register("SPRINGGREEN", 0xFF00FF7F);
    /** The color STEELBLUE. */
    public static final Color STEELBLUE = register("STEELBLUE", 0xFF4682B4);
    /** The color TAN. */
    public static final Color TAN = register("TAN", 0xFFD2B48C);
    /** The color TEAL. */
    public static final Color TEAL = register("TEAL", 0xFF008080);
    /** The color THISTLE. */
    public static final Color THISTLE = register("THISTLE", 0xFFD8BFD8);
    /** The color TOMATO. */
    public static final Color TOMATO = register("TOMATO", 0xFFFF6347);
    /** The color TURQUOISE. */
    public static final Color TURQUOISE = register("TURQUOISE", 0xFF40E0D0);
    /** The color VIOLET. */
    public static final Color VIOLET = register("VIOLET", 0xFFEE82EE);
    /** The color WHEAT. */
    public static final Color WHEAT = register("WHEAT", 0xFFF5DEB3);
    /** The color WHITE. */
    public static final Color WHITE = register("WHITE", 0xFFFFFFFF);
    /** The color WHITESMOKE. */
    public static final Color WHITESMOKE = register("WHITESMOKE", 0xFFF5F5F5);
    /** The color YELLOW. */
    public static final Color YELLOW = register("YELLOW", 0xFFFFFF00);
    /** The color YELLOWGREEN. */
    public static final Color YELLOWGREEN = register("YELLOWGREEN", 0xFF9ACD32);
    /** The color TRANSPARENT. */
    public static final Color TRANSPARENT_WHITE = register("TRANSPARENT_WHITE", 0x00FFFFFF);
    /** The color TRANSPARENT. */
    public static final Color TRANSPARENT_BLACK = register("TRANSPARENT_WHITE", 0x00000000);

    private static final double F_BRIGHTEN = 0.7;

    private static final int SHIFT_A = 24;

    private static final int SHIFT_B = 0;

    private static final int SHIFT_G = 8;

    private static final int SHIFT_R = 16;

    /**
     * Get a mapping from color name to Color instance.
     *
     * @return map containing the predefined colors
     */
    public static Map<String, Color> palette() {
        return Collections.unmodifiableMap(COLORS);
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
     * @return   result of conversion
     */
    public static Color valueOf(String s) {
        // try named colors first
        Color color = COLORS.get(s);
        if (color != null) {
            return color;
        }

        // HEX colors
        if (s.startsWith("#")) {
            int i = Integer.parseUnsignedInt(s.substring(1), 16);
            return new Color(i);
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
                    return new Color(r, g, b);
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
                    return new Color(r, g, b, a);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgba color.");
        }

        // no luck so far
        throw new IllegalArgumentException("\"" + s + "\" is no valid color.");
    }

    /**
     * Get Iterable over all declared color values.
     *
     * @return Iterable
     */
    public static Iterable<Color> values() {
        return COLORS.values();
    }

    private static Color register(String name, int code) {
        Color c = new Color(code);
        COLORS.put(name, c);
        return c;
    }

    private static int shiftComponentValue(int value, int bits) {
        LangUtil.check(value >= 0 && value <= 255, () -> new IllegalArgumentException("value out of range: "+value));
        return value << bits;
    }

    private final int argb;

    /**
     * Create a new Color.
     *
     * @param r
     *          red (0..255)
     * @param g
     *          green (0..255)
     * @param b
     *          blue (0..255)
     */
    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Create a new Color.
     *
     * @param r
     *          red (0..255)
     * @param g
     *          green (0..255)
     * @param b
     *          blue (0..255)
     * @param a
     *          alpha (0..255)
     */
    public Color(int r, int g, int b, int a) {
        argb = shiftComponentValue(a, SHIFT_A) + shiftComponentValue(r, SHIFT_R) + shiftComponentValue(g, SHIFT_G)
                + shiftComponentValue(b, SHIFT_B);
    }

    private Color(int argb) {
        this.argb = argb;
    }

    /**
     * Get alpha component of color.
     *
     * @return alpha component
     */
    public int a() {
        return (argb >> SHIFT_A) & 0xff;
    }

    /**
     * Get alpha component of color.
     *
     * @return alpha component as float (between 0.0 and 1.0)
     */
    public float af() {
        return a() / 255.0f;
    }

    /**
     * Get color value.
     *
     * @return this color encoded as an integer value
     */
    public int argb() {
        return argb;
    }

    /**
     * Get blue component of color.
     *
     * @return blue component
     */
    public int b() {
        return (argb >> SHIFT_B) & 0xff;
    }

    /**
     * Get blue component of color.
     *
     * @return blue component as float (between 0.0 and 1.0)
     */
    public float bf() {
        return b() / 255.0f;
    }

    /**
     * Get a brighter color.
     *
     * @return a brighter version of this color
     */
    public Color brighter() {
        int r = r();
        int g = g();
        int b = b();
        int alpha = a();

        int i = (int) (1.0 / (1.0 - F_BRIGHTEN));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }

        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }

        //noinspection NumericCastThatLosesPrecision
        return new Color(Math.min((int) (r / F_BRIGHTEN), 255), Math.min((int) (g / F_BRIGHTEN), 255),
                Math.min((int) (b / F_BRIGHTEN), 255), alpha);
    }

    /**
     * Get a darker color.
     *
     * @return a darker version of this color
     */
    public Color darker() {
        //noinspection NumericCastThatLosesPrecision
        return new Color(Math.max((int) (r() * F_BRIGHTEN), 0), Math.max((int) (g() * F_BRIGHTEN), 0),
                Math.max((int) (b() * F_BRIGHTEN), 0), a());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && ((Color) obj).argb == argb;
    }

    /**
     * Get green component of color.
     *
     * @return green component
     */
    public int g() {
        return (argb >> SHIFT_G) & 0xff;
    }

    /**
     * Get green component of color.
     *
     * @return green component as float (between 0.0 and 1.0)
     */
    public float gf() {
        return g() / 255.0f;
    }

    @Override
    public int hashCode() {
        return argb;
    }

    /**
     * Get red component of color.
     *
     * @return red component
     */
    public int r() {
        return (argb >> SHIFT_R) & 0xff;
    }

    /**
     * Get red component of color.
     *
     * @return red component as float (between 0.0 and 1.0)
     */
    public float rf() {
        return r() / 255.0f;
    }

    /**
     * Get color components.
     *
     * @return byte array of size 4 containing this color's components in argb
     *         order
     */
    public byte[] toByteArray() {
        //noinspection NumericCastThatLosesPrecision
        return new byte[] { (byte) a(), (byte) r(), (byte) g(), (byte) b() };
    }

    /**
     * Get string representation of color.
     *
     * @return this color as hex value (format #aarrggbb)
     */
    @Override
    public String toString() {
        return String.format("#%08x", argb);
    }

}

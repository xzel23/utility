package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A mutable class holding font attributes to help create immutable font
 * instances.
 */
@SuppressWarnings({"NonFinalFieldReferenceInEquals", "NonFinalFieldReferencedInHashCode", "MagicCharacter"})
public final class FontDef implements Cloneable {

    private static final Logger LOG = LogManager.getLogger(FontDef.class);

    private static final Predicate<String> IS_FONT_SIZE = Pattern.compile("\\d+(\\.\\d*)?").asMatchPredicate();

    private @Nullable Color color;
    private @Nullable Float size;
    private @Nullable List<String> families;
    private @Nullable Boolean bold;
    private @Nullable Boolean italic;
    private @Nullable Boolean underline;
    private @Nullable Boolean strikeThrough;
    private @Nullable FontType type;

    /**
     * Default constructor that creates a {FontDe} instance without any properties set.
     */
    public FontDef() {
        // nop - everything being initialized to null is just fine
    }

    /**
     * Create FontDef instance with only the color attribute set.
     *
     * @param col the color
     * @return new FontDef instance
     */
    public static FontDef color(@Nullable Color col) {
        FontDef fd = new FontDef();
        fd.setColor(col);
        return fd;
    }

    /**
     * Create a {@code FontDef} instance with only the font family attribute set.
     *
     * @param families the font family
     * @return new FontDef instance
     */
    public static FontDef families(@Nullable List<String> families) {
        FontDef fd = new FontDef();
        fd.setFamilies(families == null || families.isEmpty() ? null : List.copyOf(families));
        return fd;
    }

    /**
     * Create FontDef instance with only the font family attribute set.
     *
     * @param family the font family
     * @return new FontDef instance
     */
    public static FontDef family(@Nullable String family) {
        return families(parseFontFamilies(family, false));
    }

    /**
     * Create a FontDef instance with only the font size set.
     *
     * @param size the font size in points
     * @return new FontDef instance
     */
    public static FontDef size(@Nullable Float size) {
        FontDef fd = new FontDef();
        fd.setSize(size);
        return fd;
    }

    /**
     * Create FontDef instance with only the bold field set.
     *
     * @param flag the value to set
     * @return new FontDef instance
     */
    public static FontDef bold(boolean flag) {
        FontDef fd = new FontDef();
        fd.setBold(flag);
        return fd;
    }

    /**
     * Create FontDef instance with only the italic field set.
     *
     * @param flag the value to set
     * @return new FontDef instance
     */
    public static FontDef italic(boolean flag) {
        FontDef fd = new FontDef();
        fd.setItalic(flag);
        return fd;
    }

    /**
     * Create FontDef instance with only the underline field set.
     *
     * @param flag the value to set
     * @return new FontDef instance
     */
    public static FontDef underline(boolean flag) {
        FontDef fd = new FontDef();
        fd.setUnderline(flag);
        return fd;
    }

    /**
     * Create FontDef instance with only the strikethrough field set.
     *
     * @param flag the value to set
     * @return new FontDef instance
     */
    public static FontDef strikeThrough(boolean flag) {
        FontDef fd = new FontDef();
        fd.setStrikeThrough(flag);
        return fd;
    }

    /**
     * Parse fontspec.
     *
     * @param fontspec the fontspec
     * @return FontDef instance matching fontspec
     * @throws IllegalArgumentException if the ont definition could not be parsed
     * @throws NumberFormatException    if a numeric value could not be parsed
     */
    public static FontDef parseFontspec(String fontspec) {
        String[] parts = fontspec.split("-");

        FontDef fd = new FontDef();

        // font-family must come first
        fd.setFamily(parts[0]);

        // check remaining parts
        for (int i = 1; i < parts.length; i++) {
            String s = parts[i];
            // check for text-decoration
            switch (s) {
                case "bold" -> fd.setBold(true);
                case "regular" -> fd.setBold(false);
                case "italic" -> fd.setItalic(true);
                case "normal" -> fd.setItalic(false);
                case "underline" -> fd.setUnderline(true);
                case "none" -> fd.setUnderline(false);
                case "strikethrough" -> fd.setStrikeThrough(true);
                case "noline" -> fd.setStrikeThrough(false);
                default -> {
                    // check for font size
                    if (IS_FONT_SIZE.test(s)) {
                        fd.setSize(Float.parseFloat(s));
                        break;
                    }

                    // check for color
                    fd.setColor(Color.valueOf(s));
                }
            }
        }

        return fd;
    }

    /**
     * Parse fontspec.
     *
     * @param fontdef the CSS font definition
     * @return FontDef instance matching fontspec
     */
    public static FontDef parseCssFontDef(String fontdef) {
        fontdef = fontdef.strip();

        if (fontdef.startsWith("{") && fontdef.endsWith("}")) {
            fontdef = fontdef.substring(1, fontdef.length() - 1);
        }

        FontDef fd = new FontDef();

        for (String rule : fontdef.split(";")) {
            if (rule.isBlank()) {
                continue;
            }

            Pair<String, String> pair = parseCssRule(rule);

            String attribute = pair.first().toLowerCase(Locale.ROOT);
            String value = pair.second().strip();

            switch (attribute) {
                case "color" -> fd.setColor(parseColor(value));
                case "font-size" -> fd.setSize(parseFontSize(value));
                case "font-family" -> fd.setFamilies(parseFontFamilies(value, true));
                case "font-weight" -> fd.setBold(parseFontWeight(value));
                case "font-style" -> fd.setItalic(parseFontStyle(value));
                default -> LOG.warn("unknown font attribute: {}", attribute);
            }
        }

        return fd;
    }

    private static @Nullable Color parseColor(String value) {
        return value.equalsIgnoreCase("inherit") ? null : Color.valueOf(value);
    }

    private static @Nullable Boolean parseFontWeight(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "bold" -> Boolean.TRUE;
            case "normal" -> Boolean.FALSE;
            case "inherit" -> null;
            default -> throw new IllegalArgumentException("invalid value for font-weight: " + value);
        };
    }

    private static @Nullable Boolean parseFontStyle(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "italic", "oblique" -> Boolean.TRUE;
            case "normal" -> Boolean.FALSE;
            case "inherit" -> null;
            default -> throw new IllegalArgumentException("invalid value for font-style: " + value);
        };
    }

    private static @Nullable Float parseFontSize(String sz) {
        sz = sz.strip().toLowerCase(Locale.ROOT);

        if (sz.equals("inherit")) {
            return null;
        }

        return InternalUtil.decodeFontSize(sz);
    }

    static @Nullable List<String> parseFontFamilies(@Nullable String s, boolean strict) {
        if (s == null || s.isBlank()) {
            return null;
        }

        if (!s.contains(",") && !s.contains("\"")) {
            // allow spaces in non-strict mode only
            String family = s.strip();
            LangUtil.check(!strict || s.indexOf(' ') < 0, () -> new IllegalArgumentException("invalid font declaration: " + s));
            return s.equals("inherit") ? null : List.of(s);
        }

        int idx = 0;
        int end = s.length();

        List<String> families = new ArrayList<>();
        while (idx < end) {
            // skip whitespace
            while (idx < end && Character.isWhitespace(s.charAt(idx))) {
                idx++;
            }

            int partStart = idx;
            if (partStart < end) {
                int partEnd;
                char c = s.charAt(idx);
                String part;
                if (c == '"') {
                    // read quoted family name

                    // move after quote
                    idx++;
                    partStart++;
                    while (idx < end && (c = s.charAt(idx)) != '"') {
                        idx++;
                    }
                    if (idx == end) {
                        throw new IllegalArgumentException("unmatched quote in argument: " + s);
                    }

                    // get current family name
                    part = s.substring(partStart, idx);

                    // move to character after quote
                    if (idx < end) {
                        c = s.charAt(idx++);
                    }
                } else {
                    // read unquoted family name
                    while (idx < end && !Character.isWhitespace(c = s.charAt(idx)) && c != ',' && c != ';') {
                        idx++;
                    }
                    part = s.substring(partStart, idx);
                }
                families.add(part);

                // skip whitespace
                while (idx < end && Character.isWhitespace(s.charAt(idx))) {
                    idx++;
                }

                // read comma
                if (idx < end) {
                    if (s.charAt(idx) == ',') {
                        idx++;
                    } else {
                        throw new IllegalArgumentException("invalid font declaration: " + s);
                    }
                }
            }
        }

        // "inherit" is treated the same as not present and it must not be combined
        int idxInherit = families.indexOf("inherit");
        if (idxInherit >= 0) {
            LangUtil.check(families.size() == 1, () -> new IllegalArgumentException("'inherit' must not be combined: " + s));
            return null;
        }

        return families.isEmpty() ? null : families;
    }

    private static Pair<String, String> parseCssRule(String rule) {
        int splitIdx = rule.indexOf(':');

        LangUtil.check(splitIdx > 0, () -> new IllegalArgumentException("invalid CSS rule: " + rule));

        String attribute = rule.substring(0, splitIdx).strip();
        String value = rule.substring(splitIdx + 1).strip();

        return Pair.of(attribute, value);
    }

    private static boolean nullOrEquals(@Nullable Object a, @Nullable Object b) {
        return a == null || b == null || a.equals(b);
    }

    // a little helper for the consumeIfDefined... methods
    private static <T> boolean consumeIfDefined(@Nullable T v, Consumer<T> c) {
        boolean run = v != null;
        if (run) {
            c.accept(v);
        }
        return run;
    }

    /**
     * Get fontspec for this FontDef instance.
     * <p>
     * The fontspec returned by this method will always return a fontspec containing all attributes. An asterisk ('*')
     * is used for attributes whose values are not defined in the FontDef instance so that it is possible to reparse a
     * fontspec into a FontDef that will be equal to the original one.
     *
     * @return String representation of this instance
     */
    public String fontspec() {
        return Objects.requireNonNullElse(getFamily(), "*") +
                LangUtil.triStateSelect(bold, "-bold", "-regular", "-*") +
                LangUtil.triStateSelect(italic, "-italic", "-normal", "-*") +
                LangUtil.triStateSelect(underline, "-underline", "-none", "-*") +
                LangUtil.triStateSelect(strikeThrough, "-strikethrough", "-no_line", "-*") +
                '-' + (size != null ? size : "*") +
                '-' + (color != null ? color.toCss() : "*");
    }

    /**
     * Retrieves the bold property of the FontDef instance.
     *
     * @return a Boolean indicating whether the bold attribute is set.
     *         It returns {@code true} if bold is enabled, {@code false} if disabled,
     *         or {@code null} if the bold attribute is not defined.
     */
    public @Nullable Boolean getBold() {
        return bold;
    }

    /**
     * Sets the bold property of the font definition.
     *
     * @param bold the value to set for the bold property. It can be true, false, or null to represent
     *             bold, non-bold, or unspecified respectively.
     */
    public void setBold(@Nullable Boolean bold) {
        this.bold = bold;
    }

    /**
     * Retrieves the color attribute associated with this instance.
     *
     * @return the color attribute, or null if no color is defined
     */
    public @Nullable Color getColor() {
        return color;
    }

    /**
     * Sets the color attribute of the FontDef instance.
     *
     * @param color the color to set, which can be null
     */
    public void setColor(@Nullable Color color) {
        this.color = color;
    }

    /**
     * Retrieves the font family value of this instance.
     *
     * @return the font family as a string, or null if no family is defined
     */
    public @Nullable List<String> getFamilies() {
        return families;
    }

    /**
     * Retrieves the font family value of this instance.
     *
     * @return the font family as a string, or null if no family is defined
     */
    public @Nullable String getFamily() {
        return families == null ? null : families.getFirst();
    }

    /**
     * Sets the font family names for the FontDef instance.
     *
     * @param families the font family names to set. Can be null if no font family is specified.
     */
    public void setFamilies(@Nullable SequencedCollection<String> families) {
        this.families = families == null || families.isEmpty() ? null : List.copyOf(families);
    }

    /**
     * Sets the font family names for the FontDef instance.
     *
     * @param family the font family names to set. Can be null if no font family is specified.
     */
    public void setFamily(@Nullable String family) {
        this.families = family == null ? null : parseFontFamilies(family, false);
    }

    /**
     * Retrieves the italic property of this FontDef instance.
     *
     * @return a Boolean indicating whether the italic property is set,
     *         or null if the italic property is undefined.
     */
    public @Nullable Boolean getItalic() {
        return italic;
    }

    /**
     * Sets the italic property of the font definition.
     *
     * @param italic the value to set for the italic property. It can be null to indicate that the property is undefined.
     */
    public void setItalic(@Nullable Boolean italic) {
        this.italic = italic;
    }

    /**
     * Retrieves the size attribute of this FontDef instance.
     *
     * @return the size of the font as a Float, or null if the size is not set
     */
    public @Nullable Float getSize() {
        return size;
    }

    /**
     * Sets the size of the font.
     *
     * @param size the font size in points. Can be null if the size is not defined.
     */
    public void setSize(@Nullable Float size) {
        this.size = size;
    }

    /**
     * Retrieves the value of the strike-through property of the font definition.
     *
     * @return the strike-through state, or null if it is not defined
     */
    public @Nullable Boolean getStrikeThrough() {
        return strikeThrough;
    }

    /**
     * Sets the strike-through property for the font.
     *
     * @param strikeThrough a Boolean value indicating whether the font
     *                       should have a strike-through effect.
     *                       If {@code null}, the strike-through property
     *                       will not be explicitly set.
     */
    public void setStrikeThrough(@Nullable Boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
    }

    /**
     * Retrieves the current value of the underline property for this FontDef instance.
     *
     * @return the underline property, which may be true, false, or null if not explicitly set
     */
    public @Nullable Boolean getUnderline() {
        return underline;
    }

    /**
     * Sets the underline attribute for the font.
     *
     * @param underline the underline attribute to set, or null to unset it
     */
    public void setUnderline(@Nullable Boolean underline) {
        this.underline = underline;
    }

    /**
     * Retrieves the current value of the type property for this FontDef instance.
     *
     * @return the type property
     */
    public @Nullable FontType getType() {
        return type;
    }

    /**
     * Sets the type attribute for the font.
     *
     * @param type the type attribute to set, or null to unset it
     */
    public void setType(@Nullable FontType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        //noinspection EqualsCalledOnEnumConstant
        return obj == this
                || (obj instanceof FontDef other)
                && Objects.equals(size, other.size)
                && Objects.equals(type, other.type)
                && Objects.equals(families, other.families)
                && Objects.equals(color, other.color)
                && Objects.equals(bold, other.bold)
                && Objects.equals(italic, other.italic)
                && Objects.equals(underline, other.underline)
                && Objects.equals(strikeThrough, other.strikeThrough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, size, type, families, bold, italic, underline, strikeThrough);
    }

    @Override
    public String toString() {
        return "FontDef{" +
                getCssStyle() +
                "}";
    }

    /**
     * Get CSS compatible fontstyle definition.
     *
     * @return fontstyle definition
     */
    public String getCssStyle() {
        boolean isUnderline = underline != null && underline;
        boolean isStrikeThrough = strikeThrough != null && strikeThrough;
        //noinspection StringConcatenationMissingWhitespace
        String css =
                (families == null ? "" : "font-family: " + families.stream()
                        .map(InternalUtil::quoteIfNeeded)
                        .collect(Collectors.joining(", ")) + "; ") +
                        (size == null ? "" : "font-size: " + size + "pt; ") +
                        (bold == null ? "" : "font-weight: " + (bold ? "bold" : "normal") + "; ") +
                        (italic == null ? "" : "font-style: " + (italic ? "italic" : "normal") + "; ") +
                        (isStrikeThrough || isUnderline
                                ? "text-decoration:" +
                                (isUnderline ? " underline" : "") +
                                (isStrikeThrough ? " line-through" : "") +
                                "; "
                                : "") +
                        (color == null ? "" : "color: " + color + ";");
        return css.stripTrailing();
    }

    /**
     * Test if all attributes defined by this instance match those of the given {@link Font}.
     *
     * @param font the {@link Font} to test
     * @return true, if the font's attributes match all attributes defined by this instance
     */
    public boolean matches(Font font) {
        return nullOrEquals(color, font.getColor())
                && nullOrEquals(size, font.getSizeInPoints())
                && nullOrEquals(getFamily(), font.getFamily())
                && nullOrEquals(bold, font.isBold())
                && nullOrEquals(italic, font.isItalic())
                && nullOrEquals(underline, font.isUnderline())
                && nullOrEquals(strikeThrough, font.isStrikeThrough()
                && nullOrEquals(type, font.getType())
        );
    }

    /**
     * Update this FontDef with the non-null values of another FontDef instance.
     *
     * @param delta the FontDef containing the values to apply
     */
    public void merge(FontDef delta) {
        if (delta.color != null) this.color = delta.color;
        if (delta.size != null) this.size = delta.size;
        if (delta.type != null) this.type = delta.type;
        if (delta.families != null) this.families = delta.families;
        if (delta.bold != null) this.bold = delta.bold;
        if (delta.italic != null) this.italic = delta.italic;
        if (delta.underline != null) this.underline = delta.underline;
        if (delta.strikeThrough != null) this.strikeThrough = delta.strikeThrough;
    }

    /**
     * Run action if a value for the color property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifColorDefined(Consumer<? super Color> c) {
        consumeIfDefined(color, c);
    }

    /**
     * Run action if a value for the size property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifSizeDefined(Consumer<? super Float> c) {
        return consumeIfDefined(size, c);
    }

    /**
     * Run action if a value for the family property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifFamiliesDefined(Consumer<List<String>> c) {
        return consumeIfDefined(families, c);
    }

    /**
     * Run action if a value for the bold property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifBoldDefined(Consumer<? super Boolean> c) {
        consumeIfDefined(bold, c);
    }

    /**
     * Run action if a value for the italic property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifItalicDefined(Consumer<? super Boolean> c) {
        consumeIfDefined(italic, c);
    }

    /**
     * Run action if a value for the underline property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifUnderlineDefined(Consumer<? super Boolean> c) {
        consumeIfDefined(underline, c);
    }

    /**
     * Run action if a value for the strike-through property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifStrikeThroughDefined(Consumer<? super Boolean> c) {
        consumeIfDefined(strikeThrough, c);
    }

    /**
     * Run action if a value for the type property is defined.
     *
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     */
    public void ifTypeDefined(Consumer<? super FontType> c) {
        consumeIfDefined(type, c);
    }

    /**
     * Test if this instance holds no data.
     *
     * @return true, if none of the attributes is set
     */
    public boolean isEmpty() {
        return color == null
                && size == null
                && type == null
                && families == null
                && bold == null
                && italic == null
                && underline == null
                && strikeThrough == null;
    }

    @Override
    public FontDef clone() throws CloneNotSupportedException {
        return (FontDef) super.clone();
    }

}

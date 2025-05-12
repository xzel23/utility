// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A Style is a set of property names and corresponding values that control the appearance of {@link RichText}.
 */
public final class Style implements Iterable<Map.Entry<String, Object>> {
    private static final Logger LOG = LogManager.getLogger(Style.class);

    // -- static fields and methods

    /**
     * property for the font.
     */
    public static final String FONT = "font";

    /**
     * property name for the font families
     */
    public static final String FONT_FAMILIES = "font-family";
    /**
     * Constant representing the commonly used sans-serif font families.
     */
    public static final List<String> FONT_FAMILIES_VALUE_SANS_SERIF = List.of("Helvetica", "Arial", "Noto Sans", "liberation Sans", "sans-serif");
    /**
     * Constant representing the commonly used serif font families.
     */
    public static final List<String> FONT_FAMILIES_VALUE_SERIF = List.of("Times New Roman", "Georgia", "Noto Serif", "Liberation Serif", "serif");
    /**
     * Constant representing the commonly used monospace font families.
     */
    public static final List<String> FONT_FAMILIES_VALUE_MONOSPACED = List.of("Courier New", "Consolas", "Monaco", "Liberation Mono", "monospace");

    /**
     * property name for the font class.
     */
    public static final String FONT_CLASS = "font-class";
    /**
     * Constant representing the value for monospace font class.
     */
    public static final String FONT_CLASS_VALUE_MONOSPACE = "monospace";
    /**
     * Constant representing the value for serif font class.
     */
    public static final String FONT_CLASS_VALUE_SERIF = "serif";
    /**
     * Constant representing the value for sansserif font class.
     */
    public static final String FONT_CLASS_VALUE_SANS_SERIF = "sans-serif";

    /**
     * property name for the font style
     */
    public static final String FONT_STYLE = "font-style";
    /**
     * Specifies the normal font style.
     * This value can be used as a setting for the font style to indicate no special styling,
     * such as italic or oblique, and to maintain the default, standard text appearance.
     */
    public static final String FONT_STYLE_VALUE_NORMAL = "normal";
    /**
     * Represents the italic font style.
     */
    public static final String FONT_STYLE_VALUE_ITALIC = "italic";
    /**
     * Represents the value for the oblique font style.
     * This is typically used to render text in a slanted manner.
     */
    public static final String FONT_STYLE_VALUE_OBLIQUE = "oblique";

    /**
     * property name for the font size
     */
    public static final String FONT_SIZE = "font-size";

    /**
     * property name for the font scale
     */
    public static final String FONT_SCALE = "font-scale";

    /**
     * property name for the font weight
     */
    public static final String FONT_WEIGHT = "font-weight";
    /**
     * Represents the font weight property value for bold text.
     */
    public static final String FONT_WEIGHT_VALUE_BOLD = "bold";
    /**
     * Represents the normal font weight value for text styling.
     */
    public static final String FONT_WEIGHT_VALUE_NORMAL = "normal";

    /**
     * property name for the font variant
     */
    public static final String FONT_VARIANT = "font-variant";
    /**
     * Represents the normal variant value for font.
     */
    public static final String FONT_VARIANT_VALUE_NORMAL = "normal";

    /**
     * property name for the underline text decoration
     */
    public static final String TEXT_DECORATION_UNDERLINE = "text-decoration-line-under";
    /**
     * Indicates that text decoration underline should be applied.
     */
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_LINE = Boolean.TRUE;
    /**
     * Indicates that text decoration underline should not be applied.
     */
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE = Boolean.FALSE;

    /**
     * property name for the line-through text decoration
     */
    public static final String TEXT_DECORATION_LINE_THROUGH = "text-decoration-line-through";
    /**
     * Indicates that text decoration line-through should be applied.
     *
     * <p>This boolean constant is used to indicate that text within a style should
     * be rendered with a line through the middle, often used for indicating deleted
     * or inactive text.
     */
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_LINE = Boolean.TRUE;
    /**
     * Indicates that text decoration line-through should not be applied.
     */
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE = Boolean.FALSE;

    /**
     * text indentation
     */
    public static final String TEXT_INDENT_LEFT = "indent-left";
    /**
     * Represents the left text indentation value of 0.
     * <p>This constant can be used to specify no indentation
     * for a text element in a styling context.
     */
    public static final String TEXT_INDENT_LEFT_VALUE_0 = "0";
    /**
     * Represents the left text indentation value of 40 points.
     */
    public static final String TEXT_INDENT_LEFT_VALUE_1 = "40";

    /**
     * foreground color
     */
    public static final String COLOR = "color";
    /**
     * background color
     */
    public static final String BACKGROUND_COLOR = "background-color";

    // -- define some default styles

    /**
     * Empty style, not defining any attributes.
     */
    public static final Style EMPTY = create("");

    /**
     * Default Sansserif font.
     */
    public static final Style SANS_SERIF = create("sans-serif",
            Map.entry(FONT_FAMILIES, FONT_FAMILIES_VALUE_SANS_SERIF),
            Map.entry(FONT_CLASS, FONT_CLASS_VALUE_SANS_SERIF)
    );
    /**
     * Default Serif font.
     */
    public static final Style SERIF = create("serif",
            Map.entry(FONT_FAMILIES, FONT_FAMILIES_VALUE_SERIF),
            Map.entry(FONT_CLASS, FONT_CLASS_VALUE_SERIF)
    );
    /**
     * Default Monospace font.
     */
    public static final Style MONOSPACE = create("monospace",
            Map.entry(FONT_FAMILIES, FONT_FAMILIES_VALUE_MONOSPACED),
            Map.entry(FONT_CLASS, FONT_CLASS_VALUE_MONOSPACE)
    );

    /**
     * Bold style.
     */
    public static final Style BOLD = create("bold", Map.entry(FONT_WEIGHT, FONT_WEIGHT_VALUE_BOLD));
    /**
     * Normal (=not bold) style.
     */
    public static final Style NORMAL = create("normal", Map.entry(FONT_WEIGHT, FONT_WEIGHT_VALUE_NORMAL));

    /**
     * Italics style.
     */
    public static final Style ITALIC = create("italic", Map.entry(FONT_STYLE, FONT_STYLE_VALUE_ITALIC));
    /**
     * Regular style.
     */
    public static final Style REGULAR = create("regular", Map.entry(FONT_STYLE, FONT_STYLE_VALUE_NORMAL));

    /**
     * Underline style.
     */
    @SuppressWarnings("ConstantValue")
    public static final Style UNDERLINE = create("underline", Map.entry(TEXT_DECORATION_UNDERLINE, TEXT_DECORATION_UNDERLINE_VALUE_LINE));
    /**
     * No-Underline style.
     */
    @SuppressWarnings("ConstantValue")
    public static final Style NO_UNDERLINE = create("no-underline", Map.entry(TEXT_DECORATION_UNDERLINE, TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE));

    /**
     * Line-through (=strikethrough) style.
     */
    @SuppressWarnings("ConstantValue")
    public static final Style LINE_THROUGH = create("line-through", Map.entry(TEXT_DECORATION_LINE_THROUGH, TEXT_DECORATION_LINE_THROUGH_VALUE_LINE));
    /**
     * No-Line-through (=no-strikethrough) style.
     */
    @SuppressWarnings("ConstantValue")
    public static final Style NO_LINE_THROUGH = create("no-line-through", Map.entry(TEXT_DECORATION_LINE_THROUGH, TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE));

    // predefined styles for colors
    /**
     * Black text.
     */
    public static final Style BLACK = create("black", Map.entry(COLOR, Color.BLACK));
    /**
     * White text.
     */
    public static final Style WHITE = create("white", Map.entry(COLOR, Color.WHITE));
    /**
     * Red text.
     */
    public static final Style RED = create("red", Map.entry(COLOR, Color.RED));
    /**
     * Green text.
     */
    public static final Style GREEN = create("green", Map.entry(COLOR, Color.GREEN));
    /**
     * Blue text.
     */
    public static final Style BLUE = create("blue", Map.entry(COLOR, Color.BLUE));
    /**
     * Yellow text.
     */
    public static final Style YELLOW = create("yellow", Map.entry(COLOR, Color.YELLOW));
    /**
     * Gray text.
     */
    public static final Style GRAY = create("gray", Map.entry(COLOR, Color.GRAY));
    /**
     * Darkgray text.
     */
    public static final Style DARKGRAY = create("darkgray", Map.entry(COLOR, Color.DARKGRAY));
    /**
     * Lightgray text.
     */
    public static final Style LIGHTGRAY = create("lightgray", Map.entry(COLOR, Color.LIGHTGRAY));

    // -- instance fields and methods

    private final String name;
    private final Map<String, @Nullable Object> properties;

    private Style(String name, Map<String, @Nullable Object> args) {
        this.name = name;
        this.properties = Collections.unmodifiableMap(args);
    }

    /**
     * Create a new Style.
     *
     * @param styleName the style name
     * @param args      Map.Entry to set as properties of this style
     * @return new instance
     */
    @SafeVarargs
    public static Style create(String styleName,
                               Map.Entry<String, @Nullable Object>... args) {
        assert(Arrays.stream(args).allMatch(Style::checkTypes)) : "invalid style arguments for style: " + styleName + " - " + Arrays.toString(args);
        return create(styleName, Map.ofEntries(args));
    }

    static boolean checkTypes(Map.Entry<String, ?> e) {
        boolean result = switch (e.getKey()) {
            case FONT -> e.getValue() instanceof Font;
            case FONT_FAMILIES -> e.getValue() instanceof List;
            case FONT_CLASS -> e.getValue() instanceof String;
            case FONT_SIZE -> e.getValue() instanceof Number;
            case FONT_STYLE -> e.getValue() instanceof String;
            case FONT_WEIGHT -> e.getValue() instanceof String;
            case TEXT_DECORATION_UNDERLINE -> e.getValue() instanceof Boolean;
            case TEXT_DECORATION_LINE_THROUGH -> e.getValue() instanceof Boolean;
            case COLOR -> e.getValue() instanceof Color;
            case BACKGROUND_COLOR -> e.getValue() instanceof Color;
            default -> true;
        };

        if (!result) {
            LOG.error("invalid style argument for style: {} - {}", e.getKey(), e.getValue());
        }

        return result;
    }

    /**
     * Create a new Style.
     *
     * @param styleName the style name
     * @param args      list of key-value pairs to set as properties of this style
     * @return new instance
     */
    public static Style create(String styleName,
                               Map<String, @Nullable Object> args) {
        return new Style(styleName, new HashMap<>(args));
    }

    @Override
    public String toString() {
        return name + properties;
    }

    /**
     * Get style name.
     *
     * @return the style name
     */
    public String name() {
        return name;
    }

    /**
     * Get a property's value.
     *
     * @param property the property name
     * @return the value of the property or {@code null} if no value was set
     */
    public @Nullable Object get(String property) {
        return properties.get(property);
    }

    /**
     * Retrieves the value associated with the specified property, or returns the provided default value
     * if the property is not set.
     *
     * @param <T>    the type of the property value
     * @param property the name of the property to retrieve
     * @param dflt     the default value to return if the property is not set
     * @return the value of the property if set, otherwise the provided default value
     */
    public <T extends @Nullable Object> T getOrDefault(String property, T dflt) {
        //noinspection unchecked - by design
        return (T) properties.getOrDefault(property, dflt);
    }

    /**
     * Consume value if property is set.
     *
     * @param key    the property
     * @param action the consumer
     * @param <T>    the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T extends @Nullable Object> void ifPresent(String key, Consumer<T> action) throws ClassCastException {
        T value = (T) get(key);
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * Consume the value of a property.
     *
     * @param key             the property
     * @param action          the consumer
     * @param defaultSupplier supplier of a default value to be used when the property is not set
     * @param <T>             the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T extends @Nullable Object> void ifPresentOrElseGet(String key,
                                                                Supplier<T> defaultSupplier,
                                                                Consumer<? super T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            action.accept(value != null ? value : defaultSupplier.get());
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '" + key + "' with value: " + raw, e);
        }
    }

    /**
     * Consume value of a property.
     *
     * @param key          the property
     * @param action       the consumer
     * @param defaultValue the default value to be used when the property is not set
     * @param <T>          the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T extends @Nullable Object> void ifPresentOrElse(String key,
                                                             @Nullable T defaultValue,
                                                             Consumer<T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            //noinspection DataFlowIssue - by design
            action.accept(value != null ? value : defaultValue);
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '" + key + "' with value: " + raw, e);
        }
    }

    /**
     * Get FontDef for this style.
     *
     * @return the FontDef
     */
    public FontDef getFontDef() {
        Font font = (Font) get(FONT);
        if (font != null) {
            return font.toFontDef();
        }

        FontDef fd = new FontDef();
        ifPresent(FONT_FAMILIES, fd::setFamilies);
        ifPresent(FONT_SIZE, fd::setSize);
        ifPresent(COLOR, fd::setColor);
        ifPresent(FONT_STYLE, v -> fd.setItalic(Objects.equals(v, FONT_STYLE_VALUE_ITALIC) || Objects.equals(v, FONT_STYLE_VALUE_OBLIQUE)));
        ifPresent(FONT_WEIGHT, v -> fd.setBold(Objects.equals(v, FONT_WEIGHT_VALUE_BOLD)));
        ifPresent(TEXT_DECORATION_UNDERLINE, v -> fd.setUnderline(Objects.equals(v, TEXT_DECORATION_UNDERLINE_VALUE_LINE)));
        ifPresent(TEXT_DECORATION_LINE_THROUGH, v -> fd.setStrikeThrough(Objects.equals(v, TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)));
        return fd;
    }

    /**
     * Get Font for this style.
     *
     * @return Optional holding the Font if this Style's Font has been set (not if just part of the FontDef is set)
     */
    public Optional<Font> getFont() {
        return Optional.ofNullable((Font) get(FONT));
    }

    /**
     * Get Font for this style.
     *
     * @param baseFont the font used to derive the requested font by applying this style
     * @return if set, the font of this style, otherwise the resulting font of applying this style's {@link FontDef} to the supplied baseFont
     */
    public Font getFont(Font baseFont) {
        return getFont().orElseGet(() -> FontUtil.getInstance().deriveFont(baseFont, getFontDef()));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Style other)) {
            return false;
        }
        return other == this || Objects.equals(name, other.name) && Objects.equals(properties, other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, properties);
    }

    @Override
    public Iterator<Map.Entry<String, @Nullable Object>> iterator() {
        return properties.entrySet().iterator();
    }

    /**
     * Get a stream of this style's entries.
     *
     * @return stream of entries
     */
    public Stream<Map.Entry<String, @Nullable Object>> stream() {
        return properties.entrySet().stream();
    }

    /**
     * Perform action for each entry of this style.
     *
     * @param action the action to perform
     */
    public void forEach(BiConsumer<String, @Nullable Object> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    /**
     * Create a style for the given font.
     *
     * @param font the font
     * @return the created style
     */
    public static Style create(Font font) {
        return create(font.fontspec(), Map.entry(FONT, font));
    }

    /**
     * Creates a new style with the specified font, foreground color, and background color.
     *
     * @param font the font to be used in the style
     * @param foreground the foreground color of the style
     * @param background the background color of the style
     * @return a new Style instance configured with the specified font, foreground color, and background color
     */
    public static Style create(Font font, Color foreground, Color background) {
        return create(font.fontspec() + foreground.toCss() + background.toCss(), Map.of(FONT, font, COLOR, foreground, BACKGROUND_COLOR, background));
    }
}

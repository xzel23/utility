// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

    // -- static fields and methods

    /**
     * property for the font.
     */
    public static final String FONT = "font";

    /**
     * property name for the font family
     */
    public static final String FONT_TYPE = "font-type";
    public static final String FONT_TYPE_VALUE_SANS_SERIF = "sans-serif";
    public static final String FONT_TYPE_VALUE_SERIF = "serif";
    public static final String FONT_TYPE_VALUE_MONOSPACE = "monospace";

    /**
     * property name for the font style
     */
    public static final String FONT_STYLE = "font-style";
    public static final String FONT_STYLE_VALUE_NORMAL = "normal";
    public static final String FONT_STYLE_VALUE_ITALIC = "italic";
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
    public static final String FONT_WEIGHT_VALUE_BOLD = "bold";
    public static final String FONT_WEIGHT_VALUE_NORMAL = "normal";

    /**
     * property name for the font variant
     */
    public static final String FONT_VARIANT = "font-variant";
    public static final String FONT_VARIANT_VALUE_NORMAL = "normal";

    /**
     * property name for the underline text decoration
     */
    public static final String TEXT_DECORATION_UNDERLINE = "text-decoration-line-under";
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE = Boolean.FALSE;

    /**
     * property name for the line-through text decoration
     */
    public static final String TEXT_DECORATION_LINE_THROUGH = "text-decoration-line-through";
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE = Boolean.FALSE;

    /**
     * text indentation
     */
    public static final String TEXT_INDENT_LEFT = "indent-left";
    public static final String TEXT_INDENT_LEFT_VALUE_0 = "0";
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
     * Default Sansserif font.
     */
    public static final Style SANS_SERIF = create("sans-serif", Map.entry(FONT_TYPE, FONT_TYPE_VALUE_SANS_SERIF));
    /**
     * Default Serif font.
     */
    public static final Style SERIF = create("serif", Map.entry(FONT_TYPE, FONT_TYPE_VALUE_SERIF));
    /**
     * Default Monospace font.
     */
    public static final Style MONOSPACE = create("monospace", Map.entry(FONT_TYPE, FONT_TYPE_VALUE_MONOSPACE));

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
    private final Map<String, Object> properties;

    private Style(String name, Map<String, Object> args) {
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
                               Map.Entry<String, Object>... args) {
        return create(styleName, Map.ofEntries(args));
    }

    /**
     * Create a new Style.
     *
     * @param styleName the style name
     * @param args      list of key-value pairs to set as properties of this style
     * @return new instance
     */
    public static Style create(String styleName,
                               Map<String, Object> args) {
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
    public Object get(String property) {
        return properties.get(property);
    }

    /**
     * Get a property's value.
     *
     * @param property the property name
     * @param dflt     the default value
     * @return the value of the property or dflt if no value was set
     */
    public Object getOrDefault(String property, Object dflt) {
        return properties.getOrDefault(property, dflt);
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
    public <T> void ifPresent(String key, Consumer<T> action) throws ClassCastException {
        T value = (T) get(key);
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * Consume value of a property.
     *
     * @param key             the property
     * @param action          the consumer
     * @param defaultSupplier supplier of a default value to be used when the property is not set
     * @param <T>             the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> void ifPresentOrElseGet(String key,
                                       Supplier<T> defaultSupplier,
                                       Consumer<? super T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            action.accept(value != null ? value : defaultSupplier.get());
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '" + key + " with value': " + raw, e);
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
    public <T> void ifPresentOrElse(String key,
                                    T defaultValue,
                                    Consumer<T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            action.accept(value != null ? value : defaultValue);
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '" + key + " with value': " + raw, e);
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
        ifPresent(FONT_TYPE, fd::setFamily);
        ifPresent(FONT_SIZE, fd::setSize);
        ifPresent(COLOR, fd::setColor);
        ifPresent(FONT_STYLE, v -> fd.setItalic(Objects.equals(v, FONT_STYLE_VALUE_ITALIC) || v.equals(FONT_STYLE_VALUE_OBLIQUE)));
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
        return getFont().orElseGet(() -> baseFont.deriveFont(getFontDef()));
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
    public Iterator<Map.Entry<String, Object>> iterator() {
        return properties.entrySet().iterator();
    }

    /**
     * Get a stream of this style's entries.
     *
     * @return stream of entries
     */
    public Stream<Map.Entry<String, Object>> stream() {
        return properties.entrySet().stream();
    }

    /**
     * Perform action for each entry of this style.
     *
     * @param action the action to perform
     */
    public void forEach(BiConsumer<String, Object> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }
}

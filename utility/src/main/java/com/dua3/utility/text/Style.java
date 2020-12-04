// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Style implements Iterable<Map.Entry<String, Object>> {

    // -- static fields and methods

    /** property for the font. */
    public static final String FONT = "font";

    /** property name for the font family */
    public static final String FONT_TYPE = "font-type";
    public static final String FONT_TYPE_VALUE_SANS_SERIF = "sans-serif";
    public static final String FONT_TYPE_VALUE_SERIF = "serif";
    public static final String FONT_TYPE_VALUE_MONOSPACE = "monospace";

    /** property name for the font style */
    public static final String FONT_STYLE = "font-style";
    public static final String FONT_STYLE_VALUE_NORMAL = "normal";
    public static final String FONT_STYLE_VALUE_ITALIC = "italic";
    public static final String FONT_STYLE_VALUE_OBLIQUE = "oblique";

    /** property name for the font weight */
    public static final String FONT_SIZE = "font-size";

    /** property name for the font weight */
    public static final String FONT_SCALE = "font-scale";

    /** property name for the font weight */
    public static final String FONT_WEIGHT = "font-weight";
    public static final String FONT_WEIGHT_VALUE_BOLD = "bold";
    public static final String FONT_WEIGHT_VALUE_NORMAL = "normal";

    /** property name for the font variant */
    public static final String FONT_VARIANT = "font-variant";
    public static final String FONT_VARIANT_VALUE_NORMAL = "normal";

    /** property name for the underline text decoration */
    public static final String TEXT_DECORATION_UNDERLINE = "text-decoration-line-under";
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE = Boolean.FALSE;

    /** property name for the line-through text decoration */
    public static final String TEXT_DECORATION_LINE_THROUGH = "text-decoration-line-through";
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE = Boolean.FALSE;

    /** text indentation */
    public static final String TEXT_INDENT_LEFT = "indent-left";
    public static final String TEXT_INDENT_LEFT_VALUE_0 = "0";
    public static final String TEXT_INDENT_LEFT_VALUE_1 = "40";

    /** foreground color */
    public static final String COLOR = "color";
    /** background color */
    public static final String BACKGROUND_COLOR = "background-color";

    // -- define some default styles
    public static final Style SANS_SERIF = Style.create("sans-serif", Pair.of(FONT_TYPE, FONT_TYPE_VALUE_SANS_SERIF));
    public static final Style SERIF = Style.create("serif", Pair.of(FONT_TYPE, FONT_TYPE_VALUE_SERIF));
    public static final Style MONOSPACE = Style.create("monospace", Pair.of(FONT_TYPE, FONT_TYPE_VALUE_MONOSPACE));

    public static final Style BOLD = Style.create("bold", Pair.of(FONT_WEIGHT, FONT_WEIGHT_VALUE_BOLD));
    public static final Style NORMAL = Style.create("normal", Pair.of(FONT_WEIGHT, FONT_WEIGHT_VALUE_NORMAL));

    public static final Style ITALIC = Style.create("italic", Pair.of(FONT_STYLE, FONT_STYLE_VALUE_ITALIC));
    public static final Style REGULAR = Style.create("regular", Pair.of(FONT_STYLE, FONT_STYLE_VALUE_NORMAL));

    public static final Style UNDERLINE = Style.create("underline", Pair.of(TEXT_DECORATION_UNDERLINE, TEXT_DECORATION_UNDERLINE_VALUE_LINE));
    public static final Style NO_UNDERLINE = Style.create("no-underline", Pair.of(TEXT_DECORATION_UNDERLINE, TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE));

    public static final Style LINE_THROUGH = Style.create("line-through", Pair.of(TEXT_DECORATION_LINE_THROUGH, TEXT_DECORATION_LINE_THROUGH_VALUE_LINE));
    public static final Style NO_LINE_THROUGH = Style.create("no-line-through", Pair.of(TEXT_DECORATION_LINE_THROUGH, TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE));
    
    // -- instance fields and methods
    
    private final String name;
    private final Map<String, Object> properties;

    private Style(String name, Map<String, Object> args) {
        this.name = name;
        this.properties = Collections.unmodifiableMap(args);
    }

    @Override
    public String toString() {
        return name + properties;
    }

    /**
     * Create a new Style.
     * @param styleName the style name
     * @param args key-value pairs to set as properties of this style
     * @return new instance
     */
    @SafeVarargs
    public static Style create(String styleName, Pair<String, Object>... args) {
        return create(styleName, Pair.toMap(args));
    }

    /**
     * Create a new Style.
     * @param styleName the style name
     * @param args list of key-value pairs to set as properties of this style
     * @return new instance
     */
    public static Style create(String styleName, Map<String, Object> args) {
        return new Style(styleName, new HashMap<>(args));
    }

    /** 
     * Get style name. 
     * @return the style name
     */
    public String name() {
        return name;
    }

    /** 
     * Get a propertx's value.
     * @param property the property name
     * @return the value of the property or {@code null} if no value was set
     */
    public Object get(String property) {
        return properties.get(property);
    }

    /**
     * Get a propertx's value.
     * @param property the property name
     * @param dflt the default value
     * @return the value of the property or dflt if no value was set
     */
    public Object getOrDefault(String property, Object dflt) {
        return properties.getOrDefault(property, dflt);
    }

    /**
     * Consume value if property is set.
     * @param key the property
     * @param action the consumer
     * @param <T> the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> void ifPresent(String key, Consumer<T> action) throws ClassCastException {
        T value = (T) get(key);
        if (value!=null) {
            action.accept(value);
        }
    }

    /**
     * Consume value of a property.
     * @param key the property
     * @param action the consumer
     * @param defaultSupplier supplier of a default value to be used when the property is not set
     * @param <T> the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> void ifPresentOrElseGet(String key, Supplier<T> defaultSupplier, Consumer<T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            action.accept(value != null ? value : defaultSupplier.get());
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '"+key+" with value': "+raw, e);
        }
    }

    /**
     * Consume value of a property.
     * @param key the property
     * @param action the consumer
     * @param defaultValue the default value to be used when the property is not set
     * @param <T> the type of property values
     * @throws ClassCastException if the property value does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> void ifPresentOrElse(String key, T defaultValue, Consumer<T> action) {
        Object raw = get(key);
        try {
            T value = (T) raw;
            action.accept(value!=null ? value : defaultValue);
        } catch (Exception e) {
            throw new IllegalStateException("error processing attribute '"+key+" with value': "+raw, e);
        }
    }

    /**
     * Get this style's font, or if the Style does only contain additional styloing information, derive the font
     * to use from the given base font.
     * @param baseFont the base font to derive a new font from
     * @return the font
     */
    public Font getFont(Font baseFont) {
        Object taFont = get(FONT);
        if (taFont instanceof  Font) {
            baseFont = (Font) taFont;
        }

        FontDef fd = new FontDef();
        ifPresent(FONT_TYPE, fd::setFamily);
        ifPresent(FONT_SIZE, fd::setSize);
        ifPresent(COLOR, fd::setColor);
        ifPresent(FONT_STYLE, v -> fd.setItalic(Objects.equals(v, FONT_STYLE_VALUE_ITALIC) || v.equals(FONT_STYLE_VALUE_OBLIQUE)));
        ifPresent(FONT_WEIGHT, v -> fd.setBold(Objects.equals(v, FONT_WEIGHT_VALUE_BOLD)));
        ifPresent(TEXT_DECORATION_UNDERLINE, v -> fd.setUnderline(Objects.equals(v, TEXT_DECORATION_UNDERLINE_VALUE_LINE)));
        ifPresent(TEXT_DECORATION_LINE_THROUGH, v -> fd.setStrikeThrough(Objects.equals(v, TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)));
        return baseFont.deriveFont(fd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Style style = (Style) o;
        return Objects.equals(name, style.name) && Objects.equals(properties, style.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, properties);
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return properties.entrySet().iterator();
    }
    
    public Stream<Map.Entry<String, Object>> stream() {
        return properties.entrySet().stream();
    }
}

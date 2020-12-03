package com.dua3.utility.text;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Options controling the conversion process.
 */
public class HtmlConversionOption {

    /**
     * Use CSS in output.
     *
     * @param flag set to true to enable CSS output
     * @return the option to use
     */
    public static HtmlConversionOption useCss(boolean flag) {
        return new HtmlConversionOption(c -> c.setUseCss(flag));
    }

    /**
     * Add default mappings for the standard {@link TextAttributes}.
     *
     * @return the option tp use
     */
    public static HtmlConversionOption addDefaultMappings() {
        return new HtmlConversionOption(HtmlConverter::addDefaultMappings);
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the attibute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption map(String attribute, Function<Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.addMapping(attribute, Objects.requireNonNull(mapper)));
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the attibute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption replaceMapping(String attribute, Function<Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.replaceMapping(attribute, Objects.requireNonNull(mapper)));
    }

    /**
     * Set the default mapper which is called when no mapper is registered for the attribute.
     */
    public static HtmlConversionOption defaultMapper(BiFunction<String, Object, HtmlTag> setDefaultMapper) {
        return new HtmlConversionOption(c -> c.setDefaultMapper(setDefaultMapper));
    }

    private final Consumer<HtmlConverter> action;

    protected HtmlConversionOption(Consumer<HtmlConverter> action) {
        this.action = action;
    }

    void apply(HtmlConverter converter) {
        action.accept(converter);
    }
}

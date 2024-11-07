package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A {@link RichTextConverter} that converts {@link RichText} to HTML.
 */
public final class HtmlConverter extends TagBasedConverter<String> {

    /**
     * The mappings of this converter.
     * <p>
     * Key: the attribute name
     * Value: the function that maps values to tags
     */
    private final Map<String, Function<Object, HtmlTag>> mappings = new HashMap<>();
    private UnaryOperator<Map<String, @Nullable Object>> refineStyleProperties = m -> m;
    /**
     * The default mapper used to generate tags for attributes without mapping.
     */
    private BiFunction<? super String, @Nullable Object, ? extends HtmlTag> defaultMapper = (attribute, value) -> HtmlTag.emptyTag();
    /**
     * Whether CSS output should be generated.
     */
    private boolean useCss;

    /**
     * Constructor.
     */
    private HtmlConverter() {
    }

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
        return new HtmlConversionOption(HtmlConverter::doAddDefaultMappings);
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the attribute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption map(String attribute,
                                           Function<Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.addMapping(attribute, mapper));
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the attribute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption replaceMapping(String attribute,
                                                      Function<Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.mappings.put(attribute, mapper));
    }

    /**
     * Set the default mapper which is called when no mapper is registered for the attribute.
     *
     * @param mapper the mapper to set as the default mapper
     * @return HtmlConversionOption that sets the default mapper
     */
    public static HtmlConversionOption defaultMapper(BiFunction<String, Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.setDefaultMapper(mapper));
    }

    /**
     * Set the refineStyleProperties function which refines the style properties of the converted HTML.
     *
     * @param refineStyleProperties the function to set as the refineStyleProperties function
     * @return HtmlConversionOption that sets the refineStyleProperties function
     */
    public static HtmlConversionOption refineStyleProperties(UnaryOperator<Map<String, Object>> refineStyleProperties) {
        return new HtmlConversionOption(c -> c.setRefineStyleProperties(refineStyleProperties));
    }

    /**
     * Replace font declarations with declarations for single properties. Use this as argument to
     * {@link #setRefineStyleProperties(UnaryOperator)} to generate style tags like <pre><b>...</b></pre>
     * instead of <pre><span>...</span></pre>.
     *
     * @param styleProperties map with style properties
     * @return style properties with font declarations replaced
     */
    public static Map<String, Object> inlineTextDecorations(Map<String, Object> styleProperties) {
        Map<String, Object> props = new LinkedHashMap<>();
        styleProperties.forEach((key, value) -> {
            switch (key) {
                case Style.FONT -> RichTextConverter.putFontProperties(props, (Font) value);
                default -> props.put(key, value);
            }
        });
        return props;
    }

    /**
     * Combine to mappers by creating a new mapper that simply concatenates tags.
     *
     * @param m1 the first mapper
     * @param m2 the second mapper
     * @return the new mapper
     */
    private static Function<Object, HtmlTag> combineMappers(Function<Object, ? extends HtmlTag> m1,
                                                            Function<Object, ? extends HtmlTag> m2) {
        return value -> {
            HtmlTag oldTag = m1.apply(value);
            HtmlTag newTag = m2.apply(value);
            return new HtmlTag() {
                @Override
                public String open() {
                    return newTag.open() + oldTag.open();
                }

                @Override
                public String close() {
                    return oldTag.close() + newTag.close();
                }
            };
        };
    }

    /**
     * Create a converter with default mappings.
     *
     * @param options the options to use
     * @return converter with standard mappings
     */
    public static HtmlConverter create(Collection<HtmlConversionOption> options) {
        HtmlConverter instance = new HtmlConverter();
        instance.doAddDefaultMappings();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    /**
     * Create a converter with default mappings.
     *
     * @param options the options to use
     * @return converter with standard mappings
     */
    public static HtmlConverter create(HtmlConversionOption... options) {
        return create(List.of(options));
    }

    /**
     * Create a converter without any mappings.
     *
     * @param options the options to use
     * @return converter
     */
    public static HtmlConverter createBlank(Collection<HtmlConversionOption> options) {
        HtmlConverter instance = new HtmlConverter();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    /**
     * Create a converter without any mappings.
     *
     * @param options the options to use
     * @return converter
     */
    public static HtmlConverter createBlank(HtmlConversionOption... options) {
        return create(List.of(options));
    }

    private void setRefineStyleProperties(UnaryOperator<Map<String, Object>> refineStyleProperties) {
        this.refineStyleProperties = refineStyleProperties;
    }

    @Override
    protected TagBasedConverterImpl<String> createConverter(RichText text) {
        return new HtmlConverterImpl(text);
    }

    private void addSimpleMapping(String attr, Object value, HtmlTag tag) {
        addMapping(attr, v -> Objects.equals(v, value) ? tag : HtmlTag.emptyTag());
    }

    void doAddDefaultMappings() {
        addSimpleMapping(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD, HtmlTag.tag("<b>", "</b>"));
        addSimpleMapping(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC, HtmlTag.tag("<i>", "</i>"));
        addSimpleMapping(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE, HtmlTag.tag("<u>", "</u>"));
        addSimpleMapping(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE, HtmlTag.tag("<strike>", "</strike>"));

        addMapping(Style.FONT_TYPE, value -> {
            if (isUseCss()) {
                return switch (value.toString()) {
                    case Style.FONT_TYPE_VALUE_MONOSPACE -> HtmlTag.tag("<span class=\"monospace\">", "</span>");
                    case Style.FONT_TYPE_VALUE_SANS_SERIF -> HtmlTag.tag("<span class=\"sans-serif\">", "</span>");
                    case Style.FONT_TYPE_VALUE_SERIF -> HtmlTag.tag("<span class=\"serif\">", "</span>");
                    default -> HtmlTag.emptyTag();
                };
            } else {
                return switch (value.toString()) {
                    case Style.FONT_TYPE_VALUE_MONOSPACE -> HtmlTag.tag("<code>", "</code>");
                    case Style.FONT_TYPE_VALUE_SANS_SERIF ->
                            HtmlTag.tag("<span style=\"font-family: sans-serif\">", "</span>");
                    case Style.FONT_TYPE_VALUE_SERIF -> HtmlTag.tag("<span style=\"font-family: serif\">", "</span>");
                    default -> HtmlTag.emptyTag();
                };
            }
        });

        addMapping(Style.FONT, value -> {
            Font font = (Font) value;
            if (isUseCss()) {
                return HtmlTag.tag("<span class=\"" + font + "\">", "</span>");
            } else {
                return HtmlTag.tag("<span style=\"" + font.getCssStyle() + "\">", "</span>");
            }
        });
    }

    void setDefaultMapper(BiFunction<? super String, Object, ? extends HtmlTag> defaultMapper) {
        this.defaultMapper = defaultMapper;
    }

    /**
     * Add mapper for an attribute.
     *
     * @param attribute the attribute
     * @param mapper    the mapper
     */
    void addMapping(String attribute, Function<Object, HtmlTag> mapper) {
        mappings.merge(attribute, mapper, HtmlConverter::combineMappers);
    }

    /**
     * Whether CSS is used in output.
     *
     * @return true, if CSS output is enabled
     */
    public boolean isUseCss() {
        return useCss;
    }

    /**
     * En-/disable CSS in output.
     *
     * @param flag true to enable CSS output
     */
    void setUseCss(boolean flag) {
        this.useCss = true;
    }

    /**
     * Get tag for attribute value.
     *
     * @param attribute the attribute
     * @param value     the attribute value
     * @return the tag
     */
    public HtmlTag get(String attribute, @Nullable Object value) {
        Function<@Nullable Object, HtmlTag> mapper = mappings.get(attribute);
        return mapper != null ? mapper.apply(value) : defaultMapper.apply(attribute, value);
    }

    private class HtmlConverterImpl extends TagBasedConverterImpl<String> {

        private final StringBuilder buffer;

        HtmlConverterImpl(RichText text) {
            // create a buffer with 25% overhead for HTML
            this.buffer = new StringBuilder(text.length() * 125 / 100);
        }

        @Override
        protected void appendOpeningTags(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            //noinspection ForLoopReplaceableByForEach - for symmetry with #appendClosingTags
            for (int i = 0; i < tags.size(); i++) {
                buffer.append(tags.get(i).open());
            }
        }

        @Override
        protected void appendClosingTags(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            for (int i = tags.size() - 1; i >= 0; i--) {
                buffer.append(tags.get(i).close());
            }
        }

        private List<HtmlTag> getTags(List<Style> styles) {
            List<HtmlTag> tags = new ArrayList<>();
            Map<String, @Nullable Object> properties = new LinkedHashMap<>();
            for (Style style : styles) {
                style.stream().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
            }
            refineStyleProperties.apply(properties).entrySet().stream().map(e -> HtmlConverter.this.get(e.getKey(), e.getValue())).forEach(tags::add);
            return tags;
        }

        @Override
        protected void appendChars(CharSequence s) {
            TextUtil.appendHtmlEscapedCharacters(buffer, s);
        }

        @Override
        protected String get() {
            return buffer.toString();
        }
    }

}

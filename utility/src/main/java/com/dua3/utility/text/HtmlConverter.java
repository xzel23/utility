package com.dua3.utility.text;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A {@link RichTextConverter} that converts {@link RichText} to HTML.
 */
public final class HtmlConverter extends TagBasedConverter<String> {

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
        return new HtmlConversionOption(c -> c.mappings.put(Objects.requireNonNull(attribute), Objects.requireNonNull(mapper)));
    }

    /**
     * Set the default mapper which is called when no mapper is registered for the attribute.
     */
    public static HtmlConversionOption defaultMapper(BiFunction<String, Object, HtmlTag> setDefaultMapper) {
        return new HtmlConversionOption(c -> c.setDefaultMapper(setDefaultMapper));
    }

    public static HtmlConversionOption refineStyleProperties(UnaryOperator<Map<String,Object>> refineStyleProperties) {
        return new HtmlConversionOption(c -> c.setRefineStyleProperties(refineStyleProperties));
    }

    private void setRefineStyleProperties(UnaryOperator<Map<String,Object>> refineStyleProperties) {
        this.refineStyleProperties = Objects.requireNonNull(refineStyleProperties);    
    }
    
    private UnaryOperator<Map<String,Object>> refineStyleProperties = m -> m;

    /**
     * Replace font declarations with declarations for single properties. Use this as argument to 
     * {@link #setRefineStyleProperties(UnaryOperator)} to generate style tags like <pre><b>...</b></pre>
     * instead of <pre><span>...</span></pre>.
     * @param styleProperties map with style properties
     * @return style properties with font declarations replaced
     */
    public static Map<String,Object> inlineTextDecorations(Map<String,Object> styleProperties) {
        Map<String,Object> props = new LinkedHashMap<>();
        styleProperties.forEach((key, value) -> {
            switch (key) {
                case Style.FONT -> {
                    Font font = (Font) value;
                    props.put(Style.FONT_TYPE, font.getFamily());
                    props.put(Style.FONT_SIZE, font.getSizeInPoints());
                    props.put(Style.FONT_STYLE, font.isItalic() ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
                    props.put(Style.FONT_WEIGHT, font.isBold() ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
                    props.put(Style.TEXT_DECORATION_UNDERLINE, font.isUnderline() ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
                    props.put(Style.TEXT_DECORATION_LINE_THROUGH, font.isStrikeThrough() ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
                    props.put(Style.COLOR, font.getColor());
                }
                default -> props.put(key, value);
            }
        });
        return props;
    }

    @Override
    protected TagBasedConverterImpl<String> createConverter(RichText text) {
        return new HtmlConverterImpl(text);
    }

    private class HtmlConverterImpl extends TagBasedConverterImpl<String> {
        
        private final StringBuilder buffer;
        
        HtmlConverterImpl(RichText text) {
            // create a buffer with 25% overhead for HTML
            this.buffer = new StringBuilder(text.length()*125/100);    
        }

        @Override
        protected void appendOpeningTags(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            //noinspection ForLoopReplaceableByForEach because of symmetry with #appendClosingTags
            for (int i=0; i<tags.size(); i++) {
                buffer.append(tags.get(i).open());
            }
        }

        @Override
        protected void appendClosingTags(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            for (int i=tags.size()-1; i>=0; i--) {
                buffer.append(tags.get(i).close());
            }
        }
        
        private List<HtmlTag> getTags(List<Style> styles) {
            List<HtmlTag> tags = new ArrayList<>();
            Map<String,Object> properties = new LinkedHashMap<>();
            for (Style style: styles) {
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

    private void addSimpleMapping(String attr, Object value, HtmlTag tag) {
        addMapping(attr, v -> Objects.equals(v,value) ? tag : HtmlTag.emptyTag());
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
                    case Style.FONT_TYPE_VALUE_SANS_SERIF -> HtmlTag.tag("<span style=\"font-family: sans-serif\">", "</span>");
                    case Style.FONT_TYPE_VALUE_SERIF -> HtmlTag.tag("<span style=\"font-family: serif\">", "</span>");
                    default -> HtmlTag.emptyTag();
                };
            }
        });

        addMapping(Style.FONT, value -> {
            Font font = (Font) value;
            if (isUseCss()) {
                return HtmlTag.tag("<span class=\"" + font.toString() + "\">", "</span>");
            } else {
                return HtmlTag.tag("<span style=\"" + font.getCssStyle() + "\">", "</span>");
            }
        });
    }

    void setDefaultMapper(BiFunction<String, Object, HtmlTag> defaultMapper) {
        this.defaultMapper = Objects.requireNonNull(defaultMapper);
    }

    /**
     * The mappings of this converter.
     *
     * Key: the attribute name
     * Value: the function that maps values to tags
     */
    private final Map<String, Function<Object, HtmlTag>> mappings = new HashMap<>();

    /**
     * The default mapper used to generate tags for attributes without mapping.
     */
    private BiFunction<String, Object, HtmlTag> defaultMapper = (attribute, value) -> HtmlTag.emptyTag();

    /**
     * Add mapper for an attribute.
     * @param attribute the attribute
     * @param mapper the mapper
     */
    void addMapping(String attribute, Function<Object, HtmlTag> mapper) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(mapper);

        mappings.merge(attribute, mapper, this::combineMappers);
    }

    /**
     * Combine to mappers by creating a new mapper that simply concatenates tags.
     * @param m1 the first mapper
     * @param m2 the second mapper
     * @return the new mapper
     */
    private Function<Object, HtmlTag> combineMappers(Function<Object, ? extends HtmlTag> m1, Function<Object, ? extends HtmlTag> m2) {
        return value -> {
            HtmlTag oldTag = m1.apply(value);
            HtmlTag newTag = m2.apply(value);
            return new HtmlTag() {
                @Override
                public String open() {
                    return newTag.open()+oldTag.open();
                }
                @Override
                public String close() {
                    return oldTag.close()+newTag.close();
                }
            };
        };
    }

    /**
     * whether or not CSS output shoud be generated.
     */
    private boolean useCss = false;

    /**
     * En-/disable CSS in output.
     * @param flag true to enable CSS output
     */
    void setUseCss(boolean flag) {
        this.useCss = true;
    }

    /**
     * Create a converter with default mappings.
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
     * @param options the options to use
     * @return converter with standard mappings
     */
    public static HtmlConverter create(HtmlConversionOption... options) {
        return create(Arrays.asList(options));
    }

    /**
     * Create a converter without any mappings.
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
     * @param options the options to use
     * @return converter
     */
    public static HtmlConverter createBlank(HtmlConversionOption... options) {
        return create(Arrays.asList(options));
    }

    /**
     * Constructor.
     */
    private HtmlConverter() {
    }

    /**
     * Whether CSS is used in output.
     * @return true, if CSS output is enabled
     */
    public boolean isUseCss() {
        return useCss;
    }

    /**
     * Get tag for attribute value.
     * @param attribute the attribute
     * @param value the attribute value
     * @return the tag
     */
    public HtmlTag get(String attribute, Object value) {
        Function<Object, HtmlTag> mapper = mappings.get(attribute);
        return mapper != null ? mapper.apply(value) : defaultMapper.apply(attribute, value);
    }
    
}

package com.dua3.utility.text;

import com.dua3.utility.lang.LangUtil;
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
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A {@link RichTextConverter} that converts {@link RichText} to HTML.
 */
public final class HtmlConverter extends TagBasedConverter<String> {

    /**
     * The style mappings of this converter.
     * <p>
     * Key: the style name
     * Value: the function that maps the style name to tags
     */
    private final Map<String, Function<Object, HtmlTag>> styleMappings = new HashMap<>();
    private UnaryOperator<Map<String, @Nullable Object>> refineStyleProperties = m -> m;

    /**
     * The default mapper used to generate tags for style names without mapping.
     */
    private BiFunction<? super String, @Nullable Object, ? extends HtmlTag> defaultStyleMapper = (styleName, value) -> HtmlTag.emptyTag();

    /**
     * The base font used for HTML conversion. This font serves as the starting point
     * for defining text appearance in the generated HTML, ensuring consistency across
     * converted content. All font-related styles in the HTML are derived or adjusted
     * based on this base font.
     */
    private final Font baseFont;
    /**
     * The default font currently being used for converting text to HTML.
     * <p>
     * This field is updated whenever a headline starts with a "&lt;hx&gt;"
     * or ends with a "&lt;/hx&gt;" tag where x is the header level. The field
     * is used to avoid inserting unwanted spans that only define the font style
     * that is already defined by the document's CSS rules.
     */
    private Font currentDefaultFont;

    /**
     * The attribute mappings of this converter.
     * <p>
     * Key: the attribute name
     * Value: the function that maps the attribute name to tags
     */
    private final Map<String, Function<AttributeChange, HtmlTag>> attributeMappings = new HashMap<>();

    /**
     * The default mapper used to generate tags for attributes without mapping.
     */
    private Function<AttributeChange, ? extends HtmlTag> defaultAttributeMapper = (attributeChange) -> HtmlTag.emptyTag();

    /**
     * Whether CSS output should be generated.
     */
    private boolean useCss;

    /**
     * Constructor.
     */
    private HtmlConverter() {
        baseFont = FontUtil.getInstance().getFont("Helvetica-12");
        currentDefaultFont = baseFont;
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
     * Set the mapper for a specific style. If the style is already mapped, the mappers are combined.
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
     * @param styleName the attribute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption replaceMapping(String styleName,
                                                      Function<Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.styleMappings.put(styleName, mapper));
    }

    /**
     * Set the default mapper which is called when no mapper is registered for the style name.
     *
     * @param mapper the mapper to set as the default mapper
     * @return HtmlConversionOption that sets the default mapper
     */
    public static HtmlConversionOption defaultMapper(BiFunction<String, Object, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.setDefaultStyleMapper(mapper));
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
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the attribute
     * @param mapper    the mapper
     * @return the option tp use
     */
    public static HtmlConversionOption mapAttribute(String attribute, Function<AttributeChange, HtmlTag> mapper) {
        return new HtmlConversionOption(c -> c.addAttributeMapping(attribute, mapper));
    }

    /**
     * Sets the header style mapping function for the HTML conversion process.
     *
     * @param getHeaderStyle the function to map header levels to corresponding {@link HeaderStyle} objects
     * @return an {@link HtmlConversionOption} that applies the header style mapping during conversion
     */
    public static HtmlConversionOption headerStyleMapper(IntFunction<HeaderStyle> getHeaderStyle) {
        return new HtmlConversionOption(c -> c.setGetHeaderStyle(getHeaderStyle));
    }

    /**
     * Sets the function used to map header levels to corresponding {@link HeaderStyle} objects
     * for the HTML conversion process.
     *
     * @param getHeaderStyle the function that maps an integer header level to a {@link HeaderStyle} object
     */
    private void setGetHeaderStyle(IntFunction<HeaderStyle> getHeaderStyle) {
        this.getHeaderStyle = getHeaderStyle;
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
    private static <T> Function<T, HtmlTag> combineMappers(Function<T, ? extends HtmlTag> m1,
                                                           Function<T, ? extends HtmlTag> m2) {
        return value -> {
            HtmlTag oldTag = m1.apply(value);
            HtmlTag newTag = m2.apply(value);
            return HtmlTag.combineTags(newTag, oldTag);
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

    @Override
    public String convert(RichText text) {
        currentDefaultFont = getHeaderStyle.apply(0).text().getFont(baseFont);
        return super.convert(text);
    }

    private void setRefineStyleProperties(UnaryOperator<Map<String, Object>> refineStyleProperties) {
        this.refineStyleProperties = refineStyleProperties;
    }

    private IntFunction<HeaderStyle> getHeaderStyle = level -> HeaderStyle.EMPTY;

    /**
     * Represents the style configuration for a header in the HTML conversion process.
     * This record encapsulates the header level, the style applied to the header itself,
     * and the style applied to the associated text.
     *
     * @param level the level of the header (e.g., 1 for h1, 2 for h2, etc.)
     * @param header the {@link Style} applied specifically to the header
     * @param text the {@link Style} applied to the text following the header
     */
    public record HeaderStyle(int level, Style header, Style text) {
        static final HeaderStyle EMPTY = new HeaderStyle(0, Style.EMPTY, Style.EMPTY);
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

        addMapping(Style.FONT_CLASS, value -> {
            if (isUseCss()) {
                return switch (value.toString()) {
                    case Style.FONT_CLASS_VALUE_MONOSPACE -> HtmlTag.tag("<span class='monospace'>", "</span>");
                    case Style.FONT_CLASS_VALUE_SANS_SERIF -> HtmlTag.tag("<span class='sans-serif'>", "</span>");
                    case Style.FONT_CLASS_VALUE_SERIF -> HtmlTag.tag("<span class='serif'>", "</span>");
                    default -> HtmlTag.emptyTag();
                };
            } else {
                return switch (value.toString()) {
                    case Style.FONT_CLASS_VALUE_MONOSPACE -> HtmlTag.tag("<code>", "</code>");
                    case Style.FONT_CLASS_VALUE_SANS_SERIF ->
                            HtmlTag.tag("<span style='font-family: sans-serif'>", "</span>");
                    case Style.FONT_CLASS_VALUE_SERIF -> HtmlTag.tag("<span style='font-family: serif'>", "</span>");
                    default -> HtmlTag.emptyTag();
                };
            }
        });

        addMapping(Style.FONT, value -> {
            Font font = (Font) value;
            if (isUseCss()) {
                return HtmlTag.tag("<span class='" + font.fontspec() + "'>", "</span>");
            } else {
                return HtmlTag.tag("<span style='" + font.getCssStyle() + "'>", "</span>");
            }
        });
    }

    void setDefaultStyleMapper(BiFunction<? super String, Object, ? extends HtmlTag> defaultStyleMapper) {
        this.defaultStyleMapper = defaultStyleMapper;
    }

    void setDefaultAttributeMapper(Function<AttributeChange, ? extends HtmlTag> defaultAttributeMapper) {
        this.defaultAttributeMapper = defaultAttributeMapper;
    }

    /**
     * Add a mapper that maps a style name to the corresponding tags.
     *
     * @param styleName the style name
     * @param mapper    the mapper
     */
    void addMapping(String styleName, Function<Object, HtmlTag> mapper) {
        styleMappings.merge(styleName, mapper, HtmlConverter::combineMappers);
    }

    /**
     * Add a mapper that maps an attribute to the corresponding tags.
     *
     * @param attribute the attribute name
     * @param mapper    the mapper
     */
    void addAttributeMapping(String attribute, Function<AttributeChange, HtmlTag> mapper) {
        attributeMappings.merge(attribute, mapper, HtmlConverter::combineMappers);
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
        this.useCss = flag;
    }

    /**
     * Get tag for style name.
     *
     * @param styleName the style name
     * @param value     the value
     * @return the tag
     */
    public HtmlTag get(String styleName, @Nullable Object value) {
        Function<@Nullable Object, HtmlTag> mapper = styleMappings.get(styleName);
        return mapper != null ? mapper.apply(value) : defaultStyleMapper.apply(styleName, value);
    }

    /**
     * Get tag for attribute.
     *
     * @param attributeChange the attribute change
     * @return the tag
     */
    public HtmlTag getTagForAttributeChange(AttributeChange attributeChange) {
        String attribute = attributeChange.attribute();
        Function<AttributeChange, HtmlTag> mapper = attributeMappings.get(attribute);
        return mapper != null ? mapper.apply(attributeChange) : defaultAttributeMapper.apply(attributeChange);
    }

    private class HtmlConverterImpl extends TagBasedConverterImpl<String> {

        private final StringBuilder buffer;

        HtmlConverterImpl(RichText text) {
            // create a buffer with 25% overhead for HTML
            this.buffer = new StringBuilder(text.length() * 125 / 100);
        }

        @Override
        protected Collection<String> relevantAttributes() {
            return attributeMappings.keySet();
        }

        @Override
        protected void appendOpeningTagsForStyles(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            //noinspection ForLoopReplaceableByForEach - for symmetry with #appendClosingTags
            for (int i = 0; i < tags.size(); i++) {
                appendOpeningTag(tags.get(i));
            }
        }

        @Override
        protected void appendClosingTagsForStyles(List<Style> styles) {
            List<HtmlTag> tags = getTags(styles);
            for (int i = tags.size() - 1; i >= 0; i--) {
                appendClosingTag(tags.get(i));
            }
        }

        @Override
        protected void appendOpeningTagsForAttributes(List<AttributeChange> attributeChanges) {
            super.appendOpeningTagsForAttributes(attributeChanges);
            appendAttributeTags(attributeChanges, HtmlTag.TagType.OPEN_TAG);
        }

        @Override
        protected void appendClosingTagsForAttributes(List<AttributeChange> attributeChanges) {
            super.appendOpeningTagsForAttributes(attributeChanges);
            appendAttributeTags(attributeChanges, HtmlTag.TagType.CLOSE_TAG);
        }

        private void appendOpeningTag(HtmlTag tag) {
            String tagString = tag.open();
            if (tagString.isEmpty()) {
                return;
            }

            if (tag.formattingHint().linebreakBeforeTag()) {
                breakLine();
            }

            int headerLevel = tag.headerChange();
            if (headerLevel >= 0) {
                currentDefaultFont = getHeaderStyle.apply(headerLevel).header().getFont(baseFont);
            }

            buffer.append(tagString);

            if (tag.formattingHint().linebreakAfterTag()) {
                breakLine();
            }
        }

        private void appendClosingTag(HtmlTag tag) {
            String tagString = tag.close();
            if (tagString.isEmpty()) {
                return;
            }

            if (tag.formattingHint().linebreakAfterTag()) {
                breakLine();
            }

            buffer.append(tagString);

            int headerLevel = tag.headerChange();
            if (headerLevel >= 0) {
                currentDefaultFont = getHeaderStyle.apply(headerLevel).text().getFont(baseFont);
            }

            if (tag.formattingHint().linebreakBeforeTag()) {
                breakLine();
            }
        }

        private void breakLine() {
            if (buffer.isEmpty()) {
                return;
            }

            char c = buffer.charAt(buffer.length() - 1);
            if (c != '\n' && c != '\r') {
                buffer.append('\n');
            }
        }

        private void appendAttributeTags(List<AttributeChange> AttributeChanges, HtmlTag.TagType type) {
            for (AttributeChange av : AttributeChanges) {
                HtmlTag tag = getTagForAttributeChange(av);
                if (type == HtmlTag.TagType.OPEN_TAG) {
                    appendOpeningTag(tag);
                } else {
                    appendClosingTag(tag);
                }
            }
        }

        private static boolean isFontRelated(String key) {
            return switch (key) {
                case Style.FONT_FAMILIES,
                     Style.FONT_SIZE,
                     Style.FONT_WEIGHT,
                     Style.FONT_STYLE,
                     Style.TEXT_DECORATION_UNDERLINE,
                     Style.TEXT_DECORATION_LINE_THROUGH,
                     Style.FONT_VARIANT,
                     Style.COLOR -> true;
                default -> false;
            };
        }

        private static boolean isOnlyTextStyleChanged(FontDef fd) {
            return fd.getFamilies() == null && fd.getSize() == null && fd.getColor() == null;
        }

        private List<HtmlTag> getTags(List<Style> styles) {
            List<HtmlTag> tags = new ArrayList<>();
            Map<String, @Nullable Object> properties = new LinkedHashMap<>();
            Predicate<Map.Entry<String, Object>> attributeFilter;
            for (Style style : styles) {
                String fontClass = (String) style.get(Style.FONT_CLASS);
                List<String> fontFamilies = (List<String>) style.get(Style.FONT_FAMILIES);
                Font font = (Font) style.get(Style.FONT);

                boolean ignoreFontFamily = fontClass != null
                        && LangUtil.isOneOf(fontFamilies,
                        Style.FONT_FAMILIES_VALUE_MONOSPACED,
                        Style.FONT_FAMILIES_VALUE_SANS_SERIF,
                        Style.FONT_FAMILIES_VALUE_SERIF
                );

                if (font == null) {
                    FontDef fd = style.getFontDef();
                    if (ignoreFontFamily) {
                        fd.setFamilies(null);
                    }
                    if (!fd.isEmpty() && !isOnlyTextStyleChanged(fd)) {
                        if (!Objects.equals(currentDefaultFont, FontUtil.getInstance().deriveFont(currentDefaultFont, fd))) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("<span style='");
                            sb.append(fd.getCssStyle());
                            sb.append("'>");
                            tags.add(HtmlTag.tag(sb.toString(), "</span>"));
                        }

                        // filter out all font-related attributes, they are handled by the generated span tag above
                        attributeFilter = entry -> !isFontRelated(entry.getKey());
                    } else {
                        // let font attributes through
                        attributeFilter = entry -> true;
                    }
                } else {
                    // filter out all font-related attributes except FONT itself
                    attributeFilter = entry -> !isFontRelated(entry.getKey());
                }

                // filter out font unnecessary font changes
                boolean keepFont = style.getFont().map(f -> !f.equals(currentDefaultFont)).orElse(true);
                style.stream()
                        .filter(attributeFilter)
                        .filter(entry -> !ignoreFontFamily || !entry.getKey().equals(Style.FONT_FAMILIES))
                        .filter(entry -> keepFont || !entry.getKey().equals(Style.FONT))
                        .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
            }
            refineStyleProperties.apply(properties).entrySet().stream().map(e -> HtmlConverter.this.get(e.getKey(), e.getValue())).forEach(tags::add);
            return tags;
        }

        @Override
        protected void appendChars(CharSequence s) {
            buffer.ensureCapacity(buffer.length() + s.length());
            int idx = 0;
            while (idx < s.length()) {
                int idxFound = TextUtil.indexOf(s, RichText.SPLIT_MARKER, idx);
                if (idxFound == -1) {
                    idxFound = s.length();
                }
                TextUtil.appendHtmlEscapedCharacters(buffer, s.subSequence(idx, idxFound));
                idx = idxFound + 1;
            }
        }

        @Override
        protected String get() {
            return buffer.toString();
        }
    }

}

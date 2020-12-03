package com.dua3.utility.text;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HtmlConverter {

    /**
     * An empty tag that does not produce any output.
     */
    public static final HtmlTag EMPTY_TAG = HtmlTag.tag("", "");

    private void addSimpleMapping(String attr, Object value, HtmlTag tag) {
        addMapping(attr, v -> Objects.equals(v,value) ? tag : EMPTY_TAG);
    }
    
    void addDefaultMappings() {
        addSimpleMapping(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD, HtmlTag.tag("<b>", "</b>"));
        addSimpleMapping(TextAttributes.FONT_STYLE, TextAttributes.FONT_STYLE_VALUE_ITALIC, HtmlTag.tag("<i>", "</i>"));
        addSimpleMapping(TextAttributes.TEXT_DECORATION_UNDERLINE, TextAttributes.TEXT_DECORATION_UNDERLINE_VALUE_LINE, HtmlTag.tag("<u>", "</u>"));
        addSimpleMapping(TextAttributes.TEXT_DECORATION_LINE_THROUGH, TextAttributes.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE, HtmlTag.tag("<strike>", "</strike>"));

        addMapping(TextAttributes.FONT_TYPE, value -> {
            if (isUseCss()) {
                switch (value.toString()) {
                    case TextAttributes.FONT_TYPE_VALUE_MONOSPACE:
                        return HtmlTag.tag("<span class=\"monospace\">", "</span>");
                    case TextAttributes.FONT_TYPE_VALUE_SANS_SERIF:
                        return HtmlTag.tag("<span class=\"sans-serif\">", "</span>");
                    case TextAttributes.FONT_TYPE_VALUE_SERIF:
                        return HtmlTag.tag("<span class=\"serif\">", "</span>");
                    default:
                        return EMPTY_TAG;
                }
            } else {
                switch (value.toString()) {
                    case TextAttributes.FONT_TYPE_VALUE_MONOSPACE:
                        return HtmlTag.tag("<code>", "</code>");
                    case TextAttributes.FONT_TYPE_VALUE_SANS_SERIF:
                        return HtmlTag.tag("<span style=\"font-family: sans-serif\">", "</span>");
                    case TextAttributes.FONT_TYPE_VALUE_SERIF:
                        return HtmlTag.tag("<span style=\"font-family: serif\">", "</span>");
                    default:
                        return EMPTY_TAG;
                }
            }
        });
        
        addMapping(TextAttributes.FONT, value -> {
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
    private final Map <String, Function<Object, HtmlTag>> mappings = new HashMap<>();

    /**
     * The default mapper used to generate tags for attributes without mapping.
     */
    private BiFunction<String, Object, HtmlTag> defaultMapper = (attribute, value) -> EMPTY_TAG;

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
    private Function<Object, HtmlTag> combineMappers(Function<Object, HtmlTag> m1, Function<Object, HtmlTag> m2) {
        return new Function<Object, HtmlTag>() {
            @Override
            public HtmlTag apply(Object value) {
                HtmlTag oldTag = m1.apply(value);
                HtmlTag newTag = m2.apply(value);
                return new HtmlTag() {
                    @Override
                    public String open() {
                        return newTag.open()+oldTag.open();
                    }
                    @Override
                    public String close() {
                        return oldTag.open()+newTag.open();
                    }
                };
            }
        };
    }

    /**
     * Replace mapper for an attribute.
     * @param attribute the attribute
     * @param mapper the mapper
     */
    void replaceMapping(String attribute, Function<Object, HtmlTag> mapper) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(mapper);

        mappings.put(attribute, mapper);
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
     * Create a read to use converter with default mappings.
     * @return converter with standard mappings
     */
    public static HtmlConverter createDefault() {
        return new HtmlConverter(HtmlConversionOption.addDefaultMappings());
    }
    
    /**
     * Constructor.
     */
    public HtmlConverter(HtmlConversionOption... options) {
        this(Arrays.asList(options));
    }

    /**
     * Constructor.
     */
    public HtmlConverter(Collection<HtmlConversionOption> options) {
        options.forEach(o -> o.apply(this));
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

    private void appendOpeningTags(Appendable app, List<Style> styles) throws IOException {
        List<HtmlTag> tags = getTags(styles);
        for (int i=0; i<tags.size(); i++) {
            app.append(tags.get(i).open());
        }
    }

    private void appendClosingTags(Appendable app, List<Style> styles) throws IOException {
        List<HtmlTag> tags = getTags(styles);
        for (int i=tags.size()-1; i>=0; i--) {
            app.append(tags.get(i).close());
        }
    }

    private List<HtmlTag> getTags(List<Style> styles) {
        List<HtmlTag> tags = new ArrayList<>();
        for (Style style: styles) {
            style.properties().entrySet().stream()
                    .map(e -> get(e.getKey(), e.getValue()))
                    .forEach(tags::add);
        }
        return tags;
    }

    public <T extends Appendable> T appendTo(T app, RichText text) throws IOException {
        List<Style> openStyles = new LinkedList<>();
        for (Run run: text) {
            List<Style> runStyles = run.getStyles();

            // add closing Tags for styles
            List<Style> closingStyles = new LinkedList<>(openStyles);
            closingStyles.removeAll(runStyles);
            appendClosingTags(app, closingStyles);
            
            // add opening Tags for styles
            List<Style> openingStyles = new LinkedList<>(runStyles);
            openingStyles.removeAll(openStyles);
            appendOpeningTags(app, openingStyles);

            // add text
            TextUtil.appendHtmlEscapedCharacters(app, run);
            
            // update open styles
            openStyles.removeAll(closingStyles);
            openStyles.addAll(openingStyles);
        }
        // close all remeining styles
        appendClosingTags(app, openStyles);
        
        return app;
    }
    
    public StringBuilder appendTo(StringBuilder sb, RichText text) {
        try {
            appendTo((Appendable) sb, text);
            return sb;
        } catch (IOException e) {
            // StringBuilder will not throw IOException
            throw new UncheckedIOException(e);
        }
    }
    
    public String toHtml(RichText text) {
        return appendTo(new StringBuilder(text.length()*12/10), text).toString();
    }

}

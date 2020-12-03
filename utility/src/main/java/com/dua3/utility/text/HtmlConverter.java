package com.dua3.utility.text;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlConverter {

    @FunctionalInterface
    public interface TagMapper {
        enum TagType {
            OPEN,
            CLOSE
        }
        
        String getTags(TagType type, Collection<Style> styles);
    }
    
    private final boolean useCss;
    private final Function<Collection<Style>, String> openingTags;
    private final Function<Collection<Style>, String> closingTags;

    /**
     * Constructor.
     *
     * @param openingTags the generator function for opening tags
     * @param closingTags the generator function for closing tags
     */
    public HtmlConverter(boolean useCss, Function<Collection<Style>,String> openingTags, Function<Collection<Style>,String> closingTags) {
        this.useCss = useCss;
        this.openingTags = Objects.requireNonNull(openingTags);
        this.closingTags = Objects.requireNonNull(closingTags);
    }

    /**
     * Create instance using the default tag mapper.
     */
    public HtmlConverter(boolean useCss) {
        this(useCss, defaultTagMapper(useCss));
    }
    
    /**
     * Constructor.
     *
     * @param mapper the generator function for tags
     */
    public HtmlConverter(boolean useCss, TagMapper mapper) {
        this(useCss, styles -> mapper.getTags(TagMapper.TagType.OPEN, styles), styles -> mapper.getTags(TagMapper.TagType.CLOSE, styles));
    }

    /**
     * Create instance using the default tag mapper.
     */
    public HtmlConverter() {
        this(false);
    }

    /**
     * Whether CSS is used in output.
     * @return true, if CSS output is enabled
     */
    public boolean isUseCss() {
        return useCss;
    }

    /** 
     * Get the default tag mapper.
     * 
     * @return default tag mapper
     */
    public static TagMapper defaultTagMapper(boolean useCss) {
        return useCss ? DEFAULT_TAG_MAPPER_CSS : DEFAULT_TAG_MAPPER_NO_CSS;
    }

    private static final TagMapper DEFAULT_TAG_MAPPER_CSS = (type, styles) -> {
        if (type == TagMapper.TagType.OPEN) {
            return defaultOpeningTags(true, TextAttributes.STYLE_CLASS_DEFAULT, styles);
        } else {
            return defaultClosingTags(true, TextAttributes.STYLE_CLASS_DEFAULT, styles);
        }
    };

    private static final TagMapper DEFAULT_TAG_MAPPER_NO_CSS = (type, styles) -> {
        if (type == TagMapper.TagType.OPEN) {
            return defaultOpeningTags(false, TextAttributes.STYLE_CLASS_DEFAULT, styles);
        } else {
            return defaultClosingTags(false, TextAttributes.STYLE_CLASS_DEFAULT, styles);
        }
    };

    private static Function<String, Function<Object, Pair<String, String>>> initTagMap(boolean useCss) {
        final Pair<String,String> noTag = Pair.of("", "");
        
        Map<String, Function<Object, Pair<String, String>>> m = new HashMap<>();
    
        m.put(TextAttributes.FONT_WEIGHT, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.FONT_WEIGHT_VALUE_BOLD, Pair.of("<b>", "</b>"))
        ), noTag));
        m.put(TextAttributes.FONT_STYLE, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.FONT_STYLE_VALUE_ITALIC, Pair.of("<i>", "</i>"))
        ), noTag));
        m.put(TextAttributes.TEXT_DECORATION_UNDERLINE, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.TEXT_DECORATION_UNDERLINE, Pair.of("<u>", "</u>"))
        ), noTag));
        m.put(TextAttributes.TEXT_DECORATION_LINE_THROUGH, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.TEXT_DECORATION_UNDERLINE, Pair.of("<strike>", "</strike>"))
        ), noTag));
        
        if (useCss) {
            // CSS
            m.put(TextAttributes.FONT_TYPE, DataUtil.asFunction(Pair.toMap(
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_MONOSPACE, Pair.of("<span class=\"monospace\">", "</span>")),
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_SANS_SERIF, Pair.of("<span class=\"sans-serif\">", "</span>")),
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_SERIF, Pair.of("<span class=\"serif\">", "</span>"))
            ), noTag));
            m.put(TextAttributes.FONT, font -> Pair.of("<span class=\""+((Font)font).fontspec()+"\">", "</span>"));
        } else {
            // no CSS
            m.put(TextAttributes.FONT_TYPE, DataUtil.asFunction(Pair.toMap(
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_MONOSPACE, Pair.of("<code>", "</code>")),
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_SANS_SERIF, Pair.of("<span style=\"font-family: sans-serif\">", "</span>")),
                    Pair.of(TextAttributes.FONT_TYPE_VALUE_SERIF, Pair.of("<span style=\"font-family: serif\">", "</span>"))
            ), noTag));
            m.put(TextAttributes.FONT, font -> Pair.of("<span style=\""+((Font)font).getCssStyle()+"\">", "</span>"));
        }
        
        return DataUtil.asFunction(m, attr -> noTag);
    }
    
    private static List<Pair<String,String>> defaultTags(boolean useCss, String styleClass, Collection<Style> styles) {
        Function<String, Function<Object, Pair<String, String>>> tagMap = initTagMap(useCss);
        List<Pair<String,String>> tags = new LinkedList<>();
        for (Style s: styles) {
            if (Objects.equals(s.getOrDefault(TextAttributes.STYLE_CLASS, null), styleClass)) {
                for (Map.Entry<String, Object> entry: s.properties().entrySet()) {
                    String attr = entry.getKey();
                    Object value = entry.getValue();
                    tags.add(tagMap.apply(attr).apply(value));
                }
            }
        }
        return tags;
    }

    public static String defaultOpeningTags(boolean useCss, String styleClass, Collection<Style> styles) {
        return defaultTags(useCss, styleClass, styles).stream().map(p -> p.first).collect(Collectors.joining());
    }

    public static String defaultClosingTags(boolean useCss, String styleClass, Collection<Style> styles) {
        return defaultTags(useCss, styleClass, styles).stream().map(p -> p.second).collect(Collectors.joining());
    }

    public <T extends Appendable> T appendTo(T app, RichText text) throws IOException {
        List<Style> openStyles = new LinkedList<>();
        for (Run run: text) {
            List<Style> runStyles = run.getStyles();

            // add closing Tags for styles
            List<Style> closingStyles = new LinkedList<>(openStyles);
            closingStyles.removeAll(runStyles);
            app.append(closingTags.apply(closingStyles));
            
            // add opening Tags for styles
            List<Style> openingStyles = new LinkedList<>(runStyles);
            openingStyles.removeAll(openStyles);
            app.append(openingTags.apply(openingStyles));

            // add text
            TextUtil.appendHtmlEscapedCharacters(app, run);
            
            // update open styles
            openStyles.removeAll(closingStyles);
            openStyles.addAll(openingStyles);
        }
        // close all remeining styles
        app.append(closingTags.apply(openStyles));
        
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

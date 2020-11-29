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
    
    private static final TagMapper DEFAULT_TAG_MAPPER = (type, styles) -> {
        if (type == TagMapper.TagType.OPEN) {
            return defaultOpeningTags(TextAttributes.STYLE_CLASS_DEFAULT, styles);
        } else {
            return defaultClosingTags(TextAttributes.STYLE_CLASS_DEFAULT, styles);
        }
    };

    private final Function<Collection<Style>, String> openingTags;
    private final Function<Collection<Style>, String> closingTags;

    /**
     * Constructor.
     *
     * @param openingTags the generator function for opening tags
     * @param closingTags the generator function for closing tags
     */
    public HtmlConverter(Function<Collection<Style>,String> openingTags, Function<Collection<Style>,String> closingTags) {
        this.openingTags = Objects.requireNonNull(openingTags);
        this.closingTags = Objects.requireNonNull(closingTags);
    }

    /**
     * Create instance using the default tag mapper.
     */
    public HtmlConverter() {
        this(DEFAULT_TAG_MAPPER);
    }
    
    /**
     * Constructor.
     *
     * @param mapper the generator function for tags
     */
    public HtmlConverter(TagMapper mapper) {
        this(styles -> mapper.getTags(TagMapper.TagType.OPEN, styles), styles -> mapper.getTags(TagMapper.TagType.CLOSE, styles));
    }
    
    /*
     * Map holding the tags to use for TextAttributes.
     * 
     * The mapping is: TextAttribute -> ( attribute value -> ( opening tag, closing tag ) ).
     */
    private static final Function<String, Function<Object, Pair<String, String>>> tagMap = initTagMap();
    
    private static Function<String, Function<Object, Pair<String, String>>> initTagMap() {
        final Pair<String,String> noTag = Pair.of("", "");
        
        
        Map<String, Function<Object, Pair<String, String>>> m = new HashMap<>();
    
        m.put(TextAttributes.FONT_WEIGHT, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.FONT_WEIGHT_VALUE_BOLD, Pair.of("<b>", "</b>"))
        ), noTag));
        m.put(TextAttributes.FONT_STYLE, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.FONT_STYLE_VALUE_ITALIC, Pair.of("<i>", "</i>"))
        ), noTag));
        m.put(TextAttributes.TEXT_DECORATION, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE, Pair.of("<u>", "</u>")),
                Pair.of(TextAttributes.TEXT_DECORATION_VALUE_LINE_THROUGH, Pair.of("<strike>", "</strike>")),
                Pair.of(TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE_LINE_THROUGH, Pair.of("<u><strike>", "</strike></u>"))
        ), noTag));
        m.put(TextAttributes.FONT_FAMILY, DataUtil.asFunction(Pair.toMap(
                Pair.of(TextAttributes.FONT_FAMILY_VALUE_MONOSPACE, Pair.of("<span style=\"font-family: monospace;\">", "</span>")),
                Pair.of(TextAttributes.FONT_FAMILY_VALUE_SANS_SERIF, Pair.of("<span style=\"font-family: sans-serif;\">", "</span>")),
                Pair.of(TextAttributes.FONT_FAMILY_VALUE_SERIF, Pair.of("<span style=\"font-family: serif;\">", "</span>"))
        ), noTag));
        
        return DataUtil.asFunction(m, attr -> noTag);
    }
    
    private static List<Pair<String,String>> defaultTags(String styleClass, Collection<Style> styles) {
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

    public static String defaultOpeningTags(String styleClass, Collection<Style> styles) {
        return defaultTags(styleClass, styles).stream().map(p -> p.first).collect(Collectors.joining());
    }

    public static String defaultClosingTags(String styleClass, Collection<Style> styles) {
        return defaultTags(styleClass, styles).stream().map(p -> p.second).collect(Collectors.joining());
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

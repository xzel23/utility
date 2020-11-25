package com.dua3.utility.text;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlConverter {

    private static final HtmlConverter DEFAULT_CONVERTER = new HtmlConverter(
            styles -> defaultOpeningTags(TextAttributes.STYLE_CLASS_DEFAULT, styles),
            styles -> defaultClosingTags(TextAttributes.STYLE_CLASS_DEFAULT, styles)
    );
    
    private final Function<Collection<Style>, String> openingTags;
    private final Function<Collection<Style>, String> closingTags;

    /**
     * Convert {@link RichText} instance to String representation using the default converter.
     * @param text the text
     * @return String with the same text and HTML markup 
     */
    public static String asHtml(RichText text) {
        return DEFAULT_CONVERTER.append(new StringBuilder(text.length()*3/2), text).toString();
    }

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
        
        return DataUtil.asFunction(m, attr -> noTag);
    }
    
    private Pair<String, String> getTags(String attr, Object value) {
        return tagMap.apply(attr).apply(value);
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

    public <T extends Appendable> T append(T app, RichText text) throws IOException {
        for (Run run: text) {
            // add opening Tags for styles
            app.append(openingTags.apply(run.getRunStartStyles()));

            // add text
            TextUtil.appendHtmlEscapedCharacters(app, run);

            // add closing Tags for styles
            app.append(closingTags.apply(run.getRunStartStyles()));
        }
        return app;
    }
    
    public StringBuilder append(StringBuilder sb, RichText text) {
        for (Run run: text) {
            // add opening Tags for styles
            sb.append(openingTags.apply(run.getRunStartStyles()));

            // add text
            TextUtil.appendHtmlEscapedCharacters(sb, run);

            // add closing Tags for styles
            sb.append(closingTags.apply(run.getRunStartStyles()));
        }
        return sb;
    }
}

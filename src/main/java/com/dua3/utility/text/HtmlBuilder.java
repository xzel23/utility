/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.dua3.utility.Pair;
import com.dua3.utility.text.TextAttributes.Attribute;

/**
 * A {@link TextBuilder} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class HtmlBuilder extends TextBuilder<String> implements AutoCloseable {

    public enum Option {
        /** Header */
        HTML_OPEN(String.class, "<!DOCTYPE html>\n<html>\n<head><meta charset=\"UTF-8\">\n"),
        /** Header */
        HTML_CLOSE(String.class, "</body>\n</html>\n"),
        /** Where to open extern links */
        TARGET_FOR_EXTERN_LINKS(String.class, "_blank"),
        /** Replace '.md' file extension in local links (i.e. with ".html") */
        REPLACEMENT_FOR_MD_EXTENSION_IN_LINK(String.class, null);

        final Class<?> valueClass;
        final Object defaultValue;

        private <T> Option(Class<T> clazz, T dflt) {
            this.valueClass = clazz;
            this.defaultValue = dflt;
        }
    }

    @SafeVarargs
    public static String toHtml(RichText text, Pair<Option, Object>... options) {
        String html;
        try (HtmlBuilder builder = new HtmlBuilder(options)) {
            builder.add(text);
            html = builder.get();
        }
        return html;
    }

    private static Object getOption(Map<Option, Object> optionMap, Option o) {
        return optionMap.getOrDefault(o, o.defaultValue);
    }
    
    /**
     * Add information about opening and closing tags for a style.
     *
     * @param tags
     *            the map that stores mapping stylen ame -> (opening_tag, closing_tag)
     * @param styleName
     *            the style name
     * @param opening
     *            the opening tag(s)
     * @param closing
     *            the closing tag(s), should be in reverse order of the corresponding opening tags
     */
    private static void putTags(Map<String, Pair<Function<Attribute, String>, Function<Attribute, String>>> tags,
            String styleName, Function<Attribute, String> opening, Function<Attribute, String> closing) {
        tags.put(styleName, Pair.of(opening, closing));
    }
    
    private final StringBuilder buffer = new StringBuilder();
    private final String htmlOpen;

    private final String htmlClose;

    private final String targetForExternLinks;

    private final String replaceMdExtensionWith;

    private final Map<String, Pair<Function<Attribute, String>, Function<Attribute, String>>> styleTags = defaultStyleTags();

    public HtmlBuilder(Pair<Option, Object>[] options) {
        Map<Option, Object> optionMap = Pair.toMap(options);

        this.htmlOpen = (String) getOption(optionMap, Option.HTML_OPEN);
        this.htmlClose = (String) getOption(optionMap, Option.HTML_CLOSE);
        this.targetForExternLinks = (String) getOption(optionMap, Option.TARGET_FOR_EXTERN_LINKS);
        this.replaceMdExtensionWith = (String) getOption(optionMap, Option.REPLACEMENT_FOR_MD_EXTENSION_IN_LINK);

        buffer.append(htmlOpen);
    }

    @Override
    public void close() {
        buffer.append(htmlClose);
    }

    private void appendChar(char c) {
        // escape characters as suggested by OWASP.org
        switch (c) {
        case '<':
            buffer.append("&lt;");
            break;
        case '>':
            buffer.append("&gt;");
            break;
        case '&':
            buffer.append("&amp;");
            break;
        case '"':
            buffer.append("&quot;");
            break;
        case '\'':
            buffer.append("&#x27;");
            break;
        case '/':
            buffer.append("&#x2F;");
            break;
        default:
            buffer.append(c);
            break;
        }
    }

    private String appendStyleTags(Run run) {
        StringBuilder openingTag = new StringBuilder();
        StringBuilder closingTag = new StringBuilder();

        // process styles whose runs terminate at this position and insert their closing tags (p.second)
        appendTagsForRun(openingTag, run, TextAttributes.STYLE_END_RUN, p -> p.second);
        // process styles whose runs start at this position and insert their opening tags (p.first)
        appendTagsForRun(openingTag, run, TextAttributes.STYLE_START_RUN, p -> p.first);

        String separator = "<span style=\"";
        String closing = "";
        String closeThisElement = "";
        for (Map.Entry<String, Object> e : run.getStyle().properties().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            if (key.startsWith("__")) {
                // don't create spans for meta info
                continue;
            }

            openingTag.append(separator).append(key).append(":").append(value);
            separator = "; ";
            closing = "\">";
            closeThisElement = "</span>";
        }
        openingTag.append(closing);
        closingTag.append(closeThisElement);

        buffer.append(openingTag);
        return closingTag.toString();
    }

    private void appendTagsForRun(StringBuilder openingTag, Run run, String property,
            Function<Pair<Function<Attribute, String>, Function<Attribute, String>>, Function<Attribute, String>> selector) {
        TextAttributes style = run.getStyle();
        Object value = style.properties().get(property);

        if (value != null) {
            if (!(value instanceof List)) {
                throw new IllegalStateException(
                        "expected a value of class List but got " + value.getClass() + " (property=" + property + ")");
            }

            @SuppressWarnings("unchecked")
            List<Attribute> attributes = (List<Attribute>) value;
            for (Attribute attr : attributes) {
                Pair<Function<Attribute, String>, Function<Attribute, String>> tag = styleTags.get(attr.style.name());
                if (tag != null) {
                    openingTag.append(selector.apply(tag).apply(attr));
                }
            }
        }
    }

    /**
     * Create attribute text for HTML tags.
     *
     * @param args
     *            the current node's arguments
     * @param textAttribute
     *            the TextAttribute to retrieve
     * @param htmlAttribute
     *            the attribute name of the HTML tag
     * @param dflt
     *            the value to use if attribute is not set. pass {@code null} to omit the attribute if not set
     * @return
     *         the text to set the attribute in the HTML tag
     */
    private String attrText(Map<String, Object> args, String textAttribute, String htmlAttribute, String dflt) {
        Object value = args.getOrDefault(textAttribute, dflt);

        if (value == null) {
            return "";
        }

        return " " + htmlAttribute + "=\"" + value.toString() + "\"";
    }

    private Map<String, Pair<Function<Attribute, String>, Function<Attribute, String>>> defaultStyleTags() {
        Map<String, Pair<Function<Attribute, String>, Function<Attribute, String>>> tags = new HashMap<>();

        putTags(tags, MarkDownStyle.BLOCK_QUOTE.name(), attr -> "<blockquote>\n", attr -> "</blockquote>\n");
        putTags(tags, MarkDownStyle.BULLET_LIST.name(), attr -> "<ul>\n", attr -> "</ul>\n");
        putTags(tags, MarkDownStyle.CODE.name(), attr -> "<code>", attr -> "</code>");
        putTags(tags, MarkDownStyle.DOCUMENT.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.EMPHASIS.name(), attr -> "<em>", attr -> "</em>");
        putTags(tags, MarkDownStyle.FENCED_CODE_BLOCK.name(), attr -> "<pre><code>\n", attr -> "</code></pre>\n");
        putTags(tags, MarkDownStyle.HARD_LINE_BREAK.name(), attr -> "<br>\n", attr -> "");
        putTags(tags, MarkDownStyle.HEADING.name(),
                attr -> "\n<h" + attr.args.get(TextAttributes.ATTR_HEADING_LEVEL)
                        + attrText(attr.args, TextAttributes.ATTR_ID, "id", "")
                        + ">",
                attr -> "</h" + attr.args.get(TextAttributes.ATTR_HEADING_LEVEL) + ">\n");
        putTags(tags, MarkDownStyle.THEMATIC_BREAK.name(), attr -> "\n<hr>\n", attr -> "");
        // HTML_BLOCK
        // HTML_INLINE
        putTags(tags, MarkDownStyle.IMAGE.name(),
                attr -> "<img"
                        + attrText(attr.args, TextAttributes.ATTR_IMAGE_SRC, "src", "")
                        + attrText(attr.args, TextAttributes.ATTR_IMAGE_TITLE, "title", null)
                        + attrText(attr.args, TextAttributes.ATTR_IMAGE_ALT, "alt", null)
                        + ">",
                attr -> "");
        putTags(tags, MarkDownStyle.INDENTED_CODE_BLOCK.name(), attr -> "<pre><code>\n", attr -> "</code></pre>\n");
        putTags(tags, MarkDownStyle.LINK.name(),
                attr -> {
                    String href = attr.args.getOrDefault(TextAttributes.ATTR_LINK_HREF, "").toString();
                    if (replaceMdExtensionWith != null) {
                        href = href.replaceAll("(\\.md|\\.MD)(\\?|#|$)", replaceMdExtensionWith + "$2");
                    }
                    String hrefAttr = " href=\"" + href + "\"";

                    //
                    return "<a"
                            + hrefAttr
                            + attrText(attr.args, TextAttributes.ATTR_LINK_TITLE, "title", null)
                            + ifSet(attr.args, TextAttributes.ATTR_LINK_EXTERN,
                                    " target=\"" + targetForExternLinks + "\"")
                            + ">";
                },
                attr -> "</a>");
        putTags(tags, MarkDownStyle.LIST_ITEM.name(), attr -> "<li>", attr -> "</li>\n");
        putTags(tags, MarkDownStyle.ORDERED_LIST.name(), attr -> "<ol>\n", attr -> "</ol>\n");
        putTags(tags, MarkDownStyle.PARAGRAPH.name(), attr -> "<p>", attr -> "</p>");
        putTags(tags, MarkDownStyle.SOFT_LINE_BREAK.name(), attr -> "", attr -> "&shy;\n");
        putTags(tags, MarkDownStyle.STRONG_EMPHASIS.name(), attr -> "<strong>", attr -> "</strong>");
        putTags(tags, MarkDownStyle.TEXT.name(), attr -> "", attr -> "");
        // CUSTOM_BLOCK
        // CUSTOM_NODE

        return Collections.unmodifiableMap(tags);
    }

    private String ifSet(Map<String, Object> args, String textAttribute, String textIfPresent) {
        Object value = args.get(textAttribute);

        boolean isSet;
        if (value instanceof Boolean) {
            isSet = (boolean) value;
        } else {
            isSet = Boolean.valueOf(String.valueOf(value));
        }

        return isSet ? textIfPresent : "";
    }

    @Override
    protected void append(Run run) {
        // handle attributes
        String closeStyleTags = appendStyleTags(run);

        // append text (need to do characterwise because of escaping)
        for (int idx = 0; idx < run.length(); idx++) {
            appendChar(run.charAt(idx));
        }

        // add end tag
        buffer.append(closeStyleTags);
    }

    @Override
    protected String get() {
        return new String(buffer);
    }
}

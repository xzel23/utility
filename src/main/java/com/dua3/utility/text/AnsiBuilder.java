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

/**
 * A {@link RichTextConverter} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class AnsiBuilder extends RichTextConverter<String> {

    public static String toAnsi(RichText text) {
        return new AnsiBuilder().add(text).get();
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
    private static void putTags(Map<String, Pair<Function<Style, String>, Function<Style, String>>> tags,
            String styleName, Function<Style, String> opening, Function<Style, String> closing) {
        tags.put(styleName, Pair.of(opening, closing));
    }

    private StringBuilder buffer = new StringBuilder();

    private final Map<String, Pair<Function<Style, String>, Function<Style, String>>> styleTags = defaultStyleTags();

    public AnsiBuilder() {
    }

    private void appendChar(char c) {
        buffer.append(c);
    }

    /**
     * Append tags to set the style for this run.
     *
     * @param run
     *      the run
     * @return
     *      the closing tags that have to be inserted after the run to reset all styles
     */
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
            Function<Pair<Function<Style, String>, Function<Style, String>>, Function<Style, String>> selector) {
        TextAttributes attrs = run.getStyle();
        Object value = attrs.properties().get(property);

        if (value != null) {
            if (!(value instanceof List)) {
                throw new IllegalStateException(
                        "expected a value of class List but got " + value.getClass() + " (property=" + property + ")");
            }

            @SuppressWarnings("unchecked")
            List<Style> styles = (List<Style>) value;
            for (Style style : styles) {
                Pair<Function<Style, String>, Function<Style, String>> tag = styleTags.get(style.name());
                if (tag != null) {
                    openingTag.append(selector.apply(tag).apply(style));
                }
            }
        }
    }

    /**
     * Create attribute text for HTML tags.
     *
     * @param style
     *            the current style
     * @param property
     *            the TextAttribute to retrieve
     * @param htmlAttribute
     *            the attribute name of the HTML tag
     * @param dflt
     *            the value to use if attribute is not set. pass {@code null} to omit the attribute if not set
     * @return
     *         the text to set the attribute in the HTML tag
     */
    private String attrText(Style style, String property, String htmlAttribute, String dflt) {
        Object value = style.getOrDefault(property, dflt);

        if (value == null) {
            return "";
        }

        return " " + htmlAttribute + "=\"" + value.toString() + "\"";
    }

    private Map<String, Pair<Function<Style, String>, Function<Style, String>>> defaultStyleTags() {
        Map<String, Pair<Function<Style, String>, Function<Style, String>>> tags = new HashMap<>();
/*
        putTags(tags, MarkDownStyle.BLOCK_QUOTE.name(), attr -> "<blockquote>", attr -> "</blockquote>");
        putTags(tags, MarkDownStyle.BULLET_LIST.name(), attr -> "<ul>\n", attr -> "</ul>\n");
        putTags(tags, MarkDownStyle.CODE.name(), attr -> "<code>", attr -> "</code>");
        putTags(tags, MarkDownStyle.DOCUMENT.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.EMPHASIS.name(), attr -> "<em>", attr -> "</em>");
        putTags(tags, MarkDownStyle.FENCED_CODE_BLOCK.name(), attr -> "<pre><code>\n", attr -> "</code></pre>\n");
        putTags(tags, MarkDownStyle.HARD_LINE_BREAK.name(), attr -> "\n<br>", attr -> "");
        putTags(tags, MarkDownStyle.HEADING.name(),
                attr -> "<h" + attr.get(MarkDownStyle.ATTR_HEADING_LEVEL)
                        + attrText(attr, MarkDownStyle.ATTR_ID, "id", "")
                        + ">",
                attr -> "</h" + attr.get(MarkDownStyle.ATTR_HEADING_LEVEL) + ">");
        putTags(tags, MarkDownStyle.THEMATIC_BREAK.name(), attr -> "\n<hr>", attr -> "");
        // HTML_BLOCK
        // HTML_INLINE
        putTags(tags, MarkDownStyle.IMAGE.name(),
                attr -> "<img"
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_SRC, "src", "")
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_TITLE, "title", null)
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_ALT, "alt", null)
                        + ">",
                attr -> "");
        putTags(tags, MarkDownStyle.INDENTED_CODE_BLOCK.name(), attr -> "<pre><code>\n", attr -> "</code></pre>\n");
        putTags(tags, MarkDownStyle.LINK.name(),
                attr -> {
                    String href = attr.getOrDefault(MarkDownStyle.ATTR_LINK_HREF, "").toString();
                    String hrefAttr = " href=\"" + href + "\"";

                    //
                    return "<a"
                            + hrefAttr
                            + attrText(attr, MarkDownStyle.ATTR_LINK_TITLE, "title", null)
                            + ifSet(attr, MarkDownStyle.ATTR_LINK_EXTERN,
                                    " target=\"" + targetForExternLinks + "\"")
                            + ">";
                },
                attr -> "</a>");
        putTags(tags, MarkDownStyle.LIST_ITEM.name(), attr -> "<li>", attr -> "</li>");
        putTags(tags, MarkDownStyle.ORDERED_LIST.name(), attr -> "<ol>\n", attr -> "</ol>\n");
        putTags(tags, MarkDownStyle.PARAGRAPH.name(), attr -> "<p>", attr -> "</p>");
        putTags(tags, MarkDownStyle.SOFT_LINE_BREAK.name(), attr -> "", attr -> "&shy;");
        putTags(tags, MarkDownStyle.STRONG_EMPHASIS.name(), attr -> "<strong>", attr -> "</strong>");
        putTags(tags, MarkDownStyle.TEXT.name(), attr -> "", attr -> "");
        // CUSTOM_BLOCK
        // CUSTOM_NODE
*/
        return Collections.unmodifiableMap(tags);
    }

    private String ifSet(Style style, String textAttribute, String textIfPresent) {
        Object value = style.get(textAttribute);

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
    public String get() {
        String ansi = buffer.toString();
        buffer = null;
        return ansi;
    }

    @Override
    protected boolean wasGetCalled() {
        return buffer == null;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating
 * {@code RichText} to HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class HtmlBuilder extends AbstractStringBasedBuilder {

    private static final Map<String, String> DEFAULT_OPTIONS = LangUtil.map(
            Pair.of(TAG_DOC_START, "<!DOCTYPE html>\n"),
            Pair.of(TAG_TEXT_START, "<html>\n<head><meta charset=\"UTF-8\"></head>\n<body>\n"),
            Pair.of(TAG_TEXT_END, "\n</body>\n</html>\n"),
            Pair.of(TARGET_FOR_EXTERNAL_LINKS, "_blank"),
            Pair.of(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, null));

    @SafeVarargs
    public static String toHtml(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, String>... options) {
        // create map with default options
        Map<String, String> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrrides

        return new HtmlBuilder(styleTraits, optionMap).add(text).get();
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
     *            the value to use if attribute is not set. pass {@code null} to
     *            omit the attribute if not set
     * @return the text to set the attribute in the HTML tag
     */
    private static String attrText(Style style, String property, String htmlAttribute, String dflt) {
        Object value = style.getOrDefault(property, dflt);

        if (value == null) {
            return "";
        }

        return " " + htmlAttribute + "=\"" + value.toString() + "\"";
    }

    private HtmlBuilder(Function<Style, TextAttributes> styleTraits, Map<String, String> options) {
        super(styleTraits, options);
    }

    private String createHRefClose(Style style) {
        return "</a>";
    }

    private String createHRefOpen(Style style) {
        String href = style.getOrDefault(MarkDownStyle.ATTR_LINK_HREF, "").toString();
        if (replaceMdExtensionWith != null) {
            href = href.replaceAll("(\\.md|\\.MD)(\\?|#|$)", replaceMdExtensionWith + "$2");
        }
        String hrefAttr = " href=\"" + href + "\"";

        return "<a"
                + hrefAttr
                + attrText(style, MarkDownStyle.ATTR_LINK_TITLE, "title", null)
                + ifSet(style, MarkDownStyle.ATTR_LINK_EXTERN,
                        " target=\"" + targetForExternalLinks + "\"")
                + ">";
    }

    /**
     * Append a single character to the buffer. Implementations of the method
     * must make sure Special characters (where the meaning of "special" depends
     * upon the concrete implementation) are handled (i.e. quoted) correctly.
     *
     * @param c
     *            the character to append
     */
    protected void appendChar(char c) {
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

    @Override
    protected void appendChars(CharSequence run) {
        for (int idx = 0; idx < run.length(); idx++) {
            appendChar(run.charAt(idx));
        }
    }

    @Override
    protected void applyAttributes(TextAttributes attributes) {
        // TODO Auto-generated method stub

    }

    @Override
    protected RunTraits getTraits(Style style) {
        String styleClass = style.getOrDefault(TextAttributes.STYLE_CLASS, "none").toString();
        String styleName = style.getOrDefault(TextAttributes.STYLE_NAME, "").toString();

        if (styleClass.equals(MarkDownStyle.CLASS)) {
            MarkDownStyle mds = MarkDownStyle.valueOf(styleName);
            switch (mds) {
            case BLOCK_QUOTE:
                return new RunTraits(TextAttributes.none(), "<blockquote>", "</blockquote>");
            case BULLET_LIST:
                return new RunTraits(TextAttributes.none(), "<ul>\n", "</ul>\n");
            case CODE:
                return new RunTraits(TextAttributes.none(), "<code>", "</code>");
            case DOCUMENT:
                return new RunTraits(TextAttributes.none(), "", "");
            case EMPHASIS:
                return new RunTraits(TextAttributes.none(), "<em>", "</em>");
            case FENCED_CODE_BLOCK:
                return new RunTraits(TextAttributes.none(), "<pre><code>\n", "</code></pre>\n");
            case HARD_LINE_BREAK:
                return new RunTraits(TextAttributes.none(), "\n<br>", "");
            case HEADING:
                return new RunTraits(
                        TextAttributes.none(),
                        "<h" + style.get(MarkDownStyle.ATTR_HEADING_LEVEL)
                                + attrText(style, MarkDownStyle.ATTR_ID, "id", "")
                                + ">",
                        "</h" + style.get(MarkDownStyle.ATTR_HEADING_LEVEL) + ">");
            case THEMATIC_BREAK:
                return new RunTraits(TextAttributes.none(), "\n<hr>", "");
            // HTML_BLOCK
            // HTML_INLINE
            case IMAGE:
                return new RunTraits(TextAttributes.none(),
                        "<img"
                                + attrText(style, MarkDownStyle.ATTR_IMAGE_SRC, "src", "")
                                + attrText(style, MarkDownStyle.ATTR_IMAGE_TITLE, "title", null)
                                + attrText(style, MarkDownStyle.ATTR_IMAGE_ALT, "alt", null)
                                + ">",
                        "");
            case INDENTED_CODE_BLOCK:
                return new RunTraits(TextAttributes.none(), "<pre><code>\n", "</code></pre>\n");
            case LINK:
                return new RunTraits(TextAttributes.none(), createHRefOpen(style), createHRefClose(style));
            case LIST_ITEM:
                return new RunTraits(TextAttributes.none(), "<li>", "</li>");
            case ORDERED_LIST:
                return new RunTraits(TextAttributes.none(), "<ol>\n", "</ol>\n");
            case PARAGRAPH:
                return new RunTraits(TextAttributes.none(), "<p>", "</p>");
            case SOFT_LINE_BREAK:
                return new RunTraits(TextAttributes.none(), "", "&shy;");
            case STRONG_EMPHASIS:
                return new RunTraits(TextAttributes.none(), "<strong>", "</strong>");
            // CUSTOM_BLOCK
            // CUSTOM_NODE
            default:
                break;
            }
        }
        return super.getTraits(style);
    }

    protected String ifSet(Style style, String textAttribute, String textIfPresent) {
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
    protected RunTraits createTraits(Run run) {
        TextAttributes attributes = TextAttributes.of(
                run.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, this::getFilteredValue)));
        return new RunTraits(attributes);
    }

    @SuppressWarnings("unchecked")
    private Object getFilteredValue(Map.Entry<String,Object> entry) {
        String key = entry.getKey();
        switch (key) {
        case TextAttributes.STYLE_START_RUN:
        case TextAttributes.STYLE_END_RUN:
            return ((List<Style>) entry.getValue()).stream()
                    .map(s -> MarkDownStyle.CLASS.equals(s.get(TextAttributes.STYLE_CLASS)) ? filterMarkdownStyle(s): s)
                    .collect(Collectors.toList());
        default:
            return entry.getValue();
        }
    }

    private Style filterMarkdownStyle(Style s) {
        String name = s.get(TextAttributes.STYLE_NAME).toString();
        MarkDownStyle mds = MarkDownStyle.valueOf(name);

        switch (mds) {
        case LIST_ITEM:
            // remove prefix (numbering) because HTML adds numbers on its own
            HashMap<String, Object> m = new HashMap<>(s.properties());
            m.put(TextAttributes.TEXT_PREFIX, "");
            return Style.create(name, m);
        default:
            return s;
        }
    }


}

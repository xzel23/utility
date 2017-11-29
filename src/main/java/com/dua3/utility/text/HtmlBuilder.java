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
import java.util.Map;
import java.util.function.Function;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.RichTextConverterBase.RunTraits;
import com.dua3.utility.text.RichTextConverterBase.SimpleRunTraits;

/**
 * A {@link RichTextConverterBase} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class HtmlBuilder extends AbstractStringBasedBuilder {

    private static final Map<String,String> DEFAULT_OPTIONS = LangUtil.map(
            Pair.of(TAG_DOC_START, "<!DOCTYPE html>\n"),
            Pair.of(TAG_TEXT_START, "<html>\n<head><meta charset=\"UTF-8\"></head>\n<body>\n"),
            Pair.of(TAG_TEXT_END, "\n</body>\n</html>\n"),
            Pair.of(TARGET_FOR_EXTERN_LINKS, "_blank"),
            Pair.of(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, null)
            );

    @SafeVarargs
    public static String toHtml(RichText text, Pair<String, String>... options) {
        // create map with default options
        Map<String,String> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrrides

		// create trait supplier
        Function<Style, RunTraits> traitSupplier = style -> new SimpleRunTraits(TextAttributes.none());
        
        return new HtmlBuilder(traitSupplier, optionMap).add(text).get();
    }

    private HtmlBuilder(Function<Style, RunTraits> traitSupplier, Map<String,String> options) {
        super(traitSupplier, options);
    }

    @Override
    protected void appendChars(CharSequence run) {
        for (int idx = 0; idx < run.length(); idx++) {
            appendChar(run.charAt(idx));
        }
    }

    /**
     * Append a single character to the buffer.
     * Implementations of the method must make sure Special characters (where the meaning of "special" depends
     * upon the concrete implementation) are handled (i.e. quoted) correctly.
     * @param c the character to append
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
    protected String attrText(Style style, String property, String htmlAttribute, String dflt) {
        Object value = style.getOrDefault(property, dflt);

        if (value == null) {
            return "";
        }

        return " " + htmlAttribute + "=\"" + value.toString() + "\"";
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
/*
    @Override
    protected Map<String, Pair<Function<Style, String>, Function<Style, String>>> defaultStyleTags() {
        Map<String, Pair<Function<Style, String>, Function<Style, String>>> tags = new HashMap<>();

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
                    if (replaceMdExtensionWith != null) {
                        href = href.replaceAll("(\\.md|\\.MD)(\\?|#|$)", replaceMdExtensionWith + "$2");
                    }
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

        return Collections.unmodifiableMap(tags);
    }
    */

	@Override
	protected void applyAttributes(TextAttributes attributes) {
		// TODO Auto-generated method stub
		
	}
}

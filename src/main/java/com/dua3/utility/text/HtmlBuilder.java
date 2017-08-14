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
public class HtmlBuilder extends TextBuilder<String> {

    private final StringBuilder buffer = new StringBuilder();

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

    private final Map<String, Pair<Function<Attribute,String>, Function<Attribute,String>>> styleTags = defaultStyleTags();

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

    private void appendTagsForRun(StringBuilder openingTag, Run run, String property, Function<Pair<Function<Attribute, String>,Function<Attribute, String>>, Function<Attribute, String>> selector) {
        TextAttributes style = run.getStyle();
        Object value = style.properties().get(property);

        if (value!=null) {
            if (!(value instanceof List)) {
                throw new IllegalStateException("expected a value of class List but got " + value.getClass()+" (property="+property+")");
            }

            @SuppressWarnings("unchecked")
            List<Attribute> attributes = (List<Attribute>) value;
            for (Attribute attr: attributes) {
                Pair<Function<Attribute, String>, Function<Attribute, String>> tag = styleTags.get(attr.style.name());
                if (tag != null) {
                    openingTag.append(selector.apply(tag).apply(attr));
                }
            }
        }
    }

    private static void putTags(Map<String, Pair<Function<Attribute,String>, Function<Attribute,String>>> tags, String styleName, Function<Attribute,String> opening, Function<Attribute,String> closing) {
        tags.put(styleName, Pair.of(opening, closing));
    }

    private Map<String, Pair<Function<Attribute,String>, Function<Attribute,String>>> defaultStyleTags() {
        Map<String, Pair<Function<Attribute,String>, Function<Attribute,String>>> tags = new HashMap<>();

    		putTags(tags, "BLOCK_QUOTE", attr -> "<blockquote>\n", attr -> "</blockquote>\n");
    		putTags(tags, "BULLET_LIST", attr -> "<ul>\n", attr -> "</ul>\n");
    	    putTags(tags, "CODE", attr -> "<code>", attr -> "</code>");
    	    putTags(tags, "DOCUMENT", attr -> "", attr -> "");
    	    putTags(tags, "EMPHASIS", attr -> "<em>", attr -> "</em>");
    	    putTags(tags, "FENCED_CODE_BLOCK", attr -> "<pre>", attr -> "</pre>");
    	    putTags(tags, "HARD_LINE_BREAK", attr -> "<br>", attr -> "\n");
    	    putTags(tags, "HEADING",
    	            attr -> "<h"+attr.args.get(TextAttributes.ATTR_HEADING_LEVEL)+">",
    	            attr -> "</h"+attr.args.get(TextAttributes.ATTR_HEADING_LEVEL)+">\n");
    	    // THEMATIC_BREAK
    	    // HTML_BLOCK
    	    // HTML_INLINE
    	    // IMAGE
            putTags(tags, "IMAGE",
                    attr -> "<img"
                            + " src=\""+attr.args.get(TextAttributes.ATTR_IMAGE_SRC)+"\""
                            + "\">",
                    attr -> "</img>");
    	    // INDENTED_CODE_BLOCK
    	    // LINK
    	    putTags(tags, "LIST_ITEM", attr -> "<li>", attr -> "</li>\n");
    	    putTags(tags, "ORDERED_LIST", attr -> "<ol>\n", attr -> "</ol>\n");
    	    putTags(tags, "PARAGRAPH", attr -> "<p>", attr -> "</p>");
    	    putTags(tags, "SOFT_LINE_BREAK", attr -> "", attr -> "&shy;\n");
    	    putTags(tags, "STRONG_EMPHASIS", attr -> "<strong>", attr -> "</strong>");
    	    putTags(tags, "TEXT", attr -> "", attr -> "");
    	    // CUSTOM_BLOCK
    	    // CUSTOM_NODE

		return Collections.unmodifiableMap(tags);
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

    @Override
    protected String get() {
        return new String(buffer);
    }

    public static String toHtml(RichText text) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.add(text);
        return builder.get();
    }
}

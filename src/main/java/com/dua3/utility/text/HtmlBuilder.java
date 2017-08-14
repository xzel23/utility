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

import com.dua3.utility.Pair;

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
        Style style = run.getStyle();
        String closeStyleTags = appendStyleTags(style);

        // append text (need to do characterwise because of escaping)
        for (int idx = 0; idx < run.length(); idx++) {
            appendChar(run.charAt(idx));
        }

        // add end tag
        buffer.append(closeStyleTags);
    }

    private final Map<String,Pair<String,String>> styleTags = defaultStyleTags();
    
	private String appendStyleTags(Style style) {
		String styleName = style.properties().get(Style.NAME);
		Pair<String, String> tags = styleTags.get(styleName);
		
		String openingTag;
		String closingTag;
		if (tags != null) {
			openingTag = tags.first;
			closingTag = tags.second;
		} else {
			StringBuilder sb = new StringBuilder();
			String separator = "<span style=\"";
	        String closing = "";
	        closingTag = "";
	        for (Map.Entry<String, String> e : style.properties().entrySet()) {
	        		sb.append(separator).append(e.getKey()).append(":").append(e.getValue());
	            separator = "; ";
	            closing = "\">";
	            closingTag = "</span>";
	        }
	        sb.append(closing);
	        openingTag = sb.toString(); 
		}
		
		buffer.append(openingTag);
		return closingTag;
	}

    private Map<String, Pair<String, String>> defaultStyleTags() {
    		Map<String, Pair<String, String>> tags = new HashMap<>();
    		
    	    tags.put("", Pair.of("<>", "</>"));
    	    // BLOCK_QUOTE
    	    tags.put("BULLET_LIST", Pair.of("<ul>", "</ul>"));
    	    // CODE
    	    // DOCUMENT
    	    // EMPHASIS
    	    tags.put("EMPHASIS", Pair.of("<em>", "</em>"));
    	    // FENCED_CODE_BLOCK
    	    tags.put("HARD_LINE_BREAK", Pair.of("<br>", "\n"));
    	    // HEADING
    	    // THEMATIC_BREAK
    	    // HTML_BLOCK
    	    // HTML_INLINE
    	    // IMAGE
    	    // INDENTED_CODE_BLOCK
    	    // LINK
    	    tags.put("LIST_ITEM", Pair.of("<li>", "</li>"));
    	    tags.put("ORDERED_LIST", Pair.of("<ol>", "</ol>"));
    	    tags.put("PARAGRAPH", Pair.of("<p>", "</p>\n"));
    	    // SOFT_LINE_BREAK
    	    // STRONG_EMPHASIS
    	    // TEXT
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

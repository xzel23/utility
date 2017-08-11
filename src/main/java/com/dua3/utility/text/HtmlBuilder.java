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

import java.util.Map;

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

	private String appendStyleTags(Style style) {
		String separator = "<span style=\"";
        String closing = "";
        String endTag = "";
        for (Map.Entry<String, String> e : style.properties().entrySet()) {
            buffer.append(separator).append(e.getKey()).append(":").append(e.getValue());

            separator = "; ";
            closing = "\">";
            endTag = "</span>";
        }
        buffer.append(closing);
		return endTag;
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

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
import com.dua3.utility.io.AnsiCode;
import com.dua3.utility.lang.LangUtil;

/**
 * A {@link RichTextConverter} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class AnsiBuilder extends AbstractStringBasedBuilder {

    private static final Map<String,String> DEFAULT_OPTIONS = LangUtil.map(
            Pair.of(TAG_DOC_START, AnsiCode.reset()),
            Pair.of(TAG_TEXT_START, ""),
            Pair.of(TAG_TEXT_END, "\n"),
            Pair.of(TARGET_FOR_EXTERN_LINKS, "_blank"),
            Pair.of(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, null)
            );

    @SafeVarargs
    public static String toAnsi(RichText text, Pair<String, String>... options) {
        // create map with default options
        Map<String,String> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        // add overrrides
        LangUtil.putAll(optionMap, options);

        return new AnsiBuilder(optionMap).add(text).get();
    }

    private AnsiBuilder(Map<String,String> options) {
        super(options);
    }

    @Override
    protected void appendChar(char c) {
        buffer.append(c);
    }

    @Override
    protected Map<String, Pair<Function<Style, String>, Function<Style, String>>> defaultStyleTags() {
        Map<String, Pair<Function<Style, String>, Function<Style, String>>> tags = new HashMap<>();

        putTags(tags, MarkDownStyle.BLOCK_QUOTE.name(), attr -> "\n", attr -> "\n");
        putTags(tags, MarkDownStyle.BULLET_LIST.name(), attr -> "\n", attr -> "\n");
        putTags(tags, MarkDownStyle.CODE.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.DOCUMENT.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.EMPHASIS.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.FENCED_CODE_BLOCK.name(), attr -> "\n", attr -> "\n");
        putTags(tags, MarkDownStyle.HARD_LINE_BREAK.name(), attr -> "\n", attr -> "");
        putTags(tags, MarkDownStyle.HEADING.name(),
                attr -> AnsiCode.esc(AnsiCode.BOLD_ON)+"\n", attr -> AnsiCode.esc(AnsiCode.BOLD_OFF)+"\n");
        putTags(tags, MarkDownStyle.THEMATIC_BREAK.name(), attr -> "\n ---\n", attr -> "\n");
        // HTML_BLOCK
        // HTML_INLINE
        putTags(tags, MarkDownStyle.IMAGE.name(),
                attr -> "<img"
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_SRC, "src", "")
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_TITLE, "title", null)
                        + attrText(attr, MarkDownStyle.ATTR_IMAGE_ALT, "alt", null)
                        + ">",
                attr -> "");
        putTags(tags, MarkDownStyle.INDENTED_CODE_BLOCK.name(), attr -> "\n", attr -> "\n");
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
        putTags(tags, MarkDownStyle.LIST_ITEM.name(), attr -> "• ", attr -> "\n");
        putTags(tags, MarkDownStyle.ORDERED_LIST.name(), attr -> "\n", attr -> "\n");
        putTags(tags, MarkDownStyle.PARAGRAPH.name(), attr -> "", attr -> "\n");
        putTags(tags, MarkDownStyle.SOFT_LINE_BREAK.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.STRONG_EMPHASIS.name(), attr -> "", attr -> "");
        putTags(tags, MarkDownStyle.TEXT.name(), attr -> "", attr -> "");
        // CUSTOM_BLOCK
        // CUSTOM_NODE

        return Collections.unmodifiableMap(tags);
    }
}

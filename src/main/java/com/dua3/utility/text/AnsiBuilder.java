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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;
import com.dua3.utility.io.AnsiCode;
import com.dua3.utility.lang.LangUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating
 * {@code RichText} to HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class AnsiBuilder extends AbstractStringBasedBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(AnsiBuilder.class);

    private static final Map<String, String> DEFAULT_OPTIONS = LangUtil.map(
            Pair.of(TAG_DOC_START, AnsiCode.reset()),
            Pair.of(TAG_TEXT_START, ""),
            Pair.of(TAG_TEXT_END, "\n"),
            Pair.of(TARGET_FOR_EXTERNAL_LINKS, "_blank"),
            Pair.of(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, null));

    private Color defaultColor = Color.BLACK;

    @SafeVarargs
    public static String toAnsi(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, String>... options) {
        // create map with default options
        Map<String, String> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrrides

        return new AnsiBuilder(styleTraits, optionMap).add(text).get();
    }

    private AnsiBuilder(Function<Style, TextAttributes> styleTraits, Map<String, String> options) {
        super(styleTraits, options);
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    private void addColor(List<Character> esc, char code, Object color) {
        Color c = color == null ? defaultColor : Color.valueOf(color.toString());
        esc.add(code);
        esc.add((char) 2);
        esc.add((char) c.r());
        esc.add((char) c.g());
        esc.add((char) c.b());
    }

    @Override
    protected void applyAttributes(TextAttributes attributes) {
        ArrayList<Character> esc = new ArrayList<>();

        for (Entry<String, Object> entry : attributes.entrySet()) {
            String attribute = entry.getKey();
            Object value = entry.getValue();

            switch (attribute) {
            case TextAttributes.COLOR:
                addColor(esc, AnsiCode.COLOR, value);
                break;
            case TextAttributes.BACKGROUND_COLOR:
                addColor(esc, AnsiCode.BACKGROUND_COLOR, value);
                break;
            case TextAttributes.FONT_WEIGHT:
                if (TextAttributes.FONT_WEIGHT_VALUE_BOLD.equals(value)) {
                    esc.add(AnsiCode.BOLD_ON);
                } else {
                    esc.add(AnsiCode.BOLD_OFF);
                }
                break;
            case TextAttributes.TEXT_DECORATION:
                if (TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE.equals(value)) {
                    esc.add(AnsiCode.UNDERLINE_ON);
                } else {
                    esc.add(AnsiCode.UNDERLINE_OFF);
                }
                break;
            case TextAttributes.FONT_STYLE:
                if (TextAttributes.FONT_STYLE_VALUE_ITALIC.equals(value)
                        || TextAttributes.FONT_STYLE_VALUE_OBLIQUE.equals(value)) {
                    esc.add(AnsiCode.ITALIC_ON);
                } else {
                    esc.add(AnsiCode.ITALIC_OFF);
                }
                break;
            default:
                break;
            }
        }

        try {
            AnsiCode.esc(buffer, esc);
        } catch (IOException e) {
            LOG.error("could not apply text attributes", e);
            throw new UncheckedIOException(e);
        }
    }

}

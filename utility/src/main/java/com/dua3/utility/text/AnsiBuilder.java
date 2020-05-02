// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;
import com.dua3.utility.lang.LangUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating
 * {@code RichText} to HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class AnsiBuilder extends AbstractStringBasedBuilder {

    private static final Map<String, String> DEFAULT_OPTIONS = LangUtil.map(
            Pair.of(TAG_DOC_START, AnsiCode.reset()),
            Pair.of(TAG_TEXT_START, ""),
            Pair.of(TAG_TEXT_END, "\n"),
            Pair.of(TARGET_FOR_EXTERNAL_LINKS, "_blank"),
            Pair.of(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, null));

    @SafeVarargs
    public static String toAnsi(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, String>... options) {
        // create map with default options
        Map<String, String> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrides

        return new AnsiBuilder(styleTraits, optionMap).add(text).get();
    }

    private AnsiBuilder(Function<Style, TextAttributes> styleTraits, Map<String, String> options) {
        super(styleTraits, options);
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static void addColor(Collection<Character> esc, char code, Color c) {
        esc.add(code);
        esc.add((char) 2);
        esc.add((char) c.r());
        esc.add((char) c.g());
        esc.add((char) c.b());
    }

    @Override
    protected void applyAttributes(TextAttributes attributes) {
        List<Character> esc = new ArrayList<>();

        for (Entry<String, Object> entry : attributes.entrySet()) {
            String attribute = entry.getKey();
            Object value = entry.getValue();

            switch (attribute) {
            case TextAttributes.COLOR:
                addColor(esc, AnsiCode.COLOR, getColor(value, getDefaultFgColor()));
                break;
            case TextAttributes.BACKGROUND_COLOR:
                addColor(esc, AnsiCode.BACKGROUND_COLOR, getColor(value, getDefaultBgColor()));
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
            throw new UncheckedIOException("could not apply text attributes", e);
        }
    }

}

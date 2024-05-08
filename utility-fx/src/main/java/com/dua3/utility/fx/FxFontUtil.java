// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.FontUtil;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Utility class for working with fonts in JavaFX.
 */
public class FxFontUtil implements FontUtil<Font> {

    private static class SingletonHolder {
        private static final FxFontUtil INSTANCE = new FxFontUtil();
    }

    /**
     * Returns an instance of FxUtil.
     *
     * @return the instance of FxUtil
     */
    public static FxFontUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Public constructor. Needed fo SPI.
     */
    private FxFontUtil() {
    }

    @Override
    public Font convert(com.dua3.utility.text.Font font) {
        return FxUtil.convert(font);
    }

    /**
     * Converts a JavaFX Font object to a com.dua3.utility.text.Font object.
     *
     * @param font the JavaFX Font object to be converted
     * @return the converted com.dua3.utility.text.Font object
     */
    public com.dua3.utility.text.Font convert(Font font) {
        return FxUtil.convert(font);
    }

    @Override
    public Dimension2f getTextDimension(CharSequence s, com.dua3.utility.text.Font f) {
        var bounds = FxUtil.getTextBounds(s, f);
        return Dimension2f.of((float) bounds.getWidth(), (float) bounds.getHeight());
    }

    @Override
    public double getTextWidth(CharSequence s, com.dua3.utility.text.Font f) {
        return FxUtil.getTextWidth(s, f);
    }

    @Override
    public double getTextHeight(CharSequence s, com.dua3.utility.text.Font f) {
        return FxUtil.getTextHeight(s, f);
    }

    @Override
    public List<com.dua3.utility.text.Font> loadFonts(InputStream in) {
        Font[] fxFonts = Font.loadFonts(in, 0);
        if (fxFonts == null || fxFonts.length == 0) {
            return Collections.emptyList();
        }

        List<com.dua3.utility.text.Font> fonts = new ArrayList<>(fxFonts.length);
        for (Font fxFont : fxFonts) {
            fonts.add(convert(fxFont));
        }

        return Collections.unmodifiableList(fonts);
    }

    private static class FontList {
        // map containing all known font families as keys, with mapping family -> monospaced
        private static final Map<String, Boolean> AVAILABLE_FONTS;
        // alphabetically sorted list of all know font families
        private static final List<String> ALL_FONTS;
        // alphabetically sorted list of all know monospaced font families
        private static final List<String> MONOSPACE_FONTS;
        // alphabetically sorted list of all know proportional font families
        private static final List<String> PROPORTIONAL_FONTS;

        static {
            Map<String, Boolean> fonts = new HashMap<>();
            Font.getFamilies().forEach(f -> fonts.put(f, isMonospaced(f)));
            AVAILABLE_FONTS = Collections.unmodifiableMap(fonts);
            ALL_FONTS = AVAILABLE_FONTS.keySet().stream().sorted().toList();
            MONOSPACE_FONTS = ALL_FONTS.stream().filter(AVAILABLE_FONTS::get).toList();
            PROPORTIONAL_FONTS =  ALL_FONTS.stream().filter(Predicate.not(AVAILABLE_FONTS::get)).toList();
        }
    }

    @Override
    public List<String> getFamilies(FontTypes types) {
        return switch (types) {
            case ALL -> FontList.ALL_FONTS;
            case MONOSPACED -> FontList.MONOSPACE_FONTS;
            case PROPORTIONAL -> FontList.PROPORTIONAL_FONTS;
        };
    }

    private static boolean isMonospaced(String family) {
        Font font = Font.font(family, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
        Text thin = new Text("1 l");
        Text thick = new Text("M_W");
        thin.setFont(font);
        thick.setFont(font);
        return thin.getLayoutBounds().getWidth() == thick.getLayoutBounds().getWidth();
    }

    @Override
    public com.dua3.utility.text.Font loadFontAs(InputStream in, com.dua3.utility.text.Font font) throws IOException {
        Font fxFont = Font.loadFont(in, font.getSizeInPoints());
        LangUtil.check(fxFont != null, () -> new IOException("no font loaded"));
        return new com.dua3.utility.fx.FxFontEmbedded(fxFont, font.getFamily(), font.getSizeInPoints(), font.getColor(), font.isBold(), font.isItalic(), font.isUnderline(), font.isStrikeThrough());
    }

}

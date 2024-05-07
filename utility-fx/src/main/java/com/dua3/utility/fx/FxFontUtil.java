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
import com.dua3.utility.text.FontUtilProvider;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
            String style = fxFont.getStyle().toLowerCase(Locale.ROOT);
            com.dua3.utility.text.Font font = new com.dua3.utility.text.Font(
                    fxFont.getFamily(),
                    (float) fxFont.getSize(),
                    com.dua3.utility.data.Color.BLACK,
                    style.contains("bold"),
                    style.contains("italic") || style.contains("oblique"),
                    style.contains("line-through"),
                    style.contains("line-under")
            );
            fonts.add(font);
        }

        return Collections.unmodifiableList(fonts);
    }

    @Override
    public List<String> getFamilies(FontTypes types) {
        List<String> fonts = Font.getFamilies();

        boolean mono;
        switch (types) {
            case ALL -> {return fonts;}
            case MONOSPACED -> mono = true;
            case PROPORTIONAL -> mono = false;
            default -> throw new IllegalArgumentException("unknown value: " + types);
        }

        List<String> list = new ArrayList<>();

        Text thin = new Text("1 l");
        Text thick = new Text("M_W");
        for (String family : fonts) {
            Font font = Font.font(family, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
            thin.setFont(font);
            thick.setFont(font);
            boolean monospaced = thin.getLayoutBounds().getWidth() == thick.getLayoutBounds().getWidth();
            if (mono == monospaced) {
                list.add(family);
            }
        }

        return list;
    }

    @Override
    public com.dua3.utility.text.Font loadFontAs(InputStream in, com.dua3.utility.text.Font font) throws IOException {
        Font fxFont = Font.loadFont(in, font.getSizeInPoints());
        LangUtil.check(fxFont != null, () -> new IOException("no font loaded"));
        return new com.dua3.utility.fx.FxFontEmbedded(fxFont, font.getFamily(), font.getSizeInPoints(), font.getColor(), font.isBold(), font.isItalic(), font.isUnderline(), font.isStrikeThrough());
    }

}

package com.dua3.utility.text.imp.rtf;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.text.AttributeBasedConverter;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes {@link RichText} as RTF.
 */
public final class RtfWriter extends AttributeBasedConverter<String> {
    private final Map<String, Integer> fontIndexByName;
    private final Map<Color, Integer> colorIndexByColor;

    private RtfWriter(RichText text) {
        LinkedHashMap<String, Integer> fonts = new LinkedHashMap<>();
        LinkedHashMap<Color, Integer> colors = new LinkedHashMap<>();

        for (Run run : text) {
            FontDef fontDef = run.getFontDef();

            String family = fontDef.getFamily();
            if (family != null && !family.isBlank()) {
                fonts.computeIfAbsent(family, key -> fonts.size() + 1);
            }

            Color color = fontDef.getColor();
            if (color != null) {
                colors.computeIfAbsent(color, key -> colors.size() + 1);
            }
        }

        this.fontIndexByName = Map.copyOf(fonts);
        this.colorIndexByColor = Map.copyOf(colors);
    }

    /**
     * Convert rich text to RTF.
     *
     * @param text the text to convert
     * @return the generated RTF string
     */
    public static String write(RichText text) {
        return new RtfWriter(text).convert(text);
    }

    @Override
    protected AttributeBasedConverterImpl<String> createConverter(RichText text) {
        return new RtfWriterImpl();
    }

    private final class RtfWriterImpl extends AttributeBasedConverterImpl<String> {
        private final StringBuilder buffer;
        private boolean closed;

        private RtfWriterImpl() {
            super(Map.of());
            this.buffer = new StringBuilder(256);
            appendHeader();
        }

        @Override
        protected String get() {
            if (!closed) {
                buffer.append('}');
                closed = true;
            }
            return buffer.toString();
        }

        @Override
        protected void apply(Map<String, Pair<@Nullable Object, @Nullable Object>> changedAttributes) {
            // not used; this implementation writes one self-contained group per run
        }

        @Override
        protected void appendChars(CharSequence s) {
            appendEscapedText(buffer, s);
        }

        @Override
        protected AttributeBasedConverterImpl<String> append(RichText text) {
            for (Run run : text) {
                appendRun(run);
            }
            return this;
        }

        private void appendHeader() {
            buffer.append("{\\rtf1\\ansi\\deff0");

            buffer.append("{\\fonttbl");
            buffer.append("{\\f0 ;}");
            for (Map.Entry<String, Integer> entry : fontEntries()) {
                buffer.append("{\\f").append(entry.getValue()).append(' ');
                appendEscapedText(buffer, entry.getKey());
                buffer.append(";}");
            }
            buffer.append('}');

            buffer.append("{\\colortbl ;");
            for (Map.Entry<Color, Integer> entry : colorEntries()) {
                RGBColor color = entry.getKey().toRGBColor();
                buffer.append("\\red").append(color.r())
                        .append("\\green").append(color.g())
                        .append("\\blue").append(color.b())
                        .append(';');
            }
            buffer.append('}');

            buffer.append("\\pard ");
        }

        private void appendRun(Run run) {
            buffer.append('{');

            FontDef fontDef = run.getFontDef();

            String family = fontDef.getFamily();
            if (family != null && !family.isBlank()) {
                int fontIndex = fontIndexByName.getOrDefault(family, 0);
                appendControlWord("f", fontIndex);
            }

            Float size = fontDef.getSize();
            if (size != null && size > 0f) {
                appendControlWord("fs", Math.max(1, Math.round(size * 2f)));
            }

            Color color = fontDef.getColor();
            if (color != null) {
                int colorIndex = colorIndexByColor.getOrDefault(color, 0);
                appendControlWord("cf", colorIndex);
            }

            if (Boolean.TRUE.equals(fontDef.getBold())) {
                appendControlWord("b");
            }
            if (Boolean.TRUE.equals(fontDef.getItalic())) {
                appendControlWord("i");
            }
            if (Boolean.TRUE.equals(fontDef.getUnderline())) {
                appendControlWord("ul");
            }
            if (Boolean.TRUE.equals(fontDef.getStrikeThrough())) {
                appendControlWord("strike");
            }

            appendChars(run);
            buffer.append('}');
        }

        private void appendControlWord(String command) {
            buffer.append('\\').append(command).append(' ');
        }

        private void appendControlWord(String command, int value) {
            buffer.append('\\').append(command).append(value).append(' ');
        }

        private List<Map.Entry<String, Integer>> fontEntries() {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(fontIndexByName.entrySet());
            entries.sort(Map.Entry.comparingByValue());
            return entries;
        }

        private List<Map.Entry<Color, Integer>> colorEntries() {
            List<Map.Entry<Color, Integer>> entries = new ArrayList<>(colorIndexByColor.entrySet());
            entries.sort(Map.Entry.comparingByValue());
            return entries;
        }
    }

    private static void appendEscapedText(StringBuilder buffer, CharSequence text) {
        int i = 0;
        int length = text.length();
        while (i < length) {
            int codePoint = Character.codePointAt(text, i);
            i += Character.charCount(codePoint);

            switch (codePoint) {
                case '\\' -> buffer.append("\\\\");
                case '{' -> buffer.append("\\{");
                case '}' -> buffer.append("\\}");
                case '\t' -> buffer.append("\\tab ");
                case '\n' -> buffer.append("\\line ");
                case '\r' -> {
                    if (i < length && text.charAt(i) == '\n') {
                        i++;
                    }
                    buffer.append("\\line ");
                }
                default -> appendEscapedCodePoint(buffer, codePoint);
            }
        }
    }

    private static void appendEscapedCodePoint(StringBuilder buffer, int codePoint) {
        if (codePoint >= 0x20 && codePoint <= 0x7E) {
            buffer.append((char) codePoint);
            return;
        }

        if (codePoint <= 0xFFFF) {
            appendUnicodeEscape(buffer, codePoint);
            return;
        }

        char[] surrogates = Character.toChars(codePoint);
        for (char surrogate : surrogates) {
            appendUnicodeEscape(buffer, surrogate);
        }
    }

    private static void appendUnicodeEscape(StringBuilder buffer, int utf16Unit) {
        int signedValue = utf16Unit > 0x7FFF ? utf16Unit - 0x10000 : utf16Unit;
        buffer.append("\\u").append(signedValue).append('?');
    }
}

package com.dua3.utility.ui;

import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for converting between RichText and RTF.
 */
public final class RtfConverter {

    private record SingletonHolder(RtfConverter instance) {
        private static final @Nullable RtfConverter INSTANCE = isJavaDesktopAvailable() ? new RtfConverter() : null;

        static Optional<RtfConverter> get() {
            return Optional.ofNullable(INSTANCE);
        }

        private static boolean isJavaDesktopAvailable() {
            if (ModuleLayer.boot().findModule("java.desktop").isEmpty()) {
                return false;
            }

            try {
                Class.forName(RTF_EDITOR_KIT_CLASS_NAME, false, RtfConverter.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    private static final String RTF_EDITOR_KIT_CLASS_NAME = "javax.swing.text.rtf.RTFEditorKit";

    private RtfConverter() {
        // nothing to do
    }

    public static Optional<RtfConverter> get() {
        return SingletonHolder.get();
    }

    /**
     * Converts RichText to RTF.
     *
     * @param text the RichText
     * @return RTF representation
     * @throws IllegalStateException if the java.desktop module is not available, or conversion fails
     */
    public String toRtf(RichText text) {
        try {
            return SwingRtfSupport.toRtf(text);
        } catch (IOException | javax.swing.text.BadLocationException e) {
            throw new IllegalStateException("failed to convert rich text to RTF", e);
        }
    }

    /**
     * Converts RTF to RichText.
     *
     * @param rtf the RTF text
     * @return RichText representation
     * @throws IllegalStateException if the java.desktop module is not available, or conversion fails
     */
    public RichText fromRtf(String rtf) {
        try {
            return SwingRtfSupport.fromRtf(rtf);
        } catch (IOException | javax.swing.text.BadLocationException e) {
            throw new IllegalStateException("failed to convert RTF to rich text", e);
        }
    }

    /**
     * Swing-backed implementation loaded only when java.desktop is available.
     */
    private final class SwingRtfSupport {
        private SwingRtfSupport() { /* utility class */ }

        private static String toRtf(RichText text) throws IOException, javax.swing.text.BadLocationException {
            javax.swing.text.DefaultStyledDocument document = new javax.swing.text.DefaultStyledDocument();

            for (Run run : text.runs()) {
                String runText = stripSplitMarker(run.toString());
                if (runText.isEmpty()) {
                    continue;
                }

                document.insertString(document.getLength(), runText, toSwingAttributes(run));
            }

            javax.swing.text.rtf.RTFEditorKit editorKit = new javax.swing.text.rtf.RTFEditorKit();
            ByteArrayOutputStream output = new ByteArrayOutputStream(Math.max(64, text.length() * 4));
            editorKit.write(output, document, 0, document.getLength());
            return output.toString(StandardCharsets.ISO_8859_1);
        }

        private static RichText fromRtf(String rtf) throws IOException, javax.swing.text.BadLocationException {
            javax.swing.text.DefaultStyledDocument document = new javax.swing.text.DefaultStyledDocument();
            javax.swing.text.rtf.RTFEditorKit editorKit = new javax.swing.text.rtf.RTFEditorKit();
            editorKit.read(new StringReader(rtf), document, 0);

            int length = document.getLength();
            if (length == 0) {
                return RichText.emptyText();
            }

            String documentText = document.getText(0, length);
            int effectiveLength = length;
            // RTFEditorKit adds a paragraph end marker for non-empty documents.
            if (documentText.charAt(length - 1) == '\n') {
                effectiveLength--;
            }
            if (effectiveLength <= 0) {
                return RichText.emptyText();
            }

            RichTextBuilder builder = new RichTextBuilder(effectiveLength);
            int offset = 0;
            while (offset < effectiveLength) {
                javax.swing.text.AttributeSet attributes = document.getCharacterElement(offset).getAttributes();
                int runEnd = Math.min(effectiveLength, document.getCharacterElement(offset).getEndOffset());
                @Nullable Style style = toStyle(attributes);
                if (style != null) {
                    builder.push(style);
                }
                builder.append(documentText, offset, runEnd);
                if (style != null) {
                    builder.pop(style);
                }
                offset = runEnd;
            }

            return builder.toRichText();
        }

        private static javax.swing.text.SimpleAttributeSet toSwingAttributes(Run run) {
            javax.swing.text.SimpleAttributeSet attributes = new javax.swing.text.SimpleAttributeSet();

            FontDef fontDef = run.getAttributes().getFontDef();
            fontDef.merge(run.getFontDef());

            fontDef.ifBoldDefined(bold -> {
                if (Boolean.TRUE.equals(bold)) {
                    javax.swing.text.StyleConstants.setBold(attributes, true);
                }
            });
            fontDef.ifItalicDefined(italic -> {
                if (Boolean.TRUE.equals(italic)) {
                    javax.swing.text.StyleConstants.setItalic(attributes, true);
                }
            });
            fontDef.ifUnderlineDefined(underline -> {
                if (Boolean.TRUE.equals(underline)) {
                    javax.swing.text.StyleConstants.setUnderline(attributes, true);
                }
            });
            fontDef.ifStrikeThroughDefined(strikeThrough -> {
                if (Boolean.TRUE.equals(strikeThrough)) {
                    javax.swing.text.StyleConstants.setStrikeThrough(attributes, true);
                }
            });
            fontDef.ifSizeDefined(size -> {
                int fontSize = Math.max(1, Math.round(size));
                javax.swing.text.StyleConstants.setFontSize(attributes, fontSize);
            });
            fontDef.ifFamiliesDefined(families -> {
                String family = families.stream().filter(s -> !s.isBlank()).findFirst().orElse("");
                if (!family.isEmpty()) {
                    javax.swing.text.StyleConstants.setFontFamily(attributes, family);
                }
            });
            fontDef.ifColorDefined(color -> javax.swing.text.StyleConstants.setForeground(attributes, toAwtColor(color)));

            Object background = run.getAttributes().get(Style.BACKGROUND_COLOR);
            if (background instanceof com.dua3.utility.data.Color color) {
                javax.swing.text.StyleConstants.setBackground(attributes, toAwtColor(color));
            }

            return attributes;
        }

        private static @Nullable Style toStyle(javax.swing.text.AttributeSet attributes) {
            Map<String, Object> properties = new LinkedHashMap<>(8);

            if (javax.swing.text.StyleConstants.isBold(attributes)) {
                properties.put(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
            }
            if (javax.swing.text.StyleConstants.isItalic(attributes)) {
                properties.put(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
            }
            if (javax.swing.text.StyleConstants.isUnderline(attributes)) {
                properties.put(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);
            }
            if (javax.swing.text.StyleConstants.isStrikeThrough(attributes)) {
                properties.put(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE);
            }
            if (attributes.isDefined(javax.swing.text.StyleConstants.FontFamily)) {
                String family = javax.swing.text.StyleConstants.getFontFamily(attributes);
                if (!family.isBlank()) {
                    properties.put(Style.FONT_FAMILIES, List.of(family));
                }
            }
            if (attributes.isDefined(javax.swing.text.StyleConstants.FontSize)) {
                int size = javax.swing.text.StyleConstants.getFontSize(attributes);
                if (size > 0) {
                    properties.put(Style.FONT_SIZE, (float) size);
                }
            }

            java.awt.Color foreground = javax.swing.text.StyleConstants.getForeground(attributes);
            if (foreground != null && !isDefaultForegroundColor(foreground)) {
                properties.put(Style.COLOR, fromAwtColor(foreground));
            }

            if (attributes.isDefined(javax.swing.text.StyleConstants.Background)) {
                java.awt.Color background = javax.swing.text.StyleConstants.getBackground(attributes);
                if (background != null) {
                    properties.put(Style.BACKGROUND_COLOR, fromAwtColor(background));
                }
            }

            return properties.isEmpty() ? null : Style.create("rtf", properties);
        }

        private static java.awt.Color toAwtColor(com.dua3.utility.data.Color color) {
            return new java.awt.Color(color.argb(), true);
        }

        private static com.dua3.utility.data.Color fromAwtColor(java.awt.Color color) {
            return com.dua3.utility.data.Color.rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }

        private static boolean isDefaultForegroundColor(java.awt.Color color) {
            return color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0 && color.getAlpha() == 255;
        }

        private static String stripSplitMarker(String text) {
            if (text.indexOf(RichText.SPLIT_MARKER) < 0) {
                return text;
            }

            StringBuilder sb = new StringBuilder(text.length());
            text.codePoints().filter(cp -> cp != RichText.SPLIT_MARKER).forEach(sb::appendCodePoint);
            return sb.toString();
        }
    }
}

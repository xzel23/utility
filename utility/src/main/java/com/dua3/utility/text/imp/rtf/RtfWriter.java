package com.dua3.utility.text.imp.rtf;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.text.AttributeBasedConverter;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.VAnchor;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Writes {@link RichText} as RTF.
 */
public final class RtfWriter extends AttributeBasedConverter<String> {
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final double TWIPS_PER_PIXEL = 15.0;
    private static final double POINTS_PER_TWIP = 1.0 / 20.0;
    private static final double POINTS_PER_PIXEL = 0.75;
    private static final double DEFAULT_FONT_SIZE_PT = 12.0;
    private static final double DEFAULT_ASCENT_RATIO = 0.8;
    private static final double DEFAULT_DESCENT_RATIO = 0.2;
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

            appendRunContent(run, fontDef);
            buffer.append('}');
        }

        private void appendRunContent(Run run, FontDef fontDef) {
            InlineImageExportData imageData = findInlineImageExportData(run);
            if (imageData == null) {
                appendChars(run);
                return;
            }

            int length = run.length();
            int segmentStart = 0;
            for (int i = 0; i < length; i++) {
                char ch = run.charAt(i);
                if (ch == RichTextBuilderExtBase.INLINE_NODE_MARKER) {
                    if (segmentStart < i) {
                        appendEscapedText(buffer, run.subSequence(segmentStart, i));
                    }
                    if (!appendPicture(imageData, fontDef)) {
                        appendEscapedCodePoint(buffer, ch);
                    }
                    segmentStart = i + 1;
                }
            }

            if (segmentStart < length) {
                appendEscapedText(buffer, run.subSequence(segmentStart, length));
            }
        }

        private static @Nullable InlineImageExportData findInlineImageExportData(Run run) {
            String text = run.toString();
            List<Style> styles = run.getStyles();
            for (int i = styles.size() - 1; i >= 0; i--) {
                Style style = styles.get(i);
                InlineNode<?> fromFactory = evaluateInlineNodeFactory(style, text);
                if (fromFactory != null) {
                    return new InlineImageExportData(
                            fromFactory,
                            style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR) instanceof VAnchor vAnchor ? vAnchor : null,
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT)),
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH)),
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT))
                    );
                }
                Object value = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE);
                if (value instanceof InlineNode<?> inlineNode) {
                    return new InlineImageExportData(
                            inlineNode,
                            style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR) instanceof VAnchor vAnchor ? vAnchor : null,
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT)),
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH)),
                            toDouble(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT))
                    );
                }
            }
            return null;
        }

        private static @Nullable Double toDouble(@Nullable Object value) {
            if (value instanceof Number n) {
                double v = n.doubleValue();
                return Double.isFinite(v) ? v : null;
            }
            return null;
        }

        private static @Nullable InlineNode<?> evaluateInlineNodeFactory(Style style, String text) {
            Object factory = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY);
            if (!(factory instanceof Function<?, ?> f)) {
                return null;
            }

            try {
                @SuppressWarnings("unchecked")
                Function<String, ?> fn = (Function<String, ?>) f;
                Object value = fn.apply(text);
                return value instanceof InlineNode<?> inlineNode ? inlineNode : null;
            } catch (RuntimeException ex) {
                return null;
            }
        }

        private boolean appendPicture(InlineImageExportData imageData, FontDef fontDef) {
            Image image = toImage(imageData.inlineNode());
            if (image == null) {
                return false;
            }

            byte[] png = encodeImageAsPng(image);
            if (png.length == 0) {
                return false;
            }

            int nativeWidth = Math.max(1, image.width());
            int nativeHeight = Math.max(1, image.height());
            PictureTarget target = computePictureTarget(nativeWidth, nativeHeight, imageData.maxWidth(), imageData.maxHeight());
            int baselineShiftHalfPoints = computeBaselineShiftHalfPoints(imageData.vAnchor(), imageData.descent(), fontDef, target.displayHeightTwips());

            if (baselineShiftHalfPoints > 0) {
                appendControlWord("up", baselineShiftHalfPoints);
            } else if (baselineShiftHalfPoints < 0) {
                appendControlWord("dn", -baselineShiftHalfPoints);
            }

            buffer.append("{\\pict\\pngblip");
            appendControlWord("picw", nativeWidth);
            appendControlWord("pich", nativeHeight);
            appendControlWord("picwgoal", target.widthGoalTwips());
            appendControlWord("pichgoal", target.heightGoalTwips());
            if (target.scaled()) {
                appendControlWord("picscalex", target.scaleXPercent());
                appendControlWord("picscaley", target.scaleYPercent());
            }
            appendHexBytes(buffer, png);
            buffer.append('}');
            if (baselineShiftHalfPoints != 0) {
                appendControlWord("up", 0);
            }
            return true;
        }

        private static @Nullable Image toImage(InlineNode<?> inlineNode) {
            try {
                return InlineNode.decodeArgbImageData(inlineNode.getData());
            } catch (IllegalArgumentException ignored) {
                Object wrapped = inlineNode.getWrapped();
                return wrapped instanceof Image image ? image : null;
            }
        }

        private static byte[] encodeImageAsPng(Image image) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
                ImageUtil.getInstance().write(image, out, "image/png");
                return out.toByteArray();
            } catch (IOException | RuntimeException ex) {
                return new byte[0];
            }
        }

        private static PictureTarget computePictureTarget(
                int nativeWidth,
                int nativeHeight,
                @Nullable Double maxWidth,
                @Nullable Double maxHeight
        ) {
            double width = nativeWidth;
            double height = nativeHeight;

            boolean hasMaxWidth = maxWidth != null && maxWidth > 0;
            boolean hasMaxHeight = maxHeight != null && maxHeight > 0;
            if (hasMaxWidth || hasMaxHeight) {
                double sx = hasMaxWidth ? maxWidth / nativeWidth : Double.POSITIVE_INFINITY;
                double sy = hasMaxHeight ? maxHeight / nativeHeight : Double.POSITIVE_INFINITY;
                double scale = Math.min(sx, sy);
                if (!Double.isFinite(scale) || scale <= 0.0) {
                    scale = 1.0;
                }
                width = nativeWidth * scale;
                height = nativeHeight * scale;
            }

            int widthGoalTwips = Math.max(1, (int) Math.round(nativeWidth * TWIPS_PER_PIXEL));
            int heightGoalTwips = Math.max(1, (int) Math.round(nativeHeight * TWIPS_PER_PIXEL));
            int displayWidthTwips = Math.max(1, (int) Math.round(width * TWIPS_PER_PIXEL));
            int displayHeightTwips = Math.max(1, (int) Math.round(height * TWIPS_PER_PIXEL));

            int scaleXPercent = Math.max(1, (int) Math.round(width * 100.0 / nativeWidth));
            int scaleYPercent = Math.max(1, (int) Math.round(height * 100.0 / nativeHeight));
            boolean scaled = Math.abs(width - nativeWidth) > 1e-6 || Math.abs(height - nativeHeight) > 1e-6;
            return new PictureTarget(widthGoalTwips, heightGoalTwips, displayWidthTwips, displayHeightTwips, scaleXPercent, scaleYPercent, scaled);
        }

        private static int computeBaselineShiftHalfPoints(
                @Nullable VAnchor vAnchor,
                @Nullable Double inlineDescent,
                FontDef fontDef,
                int pictureHeightTwips
        ) {
            VAnchor anchor = vAnchor == null ? VAnchor.BASELINE : vAnchor;

            double fontSizePt = fontDef.getSize() != null && fontDef.getSize() > 0
                    ? fontDef.getSize()
                    : DEFAULT_FONT_SIZE_PT;
            double ascentPt = fontSizePt * DEFAULT_ASCENT_RATIO;
            double descentPt = fontSizePt * DEFAULT_DESCENT_RATIO;
            double pictureHeightPt = pictureHeightTwips * POINTS_PER_TWIP;
            double explicitDescentPt = inlineDescent != null && inlineDescent >= 0.0
                    ? inlineDescent * POINTS_PER_PIXEL
                    : 0.0;

            double deltaDownPt = switch (anchor) {
                case BASELINE -> explicitDescentPt;
                case BOTTOM -> descentPt;
                case TOP -> pictureHeightPt - ascentPt;
                case MIDDLE -> (pictureHeightPt + descentPt - ascentPt) / 2.0;
            };

            double upShiftPt = -deltaDownPt;
            return (int) Math.round(upShiftPt * 2.0);
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

    private static void appendHexBytes(StringBuilder buffer, byte[] data) {
        for (byte value : data) {
            int b = value & 0xFF;
            buffer.append(HEX[b >>> 4]).append(HEX[b & 0x0F]);
        }
    }

    private record InlineImageExportData(
            InlineNode<?> inlineNode,
            @Nullable VAnchor vAnchor,
            @Nullable Double descent,
            @Nullable Double maxWidth,
            @Nullable Double maxHeight
    ) {}

    private record PictureTarget(
            int widthGoalTwips,
            int heightGoalTwips,
            int displayWidthTwips,
            int displayHeightTwips,
            int scaleXPercent,
            int scaleYPercent,
            boolean scaled
    ) {}
}

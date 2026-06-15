package com.dua3.utility.text.imp.rtf;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.io.Payload;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.ui.InlineNode;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullUnmarked;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads RTF input and converts it to {@link RichText}.
 */
public final class RtfReader {
    private static final String STANDARD_RTF_PARSER_CLASS = "com.rtfparserkit.parser.standard.StandardRtfParser";
    private static final String RTF_SOURCE_CLASS = "com.rtfparserkit.parser.RtfStringSource";
    private static final String RTF_SOURCE_INTERFACE = "com.rtfparserkit.parser.IRtfSource";
    private static final String RTF_LISTENER_INTERFACE = "com.rtfparserkit.parser.IRtfListener";

    private RtfReader() {
        // utility class
    }

    /**
     * Reads and parses the provided RTF (Rich Text Format) string, converting it into a RichText object.
     *
     * @param rtf the RTF string to be parsed; must not be null or empty
     * @return a RichText object representing the parsed content of the RTF string
     * @throws NullPointerException if the provided RTF string is null
     */
    public static RichText read(String rtf) {
        if (rtf.isEmpty()) {
            return RichText.emptyText();
        }

        StyledRtfParser parser = new StyledRtfParser();
        parser.parse(rtf);
        return parser.toRichText();
    }

    @NullUnmarked
    private static final class StyledRtfParser {
        private final RichTextBuilder builder = new RichTextBuilder();
        private final ArrayDeque<CharacterStyle> styleStack = new ArrayDeque<>();
        private final Map<StyleKey, Style> styleCache = new HashMap<>();
        private final Map<Integer, String> fontTable = new HashMap<>();
        private final Map<Integer, Color> colorTable = new HashMap<>();

        private CharacterStyle style = new CharacterStyle();
        private int defaultFontIndex = -1;
        private int groupDepth = 0;

        private boolean inFontTable = false;
        private int fontTableDepth = -1;
        private Integer currentFontIndex = null;
        private final StringBuilder currentFontName = new StringBuilder();

        private boolean inColorTable = false;
        private int colorTableDepth = -1;
        private int currentColorEntryIndex = 0;
        private int currentRed = 0;
        private int currentGreen = 0;
        private int currentBlue = 0;
        private boolean hasColorComponent = false;
        private int ignoredDestinationDepth = -1;
        private boolean inPicture = false;
        private int pictureDepth = -1;
        private final StringBuilder pictureHexData = new StringBuilder();
        private String pictureMimeType = "image/jpeg";
        private int inlineStyleId = 0;

        private void parse(String rtf) {
            try {
                Class<?> parserClass = Class.forName(STANDARD_RTF_PARSER_CLASS);
                Object parser = parserClass.getDeclaredConstructor().newInstance();

                Class<?> sourceClass = Class.forName(RTF_SOURCE_CLASS);
                Object source = sourceClass.getDeclaredConstructor(String.class).newInstance(rtf);

                Class<?> sourceInterface = Class.forName(RTF_SOURCE_INTERFACE);
                Class<?> listenerInterface = Class.forName(RTF_LISTENER_INTERFACE);
                Object listener = Proxy.newProxyInstance(
                        RtfReader.class.getClassLoader(),
                        new Class<?>[]{listenerInterface},
                        new ParserInvocationHandler(this)
                );

                Method parse = parserClass.getMethod("parse", sourceInterface, listenerInterface);
                parse.invoke(parser, source, listener);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("rtfparserkit is not available on the classpath", e);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("failed to invoke rtfparserkit parser", e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("failed to parse RTF", e.getCause() == null ? e : e.getCause());
            }
        }

        private RichText toRichText() {
            return builder.length() == 0 ? RichText.emptyText() : builder.toRichText();
        }

        private @Nullable Object handleParserEvent(Object proxy, Method method, Object @Nullable [] args) {
            String methodName = method.getName();
            switch (methodName) {
                case "processGroupStart" -> onGroupStart();
                case "processGroupEnd" -> onGroupEnd();
                case "processString" -> {
                    String text = args != null && args.length > 0 && args[0] instanceof String s ? s : "";
                    onString(text);
                }
                case "processCommand" -> {
                    if (args != null && args.length >= 4) {
                        String command = args[0] instanceof Enum<?> e ? e.name() : String.valueOf(args[0]);
                        int parameter = args[1] instanceof Number n ? n.intValue() : 0;
                        boolean hasParameter = args[2] instanceof Boolean b && b;
                        boolean optional = args[3] instanceof Boolean b && b;
                        onCommand(command, parameter, hasParameter, optional);
                    }
                }
                default -> {
                    // ignore all other callbacks
                }
            }
            return null;
        }

        private void onGroupStart() {
            styleStack.push(style.copy());
            groupDepth++;
            if (inFontTable && groupDepth == fontTableDepth + 1) {
                currentFontIndex = null;
                currentFontName.setLength(0);
            }
        }

        private void onGroupEnd() {
            if (inFontTable && groupDepth == fontTableDepth + 1) {
                finalizeCurrentFontEntry();
            }
            if (inFontTable && groupDepth == fontTableDepth) {
                inFontTable = false;
                fontTableDepth = -1;
                currentFontIndex = null;
                currentFontName.setLength(0);
            }
            if (inColorTable && groupDepth == colorTableDepth) {
                inColorTable = false;
                colorTableDepth = -1;
            }
            if (ignoredDestinationDepth >= 0 && groupDepth == ignoredDestinationDepth) {
                ignoredDestinationDepth = -1;
            }
            if (inPicture && groupDepth == pictureDepth) {
                finalizePicture();
                inPicture = false;
                pictureDepth = -1;
                pictureHexData.setLength(0);
                pictureMimeType = "image/jpeg";
            }

            style = styleStack.isEmpty() ? new CharacterStyle(defaultFontIndex) : styleStack.pop();
            if (groupDepth > 0) {
                groupDepth--;
            }
        }

        private void onString(String text) {
            if (text.isEmpty()) {
                return;
            }
            if (inPicture) {
                consumePictureText(text);
                return;
            }
            if (isIgnoringDestination()) {
                return;
            }

            if (inFontTable) {
                consumeFontTableText(text);
                return;
            }
            if (inColorTable) {
                consumeColorTableText(text);
                return;
            }

            appendStyledText(TextUtil.normalize(text));
        }

        private void onCommand(String command, int parameter, boolean hasParameter, boolean optional) {
            if (inPicture) {
                consumePictureCommand(command);
                return;
            }
            if (isIgnorableDestination(command, optional)) {
                if (ignoredDestinationDepth < 0 || groupDepth < ignoredDestinationDepth) {
                    ignoredDestinationDepth = groupDepth;
                }
                return;
            }
            if (isIgnoringDestination()) {
                return;
            }
            if ("pict".equals(command)) {
                inPicture = true;
                pictureDepth = groupDepth;
                pictureHexData.setLength(0);
                pictureMimeType = "image/jpeg";
                return;
            }

            if ("fonttbl".equals(command)) {
                inFontTable = true;
                fontTableDepth = groupDepth;
                return;
            }

            if ("colortbl".equals(command)) {
                inColorTable = true;
                colorTableDepth = groupDepth;
                currentColorEntryIndex = 0;
                currentRed = 0;
                currentGreen = 0;
                currentBlue = 0;
                hasColorComponent = false;
                return;
            }

            if (inFontTable) {
                if ("f".equals(command) && hasParameter) {
                    currentFontIndex = parameter;
                }
                return;
            }

            if (inColorTable) {
                switch (command) {
                    case "red" -> {
                        if (hasParameter) {
                            currentRed = clampColor(parameter);
                            hasColorComponent = true;
                        }
                    }
                    case "green" -> {
                        if (hasParameter) {
                            currentGreen = clampColor(parameter);
                            hasColorComponent = true;
                        }
                    }
                    case "blue" -> {
                        if (hasParameter) {
                            currentBlue = clampColor(parameter);
                            hasColorComponent = true;
                        }
                    }
                    default -> {
                        // ignore other commands in color table
                    }
                }
                return;
            }

            switch (command) {
                case "deff" -> {
                    if (hasParameter) {
                        defaultFontIndex = parameter;
                        if (style.fontIndex < 0) {
                            style.fontIndex = parameter;
                        }
                    }
                }
                case "plain" -> style = new CharacterStyle(defaultFontIndex);
                case "b" -> style.bold = !hasParameter || parameter != 0;
                case "i" -> style.italic = !hasParameter || parameter != 0;
                case "ul" -> style.underline = !hasParameter || parameter != 0;
                case "ulnone" -> style.underline = false;
                case "strike" -> style.strikeThrough = !hasParameter || parameter != 0;
                case "cf" -> {
                    if (hasParameter) {
                        style.colorIndex = Math.max(0, parameter);
                    }
                }
                case "f" -> {
                    if (hasParameter) {
                        style.fontIndex = parameter;
                    }
                }
                case "fs" -> {
                    if (hasParameter && parameter > 0) {
                        style.fontSize = parameter / 2f;
                    }
                }
                case "par", "line" -> appendStyledText("\n");
                case "tab" -> appendStyledText("\t");
                default -> {
                    // ignore unsupported control words
                }
            }
        }

        private void consumePictureCommand(String command) {
            switch (command) {
                case "jpegblip" -> pictureMimeType = "image/jpeg";
                case "pngblip" -> pictureMimeType = "image/png";
                default -> {
                    // ignore other picture properties (e.g., dimensions, scaling)
                }
            }
        }

        private void consumePictureText(String text) {
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (Character.digit(ch, 16) >= 0) {
                    pictureHexData.append(ch);
                }
            }
        }

        private void finalizePicture() {
            byte[] imageBytes = decodeHex(pictureHexData);
            if (imageBytes.length == 0) {
                appendStyledText("Image: ");
                return;
            }

            try (Payload payload = Payload.fromInputStream(new ByteArrayInputStream(imageBytes))) {
                Image image = ImageUtil.getInstance().load(payload);
                byte[] inlineData = InlineNode.encodeArgbImageData(image);
                InlineNode<Image> inlineNode = new InlineNode<>(image, inlineNodeMimeType(pictureMimeType), inlineData);
                appendInlineNodeMarker(inlineNode);
            } catch (IOException | RuntimeException ex) {
                appendStyledText("Image: ");
            }
        }

        private static String inlineNodeMimeType(String pictureMimeType) {
            if ("image/jpeg".equals(pictureMimeType)) {
                return pictureMimeType;
            }
            return "image/jpeg";
        }

        private void appendInlineNodeMarker(InlineNode<?> inlineNode) {
            Style inlineStyle = Style.create(
                    "rtf-inline-node-" + inlineStyleId++,
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, inlineNode)
            );
            appendStyledText(String.valueOf(RichTextBuilderExtBase.INLINE_NODE_MARKER), inlineStyle);
        }

        private void consumeFontTableText(String text) {
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == ';') {
                    finalizeCurrentFontEntry();
                } else if (ch != '\r' && ch != '\n') {
                    currentFontName.append(ch);
                }
            }
        }

        private void finalizeCurrentFontEntry() {
            String fontName = currentFontName.toString().trim();
            if (currentFontIndex != null && !fontName.isEmpty()) {
                fontTable.put(currentFontIndex, fontName);
            }
            currentFontIndex = null;
            currentFontName.setLength(0);
        }

        private void consumeColorTableText(String text) {
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == ';') {
                    if (hasColorComponent) {
                        colorTable.put(currentColorEntryIndex, Color.rgb(currentRed, currentGreen, currentBlue));
                    }
                    currentColorEntryIndex++;
                    currentRed = 0;
                    currentGreen = 0;
                    currentBlue = 0;
                    hasColorComponent = false;
                }
            }
        }

        private void appendStyledText(String text) {
            appendStyledText(text, null);
        }

        private void appendStyledText(String text, @Nullable Style extraStyle) {
            if (text.isEmpty()) {
                return;
            }

            Style styleForText = resolveStyle();
            if (styleForText == null && extraStyle == null) {
                builder.append(text);
            } else {
                if (styleForText != null) {
                    builder.push(styleForText);
                }
                if (extraStyle != null) {
                    builder.push(extraStyle);
                }
                builder.append(text);
                if (extraStyle != null) {
                    builder.pop(extraStyle);
                }
                if (styleForText != null) {
                    builder.pop(styleForText);
                }
            }
        }

        private Style resolveStyle() {
            StyleKey key = StyleKey.from(style, fontTable, colorTable);
            if (key.isEmpty()) {
                return null;
            }

            return styleCache.computeIfAbsent(key, k -> {
                List<Map.Entry<String, Object>> entries = new ArrayList<>(7);
                if (k.bold()) {
                    entries.add(Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));
                }
                if (k.italic()) {
                    entries.add(Map.entry(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC));
                }
                if (k.underline()) {
                    entries.add(Map.entry(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE));
                }
                if (k.strikeThrough()) {
                    entries.add(Map.entry(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE));
                }
                if (k.color() != null) {
                    entries.add(Map.entry(Style.COLOR, k.color()));
                }
                if (k.fontSize() != null) {
                    entries.add(Map.entry(Style.FONT_SIZE, k.fontSize()));
                }
                if (k.fontFamily() != null) {
                    entries.add(Map.entry(Style.FONT_FAMILIES, List.of(k.fontFamily())));
                }

                @SuppressWarnings("unchecked")
                Map.Entry<String, Object>[] styleEntries = entries.toArray(Map.Entry[]::new);
                return Style.create("rtf-style-" + styleCache.size(), styleEntries);
            });
        }

        private static int clampColor(int c) {
            return Math.min(255, Math.max(0, c));
        }

        private static byte[] decodeHex(CharSequence hex) {
            int length = hex.length();
            if (length < 2) {
                return new byte[0];
            }

            int evenLength = length & ~1;
            byte[] result = new byte[evenLength / 2];
            for (int i = 0; i < evenLength; i += 2) {
                int high = Character.digit(hex.charAt(i), 16);
                int low = Character.digit(hex.charAt(i + 1), 16);
                if (high < 0 || low < 0) {
                    return new byte[0];
                }
                result[i / 2] = (byte) ((high << 4) | low);
            }
            return result;
        }

        private boolean isIgnoringDestination() {
            return ignoredDestinationDepth >= 0 && groupDepth >= ignoredDestinationDepth;
        }

        private static boolean isIgnorableDestination(String command, boolean optional) {
            return optional
                    || "optionalcommand".equals(command)
                    || "stylesheet".equals(command)
                    || "listtable".equals(command)
                    || "listoverridetable".equals(command)
                    || "info".equals(command)
                    || "userprops".equals(command);
        }
    }

    private static final class ParserInvocationHandler implements InvocationHandler {
        private static final Object[] NO_ARGS = new Object[0];
        private final StyledRtfParser parser;

        private ParserInvocationHandler(StyledRtfParser parser) {
            this.parser = parser;
        }

        @Override
        public @Nullable Object invoke(Object proxy, Method method, Object @Nullable [] args) {
            return parser.handleParserEvent(proxy, method, args == null ? NO_ARGS : args);
        }
    }

    private record StyleKey(
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikeThrough,
            @Nullable Color color,
            @Nullable Float fontSize,
            @Nullable String fontFamily
    ) {
        private static StyleKey from(CharacterStyle style, Map<Integer, String> fontTable, Map<Integer, Color> colorTable) {
            Color color = style.colorIndex > 0 ? colorTable.get(style.colorIndex) : null;
            Float fontSize = style.fontSize > 0 ? style.fontSize : null;
            String fontFamily = style.fontIndex >= 0 ? fontTable.get(style.fontIndex) : null;
            return new StyleKey(style.bold, style.italic, style.underline, style.strikeThrough, color, fontSize, fontFamily);
        }

        private boolean isEmpty() {
            return !bold && !italic && !underline && !strikeThrough && color == null && fontSize == null && fontFamily == null;
        }
    }

    private static final class CharacterStyle {
        private boolean bold;
        private boolean italic;
        private boolean underline;
        private boolean strikeThrough;
        private int colorIndex;
        private int fontIndex;
        private float fontSize;

        private CharacterStyle() {
            this(-1);
        }

        private CharacterStyle(int defaultFontIndex) {
            this.bold = false;
            this.italic = false;
            this.underline = false;
            this.strikeThrough = false;
            this.colorIndex = 0;
            this.fontIndex = defaultFontIndex;
            this.fontSize = -1f;
        }

        private CharacterStyle copy() {
            CharacterStyle copy = new CharacterStyle();
            copy.bold = this.bold;
            copy.italic = this.italic;
            copy.underline = this.underline;
            copy.strikeThrough = this.strikeThrough;
            copy.colorIndex = this.colorIndex;
            copy.fontIndex = this.fontIndex;
            copy.fontSize = this.fontSize;
            return copy;
        }
    }
}

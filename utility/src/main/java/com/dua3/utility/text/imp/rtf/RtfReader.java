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
import com.dua3.utility.ui.VAnchor;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullUnmarked;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads RTF input and converts it to {@link RichText}.
 */
public final class RtfReader {
    private static final String STANDARD_RTF_PARSER_CLASS = "com.rtfparserkit.parser.standard.StandardRtfParser";
    private static final String RTF_SOURCE_CLASS = "com.rtfparserkit.parser.RtfStringSource";
    private static final String RTF_SOURCE_INTERFACE = "com.rtfparserkit.parser.IRtfSource";
    private static final String RTF_LISTENER_INTERFACE = "com.rtfparserkit.parser.IRtfListener";
    private static final double TWIPS_PER_PIXEL = 15.0;
    private static final double POINTS_PER_TWIP = 1.0 / 20.0;
    private static final double DEFAULT_FONT_SIZE_PT = 12.0;
    private static final double DEFAULT_ASCENT_RATIO = 0.8;
    private static final double DEFAULT_DESCENT_RATIO = 0.2;
    private static final Pattern HYPERLINK_INSTRUCTION_PATTERN = Pattern.compile("(?i)\\bHYPERLINK\\b\\s+(?:\"((?>[^\"\\\\]|\\\\.)*)\"|(\\S+))");
    private static final String METADATA_COMMAND = "userprops";
    private static final String METADATA_PREFIX = "DUA3STYLES:";
    private static final String METADATA_INFO = "info";
    private static final String METADATA_LISTOVERRIDETABLE = "listoverridetable";
    private static final String METADATA_LISTTABLE = "listtable";
    private static final String METADATA_STYLESHEET = "stylesheet";
    private static final String METADATA_OPTIONALCOMMAND = "optionalcommand";

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
        private static final Map<Color, Style> PREDEFINED_TEXT_COLOR_STYLES = Map.of(
                Color.BLACK, Style.BLACK,
                Color.WHITE, Style.WHITE,
                Color.RED, Style.RED,
                Color.GREEN, Style.GREEN,
                Color.BLUE, Style.BLUE,
                Color.YELLOW, Style.YELLOW,
                Color.GRAY, Style.GRAY,
                Color.DARKGRAY, Style.DARKGRAY,
                Color.LIGHTGRAY, Style.LIGHTGRAY
        );

        private static final Map<String, Style> STANDARD_FONT_CLASS_STYLES = Map.of(
                Style.FONT_CLASS_VALUE_SANS_SERIF, Style.SANS_SERIF,
                Style.FONT_CLASS_VALUE_SERIF, Style.SERIF,
                Style.FONT_CLASS_VALUE_MONOSPACE, Style.MONOSPACE,
                Style.FONT_CLASS_VALUE_CODE, Style.CODE
        );

        private static final Map<String, String> FONT_CLASS_BY_FAMILY = createFontClassByFamilyMap();
        private static final Base64.Decoder STYLE_NAMES_DECODER = Base64.getUrlDecoder();

        private final RichTextBuilder builder = new RichTextBuilder();
        private final ArrayDeque<CharacterStyle> styleStack = new ArrayDeque<>();
        private final Map<StyleKey, ResolvedStyle> styleCache = new HashMap<>();
        private final Map<Integer, String> fontTable = new HashMap<>();
        private final Map<Integer, String> fontClassTable = new HashMap<>();
        private final Map<Integer, Color> colorTable = new HashMap<>();
        private final ArrayDeque<FieldState> fieldStack = new ArrayDeque<>();

        private CharacterStyle style = new CharacterStyle();
        private int defaultFontIndex = -1;
        private int groupDepth = 0;

        private boolean inFontTable = false;
        private int fontTableDepth = -1;
        private Integer currentFontIndex = null;
        private @Nullable String currentFontClass = null;
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
        private String pictureMimeType = ImageUtil.MIME_TYPE_JPEG;
        private int pictureNativeWidth = 0;
        private int pictureNativeHeight = 0;
        private int pictureWidthGoalTwips = 0;
        private int pictureHeightGoalTwips = 0;
        private int pictureScaleXPercent = 100;
        private int pictureScaleYPercent = 100;
        private boolean inStyleNameMetadata = false;
        private int styleNameMetadataDepth = -1;
        private final StringBuilder styleNameMetadataText = new StringBuilder();
        private @Nullable List<String> pendingStyleNames = null;
        private int inlineStyleId = 0;
        private int syntheticStyleId = 0;

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
            return builder.isEmpty() ? RichText.emptyText() : builder.toRichText();
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
                currentFontClass = null;
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
                currentFontClass = null;
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
                pictureMimeType = ImageUtil.MIME_TYPE_JPEG;
                pictureNativeWidth = 0;
                pictureNativeHeight = 0;
                pictureWidthGoalTwips = 0;
                pictureHeightGoalTwips = 0;
                pictureScaleXPercent = 100;
                pictureScaleYPercent = 100;
            }
            if (inStyleNameMetadata && groupDepth == styleNameMetadataDepth) {
                pendingStyleNames = decodeStyleNames(styleNameMetadataText);
                inStyleNameMetadata = false;
                styleNameMetadataDepth = -1;
                styleNameMetadataText.setLength(0);
            }

            FieldState fieldState = fieldStack.peek();
            if (fieldState != null) {
                if (fieldState.instructionDepth() >= 0 && groupDepth == fieldState.instructionDepth()) {
                    fieldState.finishInstruction();
                    String target = parseHyperlinkTarget(fieldState.instructionText());
                    if (target != null && !target.isBlank()) {
                        fieldState.setHyperlinkTarget(target);
                    }
                }
                if (fieldState.resultDepth() >= 0 && groupDepth == fieldState.resultDepth()) {
                    fieldState.finishResult();
                    appendFieldResult(fieldState);
                }
                while (!fieldStack.isEmpty() && groupDepth == fieldStack.peek().fieldDepth()) {
                    fieldStack.pop();
                }
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
            if (inStyleNameMetadata) {
                styleNameMetadataText.append(text);
                return;
            }
            if (isIgnoringDestination()) {
                return;
            }

            FieldState fieldState = fieldStack.peek();
            if (fieldState != null) {
                if (fieldState.isCollectingInstruction()) {
                    fieldState.appendInstruction(text);
                    return;
                }
                if (fieldState.isCollectingResult()) {
                    fieldState.appendResult(TextUtil.normalize(text));
                    return;
                }
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
                consumePictureCommand(command, parameter, hasParameter);
                return;
            }

            switch (command) {
                case "field" -> {
                    fieldStack.push(new FieldState(groupDepth));
                    return;
                }
                case "fldinst" -> {
                    FieldState fieldState = fieldStack.peek();
                    if (fieldState != null) {
                        fieldState.startInstruction(groupDepth);
                    }
                    return;
                }
                case "fldrslt" -> {
                    FieldState fieldState = fieldStack.peek();
                    if (fieldState != null && fieldState.hyperlinkTarget() != null) {
                        fieldState.startResult(groupDepth);
                    }
                    return;
                }
                default -> {
                    // continue
                }
            }

            FieldState fieldState = fieldStack.peek();
            if (fieldState != null) {
                if (fieldState.isCollectingInstruction()) {
                    return;
                }
                if (fieldState.isCollectingResult()) {
                    switch (command) {
                        case "par", "line" -> fieldState.appendResult("\n");
                        case "tab" -> fieldState.appendResult("\t");
                        default -> {
                            // ignore formatting commands inside hyperlink field result
                        }
                    }
                    return;
                }
            }

            if (METADATA_COMMAND.equals(command)) {
                inStyleNameMetadata = true;
                styleNameMetadataDepth = groupDepth;
                styleNameMetadataText.setLength(0);
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
                pictureMimeType = ImageUtil.MIME_TYPE_JPEG;
                pictureNativeWidth = 0;
                pictureNativeHeight = 0;
                pictureWidthGoalTwips = 0;
                pictureHeightGoalTwips = 0;
                pictureScaleXPercent = 100;
                pictureScaleYPercent = 100;
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
                String fontClass = fontClassFromRtfControlWord(command);
                if (fontClass != null) {
                    currentFontClass = fontClass;
                }
                return;
            }

            if (inColorTable) {
                switch (command) {
                    case "red" -> {
                        if (hasParameter) {
                            currentRed = Math.clamp(parameter, 0, 255);
                            hasColorComponent = true;
                        }
                    }
                    case "green" -> {
                        if (hasParameter) {
                            currentGreen = Math.clamp(parameter, 0, 255);
                            hasColorComponent = true;
                        }
                    }
                    case "blue" -> {
                        if (hasParameter) {
                            currentBlue = Math.clamp(parameter, 0, 255);
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
                case "highlight", "cb" -> {
                    if (hasParameter) {
                        style.backgroundColorIndex = Math.max(0, parameter);
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
                case "up" -> style.baselineShiftHalfPoints = hasParameter ? Math.max(0, parameter) : 0;
                case "dn" -> style.baselineShiftHalfPoints = hasParameter ? -Math.max(0, parameter) : 0;
                case "par", "line" -> appendStyledText("\n");
                case "tab" -> appendStyledText("\t");
                default -> {
                    // ignore unsupported control words
                }
            }
        }

        private void consumePictureCommand(String command, int parameter, boolean hasParameter) {
            switch (command) {
                case "jpegblip" -> pictureMimeType = ImageUtil.MIME_TYPE_JPEG;
                case "pngblip" -> pictureMimeType = ImageUtil.MIME_TYPE_PNG;
                case "picw" -> {
                    if (hasParameter) {
                        pictureNativeWidth = Math.max(0, parameter);
                    }
                }
                case "pich" -> {
                    if (hasParameter) {
                        pictureNativeHeight = Math.max(0, parameter);
                    }
                }
                case "picwgoal" -> {
                    if (hasParameter) {
                        pictureWidthGoalTwips = Math.max(0, parameter);
                    }
                }
                case "pichgoal" -> {
                    if (hasParameter) {
                        pictureHeightGoalTwips = Math.max(0, parameter);
                    }
                }
                case "picscalex" -> {
                    if (hasParameter) {
                        pictureScaleXPercent = Math.max(1, parameter);
                    }
                }
                case "picscaley" -> {
                    if (hasParameter) {
                        pictureScaleYPercent = Math.max(1, parameter);
                    }
                }
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

                if (pictureNativeWidth <= 0) {
                    pictureNativeWidth = image.width();
                }
                if (pictureNativeHeight <= 0) {
                    pictureNativeHeight = image.height();
                }

                int widthTwips = resolveDisplayDimensionTwips(pictureWidthGoalTwips, pictureNativeWidth, pictureScaleXPercent, image.width());
                int heightTwips = resolveDisplayDimensionTwips(pictureHeightGoalTwips, pictureNativeHeight, pictureScaleYPercent, image.height());
                double widthPx = Math.max(1.0, widthTwips / TWIPS_PER_PIXEL);
                double heightPx = Math.max(1.0, heightTwips / TWIPS_PER_PIXEL);
                boolean scaled = Math.abs(widthPx - image.width()) > 1e-6 || Math.abs(heightPx - image.height()) > 1e-6;

                VAnchor vAnchor = deriveVAnchor(style.baselineShiftHalfPoints, heightTwips, style.fontSize);
                appendInlineNodeMarker(inlineNode, vAnchor, scaled ? widthPx : null, scaled ? heightPx : null);
            } catch (IOException | RuntimeException ex) {
                appendStyledText("Image: ");
            }
        }

        private static String inlineNodeMimeType(String pictureMimeType) {
            if (ImageUtil.MIME_TYPE_JPEG.equals(pictureMimeType)) {
                return pictureMimeType;
            }
            return ImageUtil.MIME_TYPE_JPEG;
        }

        private void appendInlineNodeMarker(
                InlineNode<?> inlineNode,
                VAnchor vAnchor,
                @Nullable Double widthPx,
                @Nullable Double heightPx
        ) {
            List<Map.Entry<String, Object>> entries = new ArrayList<>(5);
            entries.add(Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, inlineNode));
            entries.add(Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR, vAnchor));
            entries.add(Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT, 0.0));
            if (widthPx != null && widthPx > 0) {
                entries.add(Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH, widthPx));
            }
            if (heightPx != null && heightPx > 0) {
                entries.add(Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT, heightPx));
            }
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object>[] styleEntries = entries.toArray(Map.Entry[]::new);
            Style inlineStyle = Style.create("rtf-inline-node-" + inlineStyleId++, styleEntries);
            appendStyledText(String.valueOf(RichTextBuilderExtBase.INLINE_NODE_MARKER), inlineStyle);
        }

        private void appendFieldResult(FieldState fieldState) {
            String resultText = fieldState.resultText();
            if (resultText.isEmpty()) {
                return;
            }

            String hyperlinkTarget = fieldState.hyperlinkTarget();
            if (hyperlinkTarget != null && !hyperlinkTarget.isBlank()) {
                if (isInlineButtonFallbackTarget(hyperlinkTarget)) {
                    appendInlineButtonMarker(resultText, hyperlinkTarget);
                    return;
                }
                appendInlineHyperlinkMarker(resultText, hyperlinkTarget);
                return;
            }

            appendStyledText(resultText);
        }

        private void appendInlineButtonMarker(String text, String target) {
            InlineNode<String> inlineNode = new InlineNode<>(
                    text,
                    RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_BUTTON,
                    RichTextBuilderExtBase.encodeInlineButtonData(target, text)
            );
            Style inlineStyle = Style.create(
                    "rtf-inline-button-" + inlineStyleId++,
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, inlineNode)
            );
            appendStyledText(String.valueOf(RichTextBuilderExtBase.INLINE_NODE_MARKER), inlineStyle);
        }

        private void appendInlineHyperlinkMarker(String text, String target) {
            InlineNode<String> inlineNode = new InlineNode<>(
                    text,
                    RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_HYPERLINK,
                    RichTextBuilderExtBase.encodeInlineHyperlinkData(target, text)
            );
            Style inlineStyle = Style.create(
                    "rtf-inline-hyperlink-" + inlineStyleId++,
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, inlineNode)
            );
            appendStyledText(String.valueOf(RichTextBuilderExtBase.INLINE_NODE_MARKER), inlineStyle);
        }

        private static boolean isInlineButtonFallbackTarget(String target) {
            try {
                URI uri = new URI(target);
                String scheme = uri.getScheme();
                return scheme != null && RichTextBuilderExtBase.INLINE_BUTTON_FALLBACK_URI_SCHEME.equalsIgnoreCase(scheme);
            } catch (URISyntaxException ex) {
                return false;
            }
        }

        private static @Nullable String parseHyperlinkTarget(String instruction) {
            Matcher matcher = HYPERLINK_INSTRUCTION_PATTERN.matcher(instruction);
            if (!matcher.find()) {
                return null;
            }

            String quoted = matcher.group(1);
            if (quoted != null) {
                return unescapeRtfInstruction(quoted);
            }
            String token = matcher.group(2);
            return token == null ? null : unescapeRtfInstruction(token);
        }

        private static String unescapeRtfInstruction(String value) {
            StringBuilder result = new StringBuilder(value.length());
            boolean escaping = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (escaping) {
                    result.append(ch);
                    escaping = false;
                } else if (ch == '\\') {
                    escaping = true;
                } else {
                    result.append(ch);
                }
            }
            if (escaping) {
                result.append('\\');
            }
            return result.toString();
        }

        private static int resolveDisplayDimensionTwips(int goalTwips, int nativeUnits, int scalePercent, int fallbackPixels) {
            int safeScalePercent = Math.max(1, scalePercent);
            if (goalTwips > 0) {
                if (safeScalePercent == 100) {
                    return goalTwips;
                }

                int goalScaledTwips = Math.max(1, (int) Math.round(goalTwips * (safeScalePercent / 100.0)));
                if (nativeUnits > 0) {
                    int nativeGoalTwips = Math.max(1, (int) Math.round(nativeUnits * TWIPS_PER_PIXEL));
                    int nativeScaledGoalTwips = Math.max(1, (int) Math.round(nativeGoalTwips * (safeScalePercent / 100.0)));

                    // Compatibility with RTF that already stores the scaled size in \pic*goal and also sets \picscale*.
                    if (Math.abs(goalTwips - nativeScaledGoalTwips) <= 1) {
                        return goalTwips;
                    }
                    if (Math.abs(goalTwips - nativeGoalTwips) <= 1) {
                        return goalScaledTwips;
                    }
                }
                return goalScaledTwips;
            }

            int nativeValue = nativeUnits > 0 ? nativeUnits : Math.max(1, fallbackPixels);
            return Math.max(1, (int) Math.round(nativeValue * (safeScalePercent / 100.0) * TWIPS_PER_PIXEL));
        }

        private static VAnchor deriveVAnchor(int baselineShiftHalfPoints, int pictureHeightTwips, float fontSize) {
            double fontSizePt = fontSize > 0 ? fontSize : DEFAULT_FONT_SIZE_PT;
            double ascentPt = fontSizePt * DEFAULT_ASCENT_RATIO;
            double descentPt = fontSizePt * DEFAULT_DESCENT_RATIO;
            double pictureHeightPt = Math.max(1.0, pictureHeightTwips) * POINTS_PER_TWIP;
            double shiftPt = baselineShiftHalfPoints / 2.0;

            double baselineShift = 0.0;
            double bottomShift = -descentPt;
            double topShift = ascentPt - pictureHeightPt;
            double middleShift = (ascentPt - descentPt - pictureHeightPt) / 2.0;

            VAnchor best = VAnchor.BASELINE;
            double bestDistance = Math.abs(shiftPt - baselineShift);
            double distanceToBottom = Math.abs(shiftPt - bottomShift);
            if (distanceToBottom < bestDistance) {
                bestDistance = distanceToBottom;
                best = VAnchor.BOTTOM;
            }
            double distanceToTop = Math.abs(shiftPt - topShift);
            if (distanceToTop < bestDistance) {
                bestDistance = distanceToTop;
                best = VAnchor.TOP;
            }
            double distanceToMiddle = Math.abs(shiftPt - middleShift);
            if (distanceToMiddle < bestDistance) {
                best = VAnchor.MIDDLE;
            }
            return best;
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
            if (currentFontIndex != null) {
                if (!fontName.isEmpty()) {
                    fontTable.put(currentFontIndex, fontName);
                }
                if (currentFontClass != null) {
                    fontClassTable.put(currentFontIndex, currentFontClass);
                }
            }
            currentFontIndex = null;
            currentFontClass = null;
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

            ResolvedStyle resolvedStyle = resolveStyle(consumePendingStyleNames());
            if (resolvedStyle.isEmpty() && extraStyle == null) {
                builder.append(text);
            } else {
                for (Style styleForText : resolvedStyle.styles()) {
                    builder.push(styleForText);
                }
                for (Map.Entry<String, Object> attribute : resolvedStyle.attributes()) {
                    builder.push(attribute.getKey(), attribute.getValue());
                }
                if (extraStyle != null) {
                    builder.push(extraStyle);
                }
                builder.append(text);
                if (extraStyle != null) {
                    builder.pop(extraStyle);
                }
                List<Map.Entry<String, Object>> attributes = resolvedStyle.attributes();
                for (int i = attributes.size() - 1; i >= 0; i--) {
                    builder.pop(attributes.get(i).getKey());
                }
                List<Style> styles = resolvedStyle.styles();
                for (int i = styles.size() - 1; i >= 0; i--) {
                    builder.pop(styles.get(i));
                }
            }
        }

        private List<String> consumePendingStyleNames() {
            List<String> styleNames = pendingStyleNames != null ? pendingStyleNames : List.of();
            pendingStyleNames = null;
            return styleNames;
        }

        private ResolvedStyle resolveStyle(List<String> explicitStyleNames) {
            StyleKey key = StyleKey.from(style, fontTable, fontClassTable, colorTable);
            if (key.isEmpty()) {
                return ResolvedStyle.EMPTY;
            }

            ResolvedStyle baseStyle = styleCache.computeIfAbsent(key, k -> {
                List<Style> styles = new ArrayList<>(8);
                List<Map.Entry<String, Object>> attributes = new ArrayList<>(4);

                if (k.bold()) {
                    styles.add(Style.BOLD);
                }
                if (k.italic()) {
                    styles.add(Style.ITALIC);
                }
                if (k.underline()) {
                    styles.add(Style.UNDERLINE);
                }
                if (k.strikeThrough()) {
                    styles.add(Style.LINE_THROUGH);
                }
                if (k.color() != null) {
                    Style colorStyle = PREDEFINED_TEXT_COLOR_STYLES.get(k.color());
                    if (colorStyle != null) {
                        styles.add(colorStyle);
                    } else {
                        attributes.add(Map.entry(Style.COLOR, k.color()));
                    }
                }
                if (k.backgroundColor() != null) {
                    attributes.add(Map.entry(Style.BACKGROUND_COLOR, k.backgroundColor()));
                }
                if (k.fontSize() != null) {
                    attributes.add(Map.entry(Style.FONT_SIZE, k.fontSize()));
                }
                String fontClass = resolveFontClass(k.fontFamily(), k.fontClass());
                if (fontClass != null) {
                    Style fontClassStyle = STANDARD_FONT_CLASS_STYLES.get(fontClass);
                    if (fontClassStyle != null) {
                        styles.add(fontClassStyle);
                    } else {
                        attributes.add(Map.entry(Style.FONT_CLASS, fontClass));
                    }
                }
                if (k.fontFamily() != null) {
                    attributes.add(Map.entry(Style.FONT_FAMILIES, List.of(k.fontFamily())));
                }

                return new ResolvedStyle(List.copyOf(styles), List.copyOf(attributes));
            });

            List<Style> styleList = explicitStyleNames.isEmpty()
                    ? withSyntheticStyleName(baseStyle.styles())
                    : withExplicitStyleNames(baseStyle.styles(), explicitStyleNames);
            return new ResolvedStyle(styleList, baseStyle.attributes());
        }

        private List<Style> withSyntheticStyleName(List<Style> baseStyles) {
            List<Style> styles = new ArrayList<>(baseStyles.size() + 1);
            styles.addAll(baseStyles);
            styles.add(Style.create("rtf-style-" + syntheticStyleId++));
            return List.copyOf(styles);
        }

        private static List<Style> withExplicitStyleNames(List<Style> baseStyles, List<String> explicitStyleNames) {
            List<Style> styles = new ArrayList<>(Math.max(baseStyles.size(), explicitStyleNames.size()));
            int count = Math.min(baseStyles.size(), explicitStyleNames.size());
            for (int i = 0; i < count; i++) {
                Style original = baseStyles.get(i);
                String explicitName = explicitStyleNames.get(i);
                if (explicitName.isBlank() || explicitName.equals(original.name())) {
                    styles.add(original);
                } else {
                    styles.add(Style.create(explicitName, original));
                }
            }
            for (int i = count; i < baseStyles.size(); i++) {
                styles.add(baseStyles.get(i));
            }
            for (int i = count; i < explicitStyleNames.size(); i++) {
                String explicitName = explicitStyleNames.get(i);
                if (!explicitName.isBlank()) {
                    styles.add(Style.create(explicitName));
                }
            }
            return List.copyOf(styles);
        }

        private static @Nullable String resolveFontClass(@Nullable String fontFamily, @Nullable String declaredFontClass) {
            if (declaredFontClass != null) {
                return declaredFontClass;
            }
            if (fontFamily == null || fontFamily.isBlank()) {
                return null;
            }
            return FONT_CLASS_BY_FAMILY.get(normalizeFontFamily(fontFamily));
        }

        private static @Nullable String fontClassFromRtfControlWord(String command) {
            return switch (command) {
                case "froman" -> Style.FONT_CLASS_VALUE_SERIF;
                case "fswiss" -> Style.FONT_CLASS_VALUE_SANS_SERIF;
                case "fmodern", "ftech" -> Style.FONT_CLASS_VALUE_MONOSPACE;
                default -> null;
            };
        }

        private static Map<String, String> createFontClassByFamilyMap() {
            Map<String, String> mapping = new HashMap<>();
            addFontFamilies(mapping, Style.FONT_CLASS_VALUE_SANS_SERIF, Style.FONT_FAMILIES_VALUE_SANS_SERIF);
            addFontFamilies(mapping, Style.FONT_CLASS_VALUE_SERIF, Style.FONT_FAMILIES_VALUE_SERIF);
            addFontFamilies(mapping, Style.FONT_CLASS_VALUE_MONOSPACE, Style.FONT_FAMILIES_VALUE_MONOSPACED);
            mapping.put(normalizeFontFamily(Style.FONT_CLASS_VALUE_CODE), Style.FONT_CLASS_VALUE_CODE);
            return Map.copyOf(mapping);
        }

        private static void addFontFamilies(Map<String, String> mapping, String fontClass, List<String> families) {
            for (String family : families) {
                mapping.put(normalizeFontFamily(family), fontClass);
            }
        }

        private static String normalizeFontFamily(String fontFamily) {
            return fontFamily.strip().toLowerCase(Locale.ROOT);
        }

        private static List<String> decodeStyleNames(CharSequence encodedText) {
            String text = encodedText.toString().trim();
            if (!text.startsWith(METADATA_PREFIX)) {
                return List.of();
            }
            String encoded = text.substring(METADATA_PREFIX.length()).trim();
            if (encoded.isEmpty()) {
                return List.of();
            }
            try {
                String decoded = new String(STYLE_NAMES_DECODER.decode(encoded), StandardCharsets.UTF_8);
                List<String> names = new ArrayList<>();
                for (String part : decoded.split("\n", -1)) {
                    String styleName = part.strip();
                    if (!styleName.isEmpty()) {
                        names.add(styleName);
                    }
                }
                return List.copyOf(names);
            } catch (IllegalArgumentException ex) {
                return List.of();
            }
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
            if (optional) {
                return true;
            }

            return switch (command) {
                case METADATA_OPTIONALCOMMAND,
                     METADATA_STYLESHEET,
                     METADATA_LISTTABLE,
                     METADATA_LISTOVERRIDETABLE,
                     METADATA_INFO,
                     METADATA_COMMAND -> true;
                default -> false;
            };
        }

        private static final class FieldState {
            private final int fieldDepth;
            private final StringBuilder instruction = new StringBuilder();
            private final StringBuilder result = new StringBuilder();
            private int instructionDepth = -1;
            private int resultDepth = -1;
            private @Nullable String hyperlinkTarget;

            private FieldState(int fieldDepth) {
                this.fieldDepth = fieldDepth;
            }

            private int fieldDepth() {
                return fieldDepth;
            }

            private int instructionDepth() {
                return instructionDepth;
            }

            private int resultDepth() {
                return resultDepth;
            }

            private void startInstruction(int currentDepth) {
                instruction.setLength(0);
                instructionDepth = currentDepth;
                hyperlinkTarget = null;
            }

            private void appendInstruction(String text) {
                instruction.append(text);
            }

            private void finishInstruction() {
                instructionDepth = -1;
            }

            private void startResult(int currentDepth) {
                result.setLength(0);
                resultDepth = currentDepth;
            }

            private void appendResult(String text) {
                result.append(text);
            }

            private void finishResult() {
                resultDepth = -1;
            }

            private boolean isCollectingInstruction() {
                return instructionDepth >= 0;
            }

            private boolean isCollectingResult() {
                return resultDepth >= 0;
            }

            private String instructionText() {
                return instruction.toString();
            }

            private @Nullable String hyperlinkTarget() {
                return hyperlinkTarget;
            }

            private void setHyperlinkTarget(String target) {
                this.hyperlinkTarget = target;
            }

            private String resultText() {
                return result.toString();
            }
        }

        private record ResolvedStyle(
                List<Style> styles,
                List<Map.Entry<String, Object>> attributes
        ) {
            private static final ResolvedStyle EMPTY = new ResolvedStyle(List.of(), List.of());

            private boolean isEmpty() {
                return styles.isEmpty() && attributes.isEmpty();
            }
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
            @Nullable Color backgroundColor,
            @Nullable Float fontSize,
            @Nullable String fontFamily,
            @Nullable String fontClass
    ) {
        private static StyleKey from(CharacterStyle style,
                                     Map<Integer, String> fontTable,
                                     Map<Integer, String> fontClassTable,
                                     Map<Integer, Color> colorTable) {
            Color color = style.colorIndex > 0 ? colorTable.get(style.colorIndex) : null;
            Color backgroundColor = style.backgroundColorIndex > 0 ? colorTable.get(style.backgroundColorIndex) : null;
            Float fontSize = style.fontSize > 0 ? style.fontSize : null;
            String fontFamily = style.fontIndex >= 0 ? fontTable.get(style.fontIndex) : null;
            String fontClass = style.fontIndex >= 0 ? fontClassTable.get(style.fontIndex) : null;
            return new StyleKey(style.bold, style.italic, style.underline, style.strikeThrough, color, backgroundColor, fontSize, fontFamily, fontClass);
        }

        private boolean isEmpty() {
            return !bold
                    && !italic
                    && !underline
                    && !strikeThrough
                    && color == null
                    && backgroundColor == null
                    && fontSize == null
                    && fontFamily == null
                    && fontClass == null;
        }
    }

    private static final class CharacterStyle {
        private boolean bold;
        private boolean italic;
        private boolean underline;
        private boolean strikeThrough;
        private int colorIndex;
        private int backgroundColorIndex;
        private int fontIndex;
        private float fontSize;
        private int baselineShiftHalfPoints;

        private CharacterStyle() {
            this(-1);
        }

        private CharacterStyle(int defaultFontIndex) {
            this.bold = false;
            this.italic = false;
            this.underline = false;
            this.strikeThrough = false;
            this.colorIndex = 0;
            this.backgroundColorIndex = 0;
            this.fontIndex = defaultFontIndex;
            this.fontSize = -1f;
            this.baselineShiftHalfPoints = 0;
        }

        private CharacterStyle copy() {
            CharacterStyle copy = new CharacterStyle();
            copy.bold = this.bold;
            copy.italic = this.italic;
            copy.underline = this.underline;
            copy.strikeThrough = this.strikeThrough;
            copy.colorIndex = this.colorIndex;
            copy.backgroundColorIndex = this.backgroundColorIndex;
            copy.fontIndex = this.fontIndex;
            copy.fontSize = this.fontSize;
            copy.baselineShiftHalfPoints = this.baselineShiftHalfPoints;
            return copy;
        }
    }
}

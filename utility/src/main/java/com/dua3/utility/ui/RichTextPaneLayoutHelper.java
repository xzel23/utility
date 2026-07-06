package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

/**
 * A utility class for handling layouts related to rich text panes. Provides methods and data structures
 * to prepare, calculate, and manage text layouts with advanced features such as inline elements,
 * text wrapping, and attribute handling.
 */
public final class RichTextPaneLayoutHelper {

    /**
     * A predefined {@link Style} that applies to text content to make it invisible.
     * The style sets the text color to fully transparent black, effectively hiding it
     * from view while maintaining its presence in the layout.
     * <p>
     * This style is typically used for layout purposes where text needs to occupy
     * space but remain unseen to the user.
     */
    public static final Style STYLE_INVISIBLE_TEXT = Style.create(
            "text-pane-invisible",
            Map.entry(Style.COLOR, Color.TRANSPARENT_BLACK)
    );

    private static final String NO_BREAK_SPACE = "\u00A0";
    private static final String STYLE_LIST_ATTRIBUTE = "__styles";

    private RichTextPaneLayoutHelper() {
        // utility class
    }

    /**
     * Represents structured mapping data between the layout and the source text within a rich text layout system.
     * This record provides utilities to convert positions between layout and source text.
     *
     * @param text the rich text data associated with the layout
     * @param layoutToSourceMap an array mapping positions in the layout to the corresponding positions in the source text
     * @param sourceToLayoutMap an array mapping positions in the source text to the corresponding positions in the layout
     */
    public record LayoutTextData(RichText text, int[] layoutToSourceMap, int[] sourceToLayoutMap) {
        /**
         * Converts a position in the layout to the corresponding position in the source text.
         *
         * @param layoutPosition the position in the layout to be converted
         * @return the corresponding position in the source text, clamped to the valid range of the layout-to-source mapping
         */
        public int layoutToSourcePosition(int layoutPosition) {
            return layoutToSourceMap[Math.clamp(layoutPosition, 0, layoutToSourceMap.length - 1)];
        }

        /**
         * Converts a position in the source text to the corresponding position in the layout.
         *
         * @param sourcePosition the position in the source text to be converted
         * @return the corresponding position in the layout, clamped to the valid range of the source-to-layout mapping
         */
        public int sourceToLayoutPosition(int sourcePosition) {
            return sourceToLayoutMap[Math.clamp(sourcePosition, 0, sourceToLayoutMap.length - 1)];
        }
    }

    /**
     * A record representing the layout of rendered text within a specific area, including metadata
     * about the text's layout and appearance.
     *
     * @param <P> The type of placement data associated with the layout.
     *
     * @param renderLines A nested list of fragments representing the rendered lines of text. Each
     *                    inner list contains fragments for one line of text.
     * @param placements   A list of placement data corresponding to specific elements in the layout. Each
     *                     element defines specific positional or alignment information for the layout.
     * @param width        The total width of the layout.
     * @param height       The total height of the layout.
     * @param layoutTextData Metadata detailing the characteristics of the laid-out text, such as structure
     *                       and mapping between source and rendered text.
     */
    public record Layout<P>(
            List<List<FragmentedText.Fragment>> renderLines,
            List<P> placements,
            double width,
            double height,
            LayoutTextData layoutTextData
    ) {}

    /**
     * Encapsulates the data required for preparing the layout of rich text content.
     * Provides a structured representation of the processed layout text, its fragmented components,
     * and the dimensions necessary for rendering.
     *
     * @param layoutTextData      Contains the mapping between the layout text and its source text.
     *                            This data supports conversion between layout and source text
     *                            positions for accurate text rendering and manipulation.
     * @param layoutFragments     Represents the fragmented structure for layout purposes.
     *                            These fragments help in segmenting the text for layout processing.
     * @param renderFragments     Represents the fragmented structure used for rendering.
     *                            These fragments are tailored for improved rendering performance.
     * @param renderWidth         The width of the layout for rendering purposes. It specifies
     *                            the available width for the text to be rendered within the layout.
     */
    public record LayoutPreparation(
            LayoutTextData layoutTextData,
            FragmentedText layoutFragments,
            FragmentedText renderFragments,
            float renderWidth
    ) {}

    /**
     * Prepares the layout for rendering rich text content based on the specified parameters.
     * This method processes the source rich text, segments it into fragments for layout and
     * rendering, and calculates the necessary dimensions.
     *
     * @param <I>                            The type of inline objects created for processing run elements.
     * @param source                         The source {@code RichText} object that contains the text to be processed.
     * @param font                           The base {@code Font} used for rendering the text.
     * @param wrapText                       A boolean indicating whether the text should wrap at the available width.
     * @param availableWidth                 The available width within which the text layout is prepared.
     * @param inlineLeadingWidthStyleAttribute A style attribute representing the leading width for inline elements.
     * @param inlineFactory                  A {@code BiFunction} that creates inline objects from a {@code Run}
     *                                       element and the provided {@code Font}.
     * @param inlineWidthFunction            A {@code ToDoubleFunction} that computes the width of the inline objects
     *                                       created by the {@code inlineFactory}.
     * @return                               A {@code LayoutPreparation} object containing the processed layout text data,
     *                                       fragmented structures for layout and rendering, and the calculated rendering
     *                                       width.
     */
    public static <I> LayoutPreparation prepareLayout(
            RichText source,
            Font font,
            boolean wrapText,
            double availableWidth,
            String inlineLeadingWidthStyleAttribute,
            BiFunction<? super Run, ? super Font, @Nullable I> inlineFactory,
            ToDoubleFunction<? super I> inlineWidthFunction
    ) {
        FontUtil fontUtil = FontUtil.getInstance();
        LayoutTextData layoutTextData = createLayoutTextData(
                source,
                font,
                fontUtil,
                inlineLeadingWidthStyleAttribute,
                inlineFactory,
                inlineWidthFunction
        );
        float width = (float) Math.max(1.0, availableWidth);
        float wrapWidth = wrapText ? width : FragmentedText.NO_WRAP;

        RichText layoutText = layoutTextData.text();
        FragmentedText layoutFragments = generateFragments(layoutText, fontUtil, font, width, wrapWidth);
        FragmentedText renderFragments = generateFragments(createRenderedText(layoutText), fontUtil, font, width, wrapWidth);
        float renderWidth = wrapText ? width : Math.max(width, renderFragments.actualWidth());

        return new LayoutPreparation(layoutTextData, layoutFragments, renderFragments, renderWidth);
    }

    /**
     * Checks whether the given {@link Run} contains an inline node style attribute.
     * This method iterates through the styles of the provided {@code Run} and determines
     * if any of the styles include an inline node factory or an inline node entry.
     *
     * @param run the {@link Run} instance to check for inline node style attributes.
     * @return {@code true} if the {@code Run} contains an inline node style attribute,
     *         otherwise {@code false}.
     */
    public static boolean hasInlineNode(Run run) {
        for (Style style : run.getStyles()) {
            if (style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY) != null
                    || style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a {@code LayoutTextData} object by processing the given {@code RichText} source.
     * This method analyzes the text, applies styles, manages inline elements, and calculates
     * mappings to ensure proper layout rendering.
     *
     * @param <I>                            The type of inline objects created for processing run elements.
     * @param source                         The source {@code RichText} to be processed.
     * @param baseFont                       The base {@code Font} used when deriving font styling for text runs.
     * @param fontUtil                       A utility class for font-related operations, such as calculating text dimensions.
     * @param inlineLeadingWidthStyleAttribute A style attribute representing the leading width for inline elements.
     * @param inlineFactory                  A {@code BiFunction} for creating inline objects from a {@code Run} element
     *                                       and the associated {@code Font}.
     * @param inlineWidthFunction            A {@code ToDoubleFunction} for calculating the width of inline objects
     *                                       created by the {@code inlineFactory}.
     * @return                               A {@code LayoutTextData} object containing the layout text, mapping
     *                                       information between layout and source indices, and related metadata.
     */
    private static <I> LayoutTextData createLayoutTextData(
            RichText source,
            Font baseFont,
            FontUtil fontUtil,
            String inlineLeadingWidthStyleAttribute,
            BiFunction<? super Run, ? super Font, @Nullable I> inlineFactory,
            ToDoubleFunction<? super I> inlineWidthFunction
    ) {
        RichTextBuilder builder = new RichTextBuilder(source.length());
        List<Integer> layoutToSourceBoundaries = new ArrayList<>(source.length() + 1);
        layoutToSourceBoundaries.add(0);
        int sourcePosition = 0;

        for (Run run : source) {
            List<String> pushedAttributes = pushNonStyleAttributes(builder, run);
            List<Style> styles = run.getStyles();
            styles.forEach(builder::push);

            String text = run.toString();
            if (hasInlineNode(run)) {
                Font runFont = fontUtil.deriveFont(baseFont, run.getFontDef());
                I inline = inlineFactory.apply(run, runFont);
                if (inline != null) {
                    Style leadingWidthStyle = null;
                    String leadingPadding = "";
                    double extraWidth = inlineWidthFunction.applyAsDouble(inline) - fontUtil.getTextDimension(run, runFont).width();
                    if (extraWidth > 0.5) {
                        double spaceWidth = Math.max(1.0, runFont.getFontData().spaceWidth());
                        int extraSpaces = (int) Math.ceil(extraWidth / spaceWidth);
                        if (extraSpaces > 0) {
                            leadingPadding = NO_BREAK_SPACE.repeat(extraSpaces);
                            leadingWidthStyle = Style.create(
                                    "text-pane-inline-leading-width",
                                    Map.entry(inlineLeadingWidthStyleAttribute, fontUtil.getTextWidth(leadingPadding, runFont))
                            );
                        }
                    }
                    if (leadingWidthStyle != null) {
                        builder.push(leadingWidthStyle);
                    }
                    if (!leadingPadding.isEmpty()) {
                        builder.append(leadingPadding);
                        for (int i = 0; i < leadingPadding.length(); i++) {
                            layoutToSourceBoundaries.add(sourcePosition);
                        }
                    }
                    sourcePosition = appendRunText(builder, layoutToSourceBoundaries, text, sourcePosition);
                    if (leadingWidthStyle != null) {
                        builder.pop(leadingWidthStyle);
                    }
                } else {
                    sourcePosition = appendRunText(builder, layoutToSourceBoundaries, text, sourcePosition);
                }
            } else {
                sourcePosition = appendRunText(builder, layoutToSourceBoundaries, text, sourcePosition);
            }

            for (int i = styles.size() - 1; i >= 0; i--) {
                builder.pop(styles.get(i));
            }
            for (int i = pushedAttributes.size() - 1; i >= 0; i--) {
                builder.pop(pushedAttributes.get(i));
            }
        }

        RichText layoutText = builder.toRichText();
        int layoutLength = layoutText.length();
        int[] layoutToSourceMap = new int[layoutLength + 1];
        int count = Math.min(layoutToSourceBoundaries.size(), layoutLength + 1);
        for (int i = 0; i < count; i++) {
            layoutToSourceMap[i] = Math.clamp(layoutToSourceBoundaries.get(i), 0, source.length());
        }
        for (int i = count; i < layoutToSourceMap.length; i++) {
            layoutToSourceMap[i] = source.length();
        }
        return new LayoutTextData(layoutText, layoutToSourceMap, buildSourceToLayoutMap(layoutToSourceMap, source.length()));
    }

    /**
     * Processes the given {@code RichText} source and renders it by applying styles, attributes,
     * and handling inline nodes. The method constructs a new {@code RichText} instance
     * that reflects the processed and rendered state of the source text.
     *
     * @param source The source {@code RichText} object to be processed and rendered.
     * @return A new {@code RichText} instance containing the rendered text.
     */
    private static RichText createRenderedText(RichText source) {
        RichTextBuilder builder = new RichTextBuilder(source.length());
        for (Run run : source) {
            List<String> pushedAttributes = pushNonStyleAttributes(builder, run);
            List<Style> styles = run.getStyles();
            styles.forEach(builder::push);
            if (hasInlineNode(run)) {
                builder.push(STYLE_INVISIBLE_TEXT);
                builder.append(run.toString());
                builder.pop(STYLE_INVISIBLE_TEXT);
            } else {
                builder.append(run.toString());
            }
            for (int i = styles.size() - 1; i >= 0; i--) {
                builder.pop(styles.get(i));
            }
            for (int i = pushedAttributes.size() - 1; i >= 0; i--) {
                builder.pop(pushedAttributes.get(i));
            }
        }
        return builder.toRichText();
    }

    /**
     * Pushes non-style attributes from the given {@link Run} to the specified {@link RichTextBuilder}.
     * This method iterates through the attributes of the provided {@link Run}, identifies attributes
     * that are not style-related, and adds them to the builder while maintaining a list of the pushed
     * attribute keys.
     *
     * @param builder the {@link RichTextBuilder} to which the non-style attributes are pushed
     * @param run the {@link Run} instance from which non-style attributes are extracted
     * @return a {@link List} of strings containing the keys of the attributes that were pushed
     */
    private static List<String> pushNonStyleAttributes(RichTextBuilder builder, Run run) {
        List<String> pushedAttributes = new ArrayList<>();
        for (Map.Entry<String, @Nullable Object> entry : run.attributes().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!STYLE_LIST_ATTRIBUTE.equals(key) && value != null) {
                builder.push(key, value);
                pushedAttributes.add(key);
            }
        }
        return pushedAttributes;
    }

    /**
     * Appends the given text to the specified {@code RichTextBuilder}, updates the source-to-layout mapping,
     * and returns the new source position after processing the text.
     *
     * @param builder         The {@code RichTextBuilder} instance to which the text will be appended.
     * @param map             The {@code List} containing the mapping between source indices and layout indices.
     * @param text            The text to append to the builder.
     * @param sourcePosition  The current source position, representing the index in the source text that has been
     *                        processed up to this point.
     * @return The updated source position after the appended text has been processed.
     */
    private static int appendRunText(RichTextBuilder builder, List<Integer> map, String text, int sourcePosition) {
        builder.append(text);
        for (int i = 0; i < text.length(); i++) {
            map.add(++sourcePosition);
        }
        return sourcePosition;
    }

    /**
     * Builds a mapping from source indices to layout indices based on the given layout-to-source mapping.
     * This method ensures that every source position is mapped to a corresponding layout position,
     * either directly or by propagating the last known valid mapping.
     *
     * @param layoutToSourceMap An array where each index represents a layout position and the value
     *                          at that index represents the corresponding source position.
     * @param sourceLength      The length of the source, indicating the number of source positions
     *                          to be mapped.
     * @return An array where each index represents a source position, and the value at that index
     *         represents the corresponding layout position.
     */
    private static int[] buildSourceToLayoutMap(int[] layoutToSourceMap, int sourceLength) {
        int[] sourceToLayout = new int[sourceLength + 1];
        Arrays.fill(sourceToLayout, -1);
        for (int layoutPos = 0; layoutPos < layoutToSourceMap.length; layoutPos++) {
            int sourcePos = Math.clamp(layoutToSourceMap[layoutPos], 0, sourceLength);
            if (sourceToLayout[sourcePos] < 0) {
                sourceToLayout[sourcePos] = layoutPos;
            }
        }

        int lastLayoutPos = 0;
        for (int sourcePos = 0; sourcePos < sourceToLayout.length; sourcePos++) {
            if (sourceToLayout[sourcePos] < 0) {
                sourceToLayout[sourcePos] = lastLayoutPos;
            } else {
                lastLayoutPos = sourceToLayout[sourcePos];
            }
        }
        return sourceToLayout;
    }

    /**
     * Generates fragmented text based on the provided parameters and layout constraints.
     *
     * @param text      The rich text object containing the content to be fragmented.
     * @param fontUtil  Utility for font-related operations and measurements.
     * @param font      The font to be used for rendering the text.
     * @param width     The desired width for the text layout.
     * @param wrapWidth The width at which the text should wrap to the next line.
     * @return A FragmentedText instance representing the processed and fragmented text.
     */
    private static FragmentedText generateFragments(
            RichText text,
            FontUtil fontUtil,
            Font font,
            float width,
            float wrapWidth
    ) {
        return FragmentedText.generateFragments(
                text,
                fontUtil,
                font,
                width,
                Float.MAX_VALUE,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                HAnchor.LEFT,
                VAnchor.TOP,
                wrapWidth
        );
    }
}

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A {@link RichTextConverter} implementation for translating
 * {@code RichText} to Strings using ANSI escape sequences for markup.
 */
public final class AnsiConverter extends AttributeBasedConverter<String> {

    private static final Map<String, Object> DEFAULT_ATTRIBUTES = new HashMap<>();
    private final HashMap<String, BiFunction<@Nullable Object, @Nullable Object, String>> mappings = new HashMap<>();
    private boolean reset;
    private boolean reverseVideo;

    private AnsiConverter() {
    }

    /**
     * Send RESET sequence at beginning.
     *
     * @param flag set to true to enable RESET before output
     * @return the option to use
     */
    public static AnsiConversionOption reset(boolean flag) {
        return new AnsiConversionOption(c -> c.setReset(flag));
    }

    /**
     * Enable reverse video for output.
     *
     * @param flag set to true to enable reverse video output
     * @return the option to use
     */
    public static AnsiConversionOption reverseVideo(boolean flag) {
        return new AnsiConversionOption(c -> c.setReverseVideo(flag));
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     *
     * @param attribute the name of the attribute to map
     * @param mapper    the mapper, a function object mapping attribute values to a pair of (opening, closing)
     *                  ESC sequences
     * @return the option tp use
     */
    public static AnsiConversionOption map(String attribute,
                                           BiFunction<Object, Object, String> mapper) {
        return new AnsiConversionOption(c -> c.mappings.put(attribute, mapper));
    }

    /**
     * Create a converter.
     *
     * @param options the options to apply
     * @return new converter instance
     */
    public static AnsiConverter create(Collection<AnsiConversionOption> options) {
        AnsiConverter instance = new AnsiConverter();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    /**
     * Create a converter.
     *
     * @param options the options to apply
     * @return new converter instance
     */
    public static AnsiConverter create(AnsiConversionOption... options) {
        return create(List.of(options));
    }

    void setReset(boolean flag) {
        this.reset = flag;
    }

    void setReverseVideo(boolean flag) {
        this.reverseVideo = flag;
    }

    @Override
    protected AnsiConverterImpl createConverter(RichText text) {
        return new AnsiConverterImpl(text);
    }

    /**
     * Implementation of an ANSI conversion utility for rich text to ANSI formatted text.
     * This class extends the {@code AttributeBasedConverterImpl<String>} to facilitate
     * conversion of text with style attributes into ANSI escape code formatted text.
     */
    protected class AnsiConverterImpl extends AttributeBasedConverterImpl<String> {

        private final StringBuilder buffer;

        AnsiConverterImpl(RichText text) {
            super(DEFAULT_ATTRIBUTES);
            this.buffer = new StringBuilder(text.length() * 11 / 10);
            if (reset) buffer.append(AnsiCode.reset());
            if (reverseVideo) buffer.append(AnsiCode.reverse(true));
        }

        @Override
        protected String get() {
            return buffer.toString();
        }

        @Override
        protected void apply(Map<String, Pair<@Nullable Object, @Nullable Object>> changedAttributes) {
            Map<String, @Nullable Object> attributes = new HashMap<>();
            Deque<String> tags = new ArrayDeque<>();
            changedAttributes.forEach((attribute, values) -> {
                attributes.put(attribute, values.second());
                BiFunction<@Nullable Object, @Nullable Object, String> mapping = mappings.get(attribute);
                if (mapping != null) {
                    tags.push(mapping.apply(values.first(), values.second()));
                }
            });
            // apply the default font styles 
            applyFontChanges(TextAttributes.getFontDef(attributes));
            // apply the additional styles
            tags.forEach(buffer::append);
        }

        @Override
        protected void appendChars(CharSequence s) {
            buffer.append(s);
        }

        /**
         * Applies font changes to the ANSI conversion buffer based on the given font definition.
         * This method appends the appropriate ANSI escape codes to the buffer for each defined
         * font style present in the FontDef object.
         *
         * @param changes an object representing the font definition containing attributes such as
         *                color, bold, underline, strike-through, and italic.
         */
        protected void applyFontChanges(FontDef changes) {
            changes.ifColorDefined(c -> buffer.append(AnsiCode.fg(c)));
            changes.ifBoldDefined(c -> buffer.append(AnsiCode.bold(c)));
            changes.ifUnderlineDefined(c -> buffer.append(AnsiCode.underline(c)));
            changes.ifStrikeThroughDefined(c -> buffer.append(AnsiCode.strikeThrough(c)));
            changes.ifItalicDefined(c -> buffer.append(AnsiCode.italic(c)));
        }
    }

}

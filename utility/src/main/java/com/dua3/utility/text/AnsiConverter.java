package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;

import java.util.*;
import java.util.function.BiFunction;

/**
 * A {@link RichTextConverter} implementation for translating
 * {@code RichText} to Strings using ANSI escape sequences for markup.
 */
public final class AnsiConverter extends AttributeBasedConverter<String> {

    /**
     * Send RESET sequence at beginning.
     * @param flag set to true to enable RESET before output
     * @return the option to use
     */
    public static AnsiConversionOption reset(boolean flag) {
        return new AnsiConversionOption(c -> c.setReset(flag));
    }

    /**
     * Enable reverse video ofr output.
     * @param flag set to true to enable reverse video output
     * @return the option to use
     */
    public static AnsiConversionOption reverseVideo(boolean flag) {
        return new AnsiConversionOption(c -> c.setReverseVideo(flag));
    }

    /**
     * Set the mapper for a specific attribute. If the attribute is already mapped, the mappers are combined.
     * @param attribute the name of the attribute to map
     * @param mapper    the mapper, a function object mapping attribute values to a pair of (opening,closing) 
     *                  ESC sequences
     * @return the option tp use
     */
    public static AnsiConversionOption map(String attribute, BiFunction<Object,Object, String> mapper) {
        return new AnsiConversionOption(c -> c.mappings.put(Objects.requireNonNull(attribute), Objects.requireNonNull(mapper)));
    }

    private boolean reset = false;
    private boolean reverseVideo = false;
    private final HashMap<String, BiFunction<Object,Object, String>> mappings = new HashMap<>();

    /**
     * Create a ready to use converter with default mappings.
     * @return converter with standard mappings
     */
    public static AnsiConverter create(Collection<AnsiConversionOption> options) {
        AnsiConverter instance = new AnsiConverter();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    /**
     * Create a ready to use converter with default mappings.
     * @return converter with standard mappings
     */
    public static AnsiConverter create(AnsiConversionOption... options) {
        return create(Arrays.asList(options));
    }

    private AnsiConverter() {
    }

    void setReset(boolean flag) {
        this.reset = flag;
    }

    void setReverseVideo(boolean flag) {
        this.reverseVideo = flag;
    }

    private static final Map<String,Object> DEFAULT_ATTRIBUTES = new HashMap<>();
    
    @Override
    protected AnsiConverterImpl createConverter(RichText text) {
        return new AnsiConverterImpl(text);
    }
    
    class AnsiConverterImpl extends AttributeBasedConverterImpl<String> {

        private final StringBuilder buffer;

        AnsiConverterImpl(RichText text) {
            super(DEFAULT_ATTRIBUTES);
            this.buffer = new StringBuilder(text.length()*11/10);
            if (reset) buffer.append(AnsiCode.reset());
            if (reverseVideo) buffer.append(AnsiCode.reverse(true));
        }

        @Override
        protected String get() {
            return buffer.toString();
        }

        @Override
        protected void apply(Map<String, Pair<Object, Object>> changedAttributes) {
            Map<String, Object> attributes = new HashMap<>();
            Deque<String> tags = new LinkedList<>();
            changedAttributes.forEach( (attribute, values) -> {
                attributes.put(attribute, values.second);
                BiFunction<Object, Object, String> mapping = mappings.get(attribute);
                if (mapping != null) {
                    tags.push(mapping.apply(values.first, values.second));
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

        protected void applyFontChanges(FontDef changes) {
            changes.ifColorDefined(c -> buffer.append(AnsiCode.fg(c)));
            changes.ifBoldDefined(c -> buffer.append(AnsiCode.bold(c)));
            changes.ifUnderlineDefined(c -> buffer.append(AnsiCode.underline(c)));
            changes.ifStrikeThroughDefined(c -> buffer.append(AnsiCode.strikeThrough(c)));
            changes.ifItalicDefined(c -> buffer.append(AnsiCode.italic(c)));
        }
    }

}
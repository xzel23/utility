package com.dua3.utility.text;

import com.dua3.utility.io.AnsiCode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A {@link RichTextConverter} implementation for translating
 * {@code RichText} to Strings using ANSI escape sequences for markup.
 */
public final class AnsiConverter extends AttributeBasedConverter<String> {

    private boolean reset = false;
    private boolean reverseVideo = false;

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

    private static final Font DEFAULT_FONT = new Font();
    
    @Override
    protected AnsiConverterImpl createConverter(RichText text) {
        return null;
    }
    
    class AnsiConverterImpl extends AttributeBasedConverterImpl<String> {

        private final StringBuilder buffer;

        AnsiConverterImpl(RichText text) {
            super(DEFAULT_FONT);
            this.buffer = new StringBuilder(text.length()*11/10);
            if (reset) buffer.append(AnsiCode.reset());
            if (reverseVideo) buffer.append(AnsiCode.reverse(true));
        }
        
        @Override
        protected String get() {
            return buffer.toString();
        }

        @Override
        protected void apply(Font font, FontDef changes) {
            changes.ifColorDefined(c -> buffer.append(AnsiCode.fg(c)));
            changes.ifBoldDefined(c -> buffer.append(AnsiCode.bold(c)));
            changes.ifUnderlineDefined(c -> buffer.append(AnsiCode.underline(c)));
            changes.ifStrikeThroughDefined(c -> buffer.append(AnsiCode.strikeThrough(c)));
            changes.ifItalicDefined(c -> buffer.append(AnsiCode.italic(c)));
        }

        @Override
        protected void appendChars(CharSequence s) {
            buffer.append(s);
        }
    }
    
}

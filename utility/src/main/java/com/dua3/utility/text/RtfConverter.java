package com.dua3.utility.text;

import com.dua3.utility.text.imp.rtf.RtfReader;
import com.dua3.utility.text.imp.rtf.RtfWriter;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for converting between RichText and RTF.
 */
public final class RtfConverter implements RichTextConverterExt<String> {
    private static final class SingletonHolder {
        private static final RtfConverter INSTANCE = new RtfConverter();
    }

    private RtfConverter() {
        // nothing to do
    }

    /**
     * Retrieve an instance of {@code RtfConverter}.
     *
     * @return an {@code Optional} containing an instance of {@code RtfConverter}, if available.
     */
    public static Optional<RtfConverter> get() {
        return Optional.of(SingletonHolder.INSTANCE);
    }

    @Override
    public RichText toRichText(String s) {
        Objects.requireNonNull(s, "s");
        return RtfReader.read(s);
    }

    @Override
    public String fromRichText(RichText text) {
        Objects.requireNonNull(text, "text");
        return RtfWriter.write(text);
    }
}

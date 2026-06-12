package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextConverter;
import com.dua3.utility.text.RichTextConverterExt;

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

    public static Optional<RtfConverter> get() {
        return Optional.of(SingletonHolder.INSTANCE);
    }

    @Override
    public RichText toRichText(String s) {
        return null;
    }

    @Override
    public String fromRichText(RichText text) {
        return "";
    }
}

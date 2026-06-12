package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextConverterExt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for converting between RichText and RTF.
 */
public final class RtfConverter implements RichTextConverterExt<String> {
    private static final String RTF_SOURCE_CLASS = "com.rtfparserkit.parser.RtfStringSource";
    private static final String RTF_SOURCE_INTERFACE = "com.rtfparserkit.parser.IRtfSource";
    private static final String STRING_TEXT_CONVERTER_CLASS = "com.rtfparserkit.converter.text.StringTextConverter";

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
        if (s.isEmpty()) {
            return RichText.emptyText();
        }

        String text = normalizeLineEndings(parseRtfToText(s));
        return text.isEmpty() ? RichText.emptyText() : RichText.valueOf(text);
    }

    @Override
    public String fromRichText(RichText text) {
        return "";
    }

    private static String normalizeLineEndings(String s) {
        return s.indexOf('\r') < 0 ? s : s.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static String parseRtfToText(String rtf) {
        try {
            Class<?> converterClass = Class.forName(STRING_TEXT_CONVERTER_CLASS);
            Object converter = converterClass.getDeclaredConstructor().newInstance();

            Class<?> sourceClass = Class.forName(RTF_SOURCE_CLASS);
            Object source = sourceClass.getDeclaredConstructor(String.class).newInstance(rtf);

            Class<?> sourceInterface = Class.forName(RTF_SOURCE_INTERFACE);
            Method convert = converterClass.getMethod("convert", sourceInterface);
            convert.invoke(converter, source);

            Method getText = converterClass.getMethod("getText");
            Object text = getText.invoke(converter);
            return text instanceof String s ? s : "";
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("rtfparserkit is not available on the classpath", e);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("failed to invoke rtfparserkit parser", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("failed to parse RTF", e.getCause() == null ? e : e.getCause());
        }
    }
}

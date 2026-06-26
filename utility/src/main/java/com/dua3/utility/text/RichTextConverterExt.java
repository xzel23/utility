package com.dua3.utility.text;

import com.dua3.utility.data.Converter;

import java.util.function.Function;

/**
 * An extension of the {@code RichTextConverter} interface for bidirectional conversion
 * between {@link RichText} and a specified type {@code T}. This interface also extends
 * the {@code Converter} interface for added compatibility with other frameworks.
 *
 * @param <T> the target type for the conversion
 */
public interface RichTextConverterExt<T> extends RichTextConverter<T>, Converter<ToRichText, T> {
    @Override
    default Function<ToRichText, T> a2b() {
        return this::convert;
    }

    @Override
    default Function<T, ToRichText> b2a() {
        return this::convertBack;
    }

    @Override
    default T convert(ToRichText text) {
        return fromRichText(text);
    }

    @Override
    default RichText convertBack(T t) {
        return toRichText(t);
    }

    /**
     * Converts the given object of type T into a RichText representation.
     *
     * @param t the object of type T to be converted into RichText
     * @return the RichText representation of the input object
     */
    RichText toRichText(T t);

    /**
     * Converts a RichText object into an instance of type T.
     *
     * @param text the RichText object to be converted
     * @return an object of type T created from the given RichText
     */
    T fromRichText(ToRichText text);
}

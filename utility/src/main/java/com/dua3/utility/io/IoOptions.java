package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.SimpleOption;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Helper class defining some common {@link com.dua3.utility.options.Option} instances
 * that can be used for command line parsing.
 */
public final class IoOptions {

    private IoOptions() {
    }

    /**
     * Returns a ChoiceOption object that represents a choice option for selecting the character encoding.
     * The returned ChoiceOption object allows the user to choose from a collection of available character encodings.
     *
     * @return a ChoiceOption object representing the character encoding choice option
     */
    public static ChoiceOption<Charset> charset() {
        return ChoiceOption.create(
                        Charset::forName,
                        Object::toString,
                        () -> Collections.unmodifiableCollection(Charset.availableCharsets().values()),
                        "--charset")
                .description("set character encoding")
                .defaultValue(StandardCharsets.UTF_8);
    }

    /**
     * Returns a ChoiceOption object that represents a choice option for selecting the locale.
     * The returned ChoiceOption object allows the user to choose from a collection of available locales.
     *
     * @return a ChoiceOption object representing the locale choice option
     */
    public static ChoiceOption<Locale> locale() {
        return ChoiceOption.create(
                        Locale::forLanguageTag,
                        Object::toString,
                        () -> List.of(Locale.getAvailableLocales()),
                        "--locale")
                .description("set locale")
                .defaultValue(Locale::getDefault);
    }

    /**
     * Returns a SimpleOption object that represents an option for setting the input path.
     *
     * @return a SimpleOption object representing the input option
     */
    public static SimpleOption<Path> input() {
        return SimpleOption.create(
                Paths::get,
                "-i", "--input"
        ).description("set input");
    }

    /**
     * Returns a SimpleOption object that represents an option for setting the output path.
     *
     * @return a SimpleOption object representing the output option
     */
    public static SimpleOption<Path> output() {
        return SimpleOption.create(
                Paths::get,
                "-o", "--output"
        ).description("set output");
    }

    /**
     * Returns a ChoiceOption object that represents an option for setting the text delimiter character.
     *
     * @return a ChoiceOption object representing the text delimiter option
     */
    public static ChoiceOption<Character> textDelimiter() {
        return ChoiceOption.create(
                        (String s) -> s.charAt(0),
                        Object::toString,
                        () -> List.of('"', '\''),
                        "-t", "--text-delimiter")
                .description("set text delimiter")
                .defaultValue('"');
    }

    /**
     * Returns a ChoiceOption object that represents an option for setting the field separator character.
     *
     * @return a ChoiceOption object representing the field separator option
     */
    public static ChoiceOption<Character> fieldSeparator() {
        return ChoiceOption.create(
                        (String s) -> s.charAt(0),
                        Object::toString,
                        () -> List.of(',', ';'),
                        "-s", "--field-separator")
                .description("set field separator")
                .defaultValue(',');
    }

    /**
     * Returns a ChoiceOption object that represents an option for setting the date format.
     *
     * @return a ChoiceOption object representing the date format option
     */
    public static ChoiceOption<PredefinedDateFormat> dateFormat() {
        return ChoiceOption.create(
                        PredefinedDateFormat.class,
                        "--date-format")
                .defaultValue(PredefinedDateFormat.ISO_DATE);
    }

    /**
     * Returns a ChoiceOption object that represents an option for setting the date and time format.
     *
     * @return a ChoiceOption object representing the date and time format option
     */
    public static ChoiceOption<PredefinedDateTimeFormat> dateTimeFormat() {
        return ChoiceOption.create(
                        PredefinedDateTimeFormat.class,
                        "--date-time-format")
                .defaultValue(PredefinedDateTimeFormat.ISO_DATE_TIME);
    }

    // get values from arguments

    /**
     * Returns the charset specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the Charset object representing the specified character encoding
     */
    public static Charset getCharset(Arguments cmd) {
        return cmd.getOrThrow(charset());
    }

    /**
     * Returns the locale specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the Locale object representing the specified locale
     */
    public static Locale getLocale(Arguments cmd) {
        return cmd.getOrThrow(locale());
    }

    /**
     * Returns the predefined date format specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the PredefinedDateFormat object representing the specified date format
     */
    public static PredefinedDateFormat getDateFormat(Arguments cmd) {
        return cmd.getOrThrow(dateFormat());
    }

    /**
     * Returns the predefined date and time format specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the PredefinedDateTimeFormat object representing the specified date and time format
     */
    public static PredefinedDateTimeFormat getDateTimeFormat(Arguments cmd) {
        return cmd.getOrThrow(dateTimeFormat());
    }

    /**
     * Returns the text delimiter specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the Character representing the specified text delimiter
     */
    public static Character getTextDelimiter(Arguments cmd) {
        return cmd.getOrThrow(textDelimiter());
    }

    /**
     * Returns the field separator specified in the given Arguments object.
     *
     * @param cmd the Arguments object containing the command line arguments
     * @return the Character representing the specified field separator
     */
    public static Character getFieldSeparator(Arguments cmd) {
        return cmd.getOrThrow(fieldSeparator());
    }

}

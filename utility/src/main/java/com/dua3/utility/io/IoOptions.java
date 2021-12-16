package com.dua3.utility.io;

import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.SimpleOption;
import com.dua3.cabe.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

    public static ChoiceOption<Charset> charset() {
        return ChoiceOption.create(
                Charset::forName, 
                Object::toString, 
                () -> Collections.unmodifiableCollection(Charset.availableCharsets().values()),
                "--charset")
                .description("set character encoding")
                .defaultValue(StandardCharsets.UTF_8);
    }

    public static ChoiceOption<Locale> locale() {
        return ChoiceOption.create(
                Locale::forLanguageTag, 
                Object::toString, 
                () -> Arrays.asList(Locale.getAvailableLocales()),
                        "--locale")
                .description("set locale")
                .defaultValue(Locale::getDefault);
    }

    public static SimpleOption<Path> input() {
        return SimpleOption.create(
                Paths::get,
                "-i", "--input"
        ).description("set input");
    }

    public static SimpleOption<Path> output() {
        return SimpleOption.create(
                Paths::get,
                "-o", "--output"
        ).description("set output");
    }

    public static ChoiceOption<Character> textDelimiter() {
        return ChoiceOption.create(
                (String s) -> s.charAt(0),
                Object::toString,
                () -> List.of('"', '\''),
                "-t", "--text-delimiter")
                .description("set text delimiter")
                .defaultValue('"');
    }

    public static ChoiceOption<Character> fieldSeparator() {
        return ChoiceOption.create(
                (String s) -> s.charAt(0),
                Object::toString,
                () -> List.of(',', ';'),
                "-s", "--field-separator")
                .description("set field separator")
                .defaultValue(',');
    }

    public static ChoiceOption<PredefinedDateFormat> dateFormat() {
        return ChoiceOption.create(
                PredefinedDateFormat.class, 
                "--date-format")
                .defaultValue(PredefinedDateFormat.ISO_DATE);
    }

    public static ChoiceOption<PredefinedDateTimeFormat> dateTimeFormat() {
        return ChoiceOption.create(
                PredefinedDateTimeFormat.class, 
                "--date-time-format")
                .defaultValue(PredefinedDateTimeFormat.ISO_DATE_TIME);
    }

    // get values from arguments
    
    public static Charset getCharset(@NotNull Arguments cmd) {
        return cmd.getOrThrow(charset());
    }
    
    public static Locale getLocale(@NotNull Arguments cmd) {
        return cmd.getOrThrow(locale());
    }
    
    public static PredefinedDateFormat getDateFormat(@NotNull Arguments cmd) {
        return cmd.getOrThrow(dateFormat());
    }

    public static PredefinedDateTimeFormat getDateTimeFormat(@NotNull Arguments cmd) {
        return cmd.getOrThrow(dateTimeFormat());
    }
    
    public static Character getTextDelimiter(@NotNull Arguments cmd) {
        return cmd.getOrThrow(textDelimiter());
    }

    public static Character getFieldSeparator(@NotNull Arguments cmd) {
        return cmd.getOrThrow(fieldSeparator());
    }

}

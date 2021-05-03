package com.dua3.utility.io;

import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.SimpleOption;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IoOptions {

    public static ChoiceOption<Charset> charset() {
        return ChoiceOption.create(
                Charset::forName, 
                Object::toString, 
                () -> Collections.unmodifiableCollection(Charset.availableCharsets().values()),
                () -> StandardCharsets.UTF_8, 
                "--charset"
        ).description("set character encoding");
    }

    public static ChoiceOption<Locale> locale() {
        return ChoiceOption.create(
                localeName -> Locale.forLanguageTag(localeName), 
                Object::toString, 
                () -> Arrays.asList(Locale.getAvailableLocales()),
                Locale::getDefault, 
                "--locale"
        ).description("set locale");
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
                (String s) -> Character.valueOf(s.charAt(0)),
                Object::toString,
                () -> List.of('"', '\''),
                () -> '"',
                "-t", "--text-delimiter"
        ).description("set text delimiter");
    }

    public static ChoiceOption<Character> fieldSeparator() {
        return ChoiceOption.create(
                (String s) -> Character.valueOf(s.charAt(0)),
                Object::toString,
                () -> List.of(',', ';'),
                () -> Character.valueOf('"'),
                "-s", "--field-separator"
        ).description("set field separator");
    }

    public static ChoiceOption<PredefinedDateFormat> dateFormat() {
        return ChoiceOption.create(PredefinedDateFormat.class, () -> PredefinedDateFormat.ISO_DATE, "--date-format");
    }

    public static ChoiceOption<PredefinedDateTimeFormat> dateTimeFormat() {
        return ChoiceOption.create(PredefinedDateTimeFormat.class, () -> PredefinedDateTimeFormat.ISO_DATE_TIME, "--date-time-format");
    }

    // get values from arguments
    
    public static Charset getCharset(Arguments cmd) {
        return cmd.get(charset());
    }
    
    public static Locale getLocale(Arguments cmd) {
        return cmd.get(locale());
    }
    
    public static PredefinedDateFormat getDateFormat(Arguments cmd) {
        return cmd.get(dateFormat());
    }

    public static PredefinedDateTimeFormat getDateTimeFormat(Arguments cmd) {
        return cmd.get(dateTimeFormat());
    }
    
    public static Character getTextDelimiter(Arguments cmd) {
        return cmd.get(textDelimiter());
    }

    public static Character getFieldSeparator(Arguments cmd) {
        return cmd.get(fieldSeparator());
    }

}

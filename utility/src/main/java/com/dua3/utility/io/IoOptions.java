package com.dua3.utility.io;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.Param;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper class defining some common {@link com.dua3.utility.options.Option} instances
 * that can be used for command line parsing.
 */
@SuppressWarnings("MagicCharacter")
public final class IoOptions {

    /**
     * Represents an option for specifying the character encoding to be used in various text-based operations.
     * This option allows users to define a custom character encoding, and defaults to UTF-8 if no encoding is specified.
     */
    public static final Option<Charset> OPTION_CHARSET =
            charset(() -> StandardCharsets.UTF_8);
    /**
     * Represents an option for specifying the date and time format to be used in various operations.
     * This option allows the user to select a predefined date and time format, enabling consistent parsing
     * or formatting of date-time values.
     * <p>
     * The default value for this option is {@link PredefinedDateTimeFormat#ISO_DATE_TIME}, which supports
     * the ISO-8601 date-time format.
     */
    public static final Option<PredefinedDateTimeFormat> OPTION_DATE_TIME_FORMAT =
            dateFormat(() -> PredefinedDateTimeFormat.ISO_DATE_TIME);
    /**
     * Defines an option for specifying the field separator character.
     * This option allows customization of the character used to separate fields
     * within the same row during data processing, such as in CSV files.
     * <p>
     * The default field separator is provided by a {@link Supplier} that
     * specifies a default value, which is a comma (`,`).
     * <p>
     * This option can be used to align the field separator configuration
     * with the data formatting requirements of specific use cases.
     */
    public static final Option<Character> OPTION_FIELD_SEPARATOR = fieldSeparator(() -> ',');
    /**
     * Represents an optional configuration for specifying the character used as a text delimiter
     * in input or output operations. This option is typically used to define the character
     * that delimits quoted text, such as when handling CSV or similar structured text formats.
     * <p>
     * By default, the text delimiter is set to the double-quote character (`"`).
     * <p>
     * The {@code textDelimiter} method is used to initialize this option with a default character,
     * supplied through a {@code Supplier<Character>}.
     * <p>
     * This option allows flexibility by enabling customization of the quoted text delimiter
     * to suit various input or output data formats.
     */
    public static final Option<Character> OPTION_TEXT_DELIMITER = textDelimiter(() -> '"');
    /**
     * A predefined constant representing the input path option.
     * This option allows the user to specify the path to the input data.
     * The default path is determined by the provided supplier, which can return {@code null}.
     *
     * @see IoOptions#input(Supplier)
     */
    public static final Option<Path> OPTION_INPUT = input(() -> null);
    /**
     * An option for specifying the output path. This option allows users to define the
     * path where the output data will be written.
     * <p>
     * The option is created using a default {@code Supplier} that can provide a default
     * output path if none is explicitly specified.
     * <p>
     * The provided {@code Supplier} for the default value returns {@code null} by default,
     * indicating that no output path is specified unless explicitly set.
     */
    public static final Option<Path> OPTION_OUTPUT = output(() -> null);
    /**
     * Represents an option for specifying the {@code Locale} to be used in various operations.
     * This option allows configuring regional settings, such as language and formatting conventions,
     * applicable for tasks like reading or writing files.
     * <p>
     * The default locale is provided by invoking the {@code Locale::getDefault} method,
     * which retrieves the system's default locale settings.
     * <p>
     * Use this option when a specific locale must be set or when the default system locale suffices.
     */
    public static final Option<Locale> OPTION_LOCALE = locale(Locale::getDefault);

    private IoOptions() {
    }

    /**
     * Creates an option for character encoding selection. This method defines a customizable option
     * that allows the user to specify a character encoding from the available system character sets.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default character encoding if none is specified
     * @return an {@code Option<Charset>} representing the character encoding selection
     */
    public static Option<Charset> charset(Supplier<Charset> defaultSupplier) {
        return Option.createSelectionOption(
                "Charset",
                "The character encoding.",
                Charset.class,
                "charset",
                Charset.availableCharsets()
                        .values()
                        .stream()
                        .sorted(Comparator.comparing(Charset::displayName))
                        .toList(),
                Converter.create(Charset::forName, Charset::displayName),
                defaultSupplier,
                "--charset", "-cs"
        );
    }

    /**
     * Creates an option for selecting the locale. This option allows specifying
     * the locale to be used when performing operations such as reading or writing
     * files. The locale defines regional settings such as language and formatting
     * conventions.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default locale
     *                        if none is explicitly specified
     * @return an {@code Option<Locale>} representing the locale selection
     */
    public static Option<Locale> locale(Supplier<Locale> defaultSupplier) {
        return Option.createSelectionOption(
                "Locale",
                "The locale to use when reading or writing files.",
                Locale.class,
                "locale",
                LangUtil.asUnmodifiableList(Locale.getAvailableLocales()),
                Converter.create(Locale::forLanguageTag, locale -> LangUtil.orElseGet(locale, Locale::getDefault).getLanguage()),
                defaultSupplier,
                "--locale", "-lc"
        );
    }

    /**
     * Creates an option for specifying the input path.
     * This option allows the user to define the path where the input data is located.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default input path if none is explicitly specified.
     *                        The supplied path can be {@code null}.
     * @return an {@code Option<Path>} representing the input path option.
     */
    public static Option<Path> input(Supplier<@Nullable Path> defaultSupplier) {
        return Option.createSimpleOption(
                "Input path",
                "The path of the input data.",
                Param.ofPath(
                        "Input path",
                        "The path of the input data", "in",
                        Param.Required.REQUIRED,
                        Objects::nonNull
                ),
                defaultSupplier,
                "--input", "-i"
        );
    }

    /**
     * Creates an option for specifying the output path. This option allows the user to define the
     * path where the output data will be written.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default output path if none
     *                        is explicitly specified. The supplied path can be {@code null}.
     * @return an {@code Option<Path>} representing the output path option.
     */
    public static Option<Path> output(Supplier<@Nullable Path> defaultSupplier) {
        return Option.createSimpleOption(
                "Output path",
                "The path of the output data.",
                Param.ofPath(
                        "Output path",
                        "The path of the output data", "out",
                        Param.Required.REQUIRED,
                        Objects::nonNull
                ),
                defaultSupplier,
                "--output", "-o"
        );
    }

    /**
     * Creates an option for specifying the text delimiter for quoted texts.
     * This option allows the user to define the character used as a text delimiter
     * when handling quoted text in input or output operations.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default text delimiter
     *                        if none is explicitly specified
     * @return an {@code Option<Character>} representing the text delimiter option
     */
    public static Option<Character> textDelimiter(Supplier<Character> defaultSupplier) {
        return Option.createSelectionOption(
                "Text delimiter",
                "The character to use as the text delimiter for quoted texts.",
                Character.class,
                "delimiter",
                List.of('"', '\''),
                Converter.create(s -> s.charAt(0), obj -> Objects.toString(obj, "")),
                defaultSupplier,
                "--text-delimiter", "-d"
        );
    }

    /**
     * Creates an option for specifying the field separator character.
     * This option allows the user to define the character used to
     * separate fields within the same row of data.
     *
     * @param defaultSupplier a {@code Supplier<Character>} that provides the default
     *                        field separator character if none is explicitly specified
     * @return an {@code Option<Character>} representing the field separator option
     */
    public static Option<Character> fieldSeparator(Supplier<Character> defaultSupplier) {
        return Option.createSelectionOption(
                "Field separator",
                "The character used to separate fields belonging to the same row.",
                Character.class,
                "separator",
                List.of(';', ',', '|'),
                Converter.create(s -> s.charAt(0), String::valueOf),
                defaultSupplier,
                "--field-separator", "-s"
        );
    }

    /**
     * Creates an option for specifying the date format to be used in various operations.
     * This method allows the user to select a predefined date and time format.
     *
     * @param defaultSupplier a {@code Supplier} that provides the default date format
     *                        if none is explicitly specified
     * @return an {@code Option<PredefinedDateTimeFormat>} representing the configurable date format option
     */
    public static Option<PredefinedDateTimeFormat> dateFormat(Supplier<PredefinedDateTimeFormat> defaultSupplier) {
        return Option.createEnumOption(
                "Date Format",
                "The date format to use.",
                PredefinedDateTimeFormat.class,
                "date-format",
                defaultSupplier,
                "--date-format", "-df"
        );
    }
}

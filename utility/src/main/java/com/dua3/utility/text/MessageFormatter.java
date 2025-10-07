package com.dua3.utility.text;

import com.dua3.utility.i18n.I18N;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * The {@code MessageFormatter} interface provides methods to format messages
 * using different strategies. This can be achieved through standard formatting,
 * {@code MessageFormat} formatting, or localized formatting using an {@code I18N} instance.
 */
public interface MessageFormatter {
    /**
     * Defines the style of formatting to be used within the {@code MessageFormatter}.
     *
     * Each enum constant represents a specific formatting style that determines the
     * approach for formatting messages or replacing placeholders in a provided text input.
     *
     * The available format styles are:
     *
     * - {@code STRING_FORMAT}: Utilizes {@link String#format(Locale, String, Object...)}
     *   for standard Java-style formatting with locale support.
     *
     * - {@code MESSAGE_FORMAT}: Applies {@link MessageFormat} to perform a structured
     *   and localized message formatting process where placeholders are substituted with
     *   arguments.
     *
     * - {@code I18N}: Uses a specific {@code I18N} instance for internationalized and
     *   resource-based message formatting, allowing for localization and context-aware
     *   text transformations.
     */
    enum FormatStyle {
        /**
         * Represents the formatting style that uses {@link String#format(Locale, String, Object...)}
         * for formatting messages or text. This style applies standard Java-style formatting
         * with support for locale-specific formatting.
         */
        STRING_FORMAT,
        /**
         * Specifies a formatting style to be applied for processing messages.
         *
         * Represents the {@link MessageFormat} style of formatting, which is
         * used for structured and locale-sensitive message formatting. It defines a way
         * to substitute placeholders in a text input with provided arguments, allowing
         * for flexibility and internationalization support.
         */
        MESSAGE_FORMAT,
        /**
         * A constant representing the usage of a specific internationalization (I18N) instance
         * for formatting messages. This format style supports localization and context-aware
         * message handling by leveraging resource-based translations or transformations.
         *
         * Typically chosen when messages need to be dynamically adapted according to locale
         * or cultural formatting requirements.
         */
        I18N
    }

    /**
     * Formats the given format string by replacing placeholders with the provided arguments.
     * <p>
     * <strong>NOTE:</strong> The formatting process is implementation defined.
     *
     * @param fmt  the format string containing placeholders to be replaced
     * @param args the arguments to replace the placeholders in the format string
     * @return the formatted string where placeholders are replaced with the corresponding arguments
     */
    String format(String fmt, Object... args);

    /**
     * Returns the input string as is, without any formatting applied.
     *
     * @param s the input string to be returned
     * @return the same input string provided
     */
    default String text(String s) { return s; }

    /**
     * Retrieves the formatting style currently in use by the {@code MessageFormatter}.
     * <p>
     * The formatting style determines the approach taken for formatting messages or
     * processing text input, see {@link FormatStyle} for details.
     *
     * @return the current {@link FormatStyle} used for formatting
     */
    FormatStyle getFormatStyle();

    /**
     * Returns an instance of {@code MessageFormatter} that uses {@link String#format(Locale, String, Object...)}
     * for formatting, using {@code Locale.getDefault()} as locale.
     *
     * @return the default {@code MessageFormatter} instance using standard formatting and the default locale
     */
    static MessageFormatter standard() {
        return MessageFormatterStringFormat.DEFAULT_INSTANCE;
    }

    /**
     * Creates an instance of {@code MessageFormatter} that uses {@link String#format(Locale, String, Object...)}
     * for formatting using the provided locale.
     *
     * @param locale the {@link Locale} to use when formatting
     * @return a {@code MessageFormatter} instance using standard formatting and the provided locale
     */
    static MessageFormatter localized(Locale locale) {
        return new MessageFormatterStringFormat(locale);
    }

    /**
     * Returns the instance of {@code MessageFormatter} that utilizes {@code MessageFormat}
     * for formatting messages. This ensures a structured and localized message formatting
     * process, where placeholders within a string are substituted with the provided arguments.
     *
     * @return the singleton instance of {@code MessageFormatterMessageFormat} used for
     *         formatting messages.
     */
    static MessageFormatter messageFormat() {
        return MessageFormatterMessageFormat.INSTANCE;
    }

    /**
     * Creates a {@code MessageFormatter} configured with the specified {@code I18N} instance,
     * which allows for internationalized message formatting.
     * <p>
     * This implementation will look up texts using the supplied {@link I18N} instance before
     * formatting.
     *
     * @param i18n the {@code I18N} instance providing the localization rules and resources
     *             to be used for formatting messages
     * @return a {@code MessageFormatter} configured to use the provided {@code I18N} instance
     */
    static MessageFormatter i18n(I18N i18n) {
        return new MessageFormatterI18n(i18n);
    }

    /**
     * The {@code MessageFormatterStringFormat} class is an implementation of the {@code MessageFormatter}
     * interface that uses the {@link String#format(Locale, String, Object...)} method for formatting
     * messages. It allows for localized formatting based on a specified {@link Locale}.
     * <p>
     * The formatting operation takes a format string and a list of arguments, replacing placeholders
     * in the format string with the corresponding values of the arguments.
     * <p>
     * This class is immutable and thread-safe.
     */
    final class MessageFormatterStringFormat implements MessageFormatter {
        private static final MessageFormatterStringFormat DEFAULT_INSTANCE = new MessageFormatterStringFormat(Locale.getDefault());

        private final Locale locale;

        private MessageFormatterStringFormat(Locale locale) {
            this.locale = locale;
        }

        @Override
        public String format(String fmt, Object... args) {
            return String.format(locale, fmt, args);
        }

        @Override
        public FormatStyle getFormatStyle() {
            return FormatStyle.STRING_FORMAT;
        }

        @Override
        public String toString() {
            return "MessageFormatterStringFormat{" +
                    "locale=" + locale +
                    '}';
        }
    }

    /**
     * The MessageFormatterMessageFormat class is an implementation of the MessageFormatter
     * interface that formats messages using the {@link MessageFormat#format(String, Object...)}
     * method. This class provides a mechanism to replace placeholders in a format string with the
     * corresponding arguments in a structured and localized manner.
     * <p>
     * This class is immutable and thread-safe.
     */
    final class MessageFormatterMessageFormat implements MessageFormatter {
        private static final MessageFormatterMessageFormat INSTANCE = new MessageFormatterMessageFormat();

        private MessageFormatterMessageFormat() {}

        @Override
        public String format(String fmt, Object... args) {
            return MessageFormat.format(fmt, args);
        }

        @Override
        public FormatStyle getFormatStyle() {
            return FormatStyle.MESSAGE_FORMAT;
        }

        @Override
        public String toString() {
            return "MessageFormatterMessageFormat{}";
        }
    }

    /**
     * The {@code I18NFormatter} class is an implementation of the {@code MessageFormatter} interface
     * that formats messages using an {@code I18N} instance. This allows for localized message formatting
     * based on the key and provided arguments.
     * <p>
     * This class serves as a wrapper around the {@code I18N} instance, delegating the formatting logic
     * to the {@code I18N} implementation.
     * <p>
     * This class is immutable and thread-safe.
     */
    final class MessageFormatterI18n implements MessageFormatter {
        private final I18N i18n;

        private MessageFormatterI18n(I18N i18n) {
            this.i18n = i18n;
        }

        @Override
        public String format(String key, Object... args) {
            return i18n.format(key, args);
        }

        @Override
        public FormatStyle getFormatStyle() {
            return FormatStyle.I18N;
        }

        @Override
        public String toString() {
            return "MessageFormatterI18n{" +
                    "i18n=" + i18n +
                    '}';
        }
    }
}

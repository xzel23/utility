/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.io;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import com.dua3.utility.lang.NamedFunction;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.Options;

/**
 *
 * @author axel
 */
public abstract class Csv {

    public enum PredefinedDateFormat {
        LOCALE_SHORT("short (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.SHORT)),
        LOCALE_LONG("long (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.LONG)),
        ISO_DATE("2000-01-01 15:30:00", locale -> formatFromPattern("yyyy-MM-dd [HH:mm[:ss]]"));

        private static DateTimeFormatter formatFromLocale(Locale locale, FormatStyle style) {
            return DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
        }

        private static DateTimeFormatter formatFromPattern(String pattern) {
            return DateTimeFormatter.ofPattern(pattern);
        }

        private String name;

        private Function<Locale, DateTimeFormatter> factory;

        private PredefinedDateFormat(String name, Function<Locale, DateTimeFormatter> factory) {
            this.name = name;
            this.factory = factory;
        }

        public DateTimeFormatter getFormatter(Locale locale) {
            return factory.apply(locale);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final String OPTION_CHARSET = "Character Encoding";
    public static final String OPTION_LOCALE = "Locale";
    public static final String OPTION_DATEFORMAT = "Date format";

    public static final String OPTION_DELIMITER = "Text delimiter";

    public static final String OPTION_SEPARATOR = "Separator";

    private static final OptionSet OPTIONS = new OptionSet();

    static {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, (a, b) -> a.toString().compareTo(b.toString()));
        OPTIONS.addOption(OPTION_LOCALE, Locale.class, OptionSet.value("default", Locale.ROOT),
                OptionSet.wrap(locales));
        OPTIONS.addOption(OPTION_CHARSET, Charset.class, Charset.defaultCharset(),
                Charset.availableCharsets().values().toArray(new Charset[0]));
        OPTIONS.addOption(OPTION_DATEFORMAT, PredefinedDateFormat.class, PredefinedDateFormat.LOCALE_SHORT,
                PredefinedDateFormat.values());

        OPTIONS.addOption(OPTION_SEPARATOR, NamedFunction.class,
            NamedFunction.create(";", locale -> ';'),
            NamedFunction.create(",", locale -> ','), 
            NamedFunction.create("|", locale -> '|'), 
            NamedFunction.create("TAB", locale -> '\t'));

        OPTIONS.addOption(OPTION_DELIMITER, Character.class, OptionSet.value("\"", '"'), OptionSet.value("'", '\''));
    }

    public static Charset getCharset(Options options) {
        return (Charset) getOptionValue(OPTION_CHARSET, options);
    }

    public static PredefinedDateFormat getDateFormat(Options options) {
        return (PredefinedDateFormat) Csv.getOptionValue(Csv.OPTION_DATEFORMAT, options);
    }

    public static Character getDelimiter(Options options) {
        return (Character) getOptionValue(OPTION_DELIMITER, options);
    }

    public static Locale getLocale(Options options) {
        return (Locale) getOptionValue(OPTION_LOCALE, options);
    }

    public static Optional<Option<?>> getOption(String name) {
        return OPTIONS.getOption(name);
    }

    public static List<Option<?>> getOptions() {
        return OPTIONS.asList();
    }

    public static Object getOptionValue(String name, Options overrides) {
        return OPTIONS.getOptionValue(name, overrides);
    }

    public static Character getSeparator(Options options) {
        Locale locale = getLocale(options);
        @SuppressWarnings("unchecked")
        NamedFunction<Locale, Character> selector = (NamedFunction<Locale, Character>) getOptionValue(OPTION_SEPARATOR,
                options);
        return selector.apply(locale);
    }

}

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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dua3.utility.lang.NamedFunction;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;

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
    private static final OptionSet COMMON_OPTIONS = new OptionSet();

    static {
        // locale
        List<Supplier<Locale>> localesAll = Arrays.stream(Locale.getAvailableLocales())
            .filter(locale -> !Locale.ROOT.equals(locale)) // filter out root - we will add it again later with a different name
            .sorted((lc1,lc2) -> lc1.toString().compareTo(lc2.toString())).distinct().map(Option::value).collect(Collectors.toList());
        OPTIONS.addOption(OPTION_LOCALE, Locale.class, Option.value("default", Locale.ROOT), localesAll);

        List<Supplier<Locale>> localesCommon = Set.of(Locale.ROOT, Locale.getDefault(), Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN, Locale.CHINESE, Locale.JAPANESE, Locale.KOREAN)
            .stream().sorted((lc1,lc2) -> lc1.toString().compareTo(lc2.toString())).distinct().map(Option::value).collect(Collectors.toList());
        COMMON_OPTIONS.addOption(OPTION_LOCALE, Locale.class, Option.value("default", Locale.ROOT), localesCommon);
 
        // charset
        List<Supplier<Charset>> charsetsAll = Charset.availableCharsets().entrySet().stream()
            .map( entry -> Option.value(entry.getKey(), entry.getValue())).sorted().collect(Collectors.toList());
        OPTIONS.addOption(OPTION_CHARSET, Charset.class, Charset.defaultCharset().toString(), charsetsAll);

        List<Supplier<Charset>> charSetsCommon = Stream.of("UTF-8", "ISO-8859-1", "ISO-8859-2", "windows-1252")
            .map(name -> Map.entry(name, Charset.availableCharsets().get(name)))
            .filter(entry -> entry.getValue() != null)
            .map(entry -> Option.value(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());            
        COMMON_OPTIONS.addOption(OPTION_CHARSET, Charset.class, Charset.defaultCharset().toString(), charSetsCommon);

        // dateformat
        OPTIONS.addOption(OPTION_DATEFORMAT, PredefinedDateFormat.class, PredefinedDateFormat.LOCALE_SHORT, PredefinedDateFormat.values());
        COMMON_OPTIONS.addOption(OPTION_DATEFORMAT, PredefinedDateFormat.class, PredefinedDateFormat.LOCALE_SHORT, PredefinedDateFormat.values());

        // separator
        OPTIONS.addOption(OPTION_SEPARATOR, NamedFunction.class,
            NamedFunction.create(";", locale -> ';'),
            NamedFunction.create(",", locale -> ','), 
            NamedFunction.create("|", locale -> '|'), 
            NamedFunction.create("TAB", locale -> '\t'));
        
        COMMON_OPTIONS.addOption(OPTION_SEPARATOR, NamedFunction.class,
            NamedFunction.create(";", locale -> ';'),
            NamedFunction.create(",", locale -> ','), 
            NamedFunction.create("|", locale -> '|'), 
            NamedFunction.create("TAB", locale -> '\t'));

        // deleimiter
        OPTIONS.addOption(OPTION_DELIMITER, Character.class, Option.value("\"", '"'), Option.value("'", '\''));
        
        COMMON_OPTIONS.addOption(OPTION_DELIMITER, Character.class, Option.value("\"", '"'), Option.value("'", '\''));
    }

    public static Charset getCharset(OptionValues options) {
        return (Charset) getOptionValue(OPTION_CHARSET, options);
    }

    public static PredefinedDateFormat getDateFormat(OptionValues options) {
        return (PredefinedDateFormat) Csv.getOptionValue(Csv.OPTION_DATEFORMAT, options);
    }

    public static Character getDelimiter(OptionValues options) {
        return (Character) getOptionValue(OPTION_DELIMITER, options);
    }

    public static Locale getLocale(OptionValues options) {
        return (Locale) getOptionValue(OPTION_LOCALE, options);
    }

    public static Optional<Option<?>> getOption(String name) {
        return OPTIONS.getOption(name);
    }

    public static OptionSet getOptions() {
        return new OptionSet(OPTIONS);
    }

    public static OptionSet getCommonOptions() {
        return new OptionSet(COMMON_OPTIONS);
    }

    public static Object getOptionValue(String name, OptionValues overrides) {
        return OPTIONS.getOptionValue(name, overrides);
    }

    public static Character getSeparator(OptionValues options) {
        Locale locale = getLocale(options);
        @SuppressWarnings("unchecked")
        NamedFunction<Locale, Character> selector = (NamedFunction<Locale, Character>) getOptionValue(OPTION_SEPARATOR,
                options);
        return selector.apply(locale);
    }

}

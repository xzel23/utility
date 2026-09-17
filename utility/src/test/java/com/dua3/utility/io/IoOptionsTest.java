package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.Option;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IoOptionsTest {

    @Test
    void testPredefinedOptionsNotNull() {
        assertNotNull(IoOptions.OPTION_CHARSET);
        assertNotNull(IoOptions.OPTION_DATE_TIME_FORMAT);
        assertNotNull(IoOptions.OPTION_FIELD_SEPARATOR);
        assertNotNull(IoOptions.OPTION_TEXT_DELIMITER);
        assertNotNull(IoOptions.OPTION_INPUT);
        assertNotNull(IoOptions.OPTION_OUTPUT);
        assertNotNull(IoOptions.OPTION_LOCALE);
    }

    @Test
    void testCharsetOptionParsing() {
        Option<Charset> charsetOpt = IoOptions.charset(() -> StandardCharsets.ISO_8859_1);
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        builder.addOption(charsetOpt);
        ArgumentsParser parser = builder.build();

        Arguments defaultArgs = parser.parse();
        assertEquals(StandardCharsets.ISO_8859_1, defaultArgs.get(charsetOpt).orElse(StandardCharsets.ISO_8859_1));

        Arguments parsedArgs = parser.parse("--charset", "UTF-8");
        assertEquals(StandardCharsets.UTF_8, parsedArgs.get(charsetOpt).orElseThrow());
    }

    @Test
    void testDateFormatOptionParsing() {
        Option<PredefinedDateTimeFormat> dfOpt = IoOptions.dateFormat(() -> PredefinedDateTimeFormat.ISO_DATE_TIME);
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        builder.addOption(dfOpt);
        ArgumentsParser parser = builder.build();

        Arguments defaultArgs = parser.parse();
        assertEquals(PredefinedDateTimeFormat.ISO_DATE_TIME, defaultArgs.get(dfOpt).orElseThrow());

        Arguments parsedArgs = parser.parse("--date-format", "LOCALE_SHORT");
        assertEquals(PredefinedDateTimeFormat.LOCALE_SHORT, parsedArgs.get(dfOpt).orElseThrow());
    }

    @Test
    void testFieldSeparatorOptionParsing() {
        Option<Character> sepOpt = IoOptions.fieldSeparator(() -> ',');
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        builder.addOption(sepOpt);
        ArgumentsParser parser = builder.build();

        Arguments defaultArgs = parser.parse();
        assertEquals(Character.valueOf(','), defaultArgs.get(sepOpt).orElseThrow());

        Arguments parsedArgs = parser.parse("--field-separator", ";");
        assertEquals(Character.valueOf(';'), parsedArgs.get(sepOpt).orElseThrow());
    }

    @Test
    void testTextDelimiterOptionParsing() {
        Option<Character> delimOpt = IoOptions.textDelimiter(() -> '"');
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        builder.addOption(delimOpt);
        ArgumentsParser parser = builder.build();

        Arguments defaultArgs = parser.parse();
        assertEquals(Character.valueOf('"'), defaultArgs.get(delimOpt).orElseThrow());

        Arguments parsedArgs = parser.parse("--text-delimiter", "'");
        assertEquals(Character.valueOf('\''), parsedArgs.get(delimOpt).orElseThrow());
    }

    @Test
    void testInputAndOutputPathOptions() {
        Option<Path> inputOpt = IoOptions.input(() -> Paths.get("default-in.txt"));
        Option<Path> outputOpt = IoOptions.output(() -> Paths.get("default-out.txt"));
        Option<java.util.Locale> localeOpt = IoOptions.locale(() -> java.util.Locale.US);

        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        builder.addOption(inputOpt);
        builder.addOption(outputOpt);
        builder.addOption(localeOpt);
        ArgumentsParser parser = builder.build();

        Arguments defaultArgs = parser.parse();
        assertEquals(Paths.get("default-in.txt"), defaultArgs.get(inputOpt).orElseThrow());
        assertEquals(Paths.get("default-out.txt"), defaultArgs.get(outputOpt).orElseThrow());
        assertEquals(java.util.Locale.US, defaultArgs.get(localeOpt).orElseThrow());

        Arguments parsedArgs = parser.parse("-i", "custom-in.txt", "-o", "custom-out.txt", "-lc", "de-DE");
        assertEquals(Paths.get("custom-in.txt"), parsedArgs.get(inputOpt).orElseThrow());
        assertEquals(Paths.get("custom-out.txt"), parsedArgs.get(outputOpt).orElseThrow());
        assertEquals(java.util.Locale.GERMANY, parsedArgs.get(localeOpt).orElseThrow());
    }
}

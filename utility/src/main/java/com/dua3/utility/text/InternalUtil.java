package com.dua3.utility.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class InternalUtil {
    private static final Logger LOG = LogManager.getLogger(InternalUtil.class);

    static final Predicate<String> IS_QUOTING_NEEDED = Pattern.compile("[\\p{L}\\d,.;+-]*").asMatchPredicate().negate();

    private InternalUtil() { /* utility class */ }

    static String quote(String text) {
        return "\"" +
                text.replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                + "\"";
    }

    static String quoteIfNeeded(String text) {
        return IS_QUOTING_NEEDED.test(text) ? quote(text) : text;
    }

    static float decodeFontSize(String s) {
        s = s.strip().toLowerCase(Locale.ROOT);

        int idxUnit = s.length();
        while (idxUnit > 0 && !Character.isDigit(s.charAt(idxUnit - 1))) {
            idxUnit--;
        }
        String unit = s.substring(idxUnit).strip();
        String number = s.substring(0, idxUnit).strip();

        float f = switch (unit) {
            case "pt" -> 1.0f;
            case "em" -> 12.0f;
            case "px" -> 0.75f;
            case "%" -> 0.12f;
            case "vw" -> {
                LOG.warn("unit 'vw' unsupported, treating as 'em'");
                yield 12.0f;
            }
            default -> throw new IllegalArgumentException("invalid value for font-size: " + s);
        };

        return f * Float.parseFloat(number);
    }
}

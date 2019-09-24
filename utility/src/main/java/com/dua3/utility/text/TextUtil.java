// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.text.FontUtil.Bounds;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

public class TextUtil {

    private static final String TRANSFORM_REF_START = "${";

    private static final String TRANSFORM_REF_END = "}";

    /**
     * HTML-escape a string.
     *
     * @param s the string
     * @return the HTML-escaped string
     */
    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(16 + s.length() * 11 / 10);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 127 || c == '"' || c == '<' || c == '>' || c == '&' || c == '\0') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Backslash-escape a string.
     *
     * @param s the string
     * @return the escaped string
     */
    public static String escape(String s) {
        StringBuilder out = new StringBuilder(16 + s.length() * 11 / 10);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 127) {
                // ASCII characters
                switch (c) {
                    case '\0':
                        out.append("\\u0000"); // "\0" might be ambiguous if followed by digits
                        break;
                    case '\\':
                        out.append("\\\\");
                        break;
                    case '\t':
                        out.append("\\t");
                        break;
                    case '\b':
                        out.append("\\b");
                        break;
                    case '\n':
                        out.append("\\n");
                        break;
                    case '\r':
                        out.append("\\r");
                        break;
                    case '\f':
                        out.append("\\f");
                        break;
                    case '\'':
                        out.append("\\'");
                        break;
                    case '\"':
                        out.append("\\\"");
                        break;
                    default:
                        out.append(c);
                        break;
                }
            } else {
                // non-ASCII characters
                switch (Character.getType(c)) {
                    // numbers: pass through
                    case Character.DECIMAL_DIGIT_NUMBER:
                    case Character.LETTER_NUMBER:
                    case Character.OTHER_NUMBER:
                        out.append(c);
                        break;
                    // letters: pass all non-modifying letters through
                    case Character.UPPERCASE_LETTER:
                    case Character.LOWERCASE_LETTER:
                    case Character.OTHER_LETTER:
                    case Character.TITLECASE_LETTER:
                        out.append(c);
                        break;
                    // escape all remaining characters
                    default:
                        out.append("\\u").append(String.format("%04X", (int) c));
                }
            }
        }
        return out.toString();
    }

    /**
     * Transform a templated String.
     *
     * @param template the template
     * @param env      substitution environment
     * @return result of transformation
     * @see #transform(String, UnaryOperator, Consumer)
     */
    public static String transform(String template, UnaryOperator<String> env) {
        StringBuilder sb = new StringBuilder(Math.max(16, template.length()));
        transform(template, env, sb::append);
        return sb.toString();
    }

    /**
     * Transform a templated String.
     * <p>
     * Read {@code template} and copy its contents to {@code output}. For each
     * reference in the form {@code ${VARIABLE}}, the substitution is determined by
     * calling {@code env.apply("VARIABLE")}.
     * </p>
     *
     * @param template the template
     * @param env      substitution environment
     * @param output   output
     */
    public static void transform(String template, UnaryOperator<String> env, Consumer<CharSequence> output) {
        int pos = 0;
        while (pos < template.length()) {
            // find next ref
            int varPos = template.indexOf(TRANSFORM_REF_START, pos);
            if (varPos == -1) {
                // no more refs => copy the remaining text
                output.accept(template.subSequence(pos, template.length()));
                break;
            }

            // copy text from current position to start of ref
            output.accept(template.subSequence(pos, varPos));
            pos = varPos + TRANSFORM_REF_START.length();

            // determine ref name
            int varEnd = template.indexOf(TRANSFORM_REF_END, pos);
            if (varEnd == -1) {
                throw new IllegalStateException();
            }
            String varName = template.substring(pos, varEnd);
            pos = varEnd + TRANSFORM_REF_END.length();

            // insert ref substitution
            output.accept(env.apply(varName));
        }
    }

    private TextUtil() {
        // nop: utility class
    }

    /**
     * Get the font size in pt for a font size given as string.
     *
     * @param s the string
     * @return font size in pt
     */
    public static float decodeFontSize(String s) {
        float factor = 1f;
        if (s.endsWith("pt")) {
            s = s.substring(0, s.length() - 2);
            factor = 1f;
        } else if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2);
            factor = 96f / 72f;
        }
        return factor * Float.parseFloat(s);
    }

    /**
     * Compare two character sequences for content equality.
     *
     * @param a first character sequence
     * @param b second character sequence
     * @return true, if a and b contain the same characters
     */
    public static boolean contentEquals(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) {
            return false;
        }

        OfInt iter1 = a.chars().iterator();
        OfInt iter2 = b.chars().iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return !iter1.hasNext() && !iter2.hasNext();
    }

    /**
     * Find char in CharSequence.
     *
     * @param haystack the sequence to search
     * @param needle   the char to find
     * @return the position where the char was found or -1 if not found
     */
    public static int indexOf(CharSequence haystack, char needle) {
        return indexOf(haystack, needle, 0);
    }

    /**
     * Find text in CharSequence.
     *
     * @param haystack the sequence to search
     * @param needle   the sequence to find
     * @return the position where the sequence was found or -1 if not found
     */
    public static int indexOf(CharSequence haystack, CharSequence needle) {
        return indexOf(haystack, needle, 0);
    }

    /**
     * Find char in CharSequence.
     *
     * @param haystack  the sequence to search
     * @param needle    the char to find
     * @param fromIndex the index to start from
     * @return the position where the char was found or -1 if not found
     */
    public static int indexOf(CharSequence haystack, char needle, int fromIndex) {
        final int haystackLength = haystack.length();
        for (int pos = fromIndex; pos < haystackLength; pos++) {
            if (haystack.charAt(pos) == needle) {
                return pos;
            }
        }
        return -1;
    }

    /**
     * Find text in CharSequence.
     *
     * @param haystack  the sequence to search
     * @param needle    the sequence to find
     * @param fromIndex the index to start from
     * @return the position where the sequence was found or -1 if not
     * found
     */
    public static int indexOf(CharSequence haystack, CharSequence needle, int fromIndex) {
        final int haystackLength = haystack.length();
        final int needleLength = needle.length();

        outer:
        for (int pos = fromIndex; pos < haystackLength - needleLength + 1; pos++) {
            for (int i = 0; i < needleLength; i++) {
                if (haystack.charAt(pos + i) != needle.charAt(i)) {
                    continue outer;
                }
            }
            return pos;
        }

        return -1;
    }

    /**
     * Get capturing group.
     *
     * @param matcher the matcher instance
     * @param input   the input sequence
     * @param name    the capturing group name
     * @return the sequence matched or {@code Optional.empty()}, if not
     * matched
     */
    public static Optional<CharSequence> group(Matcher matcher, CharSequence input, String name) {
        int start = matcher.start(name);
        return start < 0 ? Optional.empty() : Optional.of(input.subSequence(start, matcher.end(name)));
    }

    /**
     * Get MD5 digest as hex string.
     *
     * @param text the text for which to calculate the digest
     * @return the MD5 digest as hex string
     */
    public static String getMD5String(String text) {
        return byteArrayToHexString(getMD5(text));
    }

    /**
     * Get MD5 digest.
     *
     * @param text the text for which to calculate the digest
     * @return the MD5 digest as byte array
     */
    public static byte[] getMD5(String text) {
        return getMD5(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get MD5 digest.
     *
     * @param data the data for which to calculate the digest
     * @return the MD5 digest as byte array
     */
    public static byte[] getMD5(byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            // this should never happen
            throw new IllegalStateException(e);
        }
    }

    /**
     * Format a byte array to a hex string.
     *
     * @param a the byte array
     * @return hex string
     */
    public static String byteArrayToHexString(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Convert hex string to byte array.
     *
     * @param s hex string
     * @return the byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int h1 = getHexDigit(s, i);
            int h2 = getHexDigit(s, i + 1);
            data[i / 2] = (byte) ((h1 << 4) + h2);
        }
        return data;
    }

    /**
     * Get hex value for character at index.
     *
     * @param s   the String
     * @param idx index of character in s
     * @return hex value between 0 and 15
     * @throws IllegalArgumentException if the character is not a valid hex
     *                                  character
     */
    private static int getHexDigit(String s, int idx) {
        char c = s.charAt(idx);
        int hex = Character.digit(c, 16);
        if (hex < 0) {
            throw new IllegalStateException("not a hex digit: " + c);
        }
        return hex;
    }

    /**
     * Base64-encode data.
     *
     * @param data the data to be encoded
     * @return the Base64-encoded data
     */
    public static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Base64-decode data.
     *
     * @param text the Base64-encoded data
     * @return the decoded data
     */
    public static byte[] base64Decode(String text) {
        return Base64.getDecoder().decode(text);
    }

    private static final FontUtil<?> FONT_UTIL;

    static {
        FONT_UTIL = ServiceLoader
                .load(FontUtil.class)
                .findFirst()
                .orElseGet(
                        () -> new FontUtil<Void>() {
                            @Override
                            public Void convert(Font f) {
                                throw new UnsupportedOperationException("no FontUtil implementation present");
                            }

                            @Override
                            public Bounds getTextBounds(String s, Font f) {
                                throw new UnsupportedOperationException("no FontUtil implementation present");
                            }
                        });
    }

    public static double getTextWidth(String text, Font font) {
        return FONT_UTIL.getTextWidth(text, font);
    }

    public static double getTextHeight(String text, Font font) {
        return FONT_UTIL.getTextWidth(text, font);
    }

    public static Bounds getTextHBounds(String text, Font font) {
        return FONT_UTIL.getTextBounds(text, font);
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public static String align(String s, int width, Alignment align) {
        return align(s, width, align, ' ');
    }

    public static String align(String s, int width, Alignment align, char filler) {
        String fill = Character.toString(filler);
        int len = s.length();
        switch (align) {
            case LEFT:
                return s + fill.repeat(Math.max(0, width - len));
            case RIGHT:
                return fill.repeat(Math.max(0, width - len)) + s;
            case CENTER:
                return fill.repeat(Math.max(0, width - len) / 2) + s + fill.repeat(Math.max(0, width - len - (width - len) / 2));
            default:
                throw new IllegalArgumentException(align.toString());
        }
    }
}

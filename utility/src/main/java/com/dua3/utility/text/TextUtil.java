// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Text related utility class.
 */
public final class TextUtil {

    private static final Logger LOG = LogManager.getLogger(TextUtil.class);

    /**
     * The current system's end-of-line sequence.
     */
    public static final String LINE_END_SYSTEM = System.lineSeparator();
    /**
     * UNIX end-of-line sequence.
     */
    public static final String LINE_END_UNIX = "\n";
    /**
     * Windows end-of-line sequence.
     */
    public static final String LINE_END_WINDOWS = "\r\n";
    private static final String TRANSFORM_REF_START = "${";
    private static final String TRANSFORM_REF_END = "}";
    private static final FontUtil<?> FONT_UTIL = FontUtil.getInstance();
    private static final Predicate<String> IS_NEWLINE_TERMINATED = Pattern.compile(".*\\R$").asMatchPredicate();
    private static final Predicate<String> IS_QUOTING_NEEDED = Pattern.compile("[\\p{L}\\d,.;+-]+").asMatchPredicate().negate();

    private TextUtil() {
        // nop: utility class
    }

    /**
     * HTML-escape a string.
     *
     * @param s the string
     * @return the HTML-escaped string
     */
    public static String escapeHTML(CharSequence s) {
        int length = s.length();
        StringBuilder out = new StringBuilder(16 + length * 11 / 10);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            appendHtmlEscapedCharacter(out, c);
        }
        return out.toString();
    }

    /**
     * Append HTML-escaped character to an Appendable.
     *
     * @param app the {@link Appendable}
     * @param c   the character
     * @throws IOException if an exception occurs
     */
    public static void appendHtmlEscapedCharacter(Appendable app, char c) throws IOException {
        switch (c) {
            case '\0' -> app.append("&#0;");
            case '"' -> app.append("&quot;");
            case '<' -> app.append("&lt;");
            case '>' -> app.append("&gt;");
            case '&' -> app.append("&amp;");
            case '\'' -> app.append("&apos;");
            default -> {
                if (c < 127) {
                    app.append(c);
                } else {
                    app.append("&#");
                    app.append(Integer.toString(c));
                    app.append(';');
                }
            }
        }
    }

    /**
     * Append HTML-escaped character to a {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder}
     * @param c  the character
     */
    public static void appendHtmlEscapedCharacter(StringBuilder sb, char c) {
        try {
            appendHtmlEscapedCharacter((Appendable) sb, c);
        } catch (IOException e) {
            // this should never happen since StringBuilder.append() does not declare exceptions
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Append HTML escaped characters to {@link Appendable}.
     *
     * @param app the {@link Appendable} instance
     * @param cs  the unescaped {@link CharSequence}
     * @param <T> the type of the Appendable
     * @throws IOException if an error occurs
     */
    public static <T extends Appendable> void appendHtmlEscapedCharacters(T app, CharSequence cs) throws IOException {
        int length = cs.length();
        for (int idx = 0; idx < length; idx++) {
            appendHtmlEscapedCharacter(app, cs.charAt(idx));
        }
    }

    /**
     * Append HTML escaped characters to {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder} instance
     * @param cs the unescaped {@link CharSequence}
     */
    public static void appendHtmlEscapedCharacters(StringBuilder sb, CharSequence cs) {
        int length = cs.length();
        sb.ensureCapacity(sb.length() + length);
        for (int idx = 0; idx < length; idx++) {
            appendHtmlEscapedCharacter(sb, cs.charAt(idx));
        }
    }

    /**
     * Backslash-escape a string.
     *
     * @param s the string
     * @return the escaped string
     */
    public static String escape(CharSequence s) {
        StringBuilder out = new StringBuilder(16 + s.length() * 11 / 10);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 127) {
                // ASCII characters
                switch (c) {
                    case '\0' -> out.append("\\u0000"); // "\0" might be ambiguous if followed by digits
                    case '\\' -> out.append("\\\\");
                    case '\t' -> out.append("\\t");
                    case '\b' -> out.append("\\b");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\f' -> out.append("\\f");
                    case '\'' -> out.append("\\'");
                    case '\"' -> out.append("\\\"");
                    default -> out.append(c);
                }
            } else {
                // non-ASCII characters
                switch (Character.getType(c)) {
                    // numbers: pass through
                    // letters: pass all non-modifying letters through
                    case Character.DECIMAL_DIGIT_NUMBER, Character.LETTER_NUMBER, Character.OTHER_NUMBER,
                         Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.OTHER_LETTER,
                         Character.TITLECASE_LETTER -> out.append(c);

                    // escape all remaining characters
                    default -> out.append("\\u").append(String.format(Locale.ROOT, "%04X", (int) c));
                }
            }
        }
        return out.toString();
    }

    /**
     * Transform a templated String.
     *
     * @param template      the template
     * @param substitutions the substitutions
     * @return result of transformation
     * @see #transform(String, Function)
     */
    @SafeVarargs
    public static String transform(String template, Map.Entry<String, ?>... substitutions) {
        return transform(template, Arrays.asList(substitutions));
    }

    /**
     * Transforms a given template string by replacing placeholders with corresponding values from substitutions.
     *
     * @param template       the template string to be transformed
     * @param substitutions  the key-value pairs used for substitution
     * @return the transformed string with placeholders replaced by corresponding values
     */
    public static String transform(String template, Iterable<? extends Map.Entry<String, ?>> substitutions) {
        UnaryOperator<String> env = s -> {
            for (Map.Entry<String, ?> r : substitutions) {
                if (Objects.equals(s, r.getKey())) {
                    return String.valueOf(r.getValue());
                }
            }
            return s;
        };

        return transform(template, env);
    }

    /**
     * Takes a template string and a map of substitutions, and returns a transformed string
     *
     * @param template       the template string containing placeholders for substitutions
     * @param substitutions  the map containing key-value pairs for substitutions
     * @return the transformed string with placeholders replaced by values from the map
     */
    public static String transform(String template, Map<String, ?> substitutions) {
        return transform(template, substitutions.entrySet());
    }

    /**
     * Transform a templated String.
     *
     * @param template the template
     * @param env      substitution environment
     * @return result of transformation
     * @see #transform(CharSequence, Function, Consumer)
     */
    public static String transform(String template, Function<? super String, String> env) {
        StringBuilder sb = new StringBuilder(Math.max(16, template.length()));
        transform(template, env, sb::append);
        return sb.toString();
    }

    /**
     * Transform a templated String.
     *
     * @param template the template
     * @param env      substitution environment
     * @return result of transformation
     * @see #transform(CharSequence, Function, Consumer)
     */
    public static RichText transform(RichText template, Function<? super String, RichText> env) {
        RichTextBuilder b = new RichTextBuilder(Math.max(16, template.length()));
        transform(template, env, b::append);
        return b.toRichText();
    }

    /**
     * Transform a templated Text.
     * <p>
     * Read {@code template} and copy its contents to {@code output}. For each
     * reference in the form {@code ${VARIABLE}}, the substitution is determined by
     * calling {@code env.apply("VARIABLE")}.
     * </p>
     *
     * @param <T> the generic type to use
     * @param template the template
     * @param env      substitution environment
     * @param output   output
     */
    public static <T extends CharSequence> void transform(T template,
                                                          Function<? super String, ? extends T> env,
                                                          Consumer<? super CharSequence> output) {
        int pos = 0;
        while (pos < template.length()) {
            // find next ref
            int varPos = indexOf(template, TRANSFORM_REF_START, pos);
            if (varPos == -1) {
                // no more refs => copy the remaining text
                output.accept(template.subSequence(pos, template.length()));
                break;
            }

            // copy text from current position to start of ref
            output.accept(template.subSequence(pos, varPos));
            pos = varPos + TRANSFORM_REF_START.length();

            // determine ref name
            int varEnd = indexOf(template, TRANSFORM_REF_END, pos);
            LangUtil.check(varEnd != -1, "unexpected end of template, '%s' expected", TRANSFORM_REF_END);
            String varName = template.subSequence(pos, varEnd).toString();
            pos = varEnd + TRANSFORM_REF_END.length();

            // insert ref substitution
            output.accept(env.apply(varName));
        }
    }

    /**
     * Get the font size in pt for a font size given as string.
     *
     * @param s the string
     * @return font size in pt
     */
    public static float decodeFontSize(String s) {
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
            case "px" -> 18.0f / 24.0f;
            case "%" -> 12.0f / 100.0f;
            case "vw" -> {
                LOG.warn("unit 'vw' unsupported, treating as 'em'");
                yield 12.0f;
            }
            default -> throw new IllegalArgumentException("invalid value for font-size: " + s);
        };

        return f * Float.parseFloat(number);
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
     * Find the index of the first occurrence of a char in a string.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return index of the first occurrence of a char contained in {@code chars}, or -1 if not found
     */
    public static int indexOfFirst(CharSequence s, char... chars) {
        for (int i = 0; i < s.length(); i++) {
            char c1 = s.charAt(i);
            for (char c2 : chars) {
                if (c1 == c2) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Find the index of the first occurrence of a char in a string.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return index of the first occurrence of a char contained in {@code chars}, or -1 if not found
     */
    public static int indexOfFirst(CharSequence s, String chars) {
        return indexOfFirst(s, chars.toCharArray());
    }

    /**
     * Test if {@link CharSequence} is contained-
     *
     * @param s1 the {@link CharSequence} to search in
     * @param s2 the {@link CharSequence} to search for
     * @return true, if {@code s2} is contained in {@code s1}
     */
    public static boolean contains(CharSequence s1, CharSequence s2) {
        return indexOf(s1, s2) >= 0;
    }

    /**
     * Test if string contains none of the given characters.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains none of the characters in {@code chars}
     */
    public static boolean containsNoneOf(CharSequence s, String chars) {
        return indexOfFirst(s, chars) < 0;
    }

    /**
     * Test if string contains none of the given characters.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains none of the characters in {@code chars}
     */
    public static boolean containsNoneOf(CharSequence s, char... chars) {
        return indexOfFirst(s, chars) < 0;
    }

    /**
     * Test if string contains any of the given characters.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains one or more of the characters in {@code chars}
     */
    public static boolean containsAnyOf(CharSequence s, String chars) {
        return indexOfFirst(s, chars) >= 0;
    }

    /**
     * Test if string contains any of the given characters.
     *
     * @param s     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains one or more of the characters in {@code chars}
     */
    public static boolean containsAnyOf(CharSequence s, char... chars) {
        return indexOfFirst(s, chars) >= 0;
    }

    /**
     * Find char in CharSequence.
     *
     * @param haystack the sequence to search
     * @param needle   the char to find
     * @return the position where the char was found or -1 if not found
     */
    public static int indexOf(CharSequence haystack, int needle) {
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
    public static int indexOf(CharSequence haystack, int needle, int fromIndex) {
        final int haystackLength = haystack.length();
        return IntStream.range(fromIndex, haystackLength)
                .filter(pos -> haystack.charAt(pos) == needle)
                .findFirst()
                .orElse(-1);
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
     * Test whether a {@link CharSequence} starts with another {@link CharSequence}.
     *
     * @param a the sequence to search in
     * @param b the sequence to search for
     * @return true, if a starts with s
     */
    public static boolean startsWith(CharSequence a, CharSequence b) {
        if (a.length() < b.length()) {
            return false;
        }

        for (int i = 0; i < b.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
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
    public static Optional<CharSequence> group(Matcher matcher,
                                               CharSequence input,
                                               String name) {
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
        return HexFormat.of().formatHex(getMD5(text));
    }

    /**
     * Get MD5 digest as hex string.
     *
     * @param data the data for which to calculate the digest
     * @return the MD5 digest as hex string
     */
    public static String getMD5String(byte[] data) {
        return HexFormat.of().formatHex(getMD5(data));
    }

    /**
     * Get MD5 digest as hex string.
     *
     * @param in the stream to read data from
     * @return the MD5 digest as hex string
     * @throws IOException if an I/O error occurs
     */
    public static String getMD5String(InputStream in) throws IOException {
        return HexFormat.of().formatHex(getMD5(in));
    }

    /**
     * Get MD5 digest.
     *
     * @param text the text for which to calculate the digest
     * @return the MD5 digest as byte array
     */
    public static byte[] getMD5(String text) {
        try {
            return getDigest("MD5", text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get MD5 digest.
     *
     * @param data the data for which to calculate the digest
     * @return the MD5 digest as byte array
     */
    public static byte[] getMD5(byte[] data) {
        try {
            return getDigest("MD5", data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get MD5 digest.
     *
     * @param in the stream to read data from
     * @return the MD5 digest as byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] getMD5(InputStream in) throws IOException {
        try {
            return getDigest("MD5", in);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get digest.
     *
     * @param algorithm the algorithm to use
     * @param data the data for which to calculate the digest
     * @return the digest as byte array
     * @throws NoSuchAlgorithmException if the algorithm is not implemented
     */
    public static byte[] getDigest(String algorithm, byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm).digest(data);
    }

    /**
     * Get digest.
     *
     * @param algorithm the algorithm to use
     * @param in the stream to read data from
     * @return the digest as byte array
     * @throws IOException if an I/O error occurs
     * @throws NoSuchAlgorithmException if the algorithm is not implemented
     */
    public static byte[] getDigest(String algorithm, InputStream in) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (DigestInputStream dis = new DigestInputStream(in, md)) {
            byte[] buffer = new byte[2048];
            while (dis.read(buffer) != -1) {
                // Reading data here to advance the stream, digest is updated automatically by DigestInputStream
            }
            return md.digest();
        }
    }

    /**
     * Get digest as hex string.
     *
     * @param algorithm the algorithm to use
     * @param data the data for which to calculate the digest
     * @return the MD5 digest as hex string
     * @throws NoSuchAlgorithmException if the algorithm is not implemented
     */
    public static String getDigestString(String algorithm, byte[] data) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(getDigest(algorithm, data));
    }

    /**
     * Get digest as hex string.
     *
     * @param algorithm the algorithm to use
     * @param in the stream to read data from
     * @return the MD5 digest as hex string
     * @throws IOException if an I/O error occurs
     * @throws NoSuchAlgorithmException if the algorithm is not implemented
     */
    public static String getDigestString(String algorithm, InputStream in) throws NoSuchAlgorithmException, IOException {
        return HexFormat.of().formatHex(getDigest(algorithm, in));
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

    /**
     * Convert mm to pt.
     *
     * @param mm value in millimeters
     * @return value in points
     */
    public static double mm2pt(double mm) {
        return mm * 72 / 25.4;
    }

    /**
     * Convert pt to mm.
     *
     * @param pt value in millimeters
     * @return value in points
     */
    public static double pt2mm(double pt) {
        return pt * 25.4 / 72;
    }

    /**
     * Get text height.
     *
     * @param text the text
     * @param font the font
     * @return the text height
     */
    public static double getTextHeight(CharSequence text, Font font) {
        return FONT_UTIL.getTextWidth(text, font);
    }

    /**
     * Get text width.
     *
     * @param text the text
     * @param font the font
     * @return the text width
     */
    public static double getTextWidth(CharSequence text, Font font) {
        return FONT_UTIL.getTextWidth(text, font);
    }

    /**
     * Get text bounds.
     *
     * @param text the text
     * @param font the font
     * @return the text bounds
     */
    public static Rectangle2f getTextDimension(CharSequence text, Font font) {
        return FONT_UTIL.getTextDimension(text, font);
    }

    /**
     * Get rich text bounds.
     *
     * <p>If {@code text} implements {@link ToRichText}, styles are applied prior to measuring the dimension.
     *
     * @param text the text
     * @param font the base font to apply
     * @return the text bounds
     */
    public static Rectangle2f getRichTextDimension(CharSequence text, Font font) {
        return FONT_UTIL.getRichTextDimension(text, font);
    }

    /**
     * Pad String to width with alignment.
     *
     * @param s     the string
     * @param width the width
     * @param align the alignment
     * @return the padded nd aligned string; if the input string width exceeds the requested width, the original string
     * is returned
     */
    public static String align(String s, int width, Alignment align) {
        return align(s, width, align, ' ');
    }

    /**
     * Pad String to width with alignment.
     *
     * @param s      the string
     * @param width  the width
     * @param align  the alignment
     * @param filler the fill character
     * @return the padded nd aligned string; if the input string width exceeds the requested width, the original string
     * is returned
     */
    public static String align(String s, int width, Alignment align, char filler) {
        int len = s.length();
        return switch (align) {
            case LEFT -> s + padding(filler, width - len);
            case RIGHT -> padding(filler, width - len) + s;
            case CENTER -> padding(filler, (width - len) / 2) + s + padding(filler, width - len - (width - len) / 2);
        };
    }

    private static String padding(char filler, int len) {
        return Character.toString(filler).repeat(Math.max(0, len));
    }

    /**
     * Returns the given string if it is not null or empty, otherwise returns the specified value.
     *
     * @param t The string to check.
     * @param tIfNullOrEmpty The value to return if the given string is null or empty.
     * @return The given string if it is not null or empty, otherwise the specified value.
     */
    public static <T extends CharSequence> T nonEmptyOr(@Nullable T t, T tIfNullOrEmpty) {
        return (t == null || t.isEmpty()) ? tIfNullOrEmpty : t;
    }

    /**
     * Generate mailto-link.
     *
     * @param email   the email recipient
     * @param subject the email subject
     * @return email-link
     */
    public static String generateMailToLink(String email, String subject) {
        // give some care to translate space to "%20"
        String s1 = URLEncoder.encode(subject, StandardCharsets.UTF_8);
        String s2 = URLEncoder.encode(subject.replace(" ", "_"), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(s1.length());
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) == '+' && s2.charAt(i) == '_') {
                sb.append("%20");
            } else {
                sb.append(s1.charAt(i));
            }
        }
        String s = sb.toString();

        return String.format(Locale.ROOT,
                "mailto:%s?subject=%s",
                email,
                s);
    }

    /**
     * Convert all line ends to '\n'.
     *
     * @param s the input string
     * @return input string with normalized line ends
     */
    public static String normalizeLineEnds(String s) {
        return setLineEnds(s, LINE_END_UNIX);
    }

    /**
     * Convert all line ends to Unix convention.
     *
     * @param s the input string
     * @return input string with Unix line ends
     */
    public static String toUnixLineEnds(String s) {
        return setLineEnds(s, LINE_END_UNIX);
    }

    /**
     * Convert all line ends to Windows convention.
     *
     * @param s the input string
     * @return input string with Windows line ends
     */
    public static String toWindowsLineEnds(String s) {
        return setLineEnds(s, LINE_END_WINDOWS);
    }

    /**
     * Convert all line ends to system convention.
     *
     * @param s the input string
     * @return input string with system line ends
     */
    public static String toSystemLineEnds(String s) {
        return setLineEnds(s, LINE_END_SYSTEM);
    }

    private static String setLineEnds(String s, String lineEnd) {
        boolean isNewlineTerminated = IS_NEWLINE_TERMINATED.test(s);
        return s.lines().collect(Collectors.joining(lineEnd, "", isNewlineTerminated ? lineEnd : ""));
    }

    /**
     * Surrounds a string with quotes and escapes control characters according to Java conventions.
     *
     * @param text the string to be surrounded with quotes
     * @return the quoted string
     */
    public static String quote(String text) {
        return "\"" +
                text.replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                + "\"";
    }

    /**
     * Returns a quoted string if needed, otherwise returns the original string.
     *
     * @param text the string to be checked if quoting is needed
     * @return the quoted string if needed, otherwise the original string
     */
    public static String quoteIfNeeded(String text) {
        return IS_QUOTING_NEEDED.test(text) ? quote(text) : text;
    }

    /**
     * Joins the elements of the given list into a single string, using the specified delimiter.
     * If an element needs to be quoted, it will be enclosed in quotes before joining.
     *
     * @param args      the list of elements to be joined
     * @param delimiter the delimiter to be used between the elements
     * @return the joined string with each element quoted if necessary
     */
    public static String joinQuotedIfNeeded(List<?> args, String delimiter) {
        return args.stream().map(arg -> TextUtil.quoteIfNeeded(arg != null ? arg.toString() : "")).collect(Collectors.joining(delimiter));
    }

    /**
     * Joins the elements of the given list into a single string, using ', ' as delimiter.
     * If an element needs to be quoted, it will be enclosed in quotes before joining.
     *
     * @param args      the list of elements to be joined
     * @return the joined string with each element quoted if necessary
     */
    public static String joinQuotedIfNeeded(List<?> args) {
        return joinQuotedIfNeeded(args, ", ");
    }

    /**
     * Joins the elements in the given list into a single string, quoting each element.
     * Null elements are treated in the same way as empty strings.
     *
     * @param args The list of elements to be joined, can contain elements of any type.
     * @param delimiter the delimiter to insert between args
     * @return A single string that is the result of joining all the elements, with each element quoted and separated
     *         by a comma and space.
     */
    public static String joinQuoted(List<?> args, String delimiter) {
        return args.stream().map(arg -> TextUtil.quote(arg != null ? arg.toString() : "")).collect(Collectors.joining(delimiter));
    }

    /**
     * Joins the elements in the given list into a single string, quoting each element and using ', ' as delimiter.
     * Null elements are treated in the same way as empty strings.
     *
     * @param args The list of elements to be joined, can contain elements of any type.
     * @return A single string that is the result of joining all the elements, with each element quoted and separated
     *         by a comma and space.
     */
    public static String joinQuoted(List<?> args) {
        return joinQuoted(args, ", ");
    }

    /**
     * Checks if a CharSequence is empty or consists of whitespace characters only.
     *
     * @param cs the CharSequence to check
     * @return {@code true} if the CharSequence is blank, {@code false} otherwise
     */
    public static boolean isBlank(CharSequence cs) {
        return cs.codePoints().allMatch(Character::isWhitespace);
    }

    /**
     * Alignment.
     */
    public enum Alignment {
        /**
         * align left.
         */
        LEFT,
        /**
         * align centered.
         */
        CENTER,
        /**
         * align right.
         */
        RIGHT
    }
}

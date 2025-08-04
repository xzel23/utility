// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Rectangle2f;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Text-related utility class.
 */
public final class TextUtil {

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
    /**
     * Pattern matching line ends (all types).
     */
    public static final Pattern PATTERN_LINE_END = Pattern.compile("\\R");

    private static final String TRANSFORM_REF_START = "${";
    private static final String TRANSFORM_REF_END = "}";

    private static final Pattern PATTERN_SPLIT_PRESERVING_WHITESPACE = Pattern.compile("(?<=\\s)|(?=\\s)");
    private static final Pattern PATTERN_SPLIT_LINES = Pattern.compile("\\R");

    private static final FontUtil<?> FONT_UTIL = FontUtil.getInstance();

    private TextUtil() {
        // nop: utility class
    }

    /**
     * HTML-escape a string.
     *
     * @param seq the string
     * @return the HTML-escaped string
     */
    public static String escapeHTML(CharSequence seq) {
        int length = seq.length();
        StringBuilder out = new StringBuilder(16 + length * 11 / 10);
        for (int i = 0; i < length; i++) {
            char c = seq.charAt(i);
            appendHtmlEscapedCharacter(out, c);
        }
        return out.toString();
    }

    /**
     * HTML-unescape a string.
     *
     * @param s the string
     * @return the HTML-unescaped string
     */
    public static String unescapeHtml(CharSequence s) {
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (ch == '&') {
                int semicolon = indexOf(s, ';', i);
                if (semicolon > 0) {
                    CharSequence entity = s.subSequence(i + 1, semicolon);
                    if (startsWith(entity,"#")) {
                        try {
                            if (startsWith(entity, "#x")) {
                                ch = (char) Integer.parseInt(entity, 2, entity.length(), 16);
                            } else {
                                ch = (char) Integer.parseInt(entity, 1, entity.length(), 10);
                            }
                            sb.append(ch);
                            i = semicolon + 1;
                            continue;
                        } catch (NumberFormatException ignored) {
                            // invalid numeric entity, treat as literal
                        }
                    } else {
                        ch = switch (entity.toString()) {
                            case "quot" -> '"';
                            case "amp" -> '&';
                            case "lt" -> '<';
                            case "gt" -> '>';
                            case "apos" -> '\'';
                            default -> '?';
                        };
                        if (ch != '?') {
                            sb.append(ch);
                            i = semicolon + 1;
                            continue;
                        }
                    }
                }
            }
            sb.append(ch);
            i++;
        }
        return sb.toString();
    }
    
    /**
     * Append HTML-escaped character to an Appendable.
     *
     * @param app the {@link Appendable}
     * @param c   the character
     * @throws IOException if an exception occurs
     */
    @SuppressWarnings("MagicCharacter")
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
     * @param seq the string
     * @return the escaped string
     * See also {@link #escapeASCII(CharSequence)}
     */
    public static String escape(CharSequence seq) {
        StringBuilder out = new StringBuilder(16 + seq.length() * 11 / 10);
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
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
     * Backslash-escape a string to ASCII-only representation.
     *
     * @param seq the string
     * @return the escaped string
     * See also {@link #escape(CharSequence)}
     */
    public static String escapeASCII(CharSequence seq) {
        StringBuilder out = new StringBuilder(16 + seq.length() * 11 / 10);
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
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
                out.append("\\u").append(String.format(Locale.ROOT, "%04X", (int) c));
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
        RichTextBuilder rtb = new RichTextBuilder(Math.max(16, template.length()));
        transform(template, env, rtb::append);
        return rtb.toRichText();
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
        return InternalUtil.decodeFontSize(s);
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
            if (iter1.nextInt() != iter2.nextInt()) {
                return false;
            }
        }
        return !iter1.hasNext() && !iter2.hasNext();
    }

    /**
     * Creates a lexicographic comparator for strings based on the specified locale.
     * The comparator first uses locale-specific rules for comparison and then falls back
     * to natural string order if the locale-specific comparison deems the strings equivalent.
     * Null values are considered less than non-null values.
     * <p>
     * Note: The returned Comparator is not threadsafe.
     *
     * @param locale the locale to be used for sorting strings. It determines the
     *               locale-specific rules used by the comparator.
     * @return a comparator that compares strings lexicographically based on the specified locale.
     */
    public static Comparator<@Nullable String> lexicographicComparator(Locale locale) {
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return (a, b) -> {
            if (a == null) {
                return b == null ? 0 : -1;
            }
            if (b == null) {
                return 1;
            }

            int c = collator.compare(a, b);
            if (c == 0) {
                c = a.compareTo(b);
            }
            return c;
        };
    }

    /**
     * Splits the provided character sequence into an array of lines, using
     * the predefined line splitting pattern.
     * <p>
     * For a parameter s of type string, this unit returns the same result as {@code }s.split("\\R")}.
     *
     * @param s the character sequence to be split into lines
     * @return an array of strings, where each string represents a line
     *         extracted from the input character sequence
     */
    public static String[] lines(CharSequence s) {
        return PATTERN_SPLIT_LINES.split(s);
    }

    /**
     * Find the index of the first occurrence of a char in a string.
     *
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return index of the first occurrence of a char contained in {@code chars}, or -1 if not found
     */
    public static int indexOfFirst(CharSequence seq, char... chars) {
        for (int i = 0; i < seq.length(); i++) {
            char c1 = seq.charAt(i);
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
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return index of the first occurrence of a char contained in {@code chars}, or -1 if not found
     */
    public static int indexOfFirst(CharSequence seq, String chars) {
        return indexOfFirst(seq, chars.toCharArray());
    }

    /**
     * Checks if the given string ends with a newline character.
     * Newline characters considered are '\r', '\n', '\u0085', '\u2028', and '\u2029'.
     *
     * @param s the string to check for newline termination
     * @return true if the string ends with a newline character, false otherwise
     */
    public static boolean isNewlineTerminated(CharSequence s) {
        return !s.isEmpty() && switch (s.charAt(s.length() - 1)) {
            case '\r', '\n', '\u0085', '\u2028', '\u2029' -> true;
            default -> false;
        };
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
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains none of the characters in {@code chars}
     */
    public static boolean containsNoneOf(CharSequence seq, String chars) {
        return indexOfFirst(seq, chars) < 0;
    }

    /**
     * Test if string contains none of the given characters.
     *
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains none of the characters in {@code chars}
     */
    public static boolean containsNoneOf(CharSequence seq, char... chars) {
        return indexOfFirst(seq, chars) < 0;
    }

    /**
     * Test if string contains any of the given characters.
     *
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains one or more of the characters in {@code chars}
     */
    public static boolean containsAnyOf(CharSequence seq, String chars) {
        return indexOfFirst(seq, chars) >= 0;
    }

    /**
     * Test if string contains any of the given characters.
     *
     * @param seq     the string to search
     * @param chars the chars to search for
     * @return true if {@code s} contains one or more of the characters in {@code chars}
     */
    public static boolean containsAnyOf(CharSequence seq, char... chars) {
        return indexOfFirst(seq, chars) >= 0;
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
        for (int i = fromIndex; i < haystackLength; i++) {
            if (haystack.charAt(i) == needle) {
                return i;
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

        for (int pos = fromIndex; pos < haystackLength - needleLength + 1; pos++) {
            int i;
            for (i = 0; i < needleLength; i++) {
                if (haystack.charAt(pos + i) != needle.charAt(i)) {
                    break;
                }
            }
            if (i == needleLength) {
                return pos;
            }
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
     * Get digest.
     * <p>
     * For a list of possible algorithms see
     * <a href="https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms">
     *     Java Security Standard Algorithm Names
     * </a>.
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
            //noinspection StatementWithEmptyBody
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
     * @return the digest as hex string
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
     * @return the digest as hex string
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
    public static byte[] base64Decode(CharSequence text) {
        return Base64.getMimeDecoder().decode(toByteArray(text));
    }

    /**
     * Base64-decode data.
     *
     * @param text the Base64-encoded data
     * @return the decoded data
     */
    public static byte[] base64Decode(char[] text) {
        return Base64.getMimeDecoder().decode(toByteArray(text));
    }

    /**
     * Convert mm to pt.
     *
     * @param mm value in millimeters
     * @return value in points
     */
    public static double mm2pt(double mm) {
        return mm * 72.0 / 25.4;
    }

    /**
     * Convert pt to mm.
     *
     * @param pt value in millimeters
     * @return value in points
     */
    public static double pt2mm(double pt) {
        return pt * 25.4 / 72.0;
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
     * Removes trailing whitespace characters from the given character sequence.
     *
     * @param t the character sequence to process; must not be null
     * @return a character sequence with trailing whitespace removed
     */
    public static CharSequence stripTrailing(CharSequence t) {
        int end = t.length();
        while (end > 0 && Character.isWhitespace(t.charAt(end - 1))) {
            end--;
        }
        return t.subSequence(0, end);
    }

    /**
     * Removes all leading whitespace characters from the given character sequence.
     *
     * @param t the character sequence from which leading whitespace is to be removed
     * @return a character sequence with leading whitespace removed
     */
    public static CharSequence stripLeading(CharSequence t) {
        int start = 0;
        while (start < t.length() && Character.isWhitespace(t.charAt(start))) {
            start++;
        }
        return t.subSequence(start, t.length());
    }

    /**
     * Removes leading and trailing whitespace characters from the given character sequence.
     *
     * @param t the character sequence from which leading and trailing whitespace should be removed
     * @return a subsequence of the input character sequence with leading and trailing whitespace removed
     */
    public static CharSequence strip(CharSequence t) {
        int start = 0;
        while (start < t.length() && Character.isWhitespace(t.charAt(start))) {
            start++;
        }
        int end = t.length();
        while (end > start && Character.isWhitespace(t.charAt(end - 1))) {
            end--;
        }
        return t.subSequence(start, end);
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
    public static String align(CharSequence s, int width, Alignment align) {
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
    public static String align(CharSequence s, int width, Alignment align, char filler) {
        s = stripTrailing(s);
        int len = s.length();
        return switch (align) {
            case LEFT -> s + padding(filler, width - len);
            case RIGHT -> padding(filler, width - len) + s;
            case CENTER -> padding(filler, (width - len) / 2) + s + padding(filler, width - len - (width - len) / 2);
            case JUSTIFY, DISTRIBUTE -> {
                int spaceToDistribute = Math.max(0, width - len);
                if (spaceToDistribute == 0) {
                    yield s.toString();
                }
                String[] fragments = PATTERN_SPLIT_PRESERVING_WHITESPACE.split(s);
                record Stats(int blankChars, int blankFragments) {}
                Stats stats = Arrays.stream(fragments)
                        .filter(String::isBlank)
                        .map(r -> new Stats(r.length(), 1))
                        .reduce((a, b) -> new Stats(a.blankChars + b.blankChars, a.blankFragments + b.blankFragments))
                        .orElseGet(() -> new Stats(0, 0));
                if (stats.blankFragments() == 0) {
                    yield s.toString();
                }
                double fBlank = 1.0f + (double) spaceToDistribute / stats.blankFragments();
                int used = 0;
                int processedSpaces = 0;
                StringBuilder sb = new StringBuilder(width);
                for (String fragment : fragments) {
                    if (fragment.isBlank() && used < spaceToDistribute) {
                        double ideal = (processedSpaces + fragment.length()) * fBlank - (processedSpaces + used);
                        int nChars = (int) Math.clamp(Math.round(ideal), 1, (long) (1 + spaceToDistribute - used));
                        String blank = padding(filler, nChars);
                        processedSpaces += fragment.length();
                        used += blank.length() - fragment.length();
                        sb.append(blank);
                    } else {
                        sb.append(fragment);
                    }
                }
                yield sb.toString();
            }
        };
    }

    private static String padding(char filler, int len) {
        return Character.toString(filler).repeat(Math.max(0, len));
    }

    /**
     * Returns the given character sequence if it is not {@code null} or empty, otherwise returns the specified value.
     *
     * @param <T> the generic type of the CharSequence
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

    /**
     * Adjusts the line endings of a given string by replacing the current line terminators
     * with the specified line end sequence. If the input string is newline-terminated,
     * the specified line end sequence will also terminate the result.
     *
     * @param s the input string whose line endings are to be modified
     * @param lineEnd the desired line ending sequence to apply
     * @return a string with the adjusted line endings
     */
    public static String setLineEnds(String s, String lineEnd) {
        return s.lines().collect(Collectors.joining(lineEnd, "", isNewlineTerminated(s) ? lineEnd : ""));
    }

    /**
     * Surrounds a string with quotes and escapes control characters according to Java conventions.
     *
     * @param text the string to be surrounded with quotes
     * @return the quoted string
     */
    public static String quote(String text) {
        return InternalUtil.quote(text);
    }

    /**
     * Returns a quoted string if needed, otherwise returns the original string.
     *
     * @param text the string to be checked if quoting is needed
     * @return the quoted string if needed, otherwise the original string
     */
    public static String quoteIfNeeded(String text) {
        return InternalUtil.quoteIfNeeded(text);
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
        return args.stream().map(arg -> quoteIfNeeded(arg != null ? arg.toString() : "")).collect(Collectors.joining(delimiter));
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
        return args.stream().map(arg -> quote(arg != null ? arg.toString() : "")).collect(Collectors.joining(delimiter));
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
     * Checks if the provided {@code CharSequence} is either {@code null} or blank (consists only of whitespace characters).
     *
     * @param s the {@code CharSequence} to be checked, may be {@code null}
     * @return {@code true} if the {@code CharSequence} is {@code null} or blank, otherwise {@code false}
     */
    public static boolean isNullOrBlank(@Nullable CharSequence s) {
        return s == null || isBlank(s);
    }

    /**
     * Wraps a given string `s` into multiple lines based on the specified `width`, alignment, and hard wrap options.
     * <p>
     *     This method uses the "\n" as line separator.
     *
     * @param s         the input string to be wrapped
     * @param width     the maximum width of each line
     * @param align     the alignment of the text within each line
     * @param hardWrap  a boolean value indicating whether to break words when wrapping
     * @return a new string with the wrapped lines
     */
    public static String wrap(String s, int width, Alignment align, boolean hardWrap) {
        return wrap(s, width, align, hardWrap, "\n");
    }

    /**
     * Wraps a given string `s` into multiple lines based on the specified `width`, alignment, and hard wrap options.
     *
     * @param s             the input string to be wrapped
     * @param width         the maximum width of each line
     * @param align         the alignment of the text within each line
     * @param hardWrap      a boolean value indicating whether to break words when wrapping
     * @param lineSeparator the line separator to use
     * @return a new string with the wrapped lines
     */
    public static String wrap(String s, int width, Alignment align, boolean hardWrap, String lineSeparator) {
        StringBuilder sb = new StringBuilder(s.length());
        try {
            for (var par : LineSplitter.process(s, width, hardWrap, " ", StringBuilder::new, StringBuilder::toString, StringBuilder::length)) {
                for (int i = 0; i < par.size(); i++) {
                    var line = par.get(i);
                    if (align == Alignment.JUSTIFY && i == par.size() - 1) {
                        sb.append(align(line, width, Alignment.LEFT));
                    } else {
                        sb.append(align(line, width, align));
                    }
                    sb.append(lineSeparator);
                }
                sb.append(lineSeparator);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("should not happen!", e);
        }
    }

    /**
     * Returns a string representation of the specified object.
     * If the provided object is not null, return {@code obj.toString()}.
     * If the provided object is {@code null}, return the specified value instead.
     *
     * @param obj The object to convert to string representation. Can be null.
     * @param valueIfNull The string value to return if the object is null.
     * @return A string representation of the specified object, or the specified value if the object is null.
     */
    public static String toString(@Nullable Object obj, String valueIfNull) {
        return obj != null ? obj.toString() : valueIfNull;
    }

    /**
     * Converts a {@code CharSequence} into a {@code char[]} array.
     * If the input is a {@code String}, its {@code toCharArray()} method is used directly.
     * Otherwise, the characters are extracted manually.
     * <p>
     * <strong>Security Note:</strong> This method avoids calling {@code toString()} on
     * non-String inputs, preventing potential string interning of sensitive data.
     * This allows complete cleanup of sensitive character data from memory.
     *
     * @param text the {@code CharSequence} to be converted to a {@code char[]} array
     * @return a {@code char[]} array representation of the input {@code CharSequence}
     */
    public static char[] toCharArray(CharSequence text) {
        if (text instanceof String s) {
            return s.toCharArray();
        }

        char[] textChars = new char[text.length()];
        for (int i = 0; i < text.length(); i++) {
            textChars[i] = text.charAt(i);
        }
        return textChars;
    }

    /**
     * Convert a char array to a byte array using UTF-8 encoding.
     *
     * @param chars the char array to convert
     * @return the byte array
     */
    public static byte[] toByteArray(char[] chars) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Convert a char array to a byte array using UTF-8 encoding.
     *
     * @param chars the char array to convert
     * @return the byte array
     */
    public static byte[] toByteArray(CharSequence chars) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Convert a byte array to a char array using UTF-8 encoding.
     *
     * @param bytes the byte array to convert
     * @return the char array
     */
    public static char[] toCharArray(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).toCharArray();
    }

    /**
     * Decodes the given byte array into a string using UTF-8 character encoding.
     *
     * @param bytes the byte array to decode
     * @return the resulting string after decoding the byte array
     */
    public static String decodeToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

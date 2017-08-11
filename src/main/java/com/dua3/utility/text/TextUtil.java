package com.dua3.utility.text;

import java.util.function.Consumer;
import java.util.function.Function;

public class TextUtil {

    private TextUtil() {
        // nop: utility class
    }

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static final String TRANSFORM_REF_START = "${";
    private static final String TRANSFORM_REF_END = "}";

    /**
     * Transform a templated  String.
     * <p>
     * Read {@code template} and copy its contents to {@code output}. For each reference in the form
     * {@code ${VARIABLE}}, the substitution is determined by calling {@code env.apply("VARIABLE")}.
     * </p>
     * @param template
     *  the template
     * @param env
     *  substitution environment
     * @param output
     *  output
     */
    public static void transform(String template, Function<String,String> env, Consumer<CharSequence> output) {
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

    /**
     * Transform a templated  String.
     * @param template
     *  the template
     * @param env
     *  substitution environment
     * @return
     *  result of transformation
     * @see #transform(String, Function, Consumer)
     */
    public static String transform(String template, Function<String, String> env) {
        StringBuilder sb = new StringBuilder(Math.max(16,  template.length()));
        transform(template, env, s -> sb.append(s));
        return sb.toString();
    }
}

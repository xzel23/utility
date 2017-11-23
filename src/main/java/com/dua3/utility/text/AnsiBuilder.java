package com.dua3.utility.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;
import com.dua3.utility.io.AnsiCode;
import com.dua3.utility.lang.LangUtil;

public class AnsiBuilder extends RichTextConverter<String> {

    private static final Logger LOG = LoggerFactory.getLogger(AnsiBuilder.class);

    public static String toAnsi(RichText text) {
        return new AnsiBuilder().add(text).get();
    }

    private final TextAttributes currentAttributes;

    private StringBuilder buffer = new StringBuilder();

    public AnsiBuilder() {
        this(TextAttributes.defaults());
    }

    public AnsiBuilder(TextAttributes attributes) {
        this.currentAttributes = attributes;
    }

    @Override
    protected void append(Run run) {
        LangUtil.check(!wasGetCalled(), "get() was already called");
        Pair<String, String> esc  = getEscapeSequences(currentAttributes, run.getStyle());
        String text = run.toString();
        buffer.append(esc.first).append(text).append(esc.second);
    }

    Pair<String,String> getEscapeSequences(TextAttributes current, TextAttributes patch) {
        List<Character> set = new ArrayList<>();
        List<Character> reset = new ArrayList<>();
        appendAttributes(current, patch, set, reset);

        return Pair.of(buildEscape(set), buildEscape(reset));
    }

    private void appendAttributes(TextAttributes current, TextAttributes patch, List<Character> set, List<Character> reset) {
        for (Entry<String, Object> entry: patch.properties().entrySet()) {
            String attribute = entry.getKey();
            Object value = entry.getValue();
            Object currentValue = current.get(attribute);
            if (!Objects.equals(value,currentValue)) {
                appendEscape(set,attribute,value);
                appendEscape(reset,attribute,currentValue);
            }
        }
    }

    private String buildEscape(List<Character> list) {
        if (list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(AnsiCode.ESC_START);
        list.stream().forEach(sb::append);
        sb.append(AnsiCode.ESC_END);
        return sb.toString();
    }

    private void appendEscape(List<Character> esc, String attribute, Object value) {
        Color c;
        switch (attribute) {
        case TextAttributes.COLOR:
            c = (Color) value;
            Collections.addAll(esc, AnsiCode.COLOR, (char) c.r(), (char)c.g(), (char)c.b());
            break;
        case TextAttributes.BACKGROUND_COLOR:
            c = (Color) value;
            Collections.addAll(esc, AnsiCode.BACKGROUND_COLOR, (char) c.r(), (char)c.g(), (char)c.b());
            break;
        case TextAttributes.FONT_STYLE:
            switch (String.valueOf(value)) {
            case TextAttributes.FONT_STYLE_VALUE_ITALIC: // fallthrough
            case TextAttributes.FONT_STYLE_VALUE_OBLIQUE:
                esc.add((Boolean) value ? AnsiCode.ITALIC_ON : AnsiCode.ITALIC_OFF);
                break;
            case TextAttributes.FONT_STYLE_VALUE_NORMAL:
            default:
                LOG.warn("unknown value for {}: {}", attribute, value);
                break;
            }
            break;
        default:
            LOG.info("unknown attribute: {}", attribute);
            break;
        }
    }

    @Override
    protected boolean wasGetCalled() {
        return buffer==null;
    }

    @Override
    public String get() {
        String ret = buffer.toString();
        buffer = null;
        return ret;
    }

}

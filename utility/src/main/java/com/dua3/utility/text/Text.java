package com.dua3.utility.text;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dua3.utility.lang.LangUtil;

public class Text extends AbstractList<String> {

    private static final char NEWLINE = '\n';
    private static final String NEWLINE_STR = "" + '\n';
    private static final Text EMPTY_TEXT = new Text(Collections.emptyList());

    public static Text collect(Stream<String> lines) {
        return new Text(lines.peek(Text::checkNoLineBreaks));
    }

    public static Text empty() {
        return EMPTY_TEXT;
    }

    public static Text load(Path path, Charset cs) throws IOException {
        try (Stream<String> lines = Files.lines(path, cs)) {
        	return new Text(lines);
        }
    }

    public static Text valueOf(String s) {
        return new Text(Arrays.stream(s.split(NEWLINE_STR)));
    }

    public static Text valueOf(String[] lines) {
        return collect(Arrays.stream(lines));
    }

    public static Text valueOf(Collection<String> lines) {
        return collect(lines.stream());
    }

    static void checkNoLineBreaks(String line) {
        LangUtil.check(!containsLineBreaks(line), "line must not contain linebreaks");
    }

    static boolean containsLineBreaks(String line) {
        return line.indexOf(NEWLINE) >= 0;
    }

    private final List<String> lines;

    private Text(List<String> lines) {
        this.lines = lines;
    }

    private Text(Stream<String> lines) {
        this.lines = lines.collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean equals(Object o) {
        return lines.equals(o);
    }

    @Override
    public String get(int index) {
        return lines.get(index);
    }

    @Override
    public int hashCode() {
        return lines.hashCode();
    }

    public Text region(int begin, int end) {
        LangUtil.check(begin >= 0 && begin < end && end <= lines.size(), "invalid region from %d to %d", begin, end);
        return new Text(new ArrayList<>(lines.subList(begin, end)));
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String line : lines) {
            sb.append(sep).append(line);
            sep = NEWLINE_STR;
        }
        return sb.toString();
    }
}

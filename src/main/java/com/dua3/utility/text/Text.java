package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.dua3.utility.lang.LangUtil;

public class Text extends AbstractList<String> {

    private static final char NEWLINE = '\n';
    private static final String NEWLINE_STR = "" + '\n';

    public static Text empty() {
        return new Text();
    }

    public static Text load(Path path, Charset cs) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, cs)) {
            Text text = new Text();
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                text.addUnchecked(line);
            }
            return text;
        }
    }

    private static void checkNoLineBreaks(String line) {
        if (containsLineBreaks(line)) {
            throw new IllegalArgumentException("line must not contain linebreaks");
        }
    }

    private static boolean containsLineBreaks(String line) {
        return line.indexOf(NEWLINE) >= 0;
    }

    private final List<String> lines;

    public Text() {
        lines = new ArrayList<>();
    }

    public Text(List<String> lines) {
        this.lines = new ArrayList<>(lines);
    }

    @Override
    public boolean add(String line) {
        checkNoLineBreaks(line);
        return addUnchecked(line);
    }

    public boolean addText(String text) {
        for (String s : text.split(NEWLINE_STR)) {
            addUnchecked(s);
        }
        return true;
    }

    @Override
    public String get(int index) {
        return lines.get(index);
    }

    public Text region(int begin, int end) {
        LangUtil.check(begin >= 0 && begin < end && end <= lines.size(), "invalid region from {} to {}", begin, end);
        return new Text(lines.subList(begin, end));
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

    private boolean addUnchecked(String line) {
        return lines.add(line);
    }
}

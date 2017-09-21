package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class Text extends AbstractList<String> {
    
    private final List<String> lines;
    
    public static Text load(Path path, Charset cs) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, cs)) {
            Text text = new Text();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                text.add(line);
            }
            return text;
        }    
    }

    public Text(List<String> lines) {
        this.lines = new ArrayList<>(lines);
    }

    public Text() {
        lines = new ArrayList<>();
    }

    @Override
    public String get(int index) {
        return lines.get(index);
    }

    @Override
    public int size() {
        return lines.size();
    }
    
}

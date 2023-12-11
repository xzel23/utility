package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnsiConverterTest {

    @Test
    void create() {
        Style[] styles = {
                Style.BOLD,
                Style.ITALIC,
                Style.UNDERLINE,
                Style.LINE_THROUGH,
                Style.RED
        };
        RichTextBuilder b = new RichTextBuilder();
        b.push(Style.BOLD).append("AnsiConverterTest").pop(Style.BOLD);
        b.append("\n");
        for (int i = 0; i < (1 << styles.length); i++) {
            StringBuilder sb = new StringBuilder("Styles:");
            for (int j = 0; j < styles.length; j++) {
                if ((i & (1 << j)) != 0) {
                    b.push(styles[j]);
                    sb.append(" ").append(styles[j].name());
                }
            }
            b.append(sb);
            for (int j = styles.length - 1; j >= 0; j--) {
                if ((i & (1 << j)) != 0) {
                    b.pop(styles[j]);
                }
            }
            b.append("\n");
        }
        b.append("normal\n");
        RichText rt = b.toRichText();

        AnsiConverter converter = AnsiConverter.create();
        String s = converter.convert(rt);
        System.out.println(s);
        String expected = """
                [1mAnsiConverterTest[22m
                Styles:
                [1mStyles: bold[22m
                [3mStyles: italic[23m
                [1m[3mStyles: bold italic[22m[23m
                [4mStyles: underline[24m
                [1m[4mStyles: bold underline[22m[24m
                [4m[3mStyles: italic underline[24m[23m
                [1m[4m[3mStyles: bold italic underline[22m[24m[23m
                [9mStyles: line-through[29m
                [1m[9mStyles: bold line-through[22m[29m
                [9m[3mStyles: italic line-through[29m[23m
                [1m[9m[3mStyles: bold italic line-through[22m[29m[23m
                [4m[9mStyles: underline line-through[24m[29m
                [1m[4m[9mStyles: bold underline line-through[22m[24m[29m
                [4m[9m[3mStyles: italic underline line-through[24m[29m[23m
                [1m[4m[9m[3mStyles: bold italic underline line-through[22m[24m[29m[23m
                [38;2;255;0;0mStyles: red
                [38;2;255;0;0m[1mStyles: bold red[22m
                [38;2;255;0;0m[3mStyles: italic red[23m
                [38;2;255;0;0m[1m[3mStyles: bold italic red[22m[23m
                [38;2;255;0;0m[4mStyles: underline red[24m
                [38;2;255;0;0m[1m[4mStyles: bold underline red[22m[24m
                [38;2;255;0;0m[4m[3mStyles: italic underline red[24m[23m
                [38;2;255;0;0m[1m[4m[3mStyles: bold italic underline red[22m[24m[23m
                [38;2;255;0;0m[9mStyles: line-through red[29m
                [38;2;255;0;0m[1m[9mStyles: bold line-through red[22m[29m
                [38;2;255;0;0m[9m[3mStyles: italic line-through red[29m[23m
                [38;2;255;0;0m[1m[9m[3mStyles: bold italic line-through red[22m[29m[23m
                [38;2;255;0;0m[4m[9mStyles: underline line-through red[24m[29m
                [38;2;255;0;0m[1m[4m[9mStyles: bold underline line-through red[22m[24m[29m
                [38;2;255;0;0m[4m[9m[3mStyles: italic underline line-through red[24m[29m[23m
                [38;2;255;0;0m[1m[4m[9m[3mStyles: bold italic underline line-through red[22m[24m[29m[23m
                normal
                """;
        assertEquals(expected, s);
    }
}
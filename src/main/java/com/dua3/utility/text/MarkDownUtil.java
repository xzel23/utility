package com.dua3.utility.text;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

public class MarkDownUtil {

    public static RichText convert(String source) {
        Parser parser = Parser.builder().build();
        Node node = parser.parse(source);

        RichTextRenderer renderer = new RichTextRenderer();
        return renderer.render(node);
    }

    private MarkDownUtil() {
        // utility class
    }

}

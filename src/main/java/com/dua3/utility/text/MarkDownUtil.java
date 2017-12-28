package com.dua3.utility.text;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkDownUtil {

    public static RichText toRichText(String source) {
        Parser parser = Parser.builder().build();
        Node node = parser.parse(source);

        RichTextRenderer renderer = new RichTextRenderer();
        return renderer.render(node);
    }

    public static String toHTML(String source) {
        Parser parser = Parser.builder().build();
        Node node = parser.parse(source);

        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(node);
    }

    private MarkDownUtil() {
        // utility class
    }

}

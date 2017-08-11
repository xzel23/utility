package com.dua3.utility.text;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

public class MarkDownUtil {

	private MarkDownUtil() {
		// utility class
	}

	public static RichText convert(String source) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(source);

		RichTextRenderer renderer = new RichTextRenderer();
		return renderer.render(node);
	}

}

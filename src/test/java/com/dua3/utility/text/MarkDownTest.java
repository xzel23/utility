package com.dua3.utility.text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkDownTest {

	public static void main(String[] args) throws URISyntaxException, IOException {
		Path path = Paths.get(MarkDownTest.class.getResource("syntax.md").toURI());
		String mdText = new String(Files.readAllBytes(path));
		RichText richText = MarkDownUtil.convert(mdText);
		String html = HtmlBuilder.toHtml(richText);
		System.out.println(html);
	}

}

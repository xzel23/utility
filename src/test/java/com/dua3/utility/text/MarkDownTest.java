package com.dua3.utility.text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class MarkDownTest {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String html = getHtml();
//      try (PrintStream out = new PrintStream(path)) {
//          out.println(html);
//      }
    }

    @Test
	public void testMarkDown() throws Exception {
		String htmlActual = getHtml();

        Path expectedPath = Paths.get(MarkDownTest.class.getResource("syntax.html").toURI());
        String htmlExpected = new String(Files.readAllBytes(expectedPath));

        Assert.assertEquals(htmlExpected, htmlActual);
	}

    static String getHtml() throws URISyntaxException, IOException {
        Path mdPath = Paths.get(MarkDownTest.class.getResource("syntax.md").toURI());
		String mdText = new String(Files.readAllBytes(mdPath));
		RichText richText = MarkDownUtil.convert(mdText);
		String htmlActual = HtmlBuilder.toHtml(richText);
        return htmlActual;
    }

}

package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.dua3.utility.io.IOUtil;

public class MarkDownTest {

    public static void main(String[] args) throws Exception {
        Charset cs = StandardCharsets.UTF_8;

        if (args.length == 0) {
        	System.out.println(getAnsi());
        }

        if (args.length== 1 && args[0].equals("-update")) {
	        System.err.println("WARNING! This will overwrite expected unit test results!!!\nEnter 'YES' to continue.");
	        if (!"YES".equals(new BufferedReader(new InputStreamReader(System.in, cs)).readLine())) {
	            System.err.println("aborted.");
	            System.exit(1);
	        }

	        String html = getHtml();
	        Path htmlPath = Paths.get(MarkDownTest.class.getResource("syntax.html").toURI());
	        try (PrintStream out = new PrintStream(htmlPath.toFile(), cs.name())) {
	            out.print(html);
	            System.out.println("Wrote new expected unit test result.");
	            System.err.println("Copy " + htmlPath + " to resources folder to permanently update tests.");
	        }
        }
    }

    static String getHtml() {
        String mdText = getTestDataSource();
        RichText richText = MarkDownUtil.convert(mdText);
        return HtmlBuilder.toHtml(richText, MarkDownStyle::getAttributes);
    }

    static String getAnsi() {
        String mdText = getTestDataSource();
        RichText richText = MarkDownUtil.convert(mdText);
        return AnsiBuilder.toAnsi(richText, MarkDownStyle::getAttributes);
    }

    @Test
    public void testMarkDown() {
        String htmlActual = getHtml();
        String htmlExpected = getTestDataExpectedHtml();

        Assert.assertEquals(htmlExpected, htmlActual);
    }

    public static String getTestData(String filename) {
        try {
            return IOUtil.read(Paths.get(MarkDownTest.class.getResource(filename).toURI()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException();
        }
    }

    public static String getTestDataSource() {
        return getTestData("syntax.md");
    }

    public static String getTestDataExpectedHtml() {
        return getTestData("syntax.html");
    }
}

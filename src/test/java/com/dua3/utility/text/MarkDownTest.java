package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.dua3.utility.io.IOUtil;

public class MarkDownTest {

    public static void main(String[] args) throws Exception {
        System.err.println("WARNING! This will overwrite expected unit test results!!!\nEnter 'YES' to continue.");
        if (!"YES".equals(new BufferedReader(new InputStreamReader(System.in)).readLine().trim())) {
            System.err.println("aborted.");
            System.exit(1);
        }

        String html = getHtml();
        Path htmlPath = Paths.get(MarkDownTest.class.getResource("syntax.html").toURI());
        try (PrintStream out = new PrintStream(htmlPath.toFile())) {
            out.print(html);
            System.out.println("Wrote new expected unit test result.");
            System.err.println("Copy " + htmlPath + " to resources folder to permanently update tests.");
        }
    }

    static String getHtml() throws Exception {
        String mdText = getTestDataSource();
        RichText richText = MarkDownUtil.convert(mdText);
        return HtmlBuilder.toHtml(richText);
    }

    @Test
    public void testMarkDown() throws Exception {
        String htmlActual = getHtml();
        String htmlExpected = getTestDataExpectedHtml();

        Assert.assertEquals(htmlExpected, htmlActual);
    }

    public static String getTestDataSource() throws Exception {
        return IOUtil.read(Paths.get(MarkDownTest.class.getResource("syntax.md").toURI()), StandardCharsets.UTF_8);
    }

    public static String getTestDataExpectedHtml() throws Exception {
        return IOUtil.read(Paths.get(MarkDownTest.class.getResource("syntax.html").toURI()), StandardCharsets.UTF_8);
    }
}

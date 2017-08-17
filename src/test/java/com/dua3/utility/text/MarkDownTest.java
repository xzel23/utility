package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.dua3.utility.io.IOUtil;

public class MarkDownTest {
    
    public static void main(String[] args) throws URISyntaxException, IOException {
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
    
    @Test
    public void testMarkDown() throws Exception {
        String htmlActual = getHtml();
        
        Path expectedPath = Paths.get(MarkDownTest.class.getResource("syntax.html").toURI());
        String htmlExpected = IOUtil.read(expectedPath, StandardCharsets.UTF_8);
        
        Assert.assertEquals(htmlExpected, htmlActual);
    }
    
    static String getHtml() throws URISyntaxException, IOException {
        Path mdPath = Paths.get(MarkDownTest.class.getResource("syntax.md").toURI());
        String mdText = IOUtil.read(mdPath, StandardCharsets.UTF_8);
        RichText richText = MarkDownUtil.convert(mdText);
        String htmlActual = HtmlBuilder.toHtml(richText);
        return htmlActual;
    }
    
}

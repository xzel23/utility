package com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XmlUtilTest {

    private static final XmlUtil XML_UTIL = XmlUtil.defaultInstance();

    private static final Pattern PATTERN_LEADING_WHITESPACE = Pattern.compile("^\\s+", Pattern.MULTILINE);

    private static String stripLeadingWhitespace(String s) {
        return PATTERN_LEADING_WHITESPACE.matcher(s).replaceAll("");
    }

    private static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Countries>
                <Country LongName="Canada" ShortName="CA">
                    <Provinces>
                        <Province Name="Alberta"/>
                        <Province Name="Manitoba"/>
                    </Provinces>
                </Country>
                <Country LongName="United States" ShortName="US">
                    <Provinces>
                        <Province Name="Alaska"/>
                        <Province Name="Alabama"/>
                    </Provinces>
                </Country>
            </Countries>
            """;

    private final String XML_UNFORMATTED = stripLeadingWhitespace(XML);

    private static final String XML_WITH_COMMENT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Important comment -->
            <Countries>
                <!--
                 multi-line comments
                 here
                -->
                <Country LongName="Canada" ShortName="CA">
                    <Provinces>
                        <Province Name="Alberta"/>
                        <Province Name="Manitoba"/>
                    </Provinces>
                </Country>
                <Country LongName="United States" ShortName="US">
                    <Provinces>
                        <Province Name="Alaska"/>
                        <Province Name="Alabama"/>
                    </Provinces>
                </Country>
            </Countries>
            """;

    private final String XML_WITH_COMMENT_UNFORMATTED = stripLeadingWhitespace(XML_WITH_COMMENT);

    private static final String XML_WITH_NAMESPACES = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Countries xmlns:c="https://www.dua3.com/countries" xmlns:d="https://www.dua3.com/other_countries">
                <c:Country LongName="Canada" ShortName="CA">
                    <c:Provinces>
                        <c:Province Name="Alberta"/>
                        <c:Province Name="Manitoba"/>
                    </c:Provinces>
                </c:Country>
                <d:Country Name="United States" ShortName="US">
                    <d:Provinces>
                        <d:Province Name="Alaska"/>
                        <d:Province Name="Alabama"/>
                    </d:Provinces>
                </d:Country>
            </Countries>
            """;

    private final String XML_WITH_NAMESPACES_UNFORMATTED = stripLeadingWhitespace(XML_WITH_NAMESPACES);

    private static final String XML_EXAMPLE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
                <xsl:template match="/">
                    <html>
                        <body>
                            <h2>XML-Test</h2>
                            <table border="1">
                                <tr>
                                    <th style="text-align:left">Test</th>
                                    <th style="text-align:left">Result</th>
                                </tr>
                                <xsl:for-each select="suites/testcase">
                                    <tr>
                                        <td>
                                            <xsl:value-of select="name"/>
                                        </td>
                                        <td>
                                            <xsl:value-of select="result"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </table>
                        </body>
                    </html>
                </xsl:template>
            </xsl:stylesheet>
            """;

    private final String XML_EXAMPLE_UNFORMATTED =  stripLeadingWhitespace(XML_EXAMPLE);

    @Test
    void parseString() throws Exception {
        Document document = XML_UTIL.parse(XML);
        assertNotNull(document);
    }

    @Test
    void parseStream() throws Exception {
        Document document = XML_UTIL.parse(IoUtil.stringInputStream(XML));
        assertNotNull(document);
    }

    @Test
    void prettyPrintDocument() throws Exception {
        Document document = XML_UTIL.parse(XML_UNFORMATTED);
        String text = XML_UTIL.prettyPrint(document);
        assertEquals(TextUtil.toSystemLineEnds(XML), text);
    }

    @Test
    void prettyPrintText() throws Exception {
        String text = XML_UTIL.prettyPrint(XML_UNFORMATTED);
        assertEquals(TextUtil.toSystemLineEnds(XML), text);
    }

    @Test
    void prettyPrintTextWithComment() throws Exception {
        String text = XML_UTIL.prettyPrint(XML_WITH_COMMENT_UNFORMATTED);
        assertEquals(TextUtil.toSystemLineEnds(XML_WITH_COMMENT), text);
    }

    @Test
    void prettyPrintDocumentWithNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES_UNFORMATTED);
        String text = XML_UTIL.prettyPrint(document);
        assertEquals(TextUtil.toSystemLineEnds(XML_WITH_NAMESPACES), text);
    }

    @Test
    void prettyPrintTextWithNamespace() throws Exception {
        String text = XML_UTIL.prettyPrint(XML_WITH_NAMESPACES_UNFORMATTED);
        assertEquals(TextUtil.toSystemLineEnds(XML_WITH_NAMESPACES), text);
    }

    @Test
    void prettyPrintExampleDocument() throws Exception {
        Document document = XML_UTIL.parse(XML_EXAMPLE_UNFORMATTED);
        String text = XML_UTIL.prettyPrint(document);
        assertEquals(TextUtil.toSystemLineEnds(XML_EXAMPLE), text);
    }

    @Test
    void prettyPrintExampleText() throws Exception {
        String text = XML_UTIL.prettyPrint(XML_EXAMPLE_UNFORMATTED);
        assertEquals(TextUtil.toSystemLineEnds(XML_EXAMPLE), text);
    }

    @Test
    void parseString_withNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES);
        assertNotNull(document);
    }

    @Test
    void parseStream_withNamespace() throws Exception {
        Document document = XML_UTIL.parse(IoUtil.stringInputStream(XML_WITH_NAMESPACES));
        assertNotNull(document);
    }

    @Test
    void prettyPrint_withNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES_UNFORMATTED);
        String text = XML_UTIL.prettyPrint(document);
        assertEquals(TextUtil.toSystemLineEnds(XML_WITH_NAMESPACES), text);
    }

    @Test
    void xpath() throws Exception {
        Document document = XML_UTIL.parse(XML_UNFORMATTED);
        XPath xpath = XML_UTIL.xpath();

        String expected = "Canada";
        String actual = xpath.evaluate("//Country[@ShortName='CA']/@LongName", document);

        assertEquals(expected, actual);
    }

    @Test
    void xpath_withNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES_UNFORMATTED);
        XPath xpath = XML_UTIL.xpath(document.getDocumentElement());

        String expected = "Canada";
        String actual = xpath.evaluate("//c:Country[@ShortName='CA']/@LongName", document);
        assertEquals(expected, actual);

        String expected2 = "";
        String actual2 = xpath.evaluate("//c:Country[@ShortName='US']/@LongName", document);
        assertEquals(expected2, actual2);

        String expected3 = "";
        String actual3 = xpath.evaluate("//d:Country[@ShortName='CA']/@LongName", document);
        assertEquals(expected3, actual3);

        String expected4 = "";
        String actual4 = xpath.evaluate("//d:Country[@ShortName='CA']/@Name", document);
        assertEquals(expected4, actual4);

        String expected5 = "United States";
        String actual5 = xpath.evaluate("//d:Country[@ShortName='US']/@Name", document);
        assertEquals(expected5, actual5);
    }
}

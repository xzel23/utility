package com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XmlUtilTest {

    private static final XmlUtil XML_UTIL = XmlUtil.defaultInstance();

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

    /**
     * scramble indentation of the input text.
     *
     * @param text the text
     * @return text with random indentation
     */
    private static String prepareInput(String text) {
        Random r = new Random(text.hashCode()); // reproducible results
        return text.lines()
                .map(String::strip)
                .map(s -> s.indent(r.nextInt(8)))
                .collect(Collectors.joining())
                .trim();
    }

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

    @ParameterizedTest
    @ValueSource(strings = {XML, /* don't run because Transformer formats multi-line comments differently: XML_WITH_COMMENT, */ XML_WITH_NAMESPACES, XML_EXAMPLE})
    void prettyPrintDocument(String xml) throws Exception {
        String input = prepareInput(xml);
        Document document = XML_UTIL.parse(input);
        String result = XML_UTIL.prettyPrint(document);
        assertLinesMatch(xml.lines(), result.lines());
    }

    @ParameterizedTest
    @ValueSource(strings = {XML, XML_WITH_COMMENT, XML_WITH_NAMESPACES, XML_EXAMPLE})
    void prettyPrintText(String xml) {
        String input = prepareInput(xml);
        String result = XML_UTIL.prettyPrint(input);
        assertLinesMatch(xml.lines(), result.lines());
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
    void xpath() throws Exception {
        Document document = XML_UTIL.parse(XML);
        XPath xpath = XML_UTIL.xpath();

        String expected = "Canada";
        String actual = xpath.evaluate("//Country[@ShortName='CA']/@LongName", document);

        assertEquals(expected, actual);
    }

    @Test
    void xpath_withNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES);
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

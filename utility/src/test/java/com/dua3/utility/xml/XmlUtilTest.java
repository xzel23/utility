package com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void jaxpInstance() {
        XmlUtil xmlUtil = XmlUtil.jaxpInstance();
        assertNotNull(xmlUtil);
    }

    @Test
    void parseReader() throws Exception {
        try (Reader reader = new StringReader(XML)) {
            Document document = XML_UTIL.parse(reader);
            assertNotNull(document);
        }
    }

    @Test
    void testDocumentBuilder() {
        assertNotNull(XML_UTIL.documentBuilder());
    }

    @Test
    void testFormat() throws Exception {
        Document document = XML_UTIL.parse(XML);
        String formatted = XML_UTIL.format(document);
        assertNotNull(formatted);
    }

    @Test
    void testXpathWithDefaultNamespace() throws Exception {
        Document document = XML_UTIL.parse(XML);
        XPath xpath = XML_UTIL.xpath("https://www.dua3.com/countries");
        assertNotNull(xpath);
    }

    @Test
    void testXpathWithNamespaceContext() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES);
        NamespaceContext ctx = new SimpleNamespaceContext("https://www.dua3.com/countries");
        XPath xpath = XML_UTIL.xpath(ctx);
        assertNotNull(xpath);
    }

    @Test
    void testXpathWithNamespaceMap() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES);
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("c", "https://www.dua3.com/countries");
        nsMap.put("d", "https://www.dua3.com/other_countries");
        XPath xpath = XML_UTIL.xpath(nsMap, "https://www.dua3.com/default");
        assertNotNull(xpath);
    }

    @Test
    void testPrettyPrintWriterDocument() throws Exception {
        Document document = XML_UTIL.parse(XML);
        StringWriter writer = new StringWriter();
        XML_UTIL.prettyPrint(writer, document);
        String result = writer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testPrettyPrintWriterString() throws Exception {
        StringWriter writer = new StringWriter();
        XML_UTIL.prettyPrint(writer, XML);
        String result = writer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testPrettyPrintOutputStream() throws Exception {
        Document document = XML_UTIL.parse(XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XML_UTIL.prettyPrint(out, document);
        String result = out.toString(StandardCharsets.UTF_8);
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testPrettyPrintOutputStreamString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XML_UTIL.prettyPrint(out, XML);
        String result = out.toString(StandardCharsets.UTF_8);
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testFormatOutputStream() throws Exception {
        Document document = XML_UTIL.parse(XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XML_UTIL.format(out, document);
        String result = out.toString(StandardCharsets.UTF_8);
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testFormatOutputStreamWithCharset() throws Exception {
        Document document = XML_UTIL.parse(XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XML_UTIL.format(out, document, StandardCharsets.UTF_8);
        String result = out.toString(StandardCharsets.UTF_8);
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testFormatWriter() throws Exception {
        Document document = XML_UTIL.parse(XML);
        StringWriter writer = new StringWriter();
        XML_UTIL.format(writer, document);
        String result = writer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testFormatWriterWithCharset() throws Exception {
        Document document = XML_UTIL.parse(XML);
        StringWriter writer = new StringWriter();
        XML_UTIL.format(writer, document, StandardCharsets.UTF_8);
        String result = writer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Canada"));
    }

    @Test
    void testChildren() throws Exception {
        Document document = XML_UTIL.parse(XML);
        Node root = document.getDocumentElement();
        long count = XML_UTIL.children(root).count();
        assertTrue(count > 0);
    }

    @Test
    void testNodeStream() throws Exception {
        Document document = XML_UTIL.parse(XML);
        NodeList nodeList = document.getChildNodes();
        long count = XML_UTIL.nodeStream(nodeList).count();
        assertTrue(count > 0);
    }

    @Test
    void testCollectNamespaces() throws Exception {
        Document document = XML_UTIL.parse(XML_WITH_NAMESPACES);
        Element root = document.getDocumentElement();
        Map<String, String> namespaces = XmlUtil.collectNamespaces(root);
        assertFalse(namespaces.isEmpty());
        assertTrue(namespaces.containsValue("https://www.dua3.com/countries"));
    }

    @Test
    void testNormalizeDocumentNameSpaces() throws Exception {
        Document document1 = XML_UTIL.parse(XML);
        Document document2 = XML_UTIL.parse(XML_WITH_NAMESPACES);
        List<XmlUtil.DocumentWithNamespace> normalized = XmlUtil.normalizeDocumentNameSpaces(document1, document2);
        assertEquals(2, normalized.size());
        assertNotNull(normalized.get(0).document());
        assertNotNull(normalized.get(0).namespaceContext());
    }
}

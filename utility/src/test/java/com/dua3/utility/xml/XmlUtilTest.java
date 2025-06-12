package com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.text.TextUtil;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlUtilTest {

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

    private static final String UNINDENTED_XML = XML.indent(Integer.MIN_VALUE);

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

    private static final String EXPECTED_FORMATTED_XML = """
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

    private static final String EXPECTED_PRETTY_PRINTED_XML = """
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
        Document document = XmlUtil.defaultInstance().parse(XML);
        assertNotNull(document);
    }

    @Test
    void parseStream() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(IoUtil.stringInputStream(XML));
        assertNotNull(document);
    }

    @ParameterizedTest
    @ValueSource(strings = {XML, /* don't run because Transformer formats multi-line comments differently: XML_WITH_COMMENT, */ XML_WITH_NAMESPACES, XML_EXAMPLE})
    void prettyPrintDocument(String xml) throws Exception {
        String input = prepareInput(xml);
        Document document = XmlUtil.defaultInstance().parse(input);
        String result = XmlUtil.defaultInstance().prettyPrint(document);
        assertLinesMatch(xml.lines(), result.lines());
    }

    @ParameterizedTest
    @ValueSource(strings = {XML, XML_WITH_COMMENT, XML_WITH_NAMESPACES, XML_EXAMPLE})
    void prettyPrintText(String xml) {
        String input = prepareInput(xml);
        String result = XmlUtil.defaultInstance().prettyPrint(input);
        assertLinesMatch(xml.lines(), result.lines());
    }

    @Test
    void parseString_withNamespace() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        assertNotNull(document);
    }

    @Test
    void parseStream_withNamespace() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(IoUtil.stringInputStream(XML_WITH_NAMESPACES));
        assertNotNull(document);
    }

    @Test
    void xpath() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML);
        XPath xpath = XmlUtil.defaultInstance().xpath();

        String expected = "Canada";
        String actual = xpath.evaluate("//Country[@ShortName='CA']/@LongName", document);

        assertEquals(expected, actual);
    }

    @Test
    void xpath_withNamespace() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        XPath xpath = XmlUtil.defaultInstance().xpath(document.getDocumentElement());

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
            Document document = XmlUtil.defaultInstance().parse(reader);
            assertNotNull(document);
        }
    }

    @Test
    void testDocumentBuilder() {
        assertNotNull(XmlUtil.defaultInstance().documentBuilder());
    }

    @Test
    void testFormat() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML);
        String formatted = XmlUtil.defaultInstance().format(document);
        assertNotNull(formatted);
    }

    @Test
    void testXpathWithDefaultNamespace() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML);
        XPath xpath = XmlUtil.defaultInstance().xpath("https://www.dua3.com/countries");
        assertNotNull(xpath);

        // Select an element from the document and verify the result
        String expected = "Canada";
        String actual = xpath.evaluate("//Country[@ShortName='CA']/@LongName", document);
        assertEquals(expected, actual);
    }

    @Test
    void testXpathWithNamespaceContext() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("c", "https://www.dua3.com/countries");
        NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
        XPath xpath = XmlUtil.defaultInstance().xpath(ctx);
        assertNotNull(xpath);

        // Select an element from the document and verify the result
        String expected = "Canada";
        String actual = xpath.evaluate("//c:Country[@ShortName='CA']/@LongName", document);
        assertEquals(expected, actual);
    }

    @Test
    void testXpathWithNamespaceMap() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("c", "https://www.dua3.com/countries");
        nsMap.put("d", "https://www.dua3.com/other_countries");
        XPath xpath = XmlUtil.defaultInstance().xpath(nsMap, "https://www.dua3.com/default");
        assertNotNull(xpath);

        // Select elements from the document and verify the results
        // Test with c namespace
        String expected1 = "Canada";
        String actual1 = xpath.evaluate("//c:Country[@ShortName='CA']/@LongName", document);
        assertEquals(expected1, actual1);

        // Test with d namespace
        String expected2 = "United States";
        String actual2 = xpath.evaluate("//d:Country[@ShortName='US']/@Name", document);
        assertEquals(expected2, actual2);
    }

    @Test
    void testPrettyPrintWriterDocument() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        StringWriter writer = new StringWriter();
        XmlUtil.defaultInstance().prettyPrint(writer, document);
        String result = writer.toString();
        assertLinesMatch(EXPECTED_PRETTY_PRINTED_XML.lines(), result.lines());
    }

    @Test
    void testPrettyPrintWriterString() throws Exception {
        StringWriter writer = new StringWriter();
        XmlUtil.defaultInstance().prettyPrint(writer, UNINDENTED_XML);
        String result = writer.toString();
        assertLinesMatch(EXPECTED_PRETTY_PRINTED_XML.lines(), result.lines());
    }

    @Test
    void testPrettyPrintOutputStream() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlUtil.defaultInstance().prettyPrint(out, document);
        String result = out.toString(StandardCharsets.UTF_8);
        assertLinesMatch(EXPECTED_PRETTY_PRINTED_XML.lines(), result.lines());
    }

    @Test
    void testPrettyPrintOutputStreamString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlUtil.defaultInstance().prettyPrint(out, UNINDENTED_XML);
        String result = out.toString(StandardCharsets.UTF_8);
        assertLinesMatch(EXPECTED_PRETTY_PRINTED_XML.lines(), result.lines());
    }

    @Test
    void testFormatOutputStream() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlUtil.defaultInstance().format(out, document);
        String result = out.toString(StandardCharsets.UTF_8);
        assertLinesMatch(EXPECTED_FORMATTED_XML.lines(), result.lines());
    }

    @Test
    void testFormatOutputStreamWithCharset() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlUtil.defaultInstance().format(out, document, StandardCharsets.UTF_8);
        String result = out.toString(StandardCharsets.UTF_8);
        assertLinesMatch(EXPECTED_FORMATTED_XML.lines(), result.lines());
    }

    @Test
    void testFormatWriter() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        StringWriter writer = new StringWriter();
        XmlUtil.defaultInstance().format(writer, document);
        String result = writer.toString();
        assertLinesMatch(EXPECTED_FORMATTED_XML.lines(), result.lines());
    }

    @Test
    void testFormatWriterWithCharset() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(UNINDENTED_XML);
        StringWriter writer = new StringWriter();
        XmlUtil.defaultInstance().format(writer, document, StandardCharsets.UTF_8);
        String result = TextUtil.normalizeLineEnds(writer.toString());
        assertLinesMatch(EXPECTED_FORMATTED_XML.lines(), result.lines());
    }

    @Test
    void testChildren() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML);
        Node root = document.getDocumentElement();

        // Define expected tag names for children of the root element
        List<String> expectedTagNames = List.of("Country", "Country");

        // Collect actual tag names from the children stream, filtering out non-element nodes
        List<String> actualTagNames = XmlUtil.defaultInstance().children(root)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .map(Node::getNodeName)
                .toList();

        // Compare actual tag names with expected tag names
        assertEquals(expectedTagNames, actualTagNames);
    }

    @Test
    void testNodeStream() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML);
        NodeList nodeList = document.getChildNodes();

        // Define expected tag names for child nodes of the document
        List<String> expectedTagNames = List.of("Countries");

        // Collect actual tag names from the node stream, filtering out non-element nodes
        List<String> actualTagNames = XmlUtil.defaultInstance().nodeStream(nodeList)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .map(Node::getNodeName)
                .toList();

        // Compare actual tag names with expected tag names
        assertEquals(expectedTagNames, actualTagNames);
    }

    @Test
    void testCollectNamespaces() throws Exception {
        Document document = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        Element root = document.getDocumentElement();
        Map<String, String> namespaces = XmlUtil.collectNamespaces(root);
        assertFalse(namespaces.isEmpty());

        // Verify all expected namespaces are present
        assertEquals(2, namespaces.size());
        assertEquals("https://www.dua3.com/countries", namespaces.get("c"));
        assertEquals("https://www.dua3.com/other_countries", namespaces.get("d"));
    }

    @Test
    void testNormalizeDocumentNameSpaces() throws Exception {
        Document document1 = XmlUtil.defaultInstance().parse(XML_WITH_NAMESPACES);
        Document document2 = XmlUtil.defaultInstance().parse(XML_EXAMPLE);
        List<XmlUtil.DocumentWithNamespace> normalized = XmlUtil.normalizeDocumentNameSpaces(document1, document2);

        // Verify basic structure
        assertEquals(2, normalized.size());
        assertNotNull(normalized.get(0).document());
        assertNotNull(normalized.get(0).namespaceContext());
        assertNotNull(normalized.get(1).document());
        assertNotNull(normalized.get(1).namespaceContext());

        // Verify that both documents use the same namespace context (same namespace keys)
        assertSame(normalized.get(0).namespaceContext(), normalized.get(1).namespaceContext(), 
                  "Both documents should use the same namespace context");

        // Verify the full contents of the first normalized document (XML_WITH_NAMESPACES)
        Document normalizedDoc1 = normalized.get(0).document();
        Element root1 = normalizedDoc1.getDocumentElement();
        assertEquals("Countries", root1.getNodeName());

        // Check that namespace prefixes are normalized
        NodeList countries1 = root1.getChildNodes();
        int elementCount1 = 0;
        for (int i = 0; i < countries1.getLength(); i++) {
            if (countries1.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element country = (Element) countries1.item(i);
                elementCount1++;

                // Check that namespaces are properly applied
                assertNotNull(country.getNamespaceURI());
                if (country.getAttribute("ShortName").equals("CA")) {
                    assertEquals("https://www.dua3.com/countries", country.getNamespaceURI());

                    // Navigate through the document structure to find the Province element with Name="Alberta"
                    NodeList provinces = country.getChildNodes();
                    for (int j = 0; j < provinces.getLength(); j++) {
                        Node provincesNode = provinces.item(j);
                        if (provincesNode.getNodeType() == Node.ELEMENT_NODE) {
                            NodeList provinceNodes = provincesNode.getChildNodes();
                            for (int k = 0; k < provinceNodes.getLength(); k++) {
                                Node provinceNode = provinceNodes.item(k);
                                if (provinceNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element province = (Element) provinceNode;
                                    if (province.getAttribute("Name").equals("Alberta")) {
                                        // Found the Alberta province
                                        assertEquals("Alberta", province.getAttribute("Name"));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (country.getAttribute("ShortName").equals("US")) {
                    assertEquals("https://www.dua3.com/other_countries", country.getNamespaceURI());
                }
            }
        }
        assertEquals(2, elementCount1);

        // Verify the full contents of the second normalized document (XML_EXAMPLE)
        Document normalizedDoc2 = normalized.get(1).document();
        Element root2 = normalizedDoc2.getDocumentElement();

        // Check namespace URI instead of relying on the prefix
        assertEquals("http://www.w3.org/1999/XSL/Transform", root2.getNamespaceURI());
        assertEquals("stylesheet", root2.getLocalName());

        // Check that the template element exists and has the correct attributes
        NodeList templates = root2.getElementsByTagName("*");
        boolean foundTemplate = false;
        for (int i = 0; i < templates.getLength(); i++) {
            Node node = templates.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE &&
                    node.getLocalName().equals("template") &&
                    node.getNamespaceURI().equals("http://www.w3.org/1999/XSL/Transform")) {
                foundTemplate = true;
                Element template = (Element) node;
                assertEquals("/", template.getAttribute("match"));
                break;
            }
        }
        assertTrue(foundTemplate, "Template element not found in normalized document");
    }
}

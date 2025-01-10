package com.dua3.utility.xml;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.StreamUtil;
import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Utility class for handling {@link org.w3c.dom} documents and nodes.
 */
public final class XmlUtil {
    private static final Logger LOG = LogManager.getLogger(XmlUtil.class);

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + TextUtil.LINE_END_SYSTEM;
    private static final String PRETTY_PRINT_XSLT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
              <xsl:output indent="yes"/>
              <xsl:strip-space elements="*"/>
            
              <xsl:template match="@*|node()">
                <xsl:copy>
                  <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
              </xsl:template>
            
            </xsl:stylesheet>
            """;
    private static final Pattern PATTERN_BLANK_LINE = Pattern.compile("^\\s*\n");
    private static final Pattern PATTERN_END_OF_LINE = Pattern.compile("\n$");
    private static final String MESSAGE_COULD_NOT_CREATE_DEFAULT_XML_UTIL = "Could not create default XmlUtil. Check documentation of javax.xml.transform.TransformerFactory and related classes for details.";
    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private final XPathFactory xPathFactory;
    private final DocumentBuilder documentBuilder;
    private final Transformer utf8Transformer;

    /**
     * Construct a new instance.
     *
     * @param documentBuilderFactory the {@link DocumentBuilderFactory} to use
     * @param transformerFactory     the {@link TransformerFactory} to use
     * @param xPathFactory           the {@link XPathFactory} to use
     * @throws ParserConfigurationException if a configuration error occurs
     */
    public XmlUtil(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory, XPathFactory xPathFactory) throws ParserConfigurationException {
        this.documentBuilderFactory = documentBuilderFactory;
        this.transformerFactory = transformerFactory;
        this.xPathFactory = xPathFactory;
        this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        this.utf8Transformer = getTransformer(StandardCharsets.UTF_8);
    }

    /**
     * Get instance using the default implementation supplied by the JDK.
     *
     * @return default instance
     * @throws IllegalStateException if default instance could not be created
     */
    public static XmlUtil defaultInstance() {
        try {
            return new XmlUtil(DocumentBuilderFactory.newDefaultNSInstance(), TransformerFactory.newDefaultInstance(), XPathFactory.newDefaultInstance());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not create XmlUtil. Check documentation of javax.xml.transform.TransformerFactory and related classes for details.", e);
        }
    }

    /**
     * Get instance using JAXP Lookup Mechanism to obtain implementing classes.
     *
     * @return new instance
     * @throws IllegalStateException if default instance could not be created
     */
    public static XmlUtil jaxpInstance() {
        try {
            return new XmlUtil(DocumentBuilderFactory.newNSInstance(), TransformerFactory.newInstance(), XPathFactory.newInstance());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not create XmlUtil. Check documentation of javax.xml.transform.TransformerFactory and related classes for details.", e);
        }
    }

    private static <T> Consumer<T> consume(LangUtil.ConsumerThrows<? super T, ? extends XMLStreamException> c) {
        return (T arg) -> {
            try {
                c.accept(arg);
            } catch (XMLStreamException e) {
                throw new WrappedXMLStreamException(e);
            }
        };
    }

    private static void skipWhitespace(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext() && reader.peek().getEventType() == XMLStreamConstants.SPACE) {
            reader.next();
        }
    }

    private static void writeIndentation(XMLStreamWriter writer, int level) throws XMLStreamException {
        assert level >= 0;
        writer.writeCharacters(" ".repeat(indentation(level)));
    }

    private static int indentation(int level) {
        int indent = 4;
        return level * indent;
    }

    /**
     * Get stream of child nodes.
     *
     * @param node the node
     * @return stream of the child nodes
     */
    public Stream<Node> children(Node node) {
        return nodeStream(node.getChildNodes());
    }

    /**
     * Convert {@code NodeList} to {@code Stream<Node>}.
     *
     * @param nodes the NodeList
     * @return stream of nodes
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Stream<Node> nodeStream(NodeList nodes) {
        return StreamSupport.stream(new NodeSpliterator(nodes), false);
    }

    /**
     * Read XML from an {@link InputStream} and parse it to {@link Document}.
     *
     * @param in the stream to read the XML from
     * @return the parsed {@link Document}
     * @throws IOException  in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public Document parse(InputStream in) throws IOException, SAXException {
        return documentBuilder().parse(in);
    }

    /**
     * Read XML from an {@link InputStream} and parse it to {@link Document}.
     *
     * @param reader the {@link Reader} to read the XML from
     * @return the parsed {@link Document}
     * @throws IOException  in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public Document parse(Reader reader) throws IOException, SAXException {
        return documentBuilder().parse(new InputSource(reader));
    }

    /**
     * Parse the content of {@code file} to {@link Document}.
     *
     * @param uri the URI to read the XML from
     * @return the parsed {@link Document}
     * @throws IOException  in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public Document parse(URI uri) throws IOException, SAXException {
        return documentBuilder().parse(uri.toString());
    }

    /**
     * Parse the content of {@code path} to {@link Document}.
     *
     * @param path the path to read the XML from
     * @return the parsed {@link Document}
     * @throws IOException  in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public Document parse(Path path) throws IOException, SAXException {
        return documentBuilder().parse(path.toFile());
    }

    /**
     * Parse text to {@link Document}.
     *
     * @param text the XML as a String
     * @return the parsed {@link Document}
     * @throws IOException  in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public Document parse(String text) throws IOException, SAXException {
        try (Reader reader = new StringReader(text)) {
            return documentBuilder.parse(new InputSource(reader));
        }
    }

    /**
     * Pretty print W3C Node using UTF-8 encoding.
     *
     * @param out  the stream to write to
     * @param node the node
     * @throws IOException when an I/O error occurs
     */
    public void format(OutputStream out, Node node) throws IOException {
        format(out, node, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Node using the provided charset for encoding.
     *
     * @param out     the stream to write to
     * @param node    the node
     * @param charset the {@link Charset} to use for encoding the output
     * @throws IOException when an I/O error occurs
     */
    public void format(OutputStream out, Node node, Charset charset) throws IOException {
        format(new OutputStreamWriter(out, charset), node, charset);
    }

    /**
     * Pretty print W3C Node.
     * <br>
     * <strong>Note:</strong> the writer should be using the UTF-8 character encoding!
     *
     * @param writer the writer to write to
     * @param node   the node
     * @throws IOException when an I/O error occurs
     */
    public void format(Writer writer, Node node) throws IOException {
        format(writer, node, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Document. Note that the provided charset should match the one used by the writer!
     *
     * @param writer  the writer to write to
     * @param node    the node
     * @param charset the {@link Charset} to use for encoding the output
     * @throws IOException when an I/O error occurs
     */
    public void format(Writer writer, Node node, Charset charset) throws IOException {
        try {
            Transformer transformer = charset.equals(StandardCharsets.UTF_8) ? utf8Transformer : getTransformer(charset);
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (TransformerConfigurationException e) {
            // should not happen(tm)
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IOException("error in transformation: " + e.getMessage(), e);
        }
    }

    private Transformer getTransformer(Charset charset) {
        try {
            Source source = new StreamSource(IoUtil.stringInputStream(PRETTY_PRINT_XSLT));
            Transformer transformer = transformerFactory.newTransformer(source);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset.name());
            return transformer;
        } catch (TransformerConfigurationException e) {
            LOG.error("unexpected error creating transformer", e);
            throw new IllegalStateException("error creating transformer", e);
        }
    }

    /**
     * Format node to XML.
     *
     * @param node the node
     * @return XML for the node
     */
    public String format(Node node) {
        return formatNode(node, "");
    }

    /**
     * Pretty print W3C Document.
     *
     * @param document the document
     * @return formatted XML for the document
     */
    public String prettyPrint(Document document) {
        return formatNode(document, XML_DECLARATION);
    }

    /**
     * Pretty print W3C Document.
     *
     * @param writer   the {@link Writer} to use for printing the formatted XML
     * @param document the document to be pretty printed
     * @throws IOException if an I/O error occurs while writing the formatted XML to the writer
     */
    public void prettyPrint(Writer writer, Document document) throws IOException {
        formatNode(writer, document, XML_DECLARATION);
        writer.flush();
    }

    /**
     * Pretty print W3C Document.
     *
     * @param out      the {@link OutputStream} to use for printing the formatted XML
     * @param document the document to be pretty printed
     * @throws IOException if an I/O error occurs while writing the formatted XML to the output stream
     */
    public void prettyPrint(OutputStream out, Document document) throws IOException {
        prettyPrint(out, document, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Document.
     *
     * @param out      the {@link OutputStream} to use for printing the formatted XML
     * @param document the document to be pretty printed
     * @param cs       the {@link Charset} to use
     * @throws IOException if an I/O error occurs while writing the formatted XML to the output stream
     */
    public void prettyPrint(OutputStream out, Document document, Charset cs) throws IOException {
        prettyPrint(new OutputStreamWriter(out, cs), document);
    }

    /**
     * Pretty print XML. If the document cannot be parsed, the unchanged text is returned.
     *
     * @param xml the XML text
     * @return formatted XML for the document
     */
    @SuppressWarnings("MethodMayBeStatic")
    public String prettyPrint(String xml) {
        boolean hasChildren = false;
        int level = 0;
        try (StringReader in = new StringReader(xml.indent(Integer.MIN_VALUE));
             StringWriter out = new StringWriter(xml.length() * 6 / 5)) {

            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(in);

            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(out);

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if (event instanceof StartElement se) {
                    skipWhitespace(reader);
                    writeIndentation(writer, level);
                    hasChildren = false;
                    level++;
                    QName seName = se.getName();
                    if (reader.peek().isEndElement() && reader.next() instanceof EndElement) {
                        writer.writeEmptyElement(seName.getPrefix(), seName.getLocalPart(), seName.getNamespaceURI());
                        level--;
                        hasChildren = true;
                    } else {
                        writer.writeStartElement(seName.getPrefix(), seName.getLocalPart(), seName.getNamespaceURI());
                    }
                    // write namespaces and attributes in alphabetical order to obtain reproducible results
                    //noinspection DataFlowIssue - false positive; getPrefix() returns "" for the default namespace
                    StreamUtil.stream(se.getNamespaces())
                            .sorted(Comparator.comparing(Namespace::getPrefix))
                            .forEach(consume(ns -> writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI())));
                    StreamUtil.stream(se.getAttributes())
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(Attribute::toString))
                            .forEach(consume(attr -> {
                                //noinspection DataFlowIssue - false positive
                                QName attrName = attr.getName();
                                writer.writeAttribute(attrName.getPrefix(), attrName.getNamespaceURI(), attrName.getLocalPart(), attr.getValue());
                            }));
                } else if (event instanceof EndElement) {
                    level--;
                    if (hasChildren) {
                        writeIndentation(writer, level);
                    }
                    writer.writeEndElement();
                    hasChildren = true;
                } else if (event.getEventType() == XMLStreamConstants.SPACE) {
                    // do nothing - skip whitespace
                } else if (event instanceof ProcessingInstruction pi) {
                    skipWhitespace(reader);
                    writeIndentation(writer, level);
                    writer.writeProcessingInstruction(pi.getTarget(), pi.getData());
                    hasChildren = true;
                } else if (event instanceof Characters ch) {
                    if (ch.isCData()) {
                        writer.writeCData(ch.getData());
                    } else {
                        writer.writeCharacters(ch.getData());
                    }
                } else if (event instanceof Comment co) {
                    writeIndentation(writer, level);
                    String text = co.getText();
                    if (text.contains("\n")) {
                        // multi line comment
                        writer.writeComment(
                                PATTERN_END_OF_LINE.matcher(PATTERN_BLANK_LINE.matcher(text.indent(indentation(level) + 1)).replaceFirst("\n")).replaceFirst("\n" + " ".repeat(indentation(level)))
                        );
                    } else {
                        // single line comment
                        writer.writeComment(text);
                        writer.writeCharacters(TextUtil.LINE_END_SYSTEM);
                    }
                } else if (event instanceof StartDocument sd) {
                    writer.writeStartDocument(StandardCharsets.UTF_8.name(), sd.getVersion());
                    writer.writeCharacters(TextUtil.LINE_END_SYSTEM);
                } else if (event instanceof EndDocument) {
                    writer.writeEndDocument();
                    writer.writeCharacters(TextUtil.LINE_END_SYSTEM);
                } else if (event instanceof EntityReference er) {
                    writer.writeEntityRef(er.getName());
                } else if (event instanceof Namespace ns) {
                    writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
                } else if (event instanceof Attribute at) {
                    QName qName = at.getName();
                    writer.writeAttribute(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), at.getValue());
                } else if (event instanceof DTD dtd) {
                    writer.writeDTD(dtd.getDocumentTypeDeclaration());
                } else {
                    LOG.trace("ignoring element: {}", event);
                }
            }
            writer.flush();

            return TextUtil.toSystemLineEnds(out.toString());
        } catch (IOException | XMLStreamException | WrappedXMLStreamException e) {
            LOG.warn("could not parse XML", e);
            return xml;
        }
    }

    /**
     * Pretty print XML. If the document cannot be parsed, the unchanged text is written.
     *
     * @param writer the {@link Writer} to use
     * @param xml    the XML text
     * @throws IOException if an I/O error occurs
     */
    public void prettyPrint(Writer writer, String xml) throws IOException {
        try {
            prettyPrint(writer, parse(xml));
        } catch (SAXException e) {
            LOG.warn("could not parse XML, writing unchanged data");
            writer.write(xml);
        }
    }

    /**
     * Pretty print XML. If the document cannot be parsed, the unchanged text is written.
     *
     * @param out    the {@link OutputStream} to use
     * @param xml    the XML text
     * @throws IOException if an I/O error occurs
     */
    public void prettyPrint(OutputStream out, String xml) throws IOException {
        prettyPrint(out, xml, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print XML. If the document cannot be parsed, the unchanged text is written.
     *
     * @param out the {@link OutputStream} to use
     * @param xml the XML text
     * @param cs  the {@link Charset} to use
     * @throws IOException if an I/O error occurs
     */
    public void prettyPrint(OutputStream out, String xml, Charset cs) throws IOException {
        try {
            prettyPrint(new OutputStreamWriter(out, cs), parse(xml));
        } catch (SAXException e) {
            LOG.warn("could not parse XML, writing unchanged data to stream");
            try (Writer writer = new OutputStreamWriter(out, cs)) {
                writer.write(xml);
            }
        }
    }

    private String formatNode(Node node, String prefix) {
        try (StringWriter writer = new StringWriter(64)) {
            formatNode(writer, node, prefix);
            return writer.toString();
        } catch (IOException e) {
            // should not happen when writing to a String
            throw new UncheckedIOException(e);
        }
    }

    private void formatNode(Writer writer, Node node, String prefix) throws IOException {
        writer.write(prefix);
        format(writer, node, StandardCharsets.UTF_8);
    }

    /**
     * Create {@link XPath} instance.
     * <p>
     * The returned instance is <strong>not</strong> namespace aware. Either use {@link #xpath(Node)} or supply
     * a custom {@link NamespaceContext} if namespaces should be supported.
     *
     * @return new {@link XPath} instance.
     */
    public XPath xpath() {
        return xPathFactory.newXPath();
    }

    /**
     * Create {@link XPath} instance with a namespace context.
     *
     * @param ctx the {@link NamespaceContext} to use
     * @return new {@link XPath} instance.
     */
    public XPath xpath(NamespaceContext ctx) {
        XPath xpath = xpath();
        xpath.setNamespaceContext(ctx);
        return xpath;
    }

    /**
     * Create {@link XPath} instance with only a default namespace set The default namespace will be identified by
     * the name "ns" in xpath expressions.
     *
     * @param defaultUri the URI of the default namespace
     * @return new {@link XPath} instance.
     */
    public XPath xpath(String defaultUri) {
        return xpath(new SimpleNamespaceContext(defaultUri));
    }

    /**
     * Create {@link XPath} instance with a mapping and a default namespace.
     *
     * @param nsToUri    mapping from namespace name to URI
     * @param defaultUri the URI of the default namespace
     * @return new {@link XPath} instance.
     */
    public XPath xpath(Map<String, String> nsToUri, @Nullable String defaultUri) {
        return xpath(new SimpleNamespaceContext(nsToUri, defaultUri));
    }

    /**
     * Create {@link XPath} instance for a node with a matching {@link NamespaceContext}.
     *
     * @param node the node to determine the used namespaces from
     * @return new {@link XPath} instance generated from he supplied argument and its parent's
     * namespace declarations.
     */
    public XPath xpath(Node node) {
        Map<String, String> nsToUri = new HashMap<>();

        String defaultUri = null;
        for (Node n = node; n != null; n = n.getParentNode()) {
            NamedNodeMap attrs = n.getAttributes();

            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);

                    String key = item.getNodeName();
                    String value = item.getTextContent();

                    if (key.equals("xmlns") && defaultUri == null) {
                        defaultUri = value;
                    } else if (key.startsWith("xmlns:")) {
                        String ns = key.substring("xmlns:".length());
                        nsToUri.putIfAbsent(ns, value);
                    }
                }
            }
        }

        return xpath(nsToUri, defaultUri);
    }

    /**
     * Create new {@link DocumentBuilder}.
     *
     * @return new {@link DocumentBuilder} instance
     */
    public DocumentBuilder documentBuilder() {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // this shouldn't happen since a DocumentBuilder has already been created in the constructor
            throw new IllegalStateException("DocumentBuilderFactory configuration error", e);
        }
    }

    private static class WrappedXMLStreamException extends RuntimeException {
        WrappedXMLStreamException(XMLStreamException e) {
            super(e);
        }
    }

    private static class NodeSpliterator implements Spliterator<Node> {
        private final NodeList nodes;
        int idx;

        NodeSpliterator(NodeList nodes) {
            this.nodes = nodes;
            idx = 0;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Node> action) {
            if (idx >= nodes.getLength()) {
                return false;
            }
            action.accept(nodes.item(idx++));
            return true;
        }

        @Override
        public @Nullable Spliterator<Node> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return nodes.getLength();
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | NONNULL | ORDERED | SIZED;
        }
    }

}

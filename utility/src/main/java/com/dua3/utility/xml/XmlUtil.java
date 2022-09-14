package com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.text.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Utility class for handling {@link org.w3c.dom} documents and nodes.
 */
public final class XmlUtil {
    private static final Logger LOG = LoggerFactory.getLogger(XmlUtil.class);
    
    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private final XPathFactory xPathFactory;
    private final DocumentBuilder documentBuilder;
    private final Transformer utf8Transformer;
    
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
    
    /*
     * Lazily construct the default instance since it might pull in a lot of dependencies which is not desirable
     * in case only a specialized version is needed.
     */
    private static class LazySingleton {
        private static final XmlUtil INSTANCE;

        static {
            try {
                INSTANCE = new XmlUtil();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Could not create default XmlUtil. Check documentation of javax.xml.transform.TransformerFactory and related classes for details.", e);
            }
        }
    }
    
    /**
     * Get default instance with factories configured according to the description in the package javax.xml. 
     * @return default instance
     * @throws IllegalStateException if default instance could not be created
     */
    public static XmlUtil defaultInstance() {
        return LazySingleton.INSTANCE;
    }
    
    /**
     * Construct a new instance.
     * @throws ParserConfigurationException if a configuration error occurs
     */
    public XmlUtil() throws ParserConfigurationException {
        this(newDocumentBuilderFactory(), newTransformerFactory(), newXPathFactory());
    }

    private static DocumentBuilderFactory newDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

    private static TransformerFactory newTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        return factory;
    }

    private static XPathFactory newXPathFactory() {
        XPathFactory factory = XPathFactory.newInstance();
        return factory;
    }

    /**
     * Construct a new instance.
     * @param documentBuilderFactory the {@link DocumentBuilderFactory} to use
     * @param transformerFactory the {@link TransformerFactory} to use
     * @param xPathFactory the {@link XPathFactory} to use
     * @throws ParserConfigurationException if a configuration error occurs
     */
    public XmlUtil(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory, XPathFactory xPathFactory) throws ParserConfigurationException {
        this.documentBuilderFactory = Objects.requireNonNull(documentBuilderFactory);
        this.transformerFactory = Objects.requireNonNull(transformerFactory);
        this.xPathFactory = Objects.requireNonNull(xPathFactory);
        this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        this.utf8Transformer = getTransformer(StandardCharsets.UTF_8);
    }

    /**
     * Get stream of child nodes.
     * @param node the node
     * @return stream of the child nodes
     */
    public Stream<Node> children(Node node) {
        return nodeStream(node.getChildNodes());
    }

    /**
     * Convert {@code NodeList} to {@code Stream<Node>}.
     * @param nodes the NodeList
     * @return stream of nodes
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Stream<Node> nodeStream(NodeList nodes) {
        return StreamSupport.stream(new NodeSpliterator(nodes), false);
    }

    /**
     * Read XML from an {@link InputStream} and parse it to {@link org.w3c.dom.Document}.
     * @param in the stream to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public org.w3c.dom.Document parse(InputStream in) throws IOException, SAXException {
        return documentBuilder().parse(in);
    }

    /**
     * Parse the content of {@code file} to {@link org.w3c.dom.Document}.
     * @param uri the URI to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public org.w3c.dom.Document parse(URI uri) throws IOException, SAXException {
        return documentBuilder().parse(uri.toString());
    }

    /**
     * Parse the content of {@code path} to {@link org.w3c.dom.Document}.
     * @param path the path to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public org.w3c.dom.Document parse(Path path) throws IOException, SAXException {
        return documentBuilder().parse(path.toFile());
    }

    /**
     * Parse text to {@link org.w3c.dom.Document}.
     * @param text the XML as a String
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i.e. the input is not valid
     */
    public org.w3c.dom.Document parse(String text) throws IOException, SAXException {
        try (Reader reader = new StringReader(text)) {
            return documentBuilder.parse(new InputSource(reader));
        }
    }

    /**
     * Pretty print W3C Node using UTF-8 encoding.
     * @param out the stream to write to
     * @param node the node
     * @throws IOException when an I/O error occurs
     */
    public void format(OutputStream out, Node node) throws IOException {
        format(out, node, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Node using the provided charset for encoding.
     * @param out the stream to write to
     * @param node the node
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
     * @param node the node
     * @throws IOException when an I/O error occurs
     */
    public void format(Writer writer, Node node) throws IOException {
        format(writer, node, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Document. Note that the provided charset should match the one used by the writer!
     * @param writer the writer to write to
     * @param node the node
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
            throw new IOException("error in transformation: "+e.getMessage(), e);
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
     * @param node the node
     * @return XML for the node
     */
    public String format(Node node) {
        return formatNode(node, "");
    }

    /**
     * Pretty print W3C Document.
     * @param document the document
     * @return XML for the document
     */
    public String prettyPrint(Document document) {
        return formatNode(document, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ TextUtil.LINE_END_SYSTEM);
    }

    private String formatNode(Node node, String prefix) {
        try (StringWriter writer = new StringWriter(64)) {
            writer.write(prefix);
            format(writer, node, StandardCharsets.UTF_8);
            return writer.toString();
        } catch (IOException e) {
            // should not happen when writing to a String
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create {@link XPath} instance.
     * @return new {@link XPath} instance.
     */
    public XPath xpath() {
        return xPathFactory.newXPath();
    }

    /**
     * Create new {@link DocumentBuilder}.
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
        public Spliterator<Node> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return nodes.getLength();
        }

        @Override
        public int characteristics() {
            return Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED;
        }
    }
}

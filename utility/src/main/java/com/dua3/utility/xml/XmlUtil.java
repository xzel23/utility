package com.dua3.utility.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.*;
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

    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private final XPathFactory xPathFactory;
    private final DocumentBuilder documentBuilder;
    
    /*
     * Lazily construct the default instance since it might pull in a lot of dependencies which is not desirable
     * in case only a specialized version is needed.
     * 
     * Note that double checked locking as of JDK 5 actually works when the helper is declared volatile. 
     */
    private static volatile XmlUtil DEFAULT_INSTANCE = null; // volatile is needed for double-checked locking

    /**
     * Get default instance with factories configured according to the description in the package javax.xml. 
     * @return default instance
     * @throws IllegalStateException if default instance could not be created
     */
    public static XmlUtil defaultInstance() {
        if (DEFAULT_INSTANCE==null) {
            synchronized (XmlUtil.class) {
                if (DEFAULT_INSTANCE == null) {
                    try {
                        DEFAULT_INSTANCE = new XmlUtil();
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException("Could not create default XmlUtil. Check documentation of javax.xml.transform.TransformerFactory and related classes for details.", e);
                    }
                }
            }
        }
    
        return DEFAULT_INSTANCE;
    }
    
    /**
     * Construct a new instance.
     */
    public XmlUtil() throws ParserConfigurationException {
        this(DocumentBuilderFactory.newInstance(), TransformerFactory.newInstance(), XPathFactory.newInstance());
    }

    /**
     * Construct a new instance.
     * @param documentBuilderFactory the {@link DocumentBuilderFactory} to use
     */
    public XmlUtil(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory, XPathFactory xPathFactory) throws ParserConfigurationException {
        this.documentBuilderFactory = Objects.requireNonNull(documentBuilderFactory);
        this.transformerFactory = Objects.requireNonNull(transformerFactory);
        this.xPathFactory = Objects.requireNonNull(xPathFactory);
        this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
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
    public Stream<Node> nodeStream(NodeList nodes) {
        Spliterator<Node> spliterator = new Spliterator<Node>() {
            int idx = 0;
            
            @Override
            public boolean tryAdvance(Consumer<? super Node> action) {
                if (idx>=nodes.getLength()) {
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
        };
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Read XML from an {@link InputStream} and parse it to {@link org.w3c.dom.Document}.
     * @param in the stream to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i. e. the input is not valid
     */
    public org.w3c.dom.Document parse(InputStream in) throws IOException, SAXException {
        return documentBuilder().parse(in);
    }

    /**
     * Parse the content of {@code file} to {@link org.w3c.dom.Document}.
     * @param uri the URI to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i. e. the input is not valid
     */
    public org.w3c.dom.Document parse(URI uri) throws IOException, SAXException {
        return documentBuilder().parse(uri.toString());
    }

    /**
     * Parse the content of {@code file} to {@link org.w3c.dom.Document}.
     * @param file the file to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i. e. the input is not valid
     */
    public org.w3c.dom.Document parse(File file) throws IOException, SAXException {
        return documentBuilder().parse(file);
    }

    /**
     * Parse the content of {@code path} to {@link org.w3c.dom.Document}.
     * @param path the path to read the XML from
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i. e. the input is not valid
     */
    public org.w3c.dom.Document parse(Path path) throws IOException, SAXException {
        return documentBuilder().parse(path.toFile());
    }

    /**
     * Parse text to {@link org.w3c.dom.Document}.
     * @param text the XML as a String
     * @return the parsed {@link org.w3c.dom.Document}
     * @throws IOException in case of an I/O error
     * @throws SAXException if an exception is thrown during parsing, i. e. the input is not valid
     */
    public org.w3c.dom.Document parse(String text) throws IOException, SAXException {
        try (Reader reader = new StringReader(text)) {
            return documentBuilder.parse(new InputSource(reader));
        }
    }

    /**
     * Pretty print W3C Node using UTF-8 encoding.
     * @param <O> the type of the OutputStream
     * @param out the stream to write to
     * @param node the node
     * @return {@code out}
     * @throws IOException when an I/O error occurs
     */
    public <O extends OutputStream> O format(O out, Node node) throws IOException {
        format(out, node, StandardCharsets.UTF_8);
        return out;
    }

    /**
     * Pretty print W3C Node using the provided charset for encoding.
     * @param <O> the type of the OutputStream
     * @param out the stream to write to
     * @param node the node
     * @param charset the {@link Charset} to use for encoding the output
     * @return {@code out}
     * @throws IOException when an I/O error occurs
     */
    public <O extends OutputStream> O format(O out, Node node, Charset charset) throws IOException {
        format(new OutputStreamWriter(out, charset), node, charset);
        return out;
    }

    /**
     * Pretty print W3C Node. 
     * <br>
     * <strong>Note:</strong> the writer should be using the UTF-8 character encoding!
     *
     * @param <W> the type of the Writer
     * @param writer the writer to write to
     * @param node the node
     * @return {@code writer}
     * @throws IOException when an I/O error occurs
     */
    public <W extends Writer> W format(W writer, Node node) throws IOException {
        return format(writer, node, StandardCharsets.UTF_8);
    }

    /**
     * Pretty print W3C Document. Note that the provided charset should match the one used by the writer!
     * @param <W> the type of the Writer
     * @param writer the writer to write to
     * @param node the node
     * @param charset the {@link Charset} to use for encoding the output
     * @return {@code writer}
     * @throws IOException when an I/O error occurs
     */
    public <W extends Writer> W format(W writer, Node node, Charset charset) throws IOException {
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset.name());
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (TransformerConfigurationException e) {
            // should not happen(tm)
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IOException("error in transformation: "+e.getMessage(), e);
        }
        return writer;
    }

    /**
     * Pretty print W3C Document.
     * @param node the document
     * @return HTML for the document
     */
    public String prettyPrint(Node node) {
        try (StringWriter writer = new StringWriter()) {
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
}

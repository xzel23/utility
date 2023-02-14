package com.dua3.utility.xml;

import com.dua3.cabe.annotations.Nullable;

import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.BitSet;

public class XmlStreamWriterProxyPrettyPrint implements InvocationHandler {

    public static final String DEFAULT_LINE_FEED = "%n".formatted();
    private static final int DEFAULT_INDENT = 4;

    private final XMLStreamWriter target;
    private final BitSet hasChildren = new BitSet();
    private final String lineFeed;
    private final int indentWidth;
    int depth = 0;

    public XmlStreamWriterProxyPrettyPrint(XMLStreamWriter target) {
        this(target, DEFAULT_INDENT, DEFAULT_LINE_FEED);
    }

    public XmlStreamWriterProxyPrettyPrint(XMLStreamWriter target, int indentWidth) {
        this(target, indentWidth, DEFAULT_LINE_FEED);
    }

    private XmlStreamWriterProxyPrettyPrint(XMLStreamWriter target, int indentWidth, String lineFeed) {
        this.target = target;
        this.indentWidth = indentWidth;
        this.lineFeed = lineFeed;
    }

    @Override
    public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "writeStartElement" -> {
                // update state of parent node
                if (depth > 0) {
                    hasChildren.set(depth-1);
                }

                // reset state of current node
                hasChildren.clear(depth);

                // indent for current depth
                target.writeCharacters(lineFeed);
                target.writeCharacters(" ".repeat(depth*indentWidth));
                depth++;
            }
            case "writeEndElement" -> {
                depth--;
                if (hasChildren.get(depth)) {
                    target.writeCharacters(lineFeed);
                    target.writeCharacters(" ".repeat(depth*indentWidth));
                }
            }
            case "writeEmptyElement" -> {
                // update state of parent node
                if (depth > 0) {
                    hasChildren.set(depth-1);
                }
                // indent for current depth
                target.writeCharacters(lineFeed);
                target.writeCharacters(" ".repeat(depth*indentWidth));
            }
        }

        return method.invoke(target, args);
    }

}

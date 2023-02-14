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
    private final String lineFeed;
    private final int indentWidth;
    private int depth = 0;
    private boolean hasChildren = false;

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
                hasChildren = false;

                // indent for current depth
                target.writeCharacters(lineFeed);
                target.writeCharacters(" ".repeat(depth*indentWidth));
                depth++;
            }
            case "writeEndElement" -> {
                depth--;
                if (hasChildren) {
                    target.writeCharacters(lineFeed);
                    target.writeCharacters(" ".repeat(depth*indentWidth));
                }

                // parent has children
                hasChildren = true;
            }
            case "writeEmptyElement" -> {
                // indent for current depth
                target.writeCharacters(lineFeed);
                target.writeCharacters(" ".repeat(depth*indentWidth));

                // parent has children
                hasChildren = true;
            }
        }

        return method.invoke(target, args);
    }

}

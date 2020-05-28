package com.dua3.utility.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Utility class for handling {@link org.w3c.dom} documents and nodes.
 */
public final class DomUtil {
    private DomUtil() {}
    
    public static Stream<Node> children(Node node) {
        return nodeStream(node.getChildNodes());
    }

    /**
     * Convert {@code NodeList} to {@code Stream<Node>}.
     * @param nodes the NodeList
     * @return stream of nodes
     */
    public static Stream<Node> nodeStream(NodeList nodes) {
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
}

package com.dua3.utility.xml;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SimpleNamespaceContextTest {

    @Test
    void SimpleNameSpaceContext_defaultUri() {
        SimpleNamespaceContext ctx = new SimpleNamespaceContext("http://www.namespace.com");

        assertEquals("ns", ctx.getDefaultPrefix().orElseThrow());
        assertEquals("ns", ctx.getPrefix("http://www.namespace.com"));
        assertIterableEquals(List.of("ns"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespace.com"));
        assertEquals("http://www.namespace.com", ctx.getNamespaceURI("ns"));
    }

    @Test
    void SimpleNameSpaceContext_mapping() {
        SimpleNamespaceContext ctx = new SimpleNamespaceContext(Map.of(
                "ns", "http://www.namespace.com",
                "ns1", "http://www.namespaceA.com",
                "ns2", "http://www.namespaceB.com",
                "ns3", "http://www.namespaceA.com"
        ));

        assertEquals("ns", ctx.getPrefix("http://www.namespace.com"));
        assertIterableEquals(List.of("ns"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespace.com"));
        assertIterableEquals(List.of("ns1", "ns3"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespaceA.com"));
        assertIterableEquals(List.of("ns2"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespaceB.com"));
        assertEquals("http://www.namespace.com", ctx.getNamespaceURI("ns"));
        assertEquals("http://www.namespaceA.com", ctx.getNamespaceURI("ns1"));
        assertEquals("http://www.namespaceB.com", ctx.getNamespaceURI("ns2"));
        assertEquals("http://www.namespaceA.com", ctx.getNamespaceURI("ns3"));
    }

    @Test
    void SimpleNameSpaceContext_mapping_and_defaultUri() {
        SimpleNamespaceContext ctx = new SimpleNamespaceContext(Map.of(
                "ns", "http://www.namespace.com",
                "ns1", "http://www.namespaceA.com",
                "ns2", "http://www.namespaceB.com",
                "ns3", "http://www.namespaceA.com"
        ),
                "http://www.default.com"
        );

        assertEquals("ns4", ctx.getDefaultPrefix().orElseThrow());
        assertEquals("ns4", ctx.getPrefix("http://www.default.com"));
        assertIterableEquals(List.of("ns4"), (Iterable<String>) () -> ctx.getPrefixes("http://www.default.com"));
        assertEquals("http://www.default.com", ctx.getNamespaceURI("ns4"));

        assertEquals("ns", ctx.getPrefix("http://www.namespace.com"));
        assertIterableEquals(List.of("ns"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespace.com"));
        assertIterableEquals(List.of("ns1", "ns3"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespaceA.com"));
        assertIterableEquals(List.of("ns2"), (Iterable<String>) () -> ctx.getPrefixes("http://www.namespaceB.com"));
        assertEquals("http://www.namespace.com", ctx.getNamespaceURI("ns"));
        assertEquals("http://www.namespaceA.com", ctx.getNamespaceURI("ns1"));
        assertEquals("http://www.namespaceB.com", ctx.getNamespaceURI("ns2"));
        assertEquals("http://www.namespaceA.com", ctx.getNamespaceURI("ns3"));
    }

}

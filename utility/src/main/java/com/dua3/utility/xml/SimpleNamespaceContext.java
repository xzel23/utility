package com.dua3.utility.xml;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link NamespaceContext} implementation that automatically creates the reverse mapping from URI to
 * namespace. 
 */
public class SimpleNamespaceContext implements NamespaceContext {
    private final HashMap<String, String> nsToUri;
    private final HashMap<String, List<String>> uriToNs;

    /**
     * Construct new instance with mappings.
     * @param nsToUri mapping from namespace to URI
     */
    public SimpleNamespaceContext(Map<String, String> nsToUri) {
        this.nsToUri = new HashMap<>(nsToUri);

        // create a map reverse mappings
        this.uriToNs = new HashMap<>();
        nsToUri.forEach((k, v) -> uriToNs.computeIfAbsent(v, k_ -> new ArrayList<>()).add(v));
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return nsToUri.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        List<String> namespaces = uriToNs.get(namespaceURI);
        return namespaces == null || namespaces.isEmpty() ? null : namespaces.get(0);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return Collections.unmodifiableList(uriToNs.getOrDefault(namespaceURI, Collections.emptyList())).iterator();
    }
}

package com.dua3.utility.xml;

import com.dua3.cabe.annotations.Nullable;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A simple {@link NamespaceContext} implementation that automatically creates the reverse mapping from URI to
 * namespace. When a default namespace is set via "xmlns=..." it will be associated with the namespace name "ns"
 * if that is not already in use. Otherwise, the next free namespace name of the form "ns#number" is used.
 * Whether or not a default namespace has been set and what name it uses can be queried via {@link #getDefaultNs()}.
 */
public class SimpleNamespaceContext implements NamespaceContext {
    private final HashMap<String, String> nsToUri;
    private final HashMap<String, List<String>> uriToNs;
    private final String defaultNs;

    /**
     * Construct new instance with mappings.
     *
     * @param nsToUri    mapping from namespace to URI
     */
    public SimpleNamespaceContext(Map<String, String> nsToUri) {
        this(nsToUri, null);
    }
    
    /**
     * Construct new instance with mappings and default namespace.
     *
     * @param defaultUri URI for the default namespace
     */
    public SimpleNamespaceContext(@Nullable String defaultUri) {
        this(Collections.emptyMap(), defaultUri);       
    }
    
    /**
     * Construct new instance with mappings and default namespace.
     *
     * @param nsToUri    mapping from namespace to URI
     * @param defaultUri URI for the default namespace
     */
    public SimpleNamespaceContext(Map<String, String> nsToUri, @Nullable String defaultUri) {
        this.nsToUri = new HashMap<>(nsToUri);
        
        // determine a namespace name for the default namespace
        if (defaultUri != null) {
            String ns = "ns";
            for (int i = 1; nsToUri.containsKey(ns); i++) {
                ns = "ns" + i;
            }
            this.defaultNs = ns;
            nsToUri.put(defaultNs, defaultUri);
        } else {
            this.defaultNs = null;
        }

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

    /**
     * Get default namespace.
     * @return the default namespace name (optional)
     */
    public Optional<String> getDefaultNs() {
        return Optional.ofNullable(defaultNs);
    }
}

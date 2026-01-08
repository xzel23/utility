package com.dua3.utility.logging;

import com.dua3.utility.text.SharableString;
import com.dua3.utility.text.SharedString;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LevelMap is a hierarchical mapping structure used for associating log levels
 * with logger names. It supports efficient lookups and updates of log levels at
 * various levels in the logger hierarchy.
 * <p>
 * Each logger name is represented as a hierarchical structure, where parts of
 * the name separated by dots (".") are treated as levels.
 * This allows for retrieval of the most specific log level associated with a given logger name.
 */
class LevelMap {
    // The root level handles the "empty" or "root" logger
    private final Node root;

    /**
     * Constructs a new instance of {@code LevelMap} with the specified root log level.
     * This initializes the root node of the map with the given {@code rootLevel}.
     * The class is used to manage log levels hierarchically across various loggers.
     *
     * @param rootLevel the {@code LogLevel} to be assigned to the root node of the map.
     */
    public LevelMap(LogLevel rootLevel) {
        root = new Node();
        root.level = rootLevel;
    }

    /**
     * Retrieves the root node of the hierarchical structure managed by this instance of {@code LevelMap}.
     *
     * @return the root {@code Node} of the {@code LevelMap}.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Retrieves a map of logger names/name prefixes to their associated log levels.
     * <p>
     * This method collects all log level rules defined hierarchically within the
     * underlying structure, starting from the root node. The logger names are
     * recorded as keys in the map, and their respective log levels are the values.
     *
     * @return a map containing logger name to log level mappings, where the keys are logger
     *         names and the values are their corresponding {@code LogLevel}.
     */
    public Map<String, LogLevel> rules() {
        Map<String, LogLevel> rules = new java.util.LinkedHashMap<>();
        rules.put("", root.level);
        // traverse the complete tree and add all rules to the map
        traverseAndCollectRules(rules, "", root);
        return rules;
    }

    /**
     * Recursively traverses the tree and collects all logger name to level mappings.
     *
     * @param rules  the map to collect the rules into
     * @param prefix the accumulated logger name prefix up to this node
     * @param node   the current node being traversed
     */
    private void traverseAndCollectRules(Map<String, LogLevel> rules, String prefix, Node node) {
        for (Map.Entry<SharedString, Node> entry : node.children.entrySet()) {
            String loggerName = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey();
            Node childNode = entry.getValue();

            if (childNode.level != null) {
                rules.put(loggerName, childNode.level);
            }

            traverseAndCollectRules(rules, loggerName, childNode);
        }
    }

    /**
     * Represents a single node in a hierarchical data structure, useful for storing relationships
     * between keys and associated log levels. Each node may have child nodes and an optional log level.
     *
     * This class is designed to model a tree structure where each node can house multiple children
     * identified by a {@link SharedString} key. Log levels can be dynamically assigned to nodes for
     * structured logging purposes.
     */
    public static class Node {
        // We use SharedString as the key to allow lookups with your window-view
        final Map<SharedString, Node> children = new ConcurrentHashMap<>();
        volatile @Nullable LogLevel level = null;

        @Override
        public String toString() {
            return appendTo(new StringBuilder()).toString();
        }

        /**
         * Appends the string representation of the current node and its children to the provided StringBuilder.
         * The method includes the node's log level and recursively appends all children in the format:
         * "level , {key -> child, ...}".
         *
         * @param sb the {@code StringBuilder} to which the string representation will be appended
         * @return the same {@code StringBuilder} instance, appended with the string representation of the node
         */
        public StringBuilder appendTo(StringBuilder sb) {
            sb.append(level.name());
            if (!children.isEmpty()) {
                for (Map.Entry<SharedString, Node> e : children.entrySet()) {
                    sb.append(" , {").append(e.getKey()).append(" -> ");
                    e.getValue().appendTo(sb);
                    sb.append("}");
                }
            }
            return sb;
        }
    }

    public void put(String loggerName, LogLevel level) {
        if (loggerName.isEmpty()) {
            root.level = level;
            return;
        }
        if (loggerName.endsWith(".")) {
            throw new IllegalArgumentException("loggerName must not end with '.'");
        }

        Node current = root;
        int start = 0;
        int dot;

        // Populate using SharedString segments
        SharableString name = new SharableString(loggerName);
        while ((dot = name.indexOf('.', start)) != -1) {
            SharedString segment = name.subSequence(start, dot);
            current = current.children.computeIfAbsent(segment, k -> new Node());
            start = dot + 1;
        }
        current.children.computeIfAbsent(name.subSequence(start, name.length()), k -> new Node()).level = level;
    }

    public LogLevel level(String className) {
        Node current = root;
        LogLevel level = root.level;

        int start = 0;
        int dot;

        SharableString name = new SharableString(className);
        while ((dot = name.indexOf('.', start)) != -1) {
            // No new underlying char[] is created here!
            SharedString segment = name.subSequence(start, dot);

            current = current.children.get(segment);
            if (current == null) return level;

            if (current.level != null) {
                level = current.level;
            }
            start = dot + 1;
        }

        // Final segment check
        SharedString lastSegment = name.subSequence(start, className.length());
        current = current.children.get(lastSegment);
        if (current != null && current.level != null) {
            level = current.level;
        }

        return level;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
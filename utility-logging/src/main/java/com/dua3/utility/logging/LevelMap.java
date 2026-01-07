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

    public LevelMap(LogLevel rootLevel) {
        root = new Node();
        root.level = rootLevel;
    }

    private static class Node {
        // We use SharedString as the key to allow lookups with your window-view
        final Map<SharedString, Node> children = new ConcurrentHashMap<>();
        volatile @Nullable LogLevel level = null;
    }

    public void put(String loggerName, LogLevel level) {
        if (loggerName.isEmpty()) {
            root.level = level;
            return;
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
        current.children.computeIfAbsent(name.subSequence(start, loggerName.length()), k -> new Node()).level = level;
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
}
/*
 * Copyright (c) 2026 Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Shared implementation for globbing path-like objects. */
final class Glob<T> {

    private final GlobAdapter<T> adapter;

    Glob(GlobAdapter<T> adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    /**
     * Returns the objects below {@code base} that match {@code pattern}.
     *
     * @param base the search base
     * @param pattern the glob pattern
     * @return a stream of matching objects
     * @throws IOException if the adapter cannot access the underlying storage
     */
    Stream<T> glob(T base, String pattern) throws IOException {
        int firstGlobCharIndex = findFirstGlobChar(pattern, adapter.separator());
        int lastDirectorySeparatorIndex = findLastDirectorySeparator(pattern, firstGlobCharIndex, adapter.separator());

        String fixedPart = lastDirectorySeparatorIndex == -1
                ? ""
                : pattern.substring(0, lastDirectorySeparatorIndex);
        T fixedBase = adapter.resolve().apply(base, fixedPart);

        if (firstGlobCharIndex == pattern.length()) {
            T object = adapter.resolve().apply(base, pattern);
            return adapter.exists().apply(object)
                    ? Stream.of(adapter.normalize().apply(base, object))
                    : Stream.empty();
        }

        String globPart = lastDirectorySeparatorIndex == -1
                ? "/" + pattern
                : pattern.substring(lastDirectorySeparatorIndex);
        Predicate<T> matcher = adapter.matcherFactory().apply(fixedBase, globPart);

        // The caller owns the returned stream and must close it.
        return adapter.walk().apply(fixedBase)
                .filter(matcher)
                .map(object -> adapter.normalize().apply(base, object));
    }

    @SuppressWarnings("java:S127") // accepted
    private static int findFirstGlobChar(String pattern, String separator) {
        if (separator.equals("\\")) {
            // Glob characters cannot be escaped on file systems using backslashes as separators.
            int idx = pattern.indexOf('*');
            idx = firstIndex(idx, pattern.indexOf('?'));
            idx = firstIndex(idx, pattern.indexOf('['));
            idx = firstIndex(idx, pattern.indexOf('{'));
            return idx < 0 ? pattern.length() : idx;
        }

        for (int i = 0; i < pattern.length(); i++) {
            switch (pattern.charAt(i)) {
                case '*', '?', '[', '{' -> {
                    return i;
                }
                case '\\' -> i++; // Skip the escaped character.
                default -> {
                    // nothing to do
                }
            }
        }
        return pattern.length();
    }

    private static int firstIndex(int first, int second) {
        if (first < 0) {
            return second;
        }
        if (second < 0) {
            return first;
        }
        return Math.min(first, second);
    }

    private static int findLastDirectorySeparator(String pattern, int firstGlobCharIndex, String separator) {
        if (separator.equals("\\")) {
            // Glob patterns use forward slashes as separators on these file systems.
            return pattern.lastIndexOf('/', firstGlobCharIndex);
        }
        return pattern.lastIndexOf(separator, firstGlobCharIndex);
    }
}

/** Adapts a path-like type to the generic {@link Glob} algorithm. */
record GlobAdapter<T>(
        String separator,
        LangUtil.BiFunctionThrows<T, String, T, IOException> resolve,
        LangUtil.PredicateThrows<T, IOException> exists,
        LangUtil.FunctionThrows<T, Stream<T>, IOException> walk,
        GlobMatcherFactory<T> matcherFactory,
        BiFunction<T, T, T> normalize
) {
    GlobAdapter {
        Objects.requireNonNull(separator);
        Objects.requireNonNull(resolve);
        Objects.requireNonNull(exists);
        Objects.requireNonNull(walk);
        Objects.requireNonNull(matcherFactory);
        Objects.requireNonNull(normalize);
    }
}

/** Creates a predicate that matches objects below a fixed glob prefix. */
@FunctionalInterface
interface GlobMatcherFactory<T> {

    /**
     * Creates a predicate for the glob suffix below {@code fixedBase}.
     *
     * @param fixedBase the fixed part of the glob
     * @param globPart the glob suffix, including its leading separator
     * @return a predicate for candidate objects
     */
    Predicate<T> apply(T fixedBase, String globPart);
}

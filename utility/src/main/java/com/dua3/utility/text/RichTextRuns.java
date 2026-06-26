package com.dua3.utility.text;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a collection of rich text runs within a text unit.
 * A rich text run consists of a sequence of characters with the same styling or formatting attributes.
 * <p>-
 * This interface provides methods to access and interact with the runs, allowing retrieval of
 * individual runs, access to all runs as a stream, and checking if the collection is empty.
 */
public interface RichTextRuns extends Iterable<Run> {
    /**
     * Retrieves a list of rich text runs.
     * Each run represents a sequence of characters sharing the same styling or formatting attributes.
     *
     * @return a list of {@link Run} objects, where each object represents a block of text with shared attributes
     */
    List<Run> runs();

    /**
     * Provides a {@code Stream} of {@link Run} objects in the collection.
     * This method is useful for processing the runs in a functional programming style
     * or applying bulk operations such as filtering, mapping, or reducing.
     *
     * @return a {@code Stream} of {@link Run} objects representing the rich text runs in the collection
     */
    Stream<Run> runStream();

    /**
     * Checks if the collection of rich text runs is empty.
     *
     * @return true if there are no rich text runs in the collection; false otherwise
     */
    boolean isEmpty();
}

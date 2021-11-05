package com.dua3.utility.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Interface for tree nodes.
 * @param <N> the node type
 */
public interface TreeNode<N extends TreeNode<? extends N>> {

    /**
     * Get node name.
     * @return name of this node
     */
    default @NotNull String name() {
        return toString();
    }

    /**
     * Check if this node is a root node.
     * @return true if this  is a root node
     */
    default boolean isRoot() {
        return parent() == null;
    }

    /**
     * Get this node's parent.
     * @return this node's parent node or {@code null} if this node is a root node
     */
    @Nullable N parent();

    /**
     * Get iterator over this node's children.
     * @return child iterator
     */
    @NotNull Iterable<N> children();

    /**
     * Get stream of this node's children.
     * @return child stream
     */
    @NotNull Stream<N> stream();
}

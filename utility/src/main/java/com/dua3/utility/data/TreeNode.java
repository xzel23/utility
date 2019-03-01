package com.dua3.utility.data;

import java.util.stream.Stream;

public interface TreeNode<N extends TreeNode<? extends N>> {
    default String name() {
        return toString();
    }

    default boolean isRoot() {
        return parent() == null;
    }

    N parent();

    Iterable<N> children();

    Stream<N> stream();
}

package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeNodeTest {

    static class SimpleNode implements TreeNode<SimpleNode> {
        private final String name;
        private final @Nullable SimpleNode parent;
        private final List<SimpleNode> children;

        SimpleNode(String name, @Nullable SimpleNode parent, List<SimpleNode> children) {
            this.name = name;
            this.parent = parent;
            this.children = children;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean isRoot() {
            return parent == null;
        }

        @Override
        public SimpleNode parent() {
            if (parent == null) {
                throw new IllegalStateException("Root node has no parent");
            }
            return parent;
        }

        @Override
        public Iterable<SimpleNode> children() {
            return children;
        }

        @Override
        public Stream<SimpleNode> stream() {
            return children.stream();
        }
    }

    @Test
    void testTreeNode() {
        SimpleNode child1 = new SimpleNode("child1", null, List.of());
        SimpleNode child2 = new SimpleNode("child2", null, List.of());
        SimpleNode root = new SimpleNode("root", null, List.of(child1, child2));

        assertEquals("root", root.name());
        assertTrue(root.isRoot());
        assertThrows(IllegalStateException.class, root::parent);

        assertEquals(List.of(child1, child2), root.stream().toList());

        SimpleNode attachedChild = new SimpleNode("child1", root, List.of());
        assertFalse(attachedChild.isRoot());
        assertEquals(root, attachedChild.parent());
    }
}

package com.dua3.utility.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeNodeTest {

    @TempDir
    Path tempDir;

    @Test
    void testTreeCreation() throws IOException {
        // Create a simple file structure
        Files.createFile(tempDir.resolve("file1.txt"));
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(subDir.resolve("file2.txt"));

        // Create a tree with lazy loading
        FileTreeNode<?> lazyTree = FileTreeNode.tree(tempDir);
        assertTrue(lazyTree.isLazy());
        assertEquals(tempDir, lazyTree.getFilePath());
        assertTrue(lazyTree.isRoot());

        // Create a tree with eager loading
        FileTreeNode<?> eagerTree = FileTreeNode.tree(tempDir, false);
        assertFalse(eagerTree.isLazy());
        assertEquals(tempDir, eagerTree.getFilePath());
        assertTrue(eagerTree.isRoot());
    }

    @Test
    void testChildren() throws IOException {
        // Create a simple file structure
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(subDir.resolve("file2.txt"));

        // Create a tree
        FileTreeNode<?> tree = FileTreeNode.tree(tempDir);

        // Test children collection
        Collection<?> children = tree.children();
        assertEquals(2, children.size());

        // Verify children paths
        boolean foundFile1 = false;
        boolean foundSubDir = false;

        for (Object child : children) {
            FileTreeNode<?> node = (FileTreeNode<?>) child;
            Path path = node.getFilePath();

            if (path.equals(file1)) {
                foundFile1 = true;
                assertTrue(node.isLeaf());
            } else if (path.equals(subDir)) {
                foundSubDir = true;
                assertFalse(node.isLeaf());
            }
        }

        assertTrue(foundFile1, "file1.txt should be in the children");
        assertTrue(foundSubDir, "subdir should be in the children");
    }

    @Test
    void testParent() throws IOException {
        // Create a simple file structure
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file = Files.createFile(subDir.resolve("file.txt"));

        // Create a tree
        FileTreeNode<?> tree = FileTreeNode.tree(tempDir, false);

        // Get the subdir node
        FileTreeNode<?> subDirNode = null;
        for (Object child : tree.children()) {
            FileTreeNode<?> node = (FileTreeNode<?>) child;
            if (node.getFilePath().equals(subDir)) {
                subDirNode = node;
                break;
            }
        }

        assertNotNull(subDirNode, "subdir node should exist");

        // Get the file node
        FileTreeNode<?> fileNode = null;
        for (Object child : subDirNode.children()) {
            FileTreeNode<?> node = (FileTreeNode<?>) child;
            if (node.getFilePath().equals(file)) {
                fileNode = node;
                break;
            }
        }

        assertNotNull(fileNode, "file node should exist");

        // Test parent relationships
        assertFalse(fileNode.isRoot());
        assertEquals(subDirNode, fileNode.parent());

        assertFalse(subDirNode.isRoot());
        assertEquals(tree, subDirNode.parent());

        assertTrue(tree.isRoot());
        assertThrows(IllegalStateException.class, tree::parent);
    }

    @Test
    void testRefresh() throws IOException {
        // Create initial file structure
        FileTreeNode<?> tree = FileTreeNode.tree(tempDir);

        // Initial state - no children
        assertEquals(0, tree.children().size());

        // Add a file and refresh
        Path file = Files.createFile(tempDir.resolve("newfile.txt"));
        tree.refresh();

        // Verify the new file is now in the children
        assertEquals(1, tree.children().size());
        FileTreeNode<?> fileNode = (FileTreeNode<?>) tree.children().iterator().next();
        assertEquals(file, fileNode.getFilePath());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testRefreshListeners() {
        // Create a tree
        FileTreeNode tree = FileTreeNode.tree(tempDir);

        // Create a listener to track refresh calls
        List<FileTreeNode> refreshedNodes = new ArrayList<>();
        Consumer<FileTreeNode> listener = refreshedNodes::add;

        // Add the listener
        tree.addRefreshListener(listener);

        // Trigger a refresh
        tree.refresh();

        // Verify the listener was called
        assertEquals(1, refreshedNodes.size());
        assertEquals(tree, refreshedNodes.getFirst());

        // Remove the listener
        tree.removeRefreshListener(listener);
        refreshedNodes.clear();

        // Trigger another refresh
        tree.refresh();

        // Verify the listener was not called
        assertTrue(refreshedNodes.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() throws IOException {
        // Create two trees with the same path
        FileTreeNode<?> tree1 = FileTreeNode.tree(tempDir);
        FileTreeNode<?> tree2 = FileTreeNode.tree(tempDir);

        // Create a tree with a different path
        Path otherDir = Files.createDirectory(tempDir.resolve("otherdir"));
        FileTreeNode<?> tree3 = FileTreeNode.tree(otherDir);

        // Test equals
        assertEquals(tree1, tree2);
        assertNotEquals(tree1, tree3);

        // Test hashCode
        assertEquals(tree1.hashCode(), tree2.hashCode());
        assertNotEquals(tree1.hashCode(), tree3.hashCode());
    }

    @Test
    void testToString() {
        // Create a tree
        FileTreeNode<?> tree = FileTreeNode.tree(tempDir);

        // Test toString
        assertEquals(tempDir.getFileName().toString(), tree.toString());
    }

    @Test
    void testStream() throws IOException {
        // Create a simple file structure
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));

        // Create a tree
        FileTreeNode<?> tree = FileTreeNode.tree(tempDir);

        // Test stream
        long count = tree.stream().count();
        assertEquals(2, count);
    }
}

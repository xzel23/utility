package com.dua3.utility.data;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a node in a file tree.
 * @param <T> the type of the node
 */
@SuppressWarnings("unchecked")
public class FileTreeNode<T extends FileTreeNode<T>> implements TreeNode<T> {

    private final @Nullable T parent;
    private final Path path;
    private final boolean lazy;
    private final List<Consumer<T>> refreshListeners = new ArrayList<>();
    private @Nullable List<T> children;

    /**
     * Constructor for a node in a file tree.
     * @param parent the parent node or {@code null} for a root node
     * @param path the path of the file or directory represented by this node
     * @param lazy if the child nodes should be lazily added to improve performance for collapsed subtrees
     */
    protected FileTreeNode(@Nullable T parent, Path path, boolean lazy) {
        this.parent = parent;
        this.path = path;
        this.lazy = lazy;
    }

    /**
     * Create a lazily populated FileTree with the given path as its root.
     *
     * @param root the tree root
     * @param <T>  the node type
     * @return the tree
     */
    public static <T extends FileTreeNode<T>> T tree(Path root) {
        return tree(root, true);
    }

    /**
     * Create FileTree with the given path as its root.
     *
     * @param root the tree root
     * @param lazy if true, nodes are not populated before refresh is called
     * @param <T>  the node type
     * @return the tree
     */
    public static <T extends FileTreeNode<T>> T tree(Path root, boolean lazy) {
        T t = (T) new FileTreeNode<T>(null, root, lazy);
        if (!lazy) {
            t.refresh();
        }
        return t;
    }

    @Override
    public Collection<T> children() {
        if (children == null) {
            refresh();
        }
        return Collections.unmodifiableList(children);
    }

    /**
     * Refreshes the FileTree by invoking the refresh listeners of all its children.
     *
     * @throws UncheckedIOException if an error occurs while refreshing the FileTree
     */
    public void refresh() {
        try {
            this.children = new ArrayList<>(collectChildren());
            refreshListeners.forEach(lst -> lst.accept((T) this));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Collects the children of the current node.
     *
     * @return a collection of child nodes
     * @throws IOException if an I/O error occurs while collecting the children
     */
    protected Collection<T> collectChildren() throws IOException {
        try (Stream<Path> stream = Files.walk(path, 1)) {
            return stream
                    .filter(p -> !p.equals(path))
                    .map(p -> {
                        T child = (T) new FileTreeNode<>((T) this, p, lazy);
                        if (!lazy) {
                            child.refresh();
                        }
                        return child;
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Stream<T> stream() {
        return children().stream();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public T parent() {
        LangUtil.check(parent != null, "parent() called on root node");
        return parent;
    }

    /**
     * Returns the file path associated with the FileTree.
     *
     * @return the file path associated with the FileTree
     */
    public Path getFilePath() {
        return path;
    }

    @Override
    public String toString() {
        return Objects.toString(path.getFileName(), "");
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other != null && getClass() == other.getClass() && path.equals(((FileTreeNode<T>) other).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Returns a boolean value indicating if the lazy loading mode is enabled or not.
     *
     * @return true if the lazy loading mode is enabled, false otherwise
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Returns a boolean value indicating whether this node is a leaf node.
     *
     * @return {@code true} if this node is a leaf, {@code false} otherwise
     */
    public boolean isLeaf() {
        return children().isEmpty();
    }

    /**
     * Adds a refresh listener to be notified when the FileTree is refreshed.
     *
     * @param refreshListener the refresh listener to be added
     * @throws NullPointerException if the refreshListener is null
     */
    public void addRefreshListener(Consumer<T> refreshListener) {
        refreshListeners.add(refreshListener);
    }

    /**
     * Removes the specified refresh listener from the list of refresh listeners.
     *
     * @param refreshListener the refresh listener to be removed
     */
    public void removeRefreshListener(Consumer<T> refreshListener) {
        refreshListeners.remove(refreshListener);
    }

}

package com.dua3.utility.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class FileTreeNode<T extends FileTreeNode<T>> implements TreeNode<T> {
    
    /**
     * Create a lazily populated FileTree with the given path as its root. 
     * @param root the tree root
     * @param <T> the node type
     * @return the tree
     */
    public static <T extends FileTreeNode<T>> T tree(Path root) {
        return tree(root, true);
    }

    /**
     * Create FileTree with the given path as its root. 
     * @param root the tree root
     * @param lazy if true, nodes are not populated before refresh is called
     * @param <T> the node type
     * @return the tree
     */
    public static <T extends FileTreeNode<T>> T tree(Path root, boolean lazy) {
        T t = (T) new FileTreeNode<T>(null, root, lazy);
        if (!lazy) {
            t.refresh();
        }
        return t;
    }

    private final T parent;
    private final Path path;
    private final boolean lazy;
    private List<T> children = null;

    private final List<Consumer<T>> listeners = new ArrayList<>();

    protected FileTreeNode(T parent, Path path, boolean lazy) {
        this.parent = parent;
        this.path = Objects.requireNonNull(path);
        this.lazy = lazy;
    }

    @Override
    public Collection<T> children() {
        if (children == null) {
            refresh();
        }
        return Collections.unmodifiableList(children);
    }

    public void refresh() {
        try {
            this.children = new ArrayList<>(collectChildren());
            listeners.forEach(lst -> lst.accept((T) this));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected Collection<T> collectChildren() throws IOException {
        return Files.walk(path, 1)
                .filter(p -> !p.equals(path))
                .map(p -> { 
                    T child = (T) new FileTreeNode<T>((T) this, p, lazy); 
                    if (!lazy) { 
                        child.refresh(); 
                    } 
                    return child; 
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Stream<T> stream() {
        return children().stream();
    }

    @Override
    public T parent() {
        return parent;
    }

    public Path getFilePath() {
        return path;
    }

    @Override
    public String toString() {
        return Objects.toString(path.getFileName(), "");
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && path.equals(((FileTreeNode<T>) other).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public boolean isLazy() {
        return lazy;
    }
    
    public boolean isLeaf() {
        return children().isEmpty();
    }

    public void addRefreshListener(Consumer<T> listener) {
        this.listeners.add(Objects.requireNonNull(listener));
    }

    public void removeRefreshListener(Consumer<T> listener) {
        this.listeners.remove(listener);
    }

}

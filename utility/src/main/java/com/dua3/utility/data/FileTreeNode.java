package com.dua3.utility.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileTreeNode implements TreeNode<FileTreeNode> {

    public static FileTreeNode tree(Path root) {
        return new FileTreeNode(null, root, true);
    }

    private final FileTreeNode parent;
    private final Path path;
    private List<FileTreeNode> children = null;
    private final boolean lazy;

    protected FileTreeNode(FileTreeNode parent, Path path) {
        this(parent, path, false);
    }

    protected FileTreeNode(FileTreeNode parent, Path path, boolean lazy) {
        this.parent = parent;
        this.path = Objects.requireNonNull(path);
        this.lazy = lazy;
        if (!lazy) {
            refresh();
        }
    }

    @Override
    public Collection<FileTreeNode> children() {
        if (children == null) {
            refresh();
        }
        return Collections.unmodifiableList(children);
    }

    public void refresh() {
        try {
            this.children = Files.walk(path, 1)
                    .filter(p -> !p.equals(path))
                    .map(p -> new FileTreeNode(this, p, lazy))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Stream<FileTreeNode> stream() {
        return children().stream();
    }

    @Override
    public FileTreeNode parent() {
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
        return other != null && getClass() == other.getClass() && path.equals(((FileTreeNode) other).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}

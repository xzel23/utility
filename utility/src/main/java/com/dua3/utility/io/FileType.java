/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.io;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class representing files of a certain type.
 *
 * @param <T> the type corresponding to the data contained in files of this {@link FileType} instance.
 */
public abstract class FileType<T> implements Comparable<FileType<?>> {

    /**
     * Set of defined file types.
     */
    private static final Set<FileType<?>> FILE_TYPES = new HashSet<>();

    // Load FileType  implementations
    static {
        ServiceLoader.load(FileType.class).forEach(FileType::init);
    }

    private final String name;
    private final Class<? extends T> cls;
    private final OpenMode mode;
    private final List<String> extensions; // unmodifiable!

    /**
     * Constructor.
     *
     * @param name       the file type name
     * @param mode       the {@link OpenMode} supported by files of this file type
     * @param cls        the implementing class
     * @param extensions list of extensions used by files of this file type (i.e. "txt", "xls")
     */
    protected FileType(String name, OpenMode mode, Class<? extends T> cls, String... extensions) {
        this.name = name;
        this.mode = mode;
        this.cls = cls;
        this.extensions = List.of(extensions);
    }

    /**
     * Add file type to set of available file types.
     *
     * @param ft  the type to add
     * @param <T> the file type's document type
     */
    protected static <T> void addType(FileType<T> ft) {
        FILE_TYPES.add(ft);
    }

    /**
     * Get unmodifiable Collection of registered file types. The returned collection will be updated when new file
     * types are registered.
     *
     * @return collection of the registered file types
     */
    public static Collection<FileType<?>> fileTypes() {
        return Collections.unmodifiableSet(FILE_TYPES);
    }

    /**
     * Query file type by extension.
     *
     * @param ext the extension (case-sensitive)
     * @return an {@link Optional} holding the file type or an empty {@link Optional} if no matching file type was found
     */
    public static Optional<FileType<?>> forExtension(String ext) {
        for (FileType<?> t : FILE_TYPES) {
            if (!t.isCompound() && t.extensions.contains(ext)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Query file type by URI.
     *
     * @param uri the URI
     * @return an {@link Optional} holding the file type or an empty {@link Optional} if no matching file type was found
     */
    public static Optional<FileType<?>> forUri(URI uri) {
        return forExtension(IoUtil.getExtension(uri));
    }

    /**
     * Query file type by URI and class. This method for determining the correct file type to use when data of the given
     * class has to be written. For example when several file types support a given extension, the implementation
     * matching the class must be chosen.
     *
     * @param uri the URI
     * @param cls the class
     * @param <T> the generic type of this filetype's data
     * @return an {@link Optional} holding the file type or an empty {@link Optional} if no matching file type was found
     */
    public static <T> Optional<FileType<T>> forUri(URI uri, Class<T> cls) {
        return forFileName(uri.getSchemeSpecificPart(), cls);
    }

    /**
     * Query file type by path and class. This method for determining the correct file type to use when data of the given
     * class has to be written. For example when several file types support a given extension, the implementation
     * matching the class must be chosen.
     *
     * @param path the path
     * @param cls  the class
     * @param <T>  the generic type of this filetype's data
     * @return an {@link Optional} holding the file type or an empty {@link Optional} if no matching file type was found
     */
    public static <T> Optional<FileType<T>> forPath(Path path, Class<T> cls) {
        return forFileName(String.valueOf(path.getFileName()), cls);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<FileType<T>> forFileName(String fileName, Class<T> cls) {
        for (FileType<?> t : FILE_TYPES) {
            if (t.matches(fileName) && cls.isAssignableFrom(t.getDocumentClass())) {
                return Optional.of((FileType<T>) t);
            }
        }
        return Optional.empty();
    }

    /**
     * Read data. This method determines the file type according to URI and class and then reads an object from
     * the given URI.
     *
     * @param uri the URI to read from
     * @param cls the class
     * @param <T> the generic class parameter
     * @return an {@link Optional} holding the data read or an empty {@link Optional} if the file type could not be
     * determined
     * @throws IOException if the file type could be determined but an error occurred while reading
     */
    public static <T> Optional<T> read(URI uri, Class<T> cls) throws IOException {
        return read(uri, cls, t -> Arguments.empty());
    }

    /**
     * Read data. This method determines the file type according to URI and class and then reads an object from
     * the given URI.
     *
     * @param uri     the URI to read from
     * @param cls     the class
     * @param options the options to use
     * @param <T>     the generic class parameter
     * @return an {@link Optional} holding the data read or an empty {@link Optional} if the file type could not be
     * determined
     * @throws IOException if the file type could be determined but an error occurred while reading
     */
    public static <T> Optional<T> read(URI uri, Class<T> cls, Function<FileType<? extends T>, Arguments> options) throws IOException {
        Optional<com.dua3.utility.io.FileType<T>> type = forUri(uri, cls);
        return type.isPresent() ? Optional.of(type.get().read(uri, options)) : Optional.empty();
    }

    /**
     * Read data. This method determines the file type according to URI and class and then reads an object from
     * the given URI.
     *
     * @param path the path to read from
     * @param cls  the class
     * @param <T>  the generic class parameter
     * @return an {@link Optional} holding the data read or an empty {@link Optional} if the file type could not be
     * determined
     * @throws IOException if the file type could be determined but an error occurred while reading
     */
    public static <T> Optional<T> read(Path path, Class<T> cls) throws IOException {
        return read(path, cls, t -> Arguments.empty());
    }

    /**
     * Read data. This method determines the file type according to URI and class and then reads an object from
     * the given URI.
     *
     * @param path the path to read from
     * @param cls  the class
     * @param options the options to use
     * @param <T>  the generic class parameter
     * @return an {@link Optional} holding the data read or an empty {@link Optional} if the file type could not be
     * determined
     * @throws IOException if the file type could be determined but an error occurred while reading
     */
    public static <T> Optional<T> read(Path path, Class<T> cls, Function<FileType<? extends T>, Arguments> options) throws IOException {
        Optional<com.dua3.utility.io.FileType<T>> type = forPath(path, cls);
        return type.isPresent() ? Optional.of(type.get().read(path, options)) : Optional.empty();
    }

    /**
     * Get file types supporting mode.
     *
     * @param mode the mode
     * @return the list of file types supporting the requested mode
     */
    public static List<FileType<?>> getFileTypes(OpenMode mode) {
        List<FileType<?>> list = new ArrayList<>(FILE_TYPES);
        list.removeIf(t -> (t.mode.n & mode.n) != mode.n);
        return list;
    }

    /**
     * Get list of file types for a given class.
     *
     * @param mode the mode requested
     * @param cls  the class
     * @param <T>  the class' type
     * @return list of file types that support reading/writing objects of the given class type
     */
    @SuppressWarnings("unchecked")
    public static <T> List<FileType<T>> getFileTypes(OpenMode mode, Class<T> cls) {
        return FILE_TYPES.stream()
                .filter(t -> t.isSupported(mode))
                /* either reading is not requested or files of this type must be assignable to cls */
                .filter(t -> !mode.includes(OpenMode.READ) || cls.isAssignableFrom(t.getDocumentClass()))
                /* either writing is not requested or the document must be assignable to this type's document type */
                .filter(t -> !mode.includes(OpenMode.WRITE) || t.getDocumentClass().isAssignableFrom(cls))
                /* add the generic parameter */
                .map(t -> (FileType<T>) t)
                /* make it a list */
                .collect(Collectors.toList());
    }

    /**
     * Initialise and add this type to the set of available file types.
     */
    protected void init() {
        addType(this);
    }

    /**
     * Get name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get type of documents for this file type.
     *
     * @return the document type
     */
    public Class<? extends T> getDocumentClass() {
        return cls;
    }

    /**
     * Get list of file extensions.
     *
     * @return the list of file extensions for this file type
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Check if mode is supported.
     *
     * @param mode the mode to test
     * @return true, if mode is supported by this file type
     */
    public boolean isSupported(OpenMode mode) {
        return (this.mode.n & mode.n) == mode.n;
    }

    /**
     * Check if a filename matches this type.
     *
     * @param filename the filename
     * @return true, if the filename matches this type's file extension
     */
    public boolean matches(String filename) {
        String ext1 = IoUtil.getExtension(filename);
        return extensions.stream().anyMatch(ext2 -> ext2.equals(ext1));
    }

    /**
     * Read document from file.
     *
     * @param uri the URI to read from
     * @return the document
     * @throws IOException if an error occurs
     */
    public T read(URI uri) throws IOException {
        return read(uri, t -> Arguments.empty());
    }

    /**
     * Read document from file.
     *
     * @param uri     the URI to read from
     * @param options the options to use
     * @return the document
     * @throws IOException if an error occurs
     */
    @SuppressWarnings("RedundantThrows")
    public abstract T read(URI uri, Function<FileType<? extends T>, Arguments> options) throws IOException;

    /**
     * Read document from file.
     *
     * @param path the Path to read from
     * @return the document
     * @throws IOException if an error occurs
     */
    public T read(Path path) throws IOException {
        return read(path, t -> Arguments.empty());
    }

    /**
     * Read document from file.
     *
     * @param path    the Path to read from
     * @param options the options to use
     * @return the document
     * @throws IOException if an error occurs
     */
    public T read(Path path, Function<FileType<? extends T>, Arguments> options) throws IOException {
        return read(path.toUri(), options);
    }

    /**
     * Write document to file.
     *
     * @param uri      the URI to write to
     * @param document the document to write
     * @throws IOException if an error occurs
     */
    public void write(URI uri, T document) throws IOException {
        write(uri, document, t -> Arguments.empty());
    }

    /**
     * Write document to file.
     *
     * @param uri      the URI to write to
     * @param document the document to write
     * @param options  the options to use
     * @throws IOException if an error occurs
     */
    @SuppressWarnings("RedundantThrows")
    public abstract void write(URI uri, T document, Function<FileType<? super T>, Arguments> options) throws IOException;

    /**
     * Write document to file.
     *
     * @param path     the Path to write to
     * @param document the document to write
     * @throws IOException if an error occurs
     */
    public void write(Path path, T document) throws IOException {
        write(path, document, t -> Arguments.empty());
    }

    /**
     * Write document to file.
     *
     * @param path     the Path to write to
     * @param document the document to write
     * @param options  the options to use
     * @throws IOException if an error occurs
     */
    public void write(Path path, T document, Function<FileType<? super T>, Arguments> options) throws IOException {
        write(path.toUri(), document, options);
    }

    /**
     * Whether this is a compound file type (a wrapper for different filetypes having common properties).
     * <p>
     * <strong>NOTE:</strong> Compound file types are excluded when looking up a file type by supported extensions.
     * <p>
     * Some file types are used in dialogs to group together different other types that have common properties, i.e.
     * "All supported files" of an application. These compound file types can be useful to offer a better user experience
     * but are not suitable for determining in what file format data should be written (an example being "Excel files"
     * which refers to both XLS and XLSX files).
     *
     * @return if this file type is a compound file type
     */
    @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
    public boolean isCompound() {
        return false;
    }

    /**
     * Get optional settings for this file type
     *
     * @return optional settings for file type
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Collection<Option<?>> getSettings() {
        return Collections.emptyList();
    }

    // all instances of the same class are considered equal
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && obj.getClass() == getClass();
    }

    // consistent with equals()
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public int compareTo(FileType o) {
        if (o == this) {
            return 0;
        }

        return getName().compareTo(o.getName());
    }

}

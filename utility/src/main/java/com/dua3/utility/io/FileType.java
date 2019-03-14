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

import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A class representing files of a certain type.
 */
public abstract class FileType<T> implements Comparable<FileType> {

    private static final Set<FileType<?>> types = new HashSet<>();

    /**
     * Add file type to set of available file types.
     * @param ft
     *  the type to add
     * @param <T>
     *  the file type's document type
     */
    protected static final <T> void addType(FileType<T> ft) {
        types.add(ft);
    }

    /**
     * Initialise and add this type to the set of available file types.
     */
    protected void init() {
        addType(this);
    }

    // Load FileType  impelemantations
    static {
        ServiceLoader.load(FileType.class).forEach(FileType::init);
    }

    public static Collection<FileType> filetypes() {
        return Collections.unmodifiableSet(types);
    }

    public static Optional<FileType<?>> forPath(Path p) {
        String ext = IOUtil.getExtension(p);
        for (FileType t: types) {
            if (t.extensions.contains(ext)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<FileType<T>> forPath(Path p, Class<T> cls) {
        for (FileType t: types) {
            if (t.matches(p.toString()) && cls.isAssignableFrom(t.getDocumentClass())) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    private final String name;
    private final Class<T> cls;
    private final OpenMode mode;
    private final List<String> extensions; // unmodifiable!

    public FileType(String name, OpenMode mode, Class<T> cls, String... extensions) {
        this.name = name;
        this.mode = mode;
        this.cls = cls;
        this.extensions = List.of(extensions);
    }

    /**
     * Get name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get type of documents for this file type.
     * @return
     *  the document type
     */
    public Class<T> getDocumentClass() {
        return cls;
    }

    /**
     * Get list of file extensions.
     * @return
     *  the list of file extensions for this file type
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Check if mode is supported.
     * @param mode
     *  the mode to test
     * @return
     *  true, if mode is supported by this file type
     */
    public boolean isSupported(OpenMode mode) {
        return (this.mode.n&mode.n) == mode.n;
    }

    /**
     * Check if a filename matches this type.
     * @param filename
     *  the filename
     * @return
     *  true, if the filename matches this type's file extension
     */
    public boolean matches(String filename) {
        String ext1 = IOUtil.getExtension(filename);
        for (String ext2: extensions) {
            if (ext2.equals(ext1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read document from file.
     * @param path
     *  the path to read from
     * @return
     *  the document
     * @throws IOException
     *  if an error occurs
     */
    public T read(Path path) throws IOException {
        return read(path, t -> OptionValues.empty());
    }

    /**
     * Read document from file.
     * @param path
     *  the path to read from
     * @param options
     *  the options to use
     * @return
     *  the document
     * @throws IOException
     *  if an error occurs
     */
    public abstract T read(Path path, Function<FileType, OptionValues> options) throws IOException;

    /**
     * Write document to file.
     * @param path
     *  the path to write to
     * @param document
     *  the document to write
     * @throws IOException
     *  if an error occurs
     */
    public void write(Path path, T document) throws IOException {
        write(path, document, t -> OptionValues.empty());
    }

    /**
     * Write document to file.
     * @param path
     *  the path to write to
     * @param document
     *  the document to write
     * @param options
     *  the options to use
     * @throws IOException
     *  if an error occurs
     */
    public abstract void write(Path path, T document, Function<FileType,OptionValues> options) throws IOException;

    /**
     * Get file types supporting mode.
     * @param mode
     *  the mode
     * @return
     *  the list of file types supporting the requested mode
     */
	public static List<FileType<?>> getFileTypes(OpenMode mode) {
        List<FileType<?>> list = new LinkedList<>(types);
        list.removeIf(t -> (t.mode.n&mode.n)!=mode.n);
		return list;
	}

    /**
     * Get list of file types for a given class.
     * @param mode
     *  the mode requested
     * @param cls
     *  the class
     * @param <T>
     *  the class' type
     * @return
     *  list of file types that support reading/writing objects of the given class type
     */
    public static <T> List<FileType<T>> getFileTypes(OpenMode mode, Class<T> cls) {
        return types.stream()
                .filter(t -> t.isSupported(mode))
                // either reading is not requested or files of this type must be asignable to cls
                .filter(t -> !mode.includes(OpenMode.READ) || cls.isAssignableFrom(t.getDocumentClass()))
                // either writing is not requested or the document must be assignable to this type's document type
                .filter(t -> !mode.includes(OpenMode.WRITE)  || t.getDocumentClass().isAssignableFrom(cls))
                // add the generic parameter
                .map(t -> (FileType<T>)t)
                // make it a list
                .collect(Collectors.toList());
    }

    /**
     * Get optional settings for this file type
     * @return
     *  optional settings for file type
     */
    public OptionSet getSettings() {
        return new OptionSet(Collections.emptyList());
    }

    // all instances of the same class are considered equal
    @Override
    public boolean equals(Object obj) {
        return obj!=null && obj.getClass()==getClass();
    }

    // consistent with equals()
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public int compareTo(FileType o) {
        if (o==this) {
            return 0;
        }

        if (o==null) {
            return 1;
        }
        
        return getName().compareTo(o.getName());
    }
}

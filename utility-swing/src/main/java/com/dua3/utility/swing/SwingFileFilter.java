/*
 *
 */
package com.dua3.utility.swing;

import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A FileFilter class to be used as a drop-in file filter for dialogs.
 * <p>
 * This class can be used in all cases where one of the three standard Java file
 * filter implementations is required, namely:
 * <ul>
 * <li>{@link java.io.FileFilter java.io.FileFilter}</li>
 * <li>{@link java.io.FilenameFilter java.io.FilenameFilter}</li>
 * <li>{@link javax.swing.filechooser.FileFilter
 * javax.swing.filechooser.FileFilter}</li>
 * </ul>
 *
 * @param <T> the generic parameter for {@link FileType}
 */
public final class SwingFileFilter<T> extends javax.swing.filechooser.FileFilter
        implements java.io.FileFilter, java.io.FilenameFilter {
    private final FileType<T> fileType;

    private SwingFileFilter(FileType<T> fileType) {
        this.fileType = fileType;
    }

    /**
     * Returns a list of SwingFileFilters based on the given OpenMode and class.
     *
     * @param <T> the generic parameter for {@link FileType}
     * @param mode the OpenMode to determine the filters for
     * @param cls the class type to filter
     * @return a list of SwingFileFilters with the specified mode and class
     */
    public static <T> List<SwingFileFilter<T>> getFilters(OpenMode mode, Class<T> cls) {
        List<FileType<T>> fileTypes = FileType.getFileTypes(mode, cls);
        List<SwingFileFilter<T>> filters = new ArrayList<>(fileTypes.size());
        for (FileType<T> ft : fileTypes) {
            filters.add(new SwingFileFilter<>(ft));
        }
        return filters;
    }

    @Override
    public boolean accept(File pathname) {
        return fileType.matches(pathname.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
        return fileType.matches(name);
    }

    @Override
    public String getDescription() {
        return fileType.getName();
    }

    /**
     * Returns the FileType associated with this instance.
     *
     * @return the FileType associated with this instance
     */
    public FileType<T> getFileType() {
        return fileType;
    }
}

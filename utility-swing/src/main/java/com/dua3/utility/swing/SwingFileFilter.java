/*
 *
 */
package com.dua3.utility.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;

/**
 * A FileFilter class to be used as a drop-in file filter for dialogs.
 *
 * This class can be used in all cases where one of the three standard Java file
 * filter implementations is required, namely:
 * <ul>
 * <li>{@link java.io.FileFilter java.io.FileFilter}</li>
 * <li>{@link java.io.FilenameFilter java.io.FilenameFilter}</li>
 * <li>{@link javax.swing.filechooser.FileFilter
 * javax.swing.filechooser.FileFilter}</li>
 * </ul>
 */
public class SwingFileFilter<T> extends javax.swing.filechooser.FileFilter
        implements java.io.FileFilter, java.io.FilenameFilter {
    public static <T> List<SwingFileFilter<T>> getFilters(OpenMode mode, Class<T> cls) {
        List<FileType<T>> fileTypes = FileType.getFileTypes(mode, cls);
        List<SwingFileFilter<T>> filters = new ArrayList<>(fileTypes.size());
        for (FileType<T> ft : fileTypes) {
            filters.add(new SwingFileFilter<>(ft));
        }
        return filters;
    }

    private final FileType<T> fileType;

    private SwingFileFilter(FileType<T> fileType) {
        this.fileType = fileType;
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

    public FileType<T> getFileType() {
        return fileType;
    }
}

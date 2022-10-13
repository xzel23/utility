package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A Class helping with creation of zip files.
 */
public class Zip implements AutoCloseable, Flushable {

    private static final int BUFFER_SIZE = 8 * 1024;
    private final ZipOutputStream zout;
    private String path = "";

    /**
     * Create new Zip instance.
     * @param out the {@link OutputStream} to write the zip data to
     */
    public Zip(OutputStream out) {
        this.zout = new ZipOutputStream(out);
    }

    /**
     * Add file to zip.
     * <p>
     * The file will be placed under the last created directory.
     * @param filename  the filename
     * @param data  the data
     * @throws IOException on error
     */
    public void add(String filename, byte[] data) throws IOException {
        addFileEntry(filename);
        zout.write(data);
    }

    /**
     * Add file to zip.
     * <p>
     * The file will be placed under the last created directory.
     * @param filename  the filename
     * @param in  the {@link InputStream} to read the data from
     * @throws IOException on error
     */
    public void add(String filename, InputStream in) throws IOException {
        addFileEntry(filename);
        byte[] buffer = new byte[BUFFER_SIZE];

        int n;
        while ((n = in.read(buffer)) >= 0) {
            zout.write(buffer, 0, n);
        }
    }

    private void addFileEntry(String filename) throws IOException {
        LangUtil.check(!filename.isEmpty(), "the filename must not be empty");
        LangUtil.check(filename.indexOf('/') == -1, "the filename must not contain any '/'");
        ZipEntry entry = new ZipEntry(path + filename);
        zout.putNextEntry(entry);
    }

    /**
     * Add directory to zip.
     * <p>
     * The directory will be placed under the last created directory unless it starts with '/' (denotes the zip's root).
     * @param dirname  the directory name
     * @throws IOException on error
     */
    public void directory(String dirname) throws IOException {
        LangUtil.check(!dirname.isEmpty(), "directory name must not be empty");

        // append '/' if needed
        dirname = dirname.endsWith("/") ? dirname : dirname + "/";

        // handle '/' at beginning of name
        if (dirname.startsWith("/")) {
            dirname = dirname.substring(1);
        } else {
            dirname = path + dirname;
        }

        // write directory entry
        zout.putNextEntry(new ZipEntry(dirname));
        zout.closeEntry();

        // update path
        path = dirname;
    }

    @Override
    public void close() throws IOException {
        zout.close();
    }

    @Override
    public void flush() throws IOException {
        zout.flush();
    }
}

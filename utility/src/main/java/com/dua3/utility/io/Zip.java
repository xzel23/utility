package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A Class helping with the creation of zip files.
 * This class is mainly for creating zip data on the fly.
 * For zipping the contents of a directory, use {@link IoUtil#zip(Path, Path)} instead.
 */
@SuppressWarnings("MagicCharacter")
public class Zip implements AutoCloseable, Flushable {

    private final ZipOutputStream zout;
    private String path = "";

    /**
     * Create a new Zip instance.
     *
     * @param out the {@link OutputStream} to write the zip data to
     */
    public Zip(OutputStream out) {
        this.zout = new ZipOutputStream(out);
    }

    /**
     * Add file to zip.
     * <p>
     * The file will be placed under the last created directory.
     *
     * @param filename the filename
     * @param data     the data
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
     *
     * @param filename the filename
     * @param in       the {@link InputStream} to read the data from
     * @throws IOException on error
     */
    public void add(String filename, InputStream in) throws IOException {
        addFileEntry(filename);
        in.transferTo(zout);
    }

    private void addFileEntry(String filename) throws IOException {
        LangUtil.checkArg(!filename.isEmpty(), () -> "filename must not be empty");
        LangUtil.checkArg(filename.indexOf('/') == -1, () -> "the filename must not contain any '/'");
        ZipEntry entry = new ZipEntry(path + filename);
        zout.putNextEntry(entry);
    }

    /**
     * Add directory to zip.
     * <p>
     * The directory will be placed under the last created directory unless it starts with '/' (denotes the zip's root).
     *
     * @param dirname the directory name
     * @throws IOException on error
     */
    public void directory(String dirname) throws IOException {
        LangUtil.checkArg(!dirname.isEmpty(), () -> "directory name must not be empty");

        // append '/' if needed
        dirname = dirname.endsWith("/") ? dirname : dirname + "/";

        // handle '/' at the beginning of name
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

package com.dua3.utility.prefs;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences implementation that stores to a user-defined file. See FilePreferencesFactory.
 * <p>
 * Original code developed by David C. Croft and released as under the CC0 1.0 Universal License.
 * Source: http://www.davidc.net/programming/java/java-preferences-using-file-backing-store
 */
public class FilePreferences extends AbstractPreferences {
    private static final Logger LOG = Logger.getLogger(FilePreferences.class.getName());

    private final Map<String, String> root = new TreeMap<>();
    private final Map<String, FilePreferences> children = new TreeMap<>();
    private boolean isRemoved = false;

    public FilePreferences(AbstractPreferences parent, String name) {
        super(parent, name);
        LOG.fine(() -> "instantiating node " + name);

        try {
            sync();
        } catch (BackingStoreException e) {
            LOG.log(Level.SEVERE, "unable to sync on creation of node " + name, e);
        }
    }

    protected void putSpi(String key, String value) {
        root.put(key, value);
        try {
            flush();
        } catch (BackingStoreException e) {
            LOG.log(Level.SEVERE, "unable to flush after putting " + key, e);
        }
    }

    protected String getSpi(String key) {
        return root.get(key);
    }

    protected void removeSpi(String key) {
        root.remove(key);
        try {
            flush();
        } catch (BackingStoreException e) {
            LOG.log(Level.SEVERE, "unable to flush after removing " + key, e);
        }
    }

    protected void removeNodeSpi() throws BackingStoreException {
        isRemoved = true;
        flush();
    }

    protected String[] keysSpi() throws BackingStoreException {
        return root.keySet().toArray(String[]::new);
    }

    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().toArray(String[]::new);
    }

    protected FilePreferences childSpi(String name) {
        FilePreferences child = children.get(name);
        if (child == null || child.isRemoved()) {
            child = new FilePreferences(this, name);
            children.put(name, child);
        }
        return child;
    }


    protected void syncSpi() throws BackingStoreException {
        if (isRemoved()) return;

        final File file = FilePreferencesFactory.getPreferencesFile();

        if (!file.exists()) return;

        synchronized (file) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file.toPath())) {
                p.load(in);

                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                final Enumeration<?> pnen = p.propertyNames();
                while (pnen.hasMoreElements()) {
                    String propKey = (String) pnen.nextElement();
                    if (propKey.startsWith(path)) {
                        String subKey = propKey.substring(path.length());
                        // Only load immediate descendants
                        if (subKey.indexOf('.') == -1) {
                            root.put(subKey, p.getProperty(propKey));
                        }
                    }
                }
            } catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    private void getPath(StringBuilder sb) {
        final FilePreferences parent = (FilePreferences) parent();
        if (parent == null) return;

        parent.getPath(sb);
        sb.append(name()).append('.');
    }

    protected void flushSpi() throws BackingStoreException {
        File file = FilePreferencesFactory.getPreferencesFile();
        synchronized (file) {
            Properties p = new Properties();
            try {
                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                if (file.exists()) {
                    try (InputStream in = Files.newInputStream(file.toPath())) {
                        p.load(in);

                        List<String> toRemove = new ArrayList<>();

                        // Make a list of all direct children of this node to be removed
                        final Enumeration<?> pnen = p.propertyNames();
                        while (pnen.hasMoreElements()) {
                            String propKey = (String) pnen.nextElement();
                            if (propKey.startsWith(path)) {
                                String subKey = propKey.substring(path.length());
                                // Only do immediate descendants
                                if (subKey.indexOf('.') == -1) {
                                    toRemove.add(propKey);
                                }
                            }
                        }

                        // Remove them now that the enumeration is done with
                        for (String propKey : toRemove) {
                            p.remove(propKey);
                        }
                    }
                }

                // If this node hasn't been removed, add back in any values
                if (!isRemoved) {
                    for (Map.Entry<String, String> entry : root.entrySet()) {
                        p.setProperty(path + entry.getKey(), entry.getValue());
                    }
                }

                try (OutputStream out = Files.newOutputStream(file.toPath())) {
                    p.store(out, "FilePreferences");
                }
            } catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }
}

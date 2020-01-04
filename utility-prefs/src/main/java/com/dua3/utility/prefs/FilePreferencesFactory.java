package com.dua3.utility.prefs;

import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * PreferencesFactory implementation that stores the preferences in a user-defined file. To use it,
 * set the system property {@code java.util.prefs.PreferencesFactory} to
 * {@code com.dua3.fx.preferences.FilePreferencesFactory}.
 * <p>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the system property
 * {@code com.dua3.fx.preferences.FilePreferencesFactory.file}.
 * <p>
 * Original code developed by David C. Croft and released as under the CC0 1.0 Universal License.
 * Source: http://www.davidc.net/programming/java/java-preferences-using-file-backing-store
 */
public class FilePreferencesFactory implements PreferencesFactory {
    public static final String SYSTEM_PROPERTY_FILE = "com.dua3.fx.preferences.FilePreferencesFactory.file";
    private static final Logger LOG = Logger.getLogger(FilePreferencesFactory.class.getName());
    private static File preferencesFile;
    private Preferences rootPreferences;

    public static File getPreferencesFile() {
        if (preferencesFile == null) {
            String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
            if (prefsFile == null || prefsFile.length() == 0) {
                prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs";
            }
            File file = new File(prefsFile).getAbsoluteFile();
            LOG.fine("Preferences file is " + file);
            preferencesFile = file;
        }
        return preferencesFile;
    }

    public Preferences systemRoot() {
        return userRoot();
    }

    public Preferences userRoot() {
        if (rootPreferences == null) {
            LOG.fine("Instantiating root preferences");

            rootPreferences = new FilePreferences(null, "");
        }
        return rootPreferences;
    }

}

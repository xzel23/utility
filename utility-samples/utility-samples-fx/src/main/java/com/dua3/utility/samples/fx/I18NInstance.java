package com.dua3.utility.samples.fx;

import com.dua3.utility.i18n.I18N;

import java.util.ResourceBundle;

public class I18NInstance {

    private static final class Holder {
        private static I18N INSTANCE = null;

        public static I18N getInstance() {
            I18N i18n = I18N.getInstance();
            if (INSTANCE != i18n) {
                synchronized (i18n) {
                    i18n.mergeBundle(ResourceBundle.getBundle("com.dua3.utility.samples.fx.messages", i18n.getLocale()));
                    INSTANCE = i18n;
                }
            }
            return INSTANCE;
        }
    }

    private I18NInstance() {
        // Private constructor to prevent instantiation
    }

    static I18N get() {
        return Holder.getInstance();
    }
}

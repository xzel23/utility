package com.dua3.utility.fx.controls;

import com.dua3.utility.i18n.I18N;

import java.util.ResourceBundle;

public class I18NInstance {

    private static final class Holder {
        private static final I18N INSTANCE = initInstance();

        private static I18N initInstance() {
            I18N i18N = I18N.getInstance();

            i18N.mergeBundle(ResourceBundle.getBundle("com.dua3.utility.fx.controls.messages", i18N.getLocale()));

            return i18N;
        }
    }

    static I18N get() {
        return Holder.INSTANCE;
    }
}

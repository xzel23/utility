package com.dua3.utility.fx.controls;

import com.dua3.utility.i18n.I18N;
import com.dua3.utility.i18n.I18NProvider;

import java.util.Locale;
import java.util.ResourceBundle;

public class TestI18NProvider implements I18NProvider {
    private static final I18N INSTANCE = I18N.create(ResourceBundle.getBundle(Dialogs.class.getPackageName() + ".messages", Locale.getDefault()));

    @Override
    public I18N i18n() {
        return INSTANCE;
    }
}

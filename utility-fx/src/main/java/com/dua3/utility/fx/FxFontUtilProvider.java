package com.dua3.utility.fx;

import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FontUtilProvider;

/**
 * Implementation of the {@link FontUtilProvider} interface for JavaFX.
 */
public class FxFontUtilProvider implements FontUtilProvider {
    @Override
    public FontUtil<?> get() {
        return FxFontUtil.getInstance();
    }
}

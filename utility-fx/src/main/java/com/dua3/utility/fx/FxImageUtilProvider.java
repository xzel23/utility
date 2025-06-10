package com.dua3.utility.fx;

import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.data.ImageUtilProvider;

/**
 * Implementation of the {@link ImageUtilProvider} interface for JavaFX.
 */
public class FxImageUtilProvider implements ImageUtilProvider {

    /**
     * Default constructor, called by SPI.
     */
    public FxImageUtilProvider() { /* nothing to do */ }

    @Override
    public ImageUtil<?> get() {
        return FxImageUtil.getInstance();
    }
}

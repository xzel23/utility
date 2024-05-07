package com.dua3.utility.fx;

import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.data.ImageUtilProvider;

public class FxImageUtilProvider implements ImageUtilProvider {
    @Override
    public ImageUtil<?> get() {
        return FxImageUtil.getInstance();
    }
}

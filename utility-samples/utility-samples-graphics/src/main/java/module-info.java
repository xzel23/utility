// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import org.jspecify.annotations.NullMarked;

/**
 * Open module definition for com.dua3.utility.samples.geom.
 *
 * <p>This module contains sample programs.
 */
@NullMarked
open module com.dua3.utility.samples.graphics {
    exports com.dua3.utility.samples.graphics;
    exports com.dua3.utility.samples.graphics.slides;

    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires org.jspecify;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires com.dua3.utility.fx.controls;
}

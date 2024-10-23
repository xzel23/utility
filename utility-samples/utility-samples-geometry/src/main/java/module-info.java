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
open module com.dua3.utility.samples.geom {
    exports com.dua3.utility.samples.geom;

    requires com.dua3.utility;
    requires javafx.graphics;
    requires org.jspecify;
}

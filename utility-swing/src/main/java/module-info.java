// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import org.jspecify.annotations.NullMarked;

/**
 * This module provides utilities related to Swing-based GUI applications.
 */
@NullMarked
open module com.dua3.utility.swing {
    exports com.dua3.utility.swing;

    requires static org.jspecify;    requires static com.dua3.utility.logging;

    requires java.datatransfer;
    requires java.desktop;
    requires com.dua3.utility;
    requires org.apache.logging.log4j;
}

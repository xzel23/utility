// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module provides utilities related to Swing-based GUI applications.
 * <p>
 * The module provides implementations of the FontUtil and ImageUtil interfaces.
 * These implementations are automatically chosen at runtime:
 * - com.dua3.utility.swing.SwingFontUtil is the implementation of FontUtil
 * - com.dua3.utility.swing.SwingImageUtil is the implementation of ImageUtil
 */
open module com.dua3.utility.swing {
    exports com.dua3.utility.swing;

    provides com.dua3.utility.text.FontUtil
            with com.dua3.utility.swing.SwingFontUtil;

    provides com.dua3.utility.data.ImageUtil
            with com.dua3.utility.swing.SwingImageUtil;

    requires static com.dua3.cabe.annotations;
    requires static com.dua3.utility.logging;

    requires java.datatransfer;
    requires java.desktop;
    requires com.dua3.utility;
    requires org.apache.logging.log4j;
}

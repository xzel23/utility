// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

open module com.dua3.utility.swing {
    exports com.dua3.utility.swing;

    provides com.dua3.utility.text.FontUtil
            with com.dua3.utility.swing.SwingFontUtil;

    provides com.dua3.utility.data.ImageUtil
            with com.dua3.utility.swing.SwingImageUtil;

    requires com.dua3.utility;

    requires java.datatransfer;
    requires java.desktop;

    requires org.slf4j;

    requires static com.dua3.cabe.annotations;
    requires static com.dua3.utility.logging;
}

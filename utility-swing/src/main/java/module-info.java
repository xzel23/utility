// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

module com.dua3.utility.swing {
    exports com.dua3.utility.swing;

    provides com.dua3.utility.text.FontUtil
    with com.dua3.utility.swing.SwingFontUtil;
    
    requires com.dua3.utility;

    requires java.datatransfer;
    requires java.desktop;
    requires java.logging;
}
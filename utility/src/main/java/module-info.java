// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

module dua3_utility {
    exports com.dua3.utility.concurrent;
    exports com.dua3.utility.data;
    exports com.dua3.utility.io;
    exports com.dua3.utility.lang;
    exports com.dua3.utility.math;
    exports com.dua3.utility.options;
    exports com.dua3.utility.text;
    exports com.dua3.utility.xml;

    requires java.logging;

    uses com.dua3.utility.text.FontUtil;
    uses com.dua3.utility.io.FileType;
}

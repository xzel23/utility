// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

open module com.dua3.utility {
    exports com.dua3.utility.concurrent;
    exports com.dua3.utility.data;
    exports com.dua3.utility.io;
    exports com.dua3.utility.lang;
    exports com.dua3.utility.logging;
    exports com.dua3.utility.math;
    exports com.dua3.utility.math.geometry;
    exports com.dua3.utility.options;
    exports com.dua3.utility.text;
    exports com.dua3.utility.xml;

    requires static org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;
    requires static com.dua3.cabe.annotations;

    requires java.logging;
    requires java.xml;
    
    uses com.dua3.utility.text.FontUtil;
    uses com.dua3.utility.io.FileType;
    uses com.dua3.utility.data.ImageUtil;
}

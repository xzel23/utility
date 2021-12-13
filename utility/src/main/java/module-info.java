// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

module dua3_utility {
    exports com.dua3.utility.options;
    exports com.dua3.utility.concurrent;
    exports com.dua3.utility.data;
    exports com.dua3.utility.io;
    exports com.dua3.utility.lang;
    exports com.dua3.utility.logging;
    exports com.dua3.utility.math;
    exports com.dua3.utility.text;
    exports com.dua3.utility.xml;
    exports com.dua3.utility.math.geometry;

    requires java.logging;
    
    requires static org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;
    
    requires java.xml;
    requires org.jetbrains.annotations;

    uses com.dua3.utility.text.FontUtil;
    uses com.dua3.utility.io.FileType;
    uses com.dua3.utility.data.ImageUtil;
}

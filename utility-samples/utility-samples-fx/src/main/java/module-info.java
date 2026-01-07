// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import org.jspecify.annotations.NullMarked;

/**
 * This module contains the classes for the sample applications using Log4J.
 */
@NullMarked
open module com.dua3.utility.samples.fx {
    exports com.dua3.utility.samples.fx;

    requires atlantafx.base;
    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires com.dua3.utility.fx.controls;
    requires com.dua3.utility.fx.icons;
    requires com.dua3.utility.logging;
    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.apache.commons.logging;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.jul;
    requires org.jspecify;
    requires org.slf4j;
    requires java.prefs;
}

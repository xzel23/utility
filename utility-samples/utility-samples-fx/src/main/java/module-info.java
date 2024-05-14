// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module contains the classes for the sample applications using Log4J.
 */
open module com.dua3.utility.samples.fx {
    exports com.dua3.utility.samples.fx;

    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires com.dua3.utility.fx.controls;
    requires com.dua3.utility.fx.icons;
    requires com.dua3.utility.logging;
    requires java.base;
    requires org.slf4j;
    requires com.dua3.utility.logging.log4j;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.apache.commons.logging;
    requires java.logging;
    requires org.apache.logging.log4j;
}

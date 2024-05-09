// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module contains the classes for the sample applications using Log4J.
 */
open module com.dua3.utility.samples.fxlogpane {
    exports com.dua3.utility.samples.fxlogpane;

    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires com.dua3.utility.logging;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j2.impl;
    requires org.apache.logging.log4j.jul;
    requires org.slf4j;
    requires java.desktop;
    requires java.logging;
    requires com.dua3.utility.logging.log4j;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.apache.commons.logging;
}

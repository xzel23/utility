// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module contains the classes for the sample applications using Log4J.
 */
open module com.dua3.utility.samples.log4j {
    exports com.dua3.utility.samples.log4j;

    requires com.dua3.utility;
    requires com.dua3.utility.logging;
    requires com.dua3.utility.swing;
    requires com.miglayout.swing;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j2.impl;
    requires org.apache.logging.log4j.jul;
    requires org.slf4j;
    requires java.desktop;
    requires java.logging;
    requires com.dua3.utility.samples;
}

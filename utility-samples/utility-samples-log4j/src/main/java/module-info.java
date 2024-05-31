// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module contains the classes for the sample applications using Log4J.
 */
open module com.dua3.utility.samples.log4j {
    exports com.dua3.utility.samples.log4j;

    requires com.dua3.utility.samples;
    requires com.dua3.utility.logging;
    requires com.dua3.utility.logging.log4j;
    requires org.apache.logging.log4j.jcl;
    requires org.apache.logging.log4j.jul;
}

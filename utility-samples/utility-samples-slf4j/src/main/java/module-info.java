// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * This module contains the classes for the sample applications using SLF4J.
 */
open module com.dua3.utility.samples.slf4j {
    exports com.dua3.utility.samples.slf4j;

    requires jul.to.slf4j;
    requires com.dua3.utility.samples;
    requires java.logging;
}

// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.LogDispatcherFactory;
import com.dua3.utility.logging.log4j.LogDispatcherFactoryLog4j;
import org.jspecify.annotations.NullMarked;

/**
 * This module provides logging functionality using Log4j.
 */
@NullMarked
open module com.dua3.utility.logging.log4j {
    exports com.dua3.utility.logging.log4j;

    provides LogDispatcherFactory with LogDispatcherFactoryLog4j;

    requires org.jspecify;
    requires com.dua3.utility.logging;
    requires org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;
    requires com.dua3.utility;
}

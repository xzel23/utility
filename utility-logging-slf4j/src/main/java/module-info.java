// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.slf4j.LogEntryDispatcherFactorySlf4j;
import com.dua3.utility.logging.slf4j.LoggingServiceProviderSlf4j;
import org.jspecify.annotations.NullMarked;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * This module provides SLF4J logging functionality and integration with the Logging utility module.
 */
@NullMarked
open module com.dua3.utility.logging.slf4j {
    exports com.dua3.utility.logging.slf4j;
    provides SLF4JServiceProvider with LoggingServiceProviderSlf4j;
    provides ILogEntryDispatcherFactory with LogEntryDispatcherFactorySlf4j;

    requires org.jspecify;
    requires com.dua3.utility.logging;
    requires com.dua3.utility;
    requires org.slf4j;
}

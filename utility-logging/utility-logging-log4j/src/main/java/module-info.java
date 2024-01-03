// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.log4j.LogEntryDispatcherFactoryLog4j;

/**
 * This module provides logging functionality using Log4j.
 */
open module com.dua3.utility.logging.log4j {
    exports com.dua3.utility.logging.log4j;

    provides ILogEntryDispatcherFactory with LogEntryDispatcherFactoryLog4j;

    requires static com.dua3.cabe.annotations;

    requires com.dua3.utility.logging;
    requires org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;
}

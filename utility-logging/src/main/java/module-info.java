// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.utility.logging module is responsible for providing utilities related to logging.
 * It exports the com.dua3.utility.logging package which contains classes and interfaces related to logging.
 * <p>
 * The module requires the com.dua3.cabe.annotations package at compile time.
 * <p>
 * It uses the com.dua3.utility.logging.ILogEntryDispatcherFactory service to obtain an instance of the
 * LogEntryDispatcher.
 * <p>
 * The module also requires the org.apache.logging.log4j and com.dua3.utility packages at runtime.
 */
@NullMarked
open module com.dua3.utility.logging {
    exports com.dua3.utility.logging;

    requires org.jspecify;
    uses com.dua3.utility.logging.ILogEntryDispatcherFactory;

    requires org.apache.logging.log4j;
    requires com.dua3.utility;
}

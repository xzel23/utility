// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.LogDispatcherFactory;
import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.utility.logging module is responsible for providing utilities related to logging.
 * It exports the com.dua3.utility.logging package which contains classes and interfaces related to logging.
 * <p>
 * The module requires the org.jspecify module for annotations.
 * <p>
 * It uses the com.dua3.utility.logging.ILogDispatcherFactory service to get an instance of the
 * LogDispatcher.
 * <p>
 * The module also requires the org.apache.logging.log4j and com.dua3.utility packages at runtime.
 */
@NullMarked
open module com.dua3.utility.logging {
    exports com.dua3.utility.logging;

    requires org.jspecify;
    uses LogDispatcherFactory;

    requires static java.logging;
    requires static org.slf4j;
    requires static org.apache.logging.log4j;
    requires static org.apache.commons.logging;

    requires com.dua3.utility;
}

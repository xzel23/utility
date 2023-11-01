// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.slf4j.LogEntryDispatcherFactorySlf4j;
import com.dua3.utility.logging.slf4j.LoggingServiceProviderSlf4j;
import org.slf4j.spi.SLF4JServiceProvider;

open module com.dua3.utility.logging.slf4j {
    exports com.dua3.utility.logging.slf4j;
    provides SLF4JServiceProvider with LoggingServiceProviderSlf4j;
    provides ILogEntryDispatcherFactory with LogEntryDispatcherFactorySlf4j;

    requires static com.dua3.cabe.annotations;
    requires static com.github.spotbugs.annotations;

    requires com.dua3.utility.logging;
    requires org.apache.logging.log4j;
    requires com.dua3.utility;
    requires org.slf4j;
}

// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import org.slf4j.spi.SLF4JServiceProvider;

open module com.dua3.utility.logging {
    requires org.slf4j;
    requires com.dua3.utility;
    requires static com.dua3.cabe.annotations;
    exports com.dua3.utility.logging;
    provides SLF4JServiceProvider with com.dua3.utility.logging.LoggingServiceProvider;
}

// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.logging.slf4j.LoggingServiceProvider;
import org.slf4j.spi.SLF4JServiceProvider;

open module com.dua3.utility.logging.slf4j {
    exports com.dua3.utility.logging.slf4j;
    provides SLF4JServiceProvider with LoggingServiceProvider;

    requires static com.dua3.cabe.annotations;

    requires com.dua3.utility.logging;
    requires org.apache.logging.log4j;
    requires com.dua3.utility;
    requires org.slf4j;
}

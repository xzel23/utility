// Copyright (c) 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

open module com.dua3.utility.logging {
    exports com.dua3.utility.logging;

    requires static com.dua3.cabe.annotations;

    uses com.dua3.utility.logging.ILogEntryDispatcherFactory;

    requires org.apache.logging.log4j;
    requires com.dua3.utility;
}

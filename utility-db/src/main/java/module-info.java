// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * Module containing database utilities.
 */
open module com.dua3.utility.db {
    exports com.dua3.utility.db;

    requires transitive java.sql;
    
    requires java.logging;
    requires com.dua3.utility;

    requires static com.dua3.cabe.annotations;
    requires org.slf4j;
}

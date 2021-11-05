// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

/**
 * Module containing database utilities.
 */
module dua3_utility.db {
    exports com.dua3.utility.db;

    requires transitive java.sql;
    
    requires java.logging;
    requires dua3_utility;
    requires org.jetbrains.annotations;
}

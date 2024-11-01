import org.jspecify.annotations.NullMarked;

/**
 * Module containing database utilities.
 */
@NullMarked
open module com.dua3.utility.db {
    exports com.dua3.utility.db;

    requires org.jspecify;
    requires transitive java.sql;

    requires com.dua3.utility;
    requires org.apache.logging.log4j;
}

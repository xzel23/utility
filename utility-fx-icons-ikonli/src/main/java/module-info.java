import org.jspecify.annotations.NullMarked;
import com.dua3.utility.fx.icons.IconProvider;
import com.dua3.utility.fx.icons.ikonli.IkonliIconProvider;

/**
 * This module is a Java module that provides integration of the Ikonli library with JavaFX Icons.
 * It exports the package com.dua3.fx.icons.ikonli and opens the same package for reflective access.
 * <p>
 * This module requires the following external dependencies:
 * <ul>
 * <li>org.apache.logging.log4j
 * <li>org.kordamp.ikonli.core
 * <li>org.kordamp.ikonli.javafx
 * <li>com.dua3.fx.icons
 * <li>javafx.graphics
 * </ul>
 * <p>
 * This module also uses the interface org.kordamp.ikonli.IkonHandler and provides an implementation
 * of the com.dua3.fx.icons.IconProvider interface with the class com.dua3.fx.icons.ikonli.IkonliIconProvider.
 * <p>
 * This module has a dependency on the {@code com.dua3.cabe.annotations} module for the use of null check injection
 * into the compiled classes.
 */
@NullMarked
open module com.dua3.utility.fx.icons.ikonli {
    exports com.dua3.utility.fx.icons.ikonli;

    requires org.apache.logging.log4j;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    requires transitive com.dua3.utility.fx.icons;
    requires javafx.graphics;

    uses org.kordamp.ikonli.IkonHandler;
    provides IconProvider with IkonliIconProvider;

    requires org.jspecify;
}

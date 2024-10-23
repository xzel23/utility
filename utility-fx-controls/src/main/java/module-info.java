import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.fx.controls module is a JavaFX module that provides custom controls for building UIs.
 * <p>
 * This module exports the {@code com.dua3.fx.controls} package, making it accessible to other modules.
 * <p>
 * This module requires the {@code com.dua3.fx.util} module, which provides utility classes for JavaFX.
 * It also requires the {@code com.dua3.fx.icons} module, which provides custom icons for JavaFX applications.
 * <p>
 * Additionally, this module requires the {@code org.apache.logging.log4j} module for logging purposes.
 * <p>
 * This module also requires the following JavaFX modules:
 * <ul>
 *    <li>{@code javafx.controls}: provides basic JavaFX controls for UI development</li>
 *    <li>{@code javafx.graphics}: provides graphics functionalities for JavaFX applications</li>
 * </ul>
 * <p>
 * It also requires the {@code com.dua3.utility} module for utility classes, and the {@code java.desktop} module
 * for desktop application development purposes.
 * <p>
 * This module has a dependency on the {@code com.dua3.cabe.annotations} module for the use of null check injection
 * into the compiled classes.
 */
@NullMarked
open module com.dua3.utility.fx.controls {
    exports com.dua3.utility.fx.controls;

    requires com.dua3.utility.fx.icons;

    requires org.apache.logging.log4j;

    requires javafx.controls;
    requires javafx.graphics;
    requires com.dua3.utility;
    requires java.desktop;

    requires static org.jspecify;    requires com.dua3.utility.fx;
}

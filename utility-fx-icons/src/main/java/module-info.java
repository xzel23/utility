import com.dua3.utility.fx.icons.IconProvider;
import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.fx.icons module provides a collection of icons for JavaFX applications.
 * It exports the com.dua3.fx.icons package which contains the following classes and interfaces:
 * <ul>
 * <li>IconProvider: An interface that defines a provider of icons.
 * <li>Icon: An interface that represents an icon.
 * </ul>
 * <p>
 * The module has the following dependencies:
 * <ul>
 * <li>org.apache.logging.log4j: Required for logging.
 * <li>javafx.controls: Required for JavaFX controls.
 * <li>javafx.graphics: Required for JavaFX graphics.
 * <li>com.dua3.cabe.annotations: Required for annotations.
 * </ul>
 * <p>
 * This module uses the services provided by IconProvider implementations to offer icons.
 * The IconProvider interface has two methods: name() and forName(String name).
 * The name() method returns the provider's name, while the forName(String name) method
 * returns an Icon object associated with the specified name. If the provider does not
 * offer the requested icon, null is returned.
 * <p>
 * The Icon interface represents an icon and extends the Styleable interface. It has the
 * following methods:
 * <ul>
 * <li>getIconIdentifier(): Returns the identifier of the icon.
 * <li>getIconSize(): Returns the size of the icon in pixels.
 * <li>setIconSize(int size): Sets the size of the icon.
 * <li>iconSizeProperty(): Returns the property for the icon size.
 * <li>getIconColor(): Returns the color of the icon.
 * <li>setIconColor(Paint paint): Sets the color of the icon.
 * <li>iconColorProperty(): Returns the property for the icon color.
 * <li>node(): Returns the Node object of the icon.
 * </ul>
 */
@NullMarked
open module com.dua3.utility.fx.icons {
    exports com.dua3.utility.fx.icons;

    requires org.apache.logging.log4j;

    requires static org.jspecify;
    requires javafx.controls;
    uses IconProvider;
}

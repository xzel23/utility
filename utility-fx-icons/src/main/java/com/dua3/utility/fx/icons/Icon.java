package com.dua3.utility.fx.icons;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.paint.Paint;

/**
 * Icon interface.
 */
public interface Icon extends Styleable {
    /**
     * Get icon identifier.
     *
     * @return the icon Identifier.
     */
    String getIconIdentifier();

    /**
     * Get icon size.
     *
     * @return the size of this icon in pixels
     */
    int getIconSize();

    /**
     * Set icon size.
     *
     * @param size new size in pixels
     */
    void setIconSize(int size);

    /**
     * Get icon size property.
     *
     * @return property for the icon size
     */
    IntegerProperty iconSizeProperty();

    /**
     * Get icon color.
     *
     * @return the icon color
     */
    Paint getIconColor();

    /**
     * Set icon color.
     *
     * @param paint the color
     */
    void setIconColor(Paint paint);

    /**
     * Get icon color property.
     *
     * @return the color property
     */
    ObjectProperty<Paint> iconColorProperty();

    /**
     * Get Node of this icon.
     *
     * @return the node
     */
    Node node();
}

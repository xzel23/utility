package com.dua3.utility.fx.icons;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The IconUtil class provides utility methods for working with icons.
 * It contains static methods for retrieving icons, retrieving the names of available icon providers,
 * and creating an empty icon. Icons are represented by the {@link Icon} interface.
 */
public final class IconUtil {
    private static final Logger LOG = LogManager.getLogger(IconUtil.class);

    private IconUtil() {
    }

    /**
     * Returns an optional icon based on the given name.
     *
     * @param name the name of the icon requested
     * @return an optional icon that matches the given name, or an empty optional if the icon does not exist
     */
    public static Optional<Icon> iconFromName(String name) {
        Class<IconProvider> iconProviderClass = IconProvider.class;
        return ServiceLoader.load(iconProviderClass)
                .stream()
                .peek(provider -> LOG.trace("found {} implementation: {}", iconProviderClass.getName(), provider.getClass().getName()))
                .map(provider -> provider.get().forName(name).orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * Returns a collection of names of available icon providers.
     *
     * @return a collection of strings representing the names of the available icon providers
     */
    public static Collection<String> iconProviderNames() {
        Class<IconProvider> iconProviderClass = IconProvider.class;
        return ServiceLoader.load(iconProviderClass)
                .stream()
                .map(p -> p.type().getName()).toList();
    }

    /**
     * Returns an empty icon.
     *
     * @return an empty icon represented by the {@link Icon} interface
     */
    public static Icon emptyIcon() {
        return new EmptyIcon();
    }
}

class EmptyIcon extends Text implements Icon {

    private final IntegerProperty iconSize = new SimpleIntegerProperty();
    private final ObjectProperty<Paint> iconColor = new SimpleObjectProperty<>(Paint.valueOf("BLACK"));

    @Override
    public String getIconIdentifier() {
        return "";
    }

    @Override
    public int getIconSize() {
        return iconSize.get();
    }

    @Override
    public void setIconSize(int size) {
        iconSize.set(size);
    }

    @Override
    public IntegerProperty iconSizeProperty() {
        return iconSize;
    }

    @Override
    public Paint getIconColor() {
        return iconColor.get();
    }

    @Override
    public void setIconColor(Paint paint) {
        iconColor.set(paint);
    }

    @Override
    public ObjectProperty<Paint> iconColorProperty() {
        return iconColor;
    }

    @Override
    public Node node() {
        return this;
    }
}

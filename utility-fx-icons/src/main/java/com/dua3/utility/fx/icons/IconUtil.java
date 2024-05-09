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

public final class IconUtil {
    private static final Logger LOG = LogManager.getLogger(IconUtil.class);

    private IconUtil() {
    }

    public static Optional<Icon> iconFromName(String name) {
        Class<IconProvider> iconProviderClass = IconProvider.class;
        return ServiceLoader.load(iconProviderClass)
                .stream()
                .peek(provider -> LOG.debug("found {} implementation: {}", iconProviderClass.getName(), provider.getClass().getName()))
                .map(provider -> provider.get().forName(name))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static Collection<String> iconProviderNames() {
        Class<IconProvider> iconProviderClass = IconProvider.class;
        return ServiceLoader.load(iconProviderClass)
                .stream()
                .map(p -> p.type().getName()).toList();
    }

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

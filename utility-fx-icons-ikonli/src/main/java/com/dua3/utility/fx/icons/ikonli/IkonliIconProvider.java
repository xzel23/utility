package com.dua3.utility.fx.icons.ikonli;

import com.dua3.utility.fx.icons.Icon;
import com.dua3.utility.fx.icons.IconProvider;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.ikonli.javafx.FontIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.ServiceLoader;


/**
 * The {@code IkonliIconProvider} class is an implementation of the {@link IconProvider} interface
 * that provides icons using the Ikonli library.
 */
public class IkonliIconProvider implements IconProvider {

    private static final Logger LOG = LogManager.getLogger(IkonliIconProvider.class);

    /**
     * Constructs a new instance of {@code IkonliIconProvider} which provides icons using the Ikonli library.
     */
    public IkonliIconProvider() {}

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public Optional<Icon> forName(String name) {
        for (var handler : ServiceLoader.load(IkonHandler.class)) {
            if (handler.supports(name)) {
                LOG.debug("using: {}", handler.getClass().getName());
                var ikon = handler.resolve(name);
                return Optional.of(new IkonliIcon(ikon, name));
            }
        }

        LOG.debug("icon not found: {}", name);
        return Optional.empty();
    }

    @SuppressWarnings("NullableProblems")
    static class IkonliIcon extends FontIcon implements Icon {
        private final String name;

        IkonliIcon(Ikon ikon, String name) {
            super(ikon);
            this.name = name;
        }

        @Override
        public IntegerProperty iconSizeProperty() {
            IntegerProperty p = super.iconSizeProperty();
            assert p != null;
            return p;
        }

        @Override
        public Paint getIconColor() {
            Paint c = super.getIconColor();
            assert c != null;
            return c;
        }

        @Override
        public String getIconIdentifier() {
            return name;
        }

        @Override
        public Node node() {
            return this;
        }

    }

}

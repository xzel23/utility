package com.dua3.utility.fx.icons.ikonli;

import com.dua3.utility.fx.icons.Icon;
import com.dua3.utility.fx.icons.IconProvider;
import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.ikonli.javafx.FontIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public Icon forName(String name) {
        for (var handler : ServiceLoader.load(IkonHandler.class)) {
            if (handler.supports(name)) {
                LOG.debug("using: {}", handler.getClass().getName());
                var ikon = handler.resolve(name);
                return new IkonliIcon(ikon, name);
            }
        }

        LOG.debug("icon not found: {}", name);
        return null;
    }

    static class IkonliIcon extends FontIcon implements Icon {
        private final String name;

        IkonliIcon(Ikon ikon, String name) {
            super(ikon);
            this.name = name;
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

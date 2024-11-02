package com.dua3.utility.fx.icons;

import java.util.Optional;

/**
 * The IconProvider interface represents a provider of icons.
 */
public interface IconProvider {
    /**
     * Get this provider*s name.
     *
     * @return provider name
     */
    String name();

    /**
     * Get icon.
     *
     * @param name name of the requested icon
     * @return Optional holding the icon
     */
    Optional<Icon> forName(String name);
}

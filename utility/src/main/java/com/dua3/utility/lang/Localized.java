package com.dua3.utility.lang;

/**
 * A functional interface representing an object that provides a localized string representation.
 * <p>
 * Implementations of this interface should provide a localized version of their string representation
 * by overriding the {@code toLocalizedString()} method.
 */
@FunctionalInterface
public interface Localized {
    /**
     * Provides a localized string representation of the implementing object.
     *
     * @return the localized string representation of the object
     */
    String toLocalizedString();
}

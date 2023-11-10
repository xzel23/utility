package com.dua3.utility.concurrent;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Represents a read-only value that can be accessed and observed.
 *
 * @param <T> the type of the value
 */
public interface ReadOnlyValue<T> {
    /**
     * Retrieves the value of type T.
     *
     * @return The value of type T.
     */
    T get();

    /**
     * Adds a change listener for the given method. The change listener will be triggered
     * whenever the value of type T changes.
     *
     * @param listener The change listener to be added.
     *                 The change listener is a BiConsumer that accepts two parameters:
     *                 - The previous value of type T
     *                 - The new value of type T
     *                 The change listener does not return a value.
     */
    void addChangeListener(BiConsumer<? super T, ? super T> listener);

    /**
     * Removes a change listener for the given method. The change listener will no longer be triggered
     * whenever the value of type T changes.
     *
     * @param listener The change listener to be removed.
     *                 The change listener is a BiConsumer that was previously added using the addChangeListener method.
     */
    void removeChangeListener(BiConsumer<? super T, ? super T> listener);

    /**
     * Returns a collection of all the change listeners currently registered for this method.
     *
     * @return A collection of change listeners, each represented as a BiConsumer.
     *         The change listeners are used to listen for changes in the value of type T.
     */
    Collection<BiConsumer<? super T, ? super T>> getChangeListeners();
}

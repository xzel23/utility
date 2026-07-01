package com.dua3.utility.ui;

import org.jspecify.annotations.Nullable;

/**
 * Interface for a node that can be detached from its parent.
 *
 * @param <N> the type of the node
 * @param <P> the type of the parent nodes
 */
public interface DetachableNode<N, P> {

    /**
     * Enum representing the possible locations or visibility states for a detachable node.
     * This is typically used to define where a UI element (such as a toolbar or custom component)
     * is displayed within the application or if it is hidden.
     */
    enum Location {
        /** The node is hidden. */
        HIDDEN,
        /** The node is embedded within another component. */
        EMBEDDED,
        /** The node is embedded visible in the application, for example in the application main toolbar. */
        APPLICATION,
        /** The node is floating in a separate window. */
        FLOATING
    }

    /**
     * Functional interface for receiving notifications when the location or visibility
     * state of a {@code DetachableNode} changes.
     * <p>
     * A {@code LocationListener} is typically registered with a {@code DetachableNode}
     * to monitor updates to its {@link Location}. When the location of the node changes,
     * the {@code locationChanged} method is invoked, providing the node and its new
     * location as parameters.
     * <p>
     * This interface can be used to implement dynamic behavior based on location changes,
     * such as updating the UI or triggering application-specific logic.
     */
    @FunctionalInterface
    interface LocationListener {
        /**
         * Notifies that the location of the specified {@code DetachableNode} has changed.
         *
         * @param node the {@code DetachableNode} whose location has been updated
         * @param oldLocation the old {@code Location} of the specified node
         * @param newLocation the new {@code Location} of the specified node
         */
        void locationChanged(DetachableNode<?, ?> node, Location oldLocation, Location newLocation);
    }

    /**
     * Registers a {@link LocationListener} to receive updates when the location or
     * visibility state of a {@code DetachableNode} changes.
     *
     * @param listener the {@code LocationListener} instance to be notified of location changes;
     *                 must not be null.
     */
    void addLocationListener(LocationListener listener);

    /**
     * Removes a previously registered location listener from the node.
     * The location listener will no longer receive updates when the node's location changes.
     *
     * @param listener the {@link DetachableNode.LocationListener} to be removed;
     *                 must not be null and should already be registered
     * @return {@code true} if the listener was contained in the list of listeners
     */
    boolean removeLocationListener(LocationListener listener);

    /**
     * Sets the current location or visibility of the node.
     *
     * @param location the new location or visibility state of the node
     */
    void setLocation(Location location);

    /**
     * Gets the location of the node.
     *
     * @return the current location or visibility state of the node
     */
    Location getLocation();

    /**
     * Returns the actual UI component (e.g., JToolBar or ToolBar)
     * so containers can add/remove it dynamically.
     *
     * @return the actual UI component
     */
    N getNode();

    /**
     * Sets the parent for the current application context of the node and manages the
     * addition or removal process of the node into/from the parent.
     *
     * @param parent the parent node to assign to the node; may be null to indicate no parent
     */
    void setApplicationParent(@Nullable P parent);
}

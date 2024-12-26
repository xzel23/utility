/*
 * Copyright (c) 2022. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * A JavaFX component where items can be pinned at a position.
 */
@SuppressWarnings("MagicCharacter")
public class PinBoard extends Control {

    final ObservableList<Item> items = FXCollections.observableArrayList();
    private final ObjectProperty<Rectangle2D> areaProperty = new SimpleObjectProperty<>(new Rectangle2D(0, 0, 0, 0));
    private final BooleanProperty pannableProperty = new SimpleBooleanProperty(true);
    private final DoubleProperty scrollHValueProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty scrollVValueProperty = new SimpleDoubleProperty(0.0);

    /**
     * Default constructor.
     */
    public PinBoard() {
        skinProperty().addListener((v, o, n) -> {
            if (o instanceof PinBoardSkin oldSkin) {
                oldSkin.pannableProperty().unbind();
                oldSkin.scrollHValueProperty().unbindBidirectional(scrollHValueProperty);
                oldSkin.scrollVValueProperty().unbindBidirectional(scrollVValueProperty);
            }
            if (n instanceof PinBoardSkin newSkin) {
                newSkin.pannableProperty().bind(pannableProperty);
                newSkin.scrollHValueProperty().bindBidirectional(scrollHValueProperty);
                newSkin.scrollVValueProperty().bindBidirectional(scrollVValueProperty);
            }
        });
    }

    /**
     * Sets the horizontal scrollbar policy of the PinBoardSkin's ScrollPane.
     *
     * @param policy the scrollbar policy to set
     */
    public void setHbarPolicy(ScrollPane.ScrollBarPolicy policy) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setHbarPolicy(policy);
        }
    }

    /**
     * Sets the vertical scrollbar policy of the PinBoardSkin's ScrollPane.
     *
     * @param policy the ScrollBarPolicy to set
     */
    public void setVbarPolicy(ScrollPane.ScrollBarPolicy policy) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setVbarPolicy(policy);
        }
    }

    /**
     * Returns the property representing the horizontal scroll value of the PinBoard.
     *
     * @return The DoubleProperty representing the horizontal scroll value.
     */
    public DoubleProperty scrollHValuePropertyProperty() {
        return scrollHValueProperty;
    }

    /**
     * Returns the property representing the vertical scroll value of the PinBoard.
     *
     * @return The DoubleProperty representing the vertical scroll value.
     */
    public DoubleProperty scrollVValuePropertyProperty() {
        return scrollVValueProperty;
    }

    /**
     * Retrieves the list of currently visible items on the PinBoard.
     * <p>
     * The method checks whether the PinBoard is associated with a PinBoardSkin.
     * If so, it delegates the call to the skin to retrieve the visible items.
     * If no PinBoardSkin is associated, it returns an empty list.
     *
     * @return A list of visible items. If no skin is available, an empty list is returned.
     */
    public List<Item> getVisibleItems() {
        if (getSkin() instanceof PinBoardSkin skin) {
            return skin.getVisibleItems();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Clears the PinBoard by removing all items and resetting the area property.
     * This method must be called from the JavaFX Application Thread.
     */
    public void clear() {
        PlatformHelper.checkApplicationThread();
        items.clear();
        areaProperty.set(new Rectangle2D(0, 0, 0, 0));
    }

    /**
     * Refreshes the PinBoard skin.
     * <p>
     * This method is used to refresh the visual representation of the PinBoard. It checks if the skin associated
     * with the PinBoard is an instance of PinBoardSkin and then calls the {@link PinBoardSkin#refresh()} method
     * to update the nodes displayed on the board.
     * </p>
     */
    public void refresh() {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.refresh();
        }
    }

    /**
     * Disposes the PinBoardSkin instance.
     * Stops the refresher and disposes the superclass.
     */
    public void dispose() {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.dispose();
        }
    }

    @Override
    protected Skin<PinBoard> createDefaultSkin() {
        return new PinBoardSkin(this);
    }

    /**
     * Returns the read-only property that represents the area of the PinBoard.
     *
     * @return The read-only area property of type Rectangle2D.
     */
    public ReadOnlyObjectProperty<Rectangle2D> areaProperty() {
        return areaProperty;
    }

    public BooleanProperty pannableProperty() {
        return pannableProperty;
    }

    /**
     * Returns an unmodifiable observable list of items.
     *
     * @return An unmodifiable observable list of items.
     */
    public ObservableList<Item> getItems() {
        return FXCollections.unmodifiableObservableList(items);
    }

    /**
     * Retrieves the current scroll position of the PinBoard.
     *
     * @return a Pair containing the horizontal and vertical scroll positions
     */
    public ScrollPosition getScrollPosition() {
        if (getSkin() instanceof PinBoardSkin skin) {
            return skin.getScrollPosition();
        } else {
            return ScrollPosition.ORIGIN;
        }
    }

    /**
     * Sets the scroll position of the PinBoardSkin's ScrollPane.
     *
     * @param hValue the horizontal scroll value to set
     * @param vValue the vertical scroll value to set
     */
    public void setScrollPosition(double hValue, double vValue) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setScrollPosition(hValue, vValue);
        }
    }

    /**
     * Sets the scroll position of the PinBoardSkin's ScrollPane.
     *
     * @param scrollPosition a Pair containing the horizontal and vertical scroll positions
     */
    public void setScrollPosition(ScrollPosition scrollPosition) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setScrollPosition(scrollPosition);
        }
    }

    /**
     * Scrolls the PinBoard to the specified position within an item.
     *
     * @param pos the position within an item to scroll to
     */
    public void scrollTo(PositionInItem pos) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollTo(pos);
        }
    }

    /**
     * Scrolls the PinBoard the specified position into view.
     *
     * @param pos the position
     */
    public void scrollIntoView(PositionInItem pos) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollIntoView(pos);
        }
    }

    /**
     * Get Item at point.
     *
     * @param x x-coordinate (relative to viewport)
     * @param y y-coordinate (relative to viewport)
     * @return Optional containing the item at (x,y)
     */
    public Optional<Item> getItemAt(double x, double y) {
        return getPositionInItem(x, y).map(PositionInItem::item);
    }

    /**
     * Get Item at point and coordinates transformed to item coordinates.
     *
     * @param x x-coordinate (relative to viewport)
     * @param y y-coordinate (relative to viewport)
     * @return Optional containing the item and the transformed coordinates
     */
    public Optional<PositionInItem> getPositionInItem(double x, double y) {
        if (getSkin() instanceof PinBoardSkin skin) {
            return skin.getPositionInItem(x, y);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Add item at the bottom, centered horizontally.
     *
     * @param name         item name
     * @param nodeSupplier supplier (factory) for item node
     * @param dimension    item dimension
     */
    public void pinBottom(String name, Supplier<Node> nodeSupplier, Dimension2D dimension) {
        Rectangle2D boardArea = getArea();
        double xCenter = (boardArea.getMaxX() + boardArea.getMinX()) / 2.0;
        double y = boardArea.getMaxY();
        Rectangle2D area = new Rectangle2D(xCenter - dimension.getWidth() / 2, y, dimension.getWidth(), dimension.getHeight());
        pin(new Item(name, area, nodeSupplier));
    }

    /**
     * Returns the area of the PinBoard.
     *
     * @return The Rectangle2D representing the area of the PinBoard.
     */
    public Rectangle2D getArea() {
        return areaProperty.get();
    }

    /**
     * Pins the given item or collection of items to the PinBoard.
     *
     * @param item The item to be pinned. Can be a single item or a collection of items to be pinned together.
     */
    public void pin(Item item) {
        pin(Collections.singleton(item));
    }

    /**
     * Pins the given collection of items to the PinBoard.
     *
     * @param itemsToPin The collection of items to be pinned.
     */
    public void pin(Collection<Item> itemsToPin) {
        PlatformHelper.checkApplicationThread();

        if (itemsToPin.isEmpty()) {
            return;
        }

        items.addAll(itemsToPin);

        itemsToPin.stream()
                .map(Item::area)
                .reduce(FxUtil::union)
                .map(r -> FxUtil.union(getArea(), r))
                .ifPresent(r -> {
                    if (!r.equals(getArea())) {
                        areaProperty.set(r);
                    }
                });
    }

    @Override
    public String toString() {
        return "PinBoard{" +
                "area=" + areaProperty.get() +
                ", items=" + items +
                '}';
    }

    /**
     * The Item class represents an item on a PinBoard. It contains information about the item's name, area,
     * and a supplier that can be used to build the item's Node representation.
     *
     * @param name The name of the item.
     * @param area The area occupied by the item on the PinBoard.
     * @param nodeBuilder A supplier that can be used to build the Node representation of the item.
     */
    public record Item(String name, Rectangle2D area, Supplier<Node> nodeBuilder) {}

    /**
     * The PositionInItem class represents the position within an item on a PinBoard.
     * It contains information about the item, as well as the x and y coordinates within the item.
     * This class is used for scrolling to a specific position within an item and retrieving the item at a given point.
     *
     * @param item The item within which the position is located.
     * @param x The x-coordinate within the item.
     * @param y The y-coordinate within the item.
     */
    public record PositionInItem(Item item, double x, double y) {}
}


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
import javafx.beans.property.ReadOnlyListWrapper;
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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


/**
 * A JavaFX component where items can be pinned at a position.
 */
@SuppressWarnings("MagicCharacter")
public class PinBoard extends Control {

    final ObservableList<Item> items = FXCollections.observableArrayList();
    final ObservableList<Item> visibleItems = FXCollections.observableArrayList();
    final ReadOnlyListWrapper<Item> readOnlyVisibleItems = new ReadOnlyListWrapper<>(visibleItems);

    private final ObjectProperty<Rectangle2D> areaProperty = new SimpleObjectProperty<>(new Rectangle2D(0, 0, 0, 0));
    private final BooleanProperty pannableProperty = new SimpleBooleanProperty(true);
    private final DoubleProperty scrollHValueProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty scrollVValueProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty displayScaleProperty = new SimpleDoubleProperty(1.0);

    /**
     * Default constructor.
     */
    public PinBoard() {
        skinProperty().addListener((v, o, n) -> {
            //noinspection ChainOfInstanceofChecks
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
    public void setHBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setHbarPolicy(policy);
        }
    }

    /**
     * Sets the vertical scrollbar policy of the PinBoardSkin's ScrollPane.
     *
     * @param policy the ScrollBarPolicy to set
     */
    public void setVBarPolicy(ScrollPane.ScrollBarPolicy policy) {
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
    public ReadOnlyListWrapper<Item> getVisibleItems() {
        return readOnlyVisibleItems;
    }

    /**
     * Clears the PinBoard by removing all items and refreshes the skin.
     * This method must be called from the JavaFX Application Thread.
     */
    public void clear() {
        clear(true);
    }

    /**
     * Clears the PinBoard by removing all items and optionally refreshes the skin.
     * This method must be called from the JavaFX Application Thread.
     *
     * @param refresh flag indicating whether to refresh the skin
     */
    public void clear(boolean refresh) {
        PlatformHelper.checkApplicationThread();
        items.clear();
        areaProperty.set(Rectangle2D.EMPTY);
        if (refresh) {
            refresh();
        }
    }

    /**
     * Refreshes the PinBoard skin.
     */
    public void refresh() {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.refresh(UnaryOperator.identity());
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

    /**
     * Returns the pannable property of the PinBoard.
     * The pannable property determines whether the user can pan the content within the PinBoard.
     *
     * @return The BooleanProperty representing the pannable state of the PinBoard.
     */
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

    public void scrollTo(Item item, double x, double y, double relativeXinVP, double relativeYinVP) {
        scrollTo(new PositionInItem(item, x, y), relativeXinVP, relativeYinVP);
    }

    /**
     * Scrolls the PinBoard to the specified position within an item.
     *
     * @param pos the position within an item to scroll to
     * @param relativeXinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 left, 1 right
     * @param relativeYinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 top, 1 bottom
     */
    public void scrollTo(PositionInItem pos, double relativeXinVP, double relativeYinVP) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollTo(pos, relativeXinVP, relativeYinVP);
        }
    }

    /**
     * Scrolls the PinBoard the specified amount.
     *
     * @param deltaX the amount to scroll in horizontal direction
     * @param deltaY the amount to scroll in vertical direction
     */
    public void scroll(double deltaX, double deltaY) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scroll(deltaX, deltaY);
        }
    }

    /**
     * Scrolls the PinBoard horizontally by the specified amount.
     *
     * @param delta the amount to scroll in the horizontal direction
     */
    public void scrollHorizontal(double delta) {
        scroll(delta, 0);
    }

    /**
     * Scrolls the PinBoard vertically by the specified amount.
     *
     * @param delta the amount to scroll in the vertical direction
     */
    public void scrollVertical(double delta) {
        scroll(0, delta);
    }

    /**
     * Scrolls the PinBoard so that the point (x, y) in the item becomes visible.
     *
     * @param item the item
     * @param x the x-coordinate inside the item
     * @param y the y-coordinate inside the item
     */
    public void scrollIntoView(Item item, double x, double y) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollIntoView(new PositionInItem(item, x, y));
        }
    }

    /**
     * Scrolls the PinBoard to the specified position within an item.
     *
     * <p> The parameters {@code dxBoard} and  {@code dyBoard} are used to determine where the
     * point (x,y) should end up in the viewport. For example, use 0.5 for {@code dxBoard} to center
     * the point horizontally in the viewport.
     *
     * @param x The x-coordinate in <strong>local coordinates</strong> to scroll to
     * @param y The y-coordinate in <strong>local coordinates</strong> to scroll to
     * @param relativeXinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 left, 1 right
     * @param relativeYinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 top, 1 bottom
     */
    public void scrollTo(double x, double y, double relativeXinVP, double relativeYinVP) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollTo(new BoardPosition(x, y), relativeXinVP, relativeYinVP);
        }
    }

    /**
     * Scrolls the PinBoard the specified position into view.
     *
     * @param x the x-position in board coordinates to scroll to
     * @param y the y-position in board coordinates to scroll to
     */
    public void scrollIntoView(double x, double y) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollIntoView(new BoardPosition(x, y));
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
        pinBottom(name, nodeSupplier, dimension, true);
    }

    /**
     * Add item at the bottom, centered horizontally.
     *
     * @param name         item name
     * @param nodeSupplier supplier (factory) for item node
     * @param dimension    item dimension
     * @param refresh      {@code true} to refresh skin after adding item
     */
    public void pinBottom(String name, Supplier<Node> nodeSupplier, Dimension2D dimension, boolean refresh) {
        Rectangle2D boardArea = getArea();
        double xCenter = (boardArea.getMaxX() + boardArea.getMinX()) / 2.0;
        double y = boardArea.getMaxY();
        Rectangle2D area = new Rectangle2D(xCenter - dimension.getWidth() / 2, y, dimension.getWidth(), dimension.getHeight());
        pin(new Item(name, area, nodeSupplier), refresh);
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
     * Pins the given item or collection of items to the PinBoard and refreshes the layout.
     *
     * @param item The item to be pinned. Can be a single item or a collection of items to be pinned together.
     */
    public void pin(Item item) {
        pin(Collections.singleton(item), true);
    }

    /**
     * Pins the given item or collection of items to the PinBoard and optionally refreshes the layout.
     *
     * @param item The item to be pinned. Can be a single item or a collection of items to be pinned together.
     * @param refresh flag indicating whether the skin should be refreshed after pinning
     */
    public void pin(Item item, boolean refresh) {
        pin(Collections.singleton(item), refresh);
    }

    /**
     * Pins the given collection of items to the PinBoard and refreshes the skin.
     *
     * @param itemsToPin The collection of items to be pinned.
     */
    public void pin(Collection<Item> itemsToPin) {
        pin(itemsToPin, true);
    }

    /**
     * Pins the given collection of items to the PinBoard and optionally refreshes the skin.
     *
     * @param itemsToPin The collection of items to be pinned.
     * @param refresh flag indicating whether the skin should be refreshed after pinning
     */
    public void pin(Collection<Item> itemsToPin, boolean refresh) {
        PlatformHelper.checkApplicationThread();

        if (itemsToPin.isEmpty()) {
            return;
        }
        updateArea(itemsToPin);
        items.addAll(itemsToPin);

        if (refresh) {
            refresh();
        }
    }

    /**
     * Sets the content to the given collection of items to the PinBoard and refreshes the skin.
     *
     * @param itemsToPin The collection of items to be pinned.
     */
    public void set(Collection<Item> itemsToPin) {
        PlatformHelper.checkApplicationThread();
        updateArea(itemsToPin);
        items.setAll(itemsToPin);
        refresh();
    }

    /**
     * Re-calculates the content area after items are added.
     * @param addedItems collection of the items added
     */
    private void updateArea(Collection<Item> addedItems) {
        Rectangle2D area = items.isEmpty() ? Rectangle2D.EMPTY : getArea();
        addedItems.stream()
                .map(Item::area)
                .reduce(FxUtil::union)
                .map(r -> FxUtil.union(getArea(), r))
                .ifPresent(r -> {
                    if (!r.equals(getArea())) {
                        areaProperty.set(FxUtil.union(area, r));
                    }
                });
    }

    /**
     * Returns the display scale property of the application.
     * This property represents the current scaling factor for the display.
     *
     * @return the display scale property as a DoubleProperty
     */
    public DoubleProperty displayScaleProperty() {
        return displayScaleProperty;
    }

    /**
     * Sets the display scale of the component.
     *
     * @param scale the new scale value to set for the display.
     *              This value determines the magnification or reduction applied to the component's display.
     */
    public void setDisplayScale(double scale) {
        displayScaleProperty.set(scale);
    }


    /**
     * Retrieves the current value of the display scale.
     *
     * @return the value of the display scale as a Double
     */
    public double getDisplayScale() {
        return displayScaleProperty.get();
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

    /**
     * The Position class represents a position on a PinBoard relative to the pin board's coordinate system.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public record BoardPosition(double x, double y) {}
}


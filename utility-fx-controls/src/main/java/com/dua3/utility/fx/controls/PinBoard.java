/*
 * Copyright (c) 2022. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Pair;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * A JavaFX component where items can be pinned at a position.
 */
public class PinBoard extends Control {

    final ObservableList<Item> items = FXCollections.observableArrayList();
    private final ObjectProperty<Rectangle2D> areaProperty = new SimpleObjectProperty<>(new Rectangle2D(0, 0, 0, 0));

    public PinBoard() {
    }

    public void clear() {
        PlatformHelper.checkApplicationThread();
        items.clear();
        areaProperty.set(new Rectangle2D(0, 0, 0, 0));
    }

    public void refresh() {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.refresh();
        }
    }

    public void dispose() {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.dispose();
        }
    }

    @Override
    protected Skin<PinBoard> createDefaultSkin() {
        return new PinBoardSkin(this);
    }

    public ReadOnlyObjectProperty<Rectangle2D> areaProperty() {
        return areaProperty;
    }

    public ObservableList<Item> getItems() {
        return FXCollections.unmodifiableObservableList(items);
    }

    public Pair<Double, Double> getScrollPosition() {
        if (getSkin() instanceof PinBoardSkin skin) {
            return skin.getScrollPosition();
        } else {
            return Pair.of(0.0, 0.0);
        }
    }

    public void setScrollPosition(Pair<Double, Double> scrollPosition) {
        setScrollPosition(scrollPosition.first(), scrollPosition.second());
    }

    public void setScrollPosition(double hValue, double vValue) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.setScrollPosition(hValue, vValue);
        }
    }

    public void scrollTo(PositionInItem pos) {
        if (getSkin() instanceof PinBoardSkin skin) {
            skin.scrollTo(pos);
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

    public Rectangle2D getArea() {
        return areaProperty.get();
    }

    public void pin(Item item) {
        pin(Collections.singleton(item));
    }

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

    public record Item(String name, Rectangle2D area, Supplier<Node> nodeBuilder) {}

    public record PositionInItem(Item item, double x, double y) {}
}


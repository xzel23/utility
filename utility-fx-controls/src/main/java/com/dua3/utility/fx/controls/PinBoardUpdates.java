package com.dua3.utility.fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import org.jspecify.annotations.Nullable;

record PinBoardUpdates(
        @Nullable Double scale,
        @Nullable Rectangle2D boardArea
) {
    static final PinBoardUpdates EMPTY_UPDATES = new PinBoardUpdates(null, null);

    public PinBoardUpdates withScale(double scale) {
        return new PinBoardUpdates(scale, boardArea);
    }

    public PinBoardUpdates withArea(Rectangle2D boardArea) {
        return new PinBoardUpdates(scale, boardArea);
    }

    /**
     * Sets the display scale of the PinBoard. This method adjusts the scaling of the content,
     * recalculates viewport dimensions, and maintains the current scroll positions relative to the content.
     */
    void applyDisplayScale(PinBoardSkin target) {
        if (scale == null) {
            return;
        }

        ScrollPosition oldPos = target.getScrollPosition();
        Rectangle2D boardArea = target.getSkinnable().getArea();
        Rectangle2D viewportBefore = target.getViewPortInBoardCoordinates();

        double oldX = boardArea.getMinX() + oldPos.hValue() * Math.max(0, boardArea.getWidth() - viewportBefore.getWidth());
        double oldY = boardArea.getMinY() + oldPos.vValue() * Math.max(0, boardArea.getHeight() - viewportBefore.getHeight());

        Bounds vp = target.scrollPane.getViewportBounds();
        Rectangle2D viewportAfter = new Rectangle2D(
                (boardArea.getMinX() - vp.getMinX() / scale),
                (boardArea.getMinY() - vp.getMinY() / scale),
                vp.getWidth() / scale,
                vp.getHeight() / scale
        );

        double hValue = (oldX - boardArea.getMinX()) / (boardArea.getWidth() - viewportAfter.getWidth());
        double vValue = (oldY - boardArea.getMinY()) / (boardArea.getHeight() - viewportAfter.getHeight());

        target.pane.setScaleX(scale);
        target.pane.setScaleY(scale);

        target.setScrollPosition(hValue, vValue);
    }

    void applyBoardArea(PinBoardSkin target) {
        if (boardArea == null) {
            return;
        }

        target.pane.setMinSize(boardArea.getWidth(), boardArea.getHeight());
    }
}

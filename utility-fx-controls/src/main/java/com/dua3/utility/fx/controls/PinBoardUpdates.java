package com.dua3.utility.fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import org.jspecify.annotations.Nullable;

record PinBoardUpdates(
        @Nullable Double scale,
        @Nullable Rectangle2D boardArea,
        @Nullable ScrollTarget scrollTarget
) {
    static final PinBoardUpdates EMPTY_UPDATES = new PinBoardUpdates(null, null, null);

    public PinBoardUpdates withScale(double scale) {
        return new PinBoardUpdates(scale, boardArea, scrollTarget);
    }

    public PinBoardUpdates withArea(Rectangle2D boardArea) {
        return new PinBoardUpdates(scale, boardArea, scrollTarget);
    }

    public PinBoardUpdates withScrollTarget(ScrollTarget scrollTarget) {
        if (this.scrollTarget == null || scrollTarget.itemPos != null || scrollTarget.boardPos != null) {
            // if no scroll target is set, or the new one is an item or board position, use the new one
            return new PinBoardUpdates(scale, boardArea, scrollTarget);
        }
        // if the new one is only a translation, apply it to the current one
        ScrollTarget merged = new ScrollTarget(
                this.scrollTarget.itemPos,
                this.scrollTarget.boardPos,
                this.scrollTarget.dxBoard,
                this.scrollTarget.dyBoard,
                this.scrollTarget.dxVP + scrollTarget.dxVP,
                this.scrollTarget.dyBoard + scrollTarget.dyVP
        );
        return new PinBoardUpdates(scale, boardArea, scrollTarget);
    }

    record ScrollTarget(
            PinBoard.@Nullable PositionInItem itemPos,
            PinBoard.@Nullable BoardPosition boardPos,
            double dxBoard, double dyBoard,
            double dxVP, double dyVP
    ) {}

    void apply(PinBoardSkin skin) {
        if (this == EMPTY_UPDATES) {
            return;
        }

        PinBoard.BoardPosition bp = skin.getPositionInBoard(0, 0);

        double oldScale = skin.getDisplayScale();
        double newScale = oldScale;

        // apply area change
        if (boardArea != null) {
            skin.pane.setMinSize(boardArea.getWidth(), boardArea.getHeight());
        }

        // apply scaling
        if (scale != null) {
            newScale = scale;
            skin.pane.setScaleX(newScale);
            skin.pane.setScaleY(newScale);
        }

        // apply scroll
        if (scrollTarget != null) {
            if (scrollTarget.itemPos != null) {
                bp = skin.toBoardPosition(scrollTarget.itemPos);
            } else if (scrollTarget.boardPos != null) {
                bp = scrollTarget.boardPos;
            }

            bp = new PinBoard.BoardPosition(
                    bp.x() + scrollTarget.dxBoard + scrollTarget.dxVP / oldScale,
                    bp.y() + scrollTarget.dyBoard + scrollTarget.dxVP / oldScale
            );
        }

        Bounds bounds = skin.getViewportBounds();
        Rectangle2D area = skin.getSkinnable().getArea();

        double divX = Math.max(0, (area.getWidth() - bounds.getWidth() / newScale));
        double hvalue = divX > 1.0E-8 ? (bp.x() - area.getMinX()) / divX : 0.0;

        double divY = Math.max(0, (area.getHeight() - bounds.getHeight() / newScale));
        double vvalue = divY > 1.0E-8 ? (bp.y() - area.getMinY()) / divY : 0.0;

        skin.setScrollPosition(hvalue, vvalue);
    }
}

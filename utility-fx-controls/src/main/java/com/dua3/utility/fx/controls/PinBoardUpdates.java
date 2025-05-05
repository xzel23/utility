package com.dua3.utility.fx.controls;

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
        Rectangle2D vpBoard = skin.getViewPortInBoardCoordinates();
        double vpWidth = skin.getViewportBounds().getWidth();
        double vpHeight = skin.getViewportBounds().getHeight();
        Rectangle2D board = skin.getSkinnable().getArea();
        double displayScale = skin.getDisplayScale();

        // apply area change
        if (boardArea != null) {
            skin.pane.setMinSize(boardArea.getWidth(), boardArea.getHeight());
            board = boardArea;
        }

        // apply scaling
        if (scale != null) {
            double newScale = scale;
            if (newScale != displayScale) {
                skin.pane.setScaleX(newScale);
                skin.pane.setScaleY(newScale);
                displayScale = newScale;
            }
        }

        // apply scroll
        if (scrollTarget != null) {
            if (scrollTarget.itemPos != null) {
                Rectangle2D itemArea = scrollTarget.itemPos.item().area();

                double xBoard = itemArea.getMinX() + scrollTarget.itemPos.x();
                double yBoard = itemArea.getMinY() + scrollTarget.itemPos.y();

                bp = new PinBoard.BoardPosition(xBoard, yBoard);
            } else if (scrollTarget.boardPos != null) {
                bp = scrollTarget.boardPos;
            }

            bp = new PinBoard.BoardPosition(
                    bp.x() + scrollTarget.dxBoard + scrollTarget.dxVP / displayScale,
                    bp.y() + scrollTarget.dyBoard + scrollTarget.dxVP / displayScale
            );
        }

        doScroll(skin, board, vpWidth, vpHeight, displayScale, bp);
    }

    private static void doScroll(
            PinBoardSkin skin,
            Rectangle2D board,
            double vpWidth,
            double vpHeight,
            double displayScale,
            PinBoard.BoardPosition bp
    ) {
        double hvalue = Math.clamp((bp.x() - board.getMinX()) / (board.getWidth() - vpWidth / displayScale), 0.0, 1.0);
        double vvalue = Math.clamp((bp.y() - board.getMinY()) / (board.getHeight() - vpHeight / displayScale), 0.0, 1.0);
        skin.setScrollPosition(hvalue, vvalue);
    }
}

package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A Helper class to make any JavaFX {@link Region} resizable by dragging its borders with the mouse.
 * <p>
 * Code based on
 * <a href="https://github.com/grubbcc/anagrams/blob/browser/client/java/client/DragResizer.java">https://github.com/grubbcc/../DragResizer.java</a>
 * and
 * <a href="https://gist.github.com/andytill/4369729">https://gist.github.com/andytill/4369729</a>
 */
final class DragResizer {

    /**
     * The margin (in pixels) around the control that a user can click to resize the region.
     */
    private final Region region;
    private final Set<Position> borders;
    private final int resizeMargin;

    private boolean draggingTop = false;
    private boolean draggingRight = false;
    private boolean draggingBottom = false;
    private boolean draggingLeft = false;

    private DragResizer(Region region, int resizeMargin, Position... borders) {
        this.region = region;
        this.resizeMargin = resizeMargin;
        this.borders = EnumSet.noneOf(Position.class);
        this.borders.addAll(List.of(borders));
    }

    /**
     * Make region resizable.
     *
     * @param region       the region to make resizable
     * @param resizeMargin the margin in pixels
     * @param borders      the borders where dragging should be possible
     */
    public static void makeResizable(Region region, int resizeMargin, Position... borders) {
        final DragResizer resizer = new DragResizer(region, resizeMargin, borders);

        FxUtil.addMouseEventHandler(region, MouseEvent.MOUSE_PRESSED, resizer::mousePressed);
        FxUtil.addMouseEventHandler(region, MouseEvent.MOUSE_DRAGGED, resizer::mouseDragged);
        FxUtil.addMouseEventHandler(region, MouseEvent.MOUSE_MOVED, resizer::mouseOver);
        FxUtil.addMouseEventHandler(region, MouseEvent.MOUSE_RELEASED, resizer::mouseReleased);
    }

    private boolean isDragging() {
        return draggingTop || draggingRight || draggingBottom || draggingLeft;
    }

    private void mousePressed(MouseEvent event) {
        draggingTop = isInDraggableZoneTop(event);
        draggingRight = isInDraggableZoneRight(event);
        draggingBottom = isInDraggableZoneBottom(event);
        draggingLeft = isInDraggableZoneLeft(event);

        if (isDragging()) {
            event.consume();
        }
    }

    private void mouseDragged(MouseEvent event) {
        if (isDragging()) {
            event.consume();
        }

        if (draggingBottom) {
            resizeBottom(event);
        }
        if (draggingRight) {
            resizeRight(event);
        }
        if (draggingTop) {
            resizeTop(event);
        }
        if (draggingLeft) {
            resizeLeft(event);
        }
    }

    /**
     * Set the cursor to the appropriate type.
     */
    private void mouseOver(MouseEvent event) {
        if (isInDraggableZoneTop(event) || draggingTop) {
            if (isInDraggableZoneRight(event) || draggingRight) {
                region.setCursor(Cursor.NE_RESIZE);
            } else if (isInDraggableZoneLeft(event) || draggingLeft) {
                region.setCursor(Cursor.NW_RESIZE);
            } else {
                region.setCursor(Cursor.N_RESIZE);
            }
        } else if (isInDraggableZoneBottom(event) || draggingBottom) {
            if (isInDraggableZoneRight(event) || draggingRight) {
                region.setCursor(Cursor.SE_RESIZE);
            } else if (isInDraggableZoneLeft(event) || draggingLeft) {
                region.setCursor(Cursor.SW_RESIZE);
            } else {
                region.setCursor(Cursor.S_RESIZE);
            }
        } else if (isInDraggableZoneRight(event) || draggingRight) {
            region.setCursor(Cursor.E_RESIZE);
        } else if (isInDraggableZoneLeft(event) || draggingLeft) {
            region.setCursor(Cursor.W_RESIZE);
        } else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    private void mouseReleased(MouseEvent event) {
        draggingTop = draggingRight = draggingBottom = draggingLeft = false;
        region.setCursor(Cursor.DEFAULT);
    }

    private boolean isInDraggableZoneTop(MouseEvent event) {
        return borders.contains(Position.TOP) && event.getY() < resizeMargin;
    }

    private boolean isInDraggableZoneRight(MouseEvent event) {
        return borders.contains(Position.RIGHT) && event.getX() > (region.getWidth() - resizeMargin);
    }

    private boolean isInDraggableZoneBottom(MouseEvent event) {
        return borders.contains(Position.BOTTOM) && event.getY() > (region.getHeight() - resizeMargin);
    }

    private boolean isInDraggableZoneLeft(MouseEvent event) {
        return borders.contains(Position.LEFT) && event.getX() < resizeMargin;
    }

    private void resizeBottom(MouseEvent event) {
        region.setMinHeight(event.getY());
    }

    private void resizeRight(MouseEvent event) {
        region.setMinWidth(event.getX());
    }

    private void resizeTop(MouseEvent event) {
        double prevMin = region.getMinHeight();
        region.setMinHeight(region.getMinHeight() - event.getY());

        if (region.getMinHeight() < region.getPrefHeight()) {
            region.setMinHeight(region.getPrefHeight());
            region.setTranslateY(region.getTranslateY() - (region.getPrefHeight() - prevMin));
            return;
        }

        if (region.getMinHeight() > region.getPrefHeight() || event.getY() < 0) {
            region.setTranslateY(region.getTranslateY() + event.getY());
        }
    }

    private void resizeLeft(MouseEvent event) {
        double prevMin = region.getMinWidth();
        region.setMinWidth(region.getMinWidth() - event.getX());

        if (region.getMinWidth() < region.getPrefWidth()) {
            region.setMinWidth(region.getPrefWidth());
            region.setTranslateX(region.getTranslateX() - (region.getPrefWidth() - prevMin));
            return;
        }

        if (region.getMinWidth() > region.getPrefWidth() || event.getX() < 0) {
            region.setTranslateX(region.getTranslateX() + event.getX());
        }
    }
}

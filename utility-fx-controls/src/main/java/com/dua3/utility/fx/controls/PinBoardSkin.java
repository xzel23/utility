package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxRefresh;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

class PinBoardSkin extends SkinBase<PinBoard> {

    private static final Logger LOG = LogManager.getLogger(PinBoardSkin.class);

    final FxRefresh refresher;
    final AnchorPane pane = new AnchorPane();
    final Group group = new Group(pane);
    final ScrollPane scrollPane = new ScrollPane(group);
    final ObservableList<PinBoard.Item> visibleItems = FXCollections.observableArrayList();

    PinBoardSkin(PinBoard pinBoard) {
        super(pinBoard);

        this.refresher = FxRefresh.create(
                LangUtil.defaultToString(this),
                () -> PlatformHelper.runLater(this::updateNodes),
                pinBoard
        );

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().setAll(scrollPane);

        pinBoard.getItems().addListener((ListChangeListener.Change<?> c) -> refresh());
        pane.layoutBoundsProperty().addListener((o) -> refresh());
        scrollPane.hvalueProperty().addListener((h) -> refresh());
        scrollPane.vvalueProperty().addListener((v) -> refresh());
        scrollPane.widthProperty().addListener((e) -> refresh());
        scrollPane.heightProperty().addListener((e) -> refresh());
        scrollPane.viewportBoundsProperty().addListener((v, o, n) -> refresh());

        pinBoard.displayScaleProperty().addListener((v, o, n) ->
                refresh(u -> u.withScale(n.doubleValue()))
        );
        pinBoard.areaProperty().addListener((v, o, n) ->
                refresh(u -> u.withArea(n))
        );

        // enable/disable refresher
        refresher.setActive(true);
        refresh(u -> u.withScale(pinBoard.getDisplayScale()));
    }

    private final AtomicReference<PinBoardUpdates> updates = new AtomicReference<>(PinBoardUpdates.EMPTY_UPDATES);

    private void updateNodes() {
        LOG.trace("updateNodes()");
        PlatformHelper.checkApplicationThread();

        refresher.setActive(false);
        try {
            PinBoardUpdates u = updates.getAndSet(PinBoardUpdates.EMPTY_UPDATES);

            u.applyBoardArea(this);
            u.applyDisplayScale(this);

            PinBoard board = getSkinnable();
            Rectangle2D boardArea = board.getArea();

            Rectangle2D viewPort = getViewPort();
            Rectangle2D viewportInLocal = getViewPortInBoardCoordinates();

            double dx = Math.max(0, viewportInLocal.getWidth() - boardArea.getWidth()) / 2.0 - boardArea.getMinX();
            double dy = Math.max(0, viewportInLocal.getHeight() - boardArea.getHeight()) / 2.0 - boardArea.getMinY();

            // populate the pane with nodes of visible items
            List<PinBoard.Item> visibleItems = new ArrayList<>();
            List<Node> visibleNodes = new ArrayList<>();
            board.items.forEach(item -> {
                if (item.area().intersects(viewportInLocal)) {
                    LOG.debug("item is visible: {}", item::name);
                    Rectangle2D itemArea = item.area();
                    Node node = item.nodeBuilder().get();
                    node.setTranslateX(dx + itemArea.getMinX());
                    node.setTranslateY(dy + itemArea.getMinY());
                    visibleItems.add(item);
                    visibleNodes.add(node);
                }
            });

            pane.setMinSize(boardArea.getWidth(), boardArea.getHeight());
            pane.getChildren().setAll(visibleNodes);
            board.visibleItems.setAll(visibleItems);
        } finally {
            refresher.setActive(true);
        }
    }

    void refresh() {
        refresh(UnaryOperator.identity());
    }

    void refresh(UnaryOperator<PinBoardUpdates> update) {
        updates.updateAndGet(update);
        refresher.refresh();
    }

    private Rectangle2D getViewPort() {
        Bounds vpBounds = scrollPane.getViewportBounds();
        return new Rectangle2D(-vpBounds.getMinX(), -vpBounds.getMinY(), vpBounds.getWidth(), vpBounds.getHeight());
    }

    @Override
    public void dispose() {
        refresher.stop();
        super.dispose();
    }

    public DoubleProperty scrollHValueProperty() {
        return scrollPane.hvalueProperty();
    }

    public DoubleProperty scrollVValueProperty() {
        return scrollPane.vvalueProperty();
    }

    public ScrollPosition getScrollPosition() {
        return new ScrollPosition(scrollPane.getHvalue(), scrollPane.getVvalue());
    }

    public void setScrollPosition(ScrollPosition scrollPosition) {
        setScrollPosition(scrollPosition.hValue(), scrollPosition.vValue());
    }

    public void setScrollPosition(double hValue, double vValue) {
        scrollPane.setHvalue(hValue);
        scrollPane.setVvalue(vValue);
    }

    /**
     * Get Item at point and coordinates relative to item.
     *
     * @param xViewport x-coordinate (relative to board)
     * @param yViewport y-coordinate (relative to board)
     * @return Optional containing the item at (x,y) and the coordinates relative to the item area
     */
    public Optional<PinBoard.PositionInItem> getPositionInItem(double xViewport, double yViewport) {
        Rectangle2D vp = getViewPortInBoardCoordinates();
        double x = xViewport + vp.getMinX();
        double y = yViewport + vp.getMinY();
        List<PinBoard.Item> items = List.copyOf(getSkinnable().visibleItems);
        for (PinBoard.Item item : items) {
            Rectangle2D a = item.area();
            if (a.contains(x, y)) {
                return Optional.of(new PinBoard.PositionInItem(item, x - a.getMinX(), y - a.getMinY()));
            }
        }
        return Optional.empty();
    }

    Rectangle2D getViewPortInBoardCoordinates() {
        Bounds vp = scrollPane.getViewportBounds();
        double scale = Math.max(1.0E-8, getDisplayScale());
        Rectangle2D boardArea = getSkinnable().getArea();
        return new Rectangle2D(
                (boardArea.getMinX() - vp.getMinX() / scale),
                (boardArea.getMinY() - vp.getMinY() / scale),
                vp.getWidth() / scale,
                vp.getHeight() / scale
        );
    }


    /**
     * Converts local coordinates relative to the PinBoard area into board coordinates.
     *
     * @param xLocal the x-coordinate in local space relative to the PinBoard area
     * @param yLocal the y-coordinate in local space relative to the PinBoard area
     * @return a {@link PinBoard.BoardPosition} object representing the corresponding coordinates on the board
     */
    public PinBoard.BoardPosition toBoardPosition(double xLocal, double yLocal) {
        Rectangle2D area = getSkinnable().getArea();
        return new PinBoard.BoardPosition(area.getMinX() + xLocal, area.getMinY() + yLocal);
    }

    /**
     * Converts a given {@link PinBoard.PositionInItem} to a {@link PinBoard.BoardPosition}.
     *
     * @param pos the position relative to an item on the PinBoard, containing the item and its area
     *            along with the x and y coordinates relative to the item.
     * @return a {@link PinBoard.BoardPosition} representing the equivalent position on the board.
     */
    public PinBoard.BoardPosition toBoardPosition(PinBoard.PositionInItem pos) {
        Rectangle2D area = pos.item().area();

        double xBoard = area.getMinX() + pos.x();
        double yBoard = area.getMinY() + pos.y();

        return new PinBoard.BoardPosition(xBoard, yBoard);
    }

    /**
     * Scrolls the PinBoard to the position specified by {@link PinBoard.PositionInItem} with a relative translation
     * in viewport coordinates.
     *
     * @param pos the position within an item on the PinBoard. This includes the item information
     *            and its coordinates, relative to its area on the board.
     * @param relativeXinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 left, 1 right
     * @param relativeYinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 top, 1 bottom
     */
    public void scrollTo(PinBoard.PositionInItem pos, double relativeXinVP, double relativeYinVP) {
        scrollTo(toBoardPosition(pos), relativeXinVP, relativeYinVP);
    }

    /**
     * Scrolls the PinBoard to the position specified by {@link PinBoard.BoardPosition} translation
     * in viewport coordinates.
     *
     * @param pos the position within an item on the PinBoard. This includes the item information
     *            and its coordinates, relative to its area on the board.
     * @param relativeXinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 left, 1 right
     * @param relativeYinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 top, 1 bottom
     */
    public void scrollTo(PinBoard.BoardPosition pos, double relativeXinVP, double relativeYinVP) {
        scrollTo(pos.x(), pos.y(), relativeXinVP, relativeYinVP);
    }

    /**
     * Adjusts the scroll position of the PinBoard by the specified horizontal and vertical deltas.
     *
     * @param deltaX the horizontal scroll offset to apply
     * @param deltaY the vertical scroll offset to apply
     */
    public void scroll(double deltaX, double deltaY) {
        ScrollPosition pos = getScrollPosition();
        Rectangle2D area = getSkinnable().getArea();
        Bounds vpBounds = scrollPane.getViewportBounds();

        double spWidth = scrollPane.getHmax() - scrollPane.getHmin();
        double spHeight = scrollPane.getVmax() - scrollPane.getVmin();

        double scWidth = area.getWidth() - vpBounds.getWidth();
        double scHeight = area.getHeight() - vpBounds.getHeight();

        double factorH = spWidth / scWidth;
        double factorV = spHeight / scHeight;

        setScrollPosition(
                pos.hValue() - deltaX * factorH,
                pos.vValue() - deltaY * factorV
        );
    }

    /**
     * Scrolls the specified position relative to an item into the visible area of the PinBoard.
     * The method internally converts the given position to a board position and scrolls it into view.
     *
     * @param pos the position within an item on the PinBoard, which includes the item reference
     *            and coordinates relative to the item area.
     */
    public void scrollIntoView(PinBoard.PositionInItem pos) {
        scrollIntoView(toBoardPosition(pos));
    }

    /**
     * Scrolls the PinBoard view to ensure that the specified board position
     * is brought into the visible area.
     *
     * @param pos The position on the PinBoard to scroll into view, represented
     *            as a {@link PinBoard.BoardPosition}.
     */
    public void scrollIntoView(PinBoard.BoardPosition pos) {
        scrollIntoView(pos.x(), pos.y());
    }

    /**
     * Scrolls the PinBoard upper left corner to the specified board coordinates.
     *
     * @param x The x-coordinate in <strong>local coordinates</strong> to scroll to
     * @param y The y-coordinate in <strong>local coordinates</strong> to scroll to
     * @param relativeXinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 left, 1 right
     * @param relativeYinVP the relative position inside the viewport, a value between 0 and 1, i.e., 0 top, 1 bottom
     */
    private void scrollTo(double x, double y, double relativeXinVP, double relativeYinVP) {
        LOG.debug("scrollTo({}, {}, {}, {})", x, y, relativeXinVP, relativeYinVP);

        Rectangle2D boardArea = getSkinnable().getArea();

        if (boardArea.getWidth() == 0 || boardArea.getHeight() == 0) {
            return;
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();

        double tx = Math.clamp(relativeXinVP, 0.0, 1.0) * viewportBounds.getWidth();
        double ty = Math.clamp(relativeYinVP, 0.0, 1.0) * viewportBounds.getHeight();
        double sx = calcScrollPosition(x - tx, boardArea.getMinX(), boardArea.getMaxX(), viewportBounds.getWidth());
        double sy = calcScrollPosition(y - ty, boardArea.getMinY(), boardArea.getMaxY(), viewportBounds.getHeight());

        setScrollPosition(sx, sy);
    }

    /**
     * Scrolls the given position into view.
     *
     * @param xBoard The x-coordinate
     * @param yBoard The y-coordinate
     */
    public void scrollIntoView(double xBoard, double yBoard) {
        LOG.debug("scrollIntoViewInBoardCoordinates({}, {})", xBoard, yBoard);

        Rectangle2D boardArea = getSkinnable().getArea();

        if (boardArea.getWidth() == 0 || boardArea.getHeight() == 0) {
            return;
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();

        Rectangle2D viewPortInBoardCoordinates = getViewPortInBoardCoordinates();
        if (xBoard < viewPortInBoardCoordinates.getMinX()) {
            scrollPane.setHvalue(calcScrollPosition(xBoard, boardArea.getMinX(), boardArea.getMaxX(), viewportBounds.getWidth()));
        } else if (xBoard > viewPortInBoardCoordinates.getMaxX()) {
            scrollPane.setHvalue(calcScrollPosition(xBoard - viewportBounds.getWidth(), boardArea.getMinX(), boardArea.getMaxX(), viewportBounds.getWidth()));
        }
        if (yBoard < viewPortInBoardCoordinates.getMinY()) {
            scrollPane.setVvalue(calcScrollPosition(yBoard, boardArea.getMinY(), boardArea.getMaxY(), viewportBounds.getHeight()));
        } else if (yBoard > viewPortInBoardCoordinates.getMaxY()) {
            scrollPane.setVvalue(calcScrollPosition(yBoard - viewportBounds.getHeight(), boardArea.getMinY(), boardArea.getMaxY(), viewportBounds.getHeight()));
        }
    }

    /**
     * Calculates the scroll position of a content within a viewport. The scroll position is
     * expressed as a value between 0 and 1, where 0 indicates the start of the scrollable range
     * and 1 indicates the end of the scrollable range.
     *
     * @param c the position to scroll to
     * @param tMin the minimum position of the scrollable content
     * @param tMax the maximum position of the scrollable content
     * @param viewableSize the size of the viewport
     * @return a value between 0 and 1 representing the normalized scroll position
     */
    private static double calcScrollPosition(double c, double tMin, double tMax, double viewableSize) {
        double totalSize = tMax - tMin;
        double scrollableSize = totalSize - viewableSize;

        if (scrollableSize <= 0) {
            // viewport bigger than content => no scrolling
            return 0;
        }

        if (c >= scrollableSize) {
            // position is visible when scrolled to the end
            return 1;
        }

        return (c - tMin) / scrollableSize;
    }

    public void setHbarPolicy(ScrollPane.ScrollBarPolicy policy) {
        scrollPane.setHbarPolicy(policy);
    }

    public void setVbarPolicy(ScrollPane.ScrollBarPolicy policy) {
        scrollPane.setVbarPolicy(policy);
    }

    public BooleanProperty pannableProperty() {
        return scrollPane.pannableProperty();
    }

    /**
     * Get the current display scale.
     *
     * @return the current display scale
     */
    private double getDisplayScale() {
        return pane.getScaleX();
    }
}

package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxRefresh;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class PinBoardSkin extends SkinBase<PinBoard> {

    private static final Logger LOG = LogManager.getLogger(PinBoardSkin.class);
    private final FxRefresh refresher;
    private final AnchorPane pane = new AnchorPane();
    private final ScrollPane scrollPane = new ScrollPane(pane);

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

        pinBoard.areaProperty().addListener((v, o, n) -> {
            pane.setMinWidth(n.getWidth());
            pane.setMinHeight(n.getHeight());
        });

        pinBoard.getItems().addListener((ListChangeListener.Change<?> c) -> refresh());
        pane.layoutBoundsProperty().addListener((o) -> refresh());
        scrollPane.hvalueProperty().addListener((h) -> refresh());
        scrollPane.vvalueProperty().addListener((v) -> refresh());
        scrollPane.widthProperty().addListener((e) -> refresh());
        scrollPane.heightProperty().addListener((e) -> refresh());
        scrollPane.viewportBoundsProperty().addListener((v, o, n) -> refresh());

        // enable/disable refresher
        refresher.setActive(true);
    }

    private void updateNodes() {
        LOG.trace("updateNodes()");

        PlatformHelper.checkApplicationThread();

        PinBoard board = getSkinnable();

        Rectangle2D viewPort = getViewPort();
        Rectangle2D boardArea = board.getArea();

        double dx = Math.max(0, viewPort.getWidth() - boardArea.getWidth()) / 2.0 - boardArea.getMinX();
        double dy = Math.max(0, viewPort.getHeight() - boardArea.getHeight()) / 2.0 - boardArea.getMinY();

        Rectangle2D viewportInLocal = new Rectangle2D(viewPort.getMinX() + boardArea.getMinX(), viewPort.getMinY() + boardArea.getMinY(), viewPort.getWidth(), viewPort.getHeight());

        // populate pane with nodes of visible items
        List<Node> nodes = new ArrayList<>(board.items) // copy list to avoid concurrent modification
                .stream()
                .filter(item -> item.area().intersects(viewportInLocal))
                .map(item -> {
                    LOG.debug("item is visible: {}", item.name());
                    Rectangle2D itemArea = item.area();
                    Node node = item.nodeBuilder().get();
                    node.setTranslateX(dx + itemArea.getMinX());
                    node.setTranslateY(dy + itemArea.getMinY());
                    return node;
                })
                .toList();

        pane.setMinWidth(boardArea.getWidth());
        pane.setMinHeight(boardArea.getHeight());
        pane.getChildren().setAll(nodes);
    }

    void refresh() {
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
        Rectangle2f vp =getViewPortInBoardCoordinates();
        double x = xViewport + vp.xMin();
        double y = yViewport + vp.yMin() ;
        List<PinBoard.Item> items = new ArrayList<>(getSkinnable().getItems());
        for (PinBoard.Item item : items) {
            Rectangle2D a = item.area();
            if (a.contains(x, y)) {
                return Optional.of(new PinBoard.PositionInItem(item, x - a.getMinX(), y - a.getMinY()));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of items from the PinBoard that are visible within the current viewport.
     *
     * @return a list of PinBoard.Item objects that intersect with the viewport area.
     */
    public List<PinBoard.Item> getVisibleItems() {
        List<PinBoard.Item> visibleItems = new ArrayList<>();
        Rectangle2f b = getViewPortInBoardCoordinates();
        List<PinBoard.Item> items = new ArrayList<>(getSkinnable().getItems());
        for (PinBoard.Item item : items) {
            Rectangle2f a = FxUtil.convert(item.area());
            if (a.intersects(b)) {
                visibleItems.add(item);
            }
        }
        return visibleItems;
    }

    private Rectangle2f getBoardArea() {
        return FxUtil.convert(getSkinnable().getArea());
    }

    private Rectangle2f getViewPortInBoardCoordinates() {
        Bounds vp = scrollPane.getViewportBounds();
        Rectangle2f boardArea = getBoardArea();
        return new Rectangle2f(
                (float) (boardArea.x() - vp.getMinX()),
                (float) (boardArea.y() - vp.getMinY()),
                (float) vp.getWidth(),
                (float) vp.getHeight()
        );
    }

    public void scrollTo(PinBoard.PositionInItem pos) {
        LOG.debug("scrollTo({})", pos);

        Rectangle2D area = pos.item().area();
        double xBoard = area.getMinX() + pos.x();
        double yBoard = area.getMinY() + pos.y();
        scrollToBoardCoordinates(xBoard, yBoard);
    }

    public void scrollIntoView(PinBoard.PositionInItem pos) {
        LOG.debug("scrollIntoView({})", pos);

        Rectangle2D area = pos.item().area();
        double xBoard = area.getMinX() + pos.x();
        double yBoard = area.getMinY() + pos.y();
        scrollIntoViewInBoardCoordinates(xBoard, yBoard);
    }

    /**
     * Scrolls the PinBoard upper left corner to the specified coordinates.
     *
     * @param xBoard The x-coordinate to scroll to
     * @param yBoard The y-coordinate to scroll to
     */
    public void scrollToBoardCoordinates(double xBoard, double yBoard) {
        LOG.debug("scrollToBoardCoordinates({}, {})", xBoard, yBoard);

        Rectangle2D boardArea = getSkinnable().getArea();

        if (boardArea.getWidth() == 0 || boardArea.getHeight() == 0) {
            return;
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();

        double sx = calcScrollPosition(xBoard, boardArea.getMinX(), boardArea.getMaxX(), viewportBounds.getWidth());
        double sy = calcScrollPosition(yBoard, boardArea.getMinY(), boardArea.getMaxY(), viewportBounds.getHeight());

        setScrollPosition(sx, sy);
    }

    /**
     * Scrolls the given position into view.
     *
     * @param xBoard The x-coordinate
     * @param yBoard The y-coordinate
     */
    public void scrollIntoViewInBoardCoordinates(double xBoard, double yBoard) {
        LOG.debug("scrollIntoViewInBoardCoordinates({}, {})", xBoard, yBoard);

        Rectangle2D boardArea = getSkinnable().getArea();

        if (boardArea.getWidth() == 0 || boardArea.getHeight() == 0) {
            return;
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();

        Rectangle2f viewPortInBoardCoordinates = getViewPortInBoardCoordinates();
        if (xBoard < viewPortInBoardCoordinates.x() || xBoard > viewPortInBoardCoordinates.x() + viewPortInBoardCoordinates.width()) {
            scrollPane.setHvalue(calcScrollPosition(xBoard, boardArea.getMinX(), boardArea.getMaxX(), viewportBounds.getWidth()));
        }
        if (yBoard < viewPortInBoardCoordinates.y() || yBoard > viewPortInBoardCoordinates.y() + viewPortInBoardCoordinates.height()) {
            scrollPane.setVvalue(calcScrollPosition(yBoard, boardArea.getMinY(), boardArea.getMaxY(), viewportBounds.getHeight()));
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
}

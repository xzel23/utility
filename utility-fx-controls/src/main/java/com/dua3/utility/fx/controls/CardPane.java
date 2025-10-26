package com.dua3.utility.fx.controls;

import com.dua3.utility.lang.LangUtil;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * CardPane is a custom layout component that manages a collection of "cards" (Node elements)
 * and allows switching between them by name. It utilizes a {@link StackPane} to display the cards,
 * ensuring only the desired card is visible at a time.
 */
public class CardPane extends Pane {

    /**
     * Logger instance
     */
    private static final Logger LOG = LogManager.getLogger(CardPane.class);

    /**
     * A {@link StackPane} used as the central container to manage and display
     * a collection of card-like {@link Node} elements. It ensures only one card
     * is visible at a time.
     * <p>
     * This variable serves as the primary visual layout component for the
     * {@link CardPane} class.
     */
    private final StackPane content;
    /**
     * A map that stores card names as keys and their associated {@link Node} objects as values.
     * The cards are stored in insertion order, facilitating predictable iteration.
     */
    private final LinkedHashMap<String, Node> cards = new LinkedHashMap<>();
    /**
     * Stores the name of the currently active card in the {@code CardPane}.
     */
    private @Nullable String current = null;

    /**
     * Constructs a new CardPane instance.
     *
     * Initializes the CardPane with a {@link StackPane} as its content container.
     * The content pane is used to hold all child nodes (cards) and manages their visibility,
     * ensuring only the intended card is shown at a time.
     */
    public CardPane() {
        super();
        this.content = new StackPane();
        getChildren().setAll(content);

        LOG.trace("CardPane created");
    }

    /**
     * Adds a new card to the collection of cards managed by the CardPane.
     * If a card with the specified name already exists, the method will throw an exception
     * and no modification to the existing cards will occur.
     *
     * @param name the unique name to associate with the card being added
     * @param card the Node representing the card to add
     * @throws IllegalArgumentException if a card with the specified name already exists
     */
    public void addCard(String name, Node card) {
        LOG.trace("addCard({}, {})", name, card);

        Node old = cards.putIfAbsent(name, card);
        LangUtil.check(old == null, "card with name %s already exists", name);
    }

    /**
     * Displays the card associated with the specified name by bringing it
     * to the front of the content container. If the card exists, it becomes
     * the currently active card.
     *
     * @param name the unique name of the card to display
     * @return {@code true} if the card with the specified name exists and
     *         is successfully shown, {@code false} otherwise
     */
    public boolean show(String name) {
        LOG.debug("show('{}'): active={}, total children={}", name,
                content.getChildren().stream().filter(Node::isManaged).count(),
                content.getChildren().size());

        Node card = cards.get(name);
        if (card == null) {
            return false;
        }

        current = name;
        content.getChildren().setAll(card);

        return true;
    }
    /**
     * Returns the currently displayed card if one exists. The current card is the last node
     * in the internal {@code StackPane}'s children list.
     *
     * @return an {@code Optional} containing the currently visible {@code Node}, or an empty {@code Optional}
     *         if no cards are present in the {@code StackPane}.
     */
    public Optional<Node> getCurrentCard() {
        return Optional.ofNullable(cards.get(current));
    }

    /**
     * Retrieves the name of the currently displayed card, if one is present.
     *
     * @return an {@code Optional<String>} containing the name of the currently active card,
     *         or an empty {@code Optional} if no card is currently displayed.
     */
    public Optional<String> getCurrentCardName() {
        return Optional.ofNullable(current);
    }

    @Override
    public @Nullable Orientation getContentBias() {
        // If any card has VERTICAL bias (pref width depends on height), favor that.
        boolean vertical = cards.values().stream()
                .map(Node::getContentBias)
                .anyMatch(bias -> bias == Orientation.VERTICAL);
        return vertical ? Orientation.VERTICAL : null; // null = no bias
    }

    @Override
    protected double computePrefWidth(double height) {
        LOG.trace("computePrefWidth({})", height);

        // To avoid parent<->child feedback (when a card's pref depends on the parent's pref),
        // compute based on each card's unconstrained preferred width.
        double max = 0;
        for (Node card : cards.values()) {
            max = Math.max(max, card.prefWidth(-1));
        }
        return snappedLeftInset() + max + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        LOG.trace("computePrefHeight({})", width);

        // Similarly, use unconstrained preferred height to avoid cycles.
        double max = 0;
        for (Node card : cards.values()) {
            max = Math.max(max, card.prefHeight(-1));
        }
        return snappedTopInset() + max + snappedBottomInset();
    }

    @Override
    protected double computeMinWidth(double height) {
        LOG.trace("computeMinWidth({})", height);

        // Avoid querying cards' minWidth to prevent feedback loops when a card's min depends on parent pref.
        // Use the maximum preferred width across all cards as a conservative minimum.
        double max = 0;
        for (Node card : cards.values()) {
            max = Math.max(max, card.prefWidth(height));
        }
        return snappedLeftInset() + max + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        LOG.trace("computeMinHeight({})", width);

        // Avoid querying cards' minHeight to prevent feedback loops when a card's min depends on parent pref.
        // Use the maximum preferred height across all cards as a conservative minimum.
        double max = 0;
        for (Node card : cards.values()) {
            max = Math.max(max, card.prefHeight(width));
        }
        return snappedTopInset() + max + snappedBottomInset();
    }

    @Override
    protected double computeMaxWidth(double height) {
        LOG.trace("computeMaxWidth({})", height);

        // Avoid consulting child maxWidth(height) when it depends on parent's pref and causes cycles.
        // Strategy: if any card reports an unbounded max (Double.MAX_VALUE) unconstrained, propagate it;
        // otherwise use the maximum of unconstrained preferred widths as a practical max.
        boolean unbounded = false;
        double maxPref = 0;
        for (Node card : cards.values()) {
            double childMax = card.maxWidth(-1);
            if (Double.isInfinite(childMax) || childMax == Double.MAX_VALUE) {
                unbounded = true;
            }
            maxPref = Math.max(maxPref, card.prefWidth(-1));
        }
        if (unbounded) {
            return Double.MAX_VALUE;
        }
        return snappedLeftInset() + maxPref + snappedRightInset();
    }

    @Override
    protected double computeMaxHeight(double width) {
        LOG.trace("computeMaxHeight({})", width);

        // Same logic for height: favor unbounded if any child is unbounded; otherwise cap at max pref.
        boolean unbounded = false;
        double maxPref = 0;
        for (Node card : cards.values()) {
            double childMax = card.maxHeight(-1);
            if (Double.isInfinite(childMax) || childMax == Double.MAX_VALUE) {
                unbounded = true;
            }
            maxPref = Math.max(maxPref, card.prefHeight(-1));
        }
        if (unbounded) {
            return Double.MAX_VALUE;
        }
        return snappedTopInset() + maxPref + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
        LOG.trace("layoutChildren()");

        double x = snappedLeftInset();
        double y = snappedTopInset();
        double w = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
        double h = Math.max(0, getHeight() - snappedTopInset() - snappedBottomInset());

        content.resizeRelocate(x, y, w, h);
    }

    @Override
    public void resize(double width, double height) {
        if (width != getWidth() || height != getHeight()) {
            super.resize(width, height);
        }
    }

    @Override
    public void relocate(double x, double y) {
        if (x != getLayoutX() || y != getLayoutY()) {
            super.relocate(x, y);
        }
    }

}

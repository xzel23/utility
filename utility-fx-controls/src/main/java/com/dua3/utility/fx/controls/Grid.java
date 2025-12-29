package com.dua3.utility.fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * The InputGrid class is an extension of GridPane that manages input controls and
 * ensures their validation state. It organizes input controls in a grid layout and
 * provides methods for initialization, resetting, and retrieving input values.
 */
public class Grid extends GridPane {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(Grid.class);

    /**
     * Property holding the valid state.
     *
     * @see #validProperty()
     */
    protected final BooleanProperty valid = new SimpleBooleanProperty(false);

    /**
     * Property holding the label placement.
     */
    protected final ObjectProperty<LabelPlacement> labelPlacement = new SimpleObjectProperty<>(LabelPlacement.BEFORE);

    private final MarkerSymbols markerSymbols;
    private SequencedCollection<Meta<?>> data = Collections.emptyList();
    private int columns = 1;

    /**
     * Constructs a new instance of the InputGrid class.
     *
     * @param markerSymbols the marker symbols to use
     */
    public Grid(MarkerSymbols markerSymbols) {
        this.markerSymbols = markerSymbols;

        labelPlacement.addListener((obs, oldVal, newVal) -> {
            if (oldVal != newVal) {
                init();
            }
        });

        updateColumnConstraints();
    }

    private void updateColumnConstraints() {
        getColumnConstraints().clear();
        int cols = columns;
        if (getLabelPlacement() == LabelPlacement.BEFORE) {
            for (int i = 0; i < cols; i++) {
                ColumnConstraints c0 = new ColumnConstraints();
                ColumnConstraints c1 = new ColumnConstraints();
                ColumnConstraints c2 = new ColumnConstraints();

                // Allow the control column to grow
                c1.setHgrow(Priority.ALWAYS);

                // the others should NOT grow
                c0.setHgrow(Priority.NEVER);
                c2.setHgrow(Priority.NEVER);

                getColumnConstraints().addAll(c0, c1, c2);
            }
        } else {
            for (int i = 0; i < cols; i++) {
                ColumnConstraints c0 = new ColumnConstraints();
                ColumnConstraints c1 = new ColumnConstraints();

                // Allow the control column to grow
                c0.setHgrow(Priority.ALWAYS);

                // error marker column should NOT grow
                c1.setHgrow(Priority.NEVER);

                getColumnConstraints().addAll(c0, c1);
            }
        }
    }

    /**
     * Get the label placement property.
     *
     * @return the label placement property
     */
    public ObjectProperty<LabelPlacement> labelPlacementProperty() {
        return labelPlacement;
    }

    /**
     * Get the label placement.
     *
     * @return the label placement
     */
    public LabelPlacement getLabelPlacement() {
        return labelPlacement.get();
    }

    /**
     * Set the label placement.
     *
     * @param labelPlacement the label placement
     */
    public void setLabelPlacement(LabelPlacement labelPlacement) {
        this.labelPlacement.set(labelPlacement);
    }

    /**
     * Get the valid state property.
     * <p>
     * This boolean property indicates the validity state of the input in the dialog pane.
     * It is used to dynamically track and manage whether the input provided by the user
     * meets certain predefined validation criteria.
     * <p>
     * The default value for this property is {@code false}, meaning the input is considered invalid
     * until explicitly validated or updated by the dialog's logic.
     * <p>+
     * This property is bound to the validation mechanisms within the dialog pane,
     * allowing it to automatically update based on user interactions or changes in the input fields.
     *
     * @return the valid property
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Retrieves a map of IDs and their corresponding values from the input controls.
     *
     * @return A map containing IDs as keys and their input values as values. The
     * result might contain keys that map to {@code null} values.
     */
    public Map<String, @Nullable Object> get() {
        Map<String, @Nullable Object> result = new LinkedHashMap<>();
        data.forEach(e -> {
            if (e.id != null) {
                result.put(e.id, e.control.get());
            }
        });
        return result;
    }

    /**
     * Sets the content of the grid by specifying the data and number of columns.
     *
     * @param data the sequenced collection of {@link Meta} objects, each representing
     *             metadata for an input field. This collection will be used to populate
     *             the grid with input controls, labels, and markers.
     * @param columns the number of columns into which the grid layout should be organized.
     */
    void setContent(SequencedCollection<Meta<?>> data, int columns) {
        this.data = data;
        this.columns = columns;
    }

    /**
     * Initializes the InputGrid by clearing previous input controls,
     * creating a new grid layout, and setting up input controls with labels and markers.
     *
     * <p>This method performs the following steps:
     * - Clears all existing child nodes from the grid.
     * - Iterates through the data entries to add their corresponding input controls,
     *   labels, and markers to the grid with proper layout settings.
     * - Binds the overall validity state to the validity states of individual input controls.
     * - Sets the initial focus to the first input control in the grid.
     */
    public void init() {
        LOG.trace("init: {}", data);

        updateColumnConstraints();

        getChildren().clear();
        getRowConstraints().clear();

        List<InputControl<?>> controls = new ArrayList<>();

        // create grid with input controls
        Insets insets = new Insets(2);
        Insets markerInsets = new Insets(0);

        LabelPlacement placement = getLabelPlacement();

        double minRowHeight = -1;
        if (placement == LabelPlacement.BEFORE) {
            TextField dummy = new TextField();
            dummy.setManaged(false);
            dummy.setVisible(false);
            minRowHeight = dummy.prefHeight(-1);
        }

        int r = 0;
        int c = 0;
        for (var entry : data) {
            updateMarker(entry, false);
            controls.add(entry.control);

            if (!entry.visible) {
                // do not add controls for non-visible (hidden) fields
                continue;
            }

            // add markers, label and control
            if (placement == LabelPlacement.BEFORE) {
                int gridX = 3 * c;
                int gridY = r;

                // set row height
                if (c == 0) {
                    RowConstraints rc = new RowConstraints();
                    if (minRowHeight > 0) {
                        rc.setMinHeight(minRowHeight);
                        rc.setPrefHeight(minRowHeight);
                    }
                    rc.setValignment(VPos.CENTER);
                    getRowConstraints().add(rc);
                }

                int span;
                if (entry.label != null) {
                    HBox labelBox = new HBox(entry.label, entry.requiredMarker);
                    labelBox.setSpacing(2);
                    addToGrid(labelBox, gridX++, gridY, 1, insets);
                    span = 1;
                } else {
                    addToGrid(entry.requiredMarker, gridX++, gridY, 1, markerInsets);
                    span = 1;
                }

                Node node = entry.control.node();
                node.focusedProperty().addListener((v, o, n) -> {
                    if (Objects.equals(n, Boolean.FALSE)) {
                        LOG.trace("input control lost focus: {}", entry.id);
                        updateMarker(entry, true);
                    }
                });

                entry.control.state().addValidationListener(() -> updateMarker(entry, true));

                addToGrid(node, gridX, gridY, span, insets);
                gridX += span;

                addToGrid(entry.errorMarker, gridX, gridY, 1, markerInsets);
            } else {
                // LabelPlacement.ABOVE
                int gridX = 2 * c;
                int gridY = 2 * r;

                // set row constraints (label row)
                if (c == 0) {
                    RowConstraints rcLabel = new RowConstraints();
                    rcLabel.setValignment(VPos.CENTER);
                    getRowConstraints().add(rcLabel); // row 2r
                    RowConstraints rcControl = new RowConstraints();
                    rcControl.setValignment(VPos.CENTER);
                    getRowConstraints().add(rcControl); // row 2r + 1
                }

                if (entry.label != null) {
                    HBox labelBox = new HBox(entry.label, entry.requiredMarker);
                    labelBox.setSpacing(2);
                    labelBox.setPadding(new Insets(6, 0, 0, 0)); // add vertical space above label
                    addToGrid(labelBox, gridX, gridY, 2, insets);
                } else {
                    addToGrid(entry.requiredMarker, gridX, gridY, 2, markerInsets);
                }

                Node node = entry.control.node();
                node.focusedProperty().addListener((v, o, n) -> {
                    if (Objects.equals(n, Boolean.FALSE)) {
                        LOG.trace("input control lost focus: {}", entry.id);
                        updateMarker(entry, true);
                    }
                });

                entry.control.state().addValidationListener(() -> updateMarker(entry, true));

                addToGrid(node, gridX++, gridY + 1, 1, insets);
                addToGrid(entry.errorMarker, gridX, gridY + 1, 1, markerInsets);
            }

            entry.control.init();

            // move to next position in grid
            c = (c + 1) % columns;
            if (c == 0) {
                r++;
            }
        }

        if (c != 0) {
            r++;
        }

        // add "* Required field" label
        if (data.stream().anyMatch(e -> e.control.isRequired())) {
            Label requiredFieldLabel = new Label("* Required field");
            requiredFieldLabel.getStyleClass().add("required-field-label");
            int span = (placement == LabelPlacement.BEFORE ? 3 : 2) * columns;
            int gridY = placement == LabelPlacement.BEFORE ? r : 2 * r;
            addToGrid(requiredFieldLabel, 0, gridY, span, insets);
        }

        // the valid state is true if all inputs are valid
        valid.bind(Bindings.createBooleanBinding(
                () -> controls.stream().allMatch(control -> {
                    boolean v = control.isValid();
                    LOG.trace("validate: {} -> {}", control, v);
                    return v;
                }),
                controls.stream().flatMap(control -> Stream.of(control.valueProperty(), control.validProperty())).toArray(ObservableValue[]::new)
        ));

        // request focus for the first control
        if (!data.isEmpty()) {
            data.getFirst().control.node().requestFocus();
        }
    }

    private void updateMarker(Meta<?> entry, boolean showErrors) {
        InputControl<?> control = entry.control;
        LOG.trace("updateMarker: valid={}, required={}, empty={}", control.isValid(), control.isRequired(), control.isEmpty());

        boolean isError = showErrors && !control.isValid();
        String requiredMarkerText = control.isRequired() ? (control.isEmpty() ? markerSymbols.requiredEmpty() : markerSymbols.requiredFilled()) : "";
        String errorMarkerText = isError ? (control.isRequired() ? markerSymbols.requiredError() : markerSymbols.optionalError()) : "";

        Tooltip tooltip = control.isValid() ? null : new Tooltip(control.errorProperty().get());

        entry.requiredMarker.setText(requiredMarkerText);
        entry.errorMarker.setText(errorMarkerText);
        entry.errorMarker.setTooltip(tooltip);
    }

    private void addToGrid(Node child, int c, int r, int span, Insets insets) {
        add(child, c, r, span, 1);
        setMargin(child, insets);
    }

    /**
     * Resets all input controls in the grid to their default values.
     *
     * <p>This method iterates through each data entry in the grid and invokes the reset method
     * on its associated control, thereby setting each input control back to its default state as defined
     * by the corresponding {@code Meta} object's default value supplier.
     */
    public void reset() {
        data.forEach(entry -> entry.control.reset());
    }

    /**
     * Meta data for a single input field consisting of ID, label text, default value etc.
     *
     * @param <T> the input's value type
     */
    static final class Meta<T extends @Nullable Object> {
        final @Nullable String id;
        final Class<T> cls;
        final Supplier<? extends T> dflt;
        final InputControl<? super T> control;
        final @Nullable Label label;
        final Label requiredMarker;
        final Label errorMarker;
        final boolean visible;

        Meta(@Nullable String id, @Nullable String label, Class<T> cls, Supplier<? extends @Nullable T> dflt, InputControl<? super T> control, boolean visible, double markerWidth) {
            this.id = id == null || id.isEmpty() ? null : id;
            this.label = label != null ? new Label(label) : null;
            this.requiredMarker = new Label();
            this.requiredMarker.getStyleClass().add("required-marker");
            this.errorMarker = new Label();
            this.errorMarker.getStyleClass().add("error-marker");
            this.cls = cls;
            this.dflt = dflt;
            this.control = control;
            this.visible = visible;

            requiredMarker.setMinWidth(markerWidth);
            errorMarker.setMinWidth(markerWidth);
        }

        void reset() {
            control.set(dflt.get());
        }
    }

}

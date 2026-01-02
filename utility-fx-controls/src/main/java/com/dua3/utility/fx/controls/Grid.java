package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.text.Font;
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
import javafx.geometry.Pos;
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
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Function;
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

    private static final FxFontUtil FU = FxFontUtil.getInstance();
    private final Font defaultFont;

    private final MarkerSymbols markerSymbols;
    private final Function<Map<String, Object>, Map<String, Optional<String>>> validate;

    private SequencedCollection<Meta<?>> data = Collections.emptyList();
    private int columns = 1;
    private double minRowHeight = 1.0;
    private LayoutUnit minRowHeightUnit = LayoutUnit.EM;

    /**
     * Constructs a new instance of the InputGrid class.
     *
     * @param markerSymbols the marker symbols to use
     * @param validate the page validation function
     *
     * @see GridBuilder#validate(Function)
     */
    public Grid(MarkerSymbols markerSymbols, Function<Map<String, Object>, Map<String, Optional<String>>> validate) {
        this.markerSymbols = markerSymbols;
        this.validate = validate;
        this.defaultFont = FU.convert(new Label().getFont());

        labelPlacement.addListener((obs, oldVal, newVal) -> {
            if (oldVal != newVal) {
                init();
            }
        });

        updateColumnConstraints();
    }

    private boolean validatePage(boolean fieldsValid) {
        Map<String, Optional<String>> results = validate.apply(get());
        boolean valid = true;
        for (var meta: data) {
            Optional<String> result = meta.id != null ? results.getOrDefault(meta.id, Optional.empty()) : Optional.empty();
            if (result.isPresent()) {
                valid = false;
                InputControlState<?> state = meta.control.state();
                String originalError = state.getError();
                state.setError(result.get());
                updateMarker(meta, fieldsValid);
                state.setError(originalError);
                LOG.trace("validatePage: {} -> {}", meta.id, result.get());
            } else {
                if (fieldsValid) {
                    updateMarker(meta, true);
                }
            }
        }
        LOG.trace("validatePage: {}", valid);
        return valid;
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
     * Sets the minimum row height.
     *
     * @param minRowHeight the minimum row height
     * @param unit the unit
     */
    void setMinRowHeight(double minRowHeight, LayoutUnit unit) {
        this.minRowHeight = minRowHeight;
        this.minRowHeightUnit = unit;
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

        double minHeight = switch (minRowHeightUnit) {
            case PIXELS -> minRowHeight;
            case EM -> minRowHeight * FU.getTextHeight("M", defaultFont);
            case POINTS -> minRowHeight * 96.0 / 72.0; // assuming 96 DPI
        };

        double minRowHeightFromDummy = -1;
        if (placement == LabelPlacement.BEFORE) {
            TextField dummy = new TextField();
            dummy.setManaged(false);
            dummy.setVisible(false);
            minRowHeightFromDummy = dummy.prefHeight(-1);
        }

        int y = 0;
        int c = 0;
        List<Meta<?>> dataList = new ArrayList<>(data);
        for (int i = 0; i < dataList.size(); i++) {
            var entry = dataList.get(i);
            updateMarker(entry, false);
            controls.add(entry.control);

            if (!entry.visible) {
                // do not add controls for non-visible (hidden) fields
                continue;
            }

            // decide if label row is needed for the current logical row
            boolean needsLabelRow = false;
            if (placement == LabelPlacement.ABOVE) {
                int rowStart = i - c;
                for (int j = rowStart; j < Math.min(rowStart + columns, dataList.size()); j++) {
                    var rowEntry = dataList.get(j);
                    if (rowEntry.visible && rowEntry.control.node() != null && rowEntry.label != null) {
                        needsLabelRow = true;
                        break;
                    }
                }
            }

            // apply vertical space
            if (entry.space > 0) {
                double height = switch (entry.spaceUnit) {
                    case PIXELS -> entry.space;
                    case EM -> entry.space * FU.getTextHeight("M", defaultFont);
                    case POINTS -> entry.space * 96.0 / 72.0; // assuming 96 DPI
                };

                if (c != 0) {
                    y += (placement == LabelPlacement.BEFORE || !needsLabelRow ? 1 : 2);
                    c = 0;
                    // re-calculate needsLabelRow for the new logical row
                    if (placement == LabelPlacement.ABOVE) {
                        needsLabelRow = false;
                        int rowStart = i;
                        for (int j = rowStart; j < Math.min(rowStart + columns, dataList.size()); j++) {
                            var rowEntry = dataList.get(j);
                            if (rowEntry.visible && rowEntry.control.node() != null && rowEntry.label != null) {
                                needsLabelRow = true;
                                break;
                            }
                        }
                    }
                }

                RowConstraints rc = new RowConstraints();
                rc.setMinHeight(height);
                rc.setPrefHeight(height);
                rc.setMaxHeight(height);

                getRowConstraints().add(rc);
                y++;
            }

            if (entry.control.node() == null) {
                continue;
            }

            // add markers, label and control
            if (placement == LabelPlacement.BEFORE) {
                int gridX = 3 * c;
                int gridY = y;

                // set row height
                if (c == 0) {
                    RowConstraints rc = new RowConstraints();
                    rc.setMinHeight(Math.max(minHeight, minRowHeightFromDummy));
                    rc.setValignment(VPos.BASELINE);
                    getRowConstraints().add(rc);
                }

                int span;
                if (entry.label != null) {
                    HBox labelBox = new HBox(entry.label, entry.requiredMarker);
                    labelBox.setSpacing(2);
                    labelBox.setAlignment(Pos.BASELINE_LEFT);
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
                int gridY = y;

                // set row constraints
                if (c == 0) {
                    if (needsLabelRow) {
                        RowConstraints rcLabel = new RowConstraints();
                        rcLabel.setMinHeight(minHeight);
                        rcLabel.setValignment(VPos.CENTER);
                        getRowConstraints().add(rcLabel); // row gridY
                        RowConstraints rcControl = new RowConstraints();
                        rcControl.setMinHeight(minHeight);
                        rcControl.setValignment(VPos.CENTER);
                        getRowConstraints().add(rcControl); // row gridY + 1
                    } else {
                        RowConstraints rcControl = new RowConstraints();
                        rcControl.setMinHeight(minHeight);
                        rcControl.setValignment(VPos.CENTER);
                        getRowConstraints().add(rcControl); // row gridY
                    }
                }

                if (needsLabelRow) {
                    if (entry.label != null) {
                        HBox labelBox = new HBox(entry.label, entry.requiredMarker);
                        labelBox.setSpacing(2);
                        labelBox.setPadding(new Insets(6, 0, 0, 0)); // add vertical space above label
                        addToGrid(labelBox, gridX, gridY, 2, insets);
                    } else {
                        addToGrid(entry.requiredMarker, gridX, gridY, 2, markerInsets);
                    }
                }

                Node node = entry.control.node();
                node.focusedProperty().addListener((v, o, n) -> {
                    if (Objects.equals(n, Boolean.FALSE)) {
                        LOG.trace("input control lost focus: {}", entry.id);
                        updateMarker(entry, true);
                    }
                });

                entry.control.state().addValidationListener(() -> updateMarker(entry, true));

                if (needsLabelRow) {
                    addToGrid(node, gridX++, gridY + 1, 1, insets);
                    addToGrid(entry.errorMarker, gridX, gridY + 1, 1, markerInsets);
                } else {
                    addToGrid(node, gridX++, gridY, 1, insets);
                    addToGrid(entry.errorMarker, gridX, gridY, 1, markerInsets);
                }
            }

            entry.control.init();

            // move to next position in grid
            c = (c + 1) % columns;
            if (c == 0) {
                y += (placement == LabelPlacement.BEFORE || !needsLabelRow ? 1 : 2);
            }
        }

        if (c != 0) {
            // we were in the middle of a row, check if it needed a label row
            boolean needsLabelRow = false;
            if (placement == LabelPlacement.ABOVE) {
                int rowStart = dataList.size() - c;
                for (int j = rowStart; j < dataList.size(); j++) {
                    var rowEntry = dataList.get(j);
                    if (rowEntry.visible && rowEntry.control.node() != null && rowEntry.label != null) {
                        needsLabelRow = true;
                        break;
                    }
                }
            }
            y += (placement == LabelPlacement.BEFORE || !needsLabelRow ? 1 : 2);
        }

        // add "* Required field" label
        if (data.stream().anyMatch(e -> e.control.isRequired())) {
            Label requiredFieldLabel = new Label(I18NInstance.get().get("dua3.utility.fx.controls.grid.required.field"));
            requiredFieldLabel.getStyleClass().add("required-field-label");
            int span = (placement == LabelPlacement.BEFORE ? 3 : 2) * columns;
            int gridY = y;
            addToGrid(requiredFieldLabel, 0, gridY, span, new Insets(0.5 * FU.getTextHeight("M", defaultFont), 2, 2, 2));
        }

        // the valid state is true if all inputs are valid
        valid.bind(Bindings.createBooleanBinding(
                () -> {
                    boolean fieldsValid = controls.stream().allMatch(control -> {
                        boolean v = control.isValid();
                        LOG.trace("validate: {} -> {}", control, v);
                        return v;
                    });
                    boolean pageValid = validatePage(fieldsValid);
                    return fieldsValid && pageValid;
                },
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
        final double space;
        final LayoutUnit spaceUnit;

        Meta(@Nullable String id, @Nullable String label, Class<T> cls, Supplier<? extends @Nullable T> dflt, InputControl<? super T> control, boolean visible, double markerWidth) {
            this(id, label, cls, dflt, control, visible, markerWidth, 0.0, LayoutUnit.PIXELS);
        }

        Meta(@Nullable String id, @Nullable String label, Class<T> cls, Supplier<? extends @Nullable T> dflt, InputControl<? super T> control, boolean visible, double markerWidth, double space, LayoutUnit spaceUnit) {
            this.id = id == null || id.isEmpty() ? null : id;
            if (label != null) {
                this.label = new Label(label);
                this.label.getStyleClass().add("grid-label");
            } else {
                this.label = null;
            }
            this.requiredMarker = new Label();
            this.requiredMarker.getStyleClass().add("required-marker");
            this.errorMarker = new Label();
            this.errorMarker.getStyleClass().add("error-marker");
            this.cls = cls;
            this.dflt = dflt;
            this.control = control;
            this.visible = visible;
            this.space = space;
            this.spaceUnit = spaceUnit;

            requiredMarker.setMinWidth(markerWidth);
            errorMarker.setMinWidth(markerWidth);
        }

        void reset() {
            control.set(dflt.get());
        }

        @Override
        public String toString() {
            return "Meta<" + cls.getSimpleName() + ">[" + id + "]";
        }
    }

}

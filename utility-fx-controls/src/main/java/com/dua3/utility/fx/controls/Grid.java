package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.TextUtil;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.FxUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
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

    private static final String MARKER_OK_OPTIONAL = "";
    private static final String MARKER_REQUIRED = "✱";
    private static final String MARKER_ERROR = "⚠";

    private static final Font LABEL_FONT = FxFontUtil.getInstance().convert(new Label().getFont());
    private static final Dimension2D MARKER_SIZE = FxUtil.convert(
            Stream.of(MARKER_OK_OPTIONAL, MARKER_REQUIRED, MARKER_ERROR)
                    .map(m -> TextUtil.getTextDimension(m, LABEL_FONT).getDimension())
                    .reduce(Dimension2f::max)
                    .orElse(Dimension2f.of(0, 0))
    );

    /**
     * Property holding the valid state.
     *
     * @see #validProperty()
     */
    protected final BooleanProperty valid = new SimpleBooleanProperty(false);

    private SequencedCollection<Meta<?>> data = Collections.emptyList();
    private int columns = 1;

    /**
     * Constructs a new instance of the InputGrid class.
     */
    public Grid() { /* nothing to do */ }

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

        getChildren().clear();

        List<InputControl<?>> controls = new ArrayList<>();

        // create grid with input controls
        Insets insets = new Insets(2);
        Insets markerInsets = new Insets(0);
        int r = 0;
        int c = 0;
        for (var entry : data) {
            controls.add(entry.control);

            if (entry.hidden) {
                // do not add cotrols for hidden fields
                continue;
            }

            // add label and control
            int gridX = 3 * c;
            int gridY = r;

            int span;
            if (entry.label != null) {
                addToGrid(entry.label, gridX, gridY, 1, insets);
                gridX++;
                span = 1;
            } else {
                span = 2;
            }

            Node node = entry.control.node();
            node.focusedProperty().addListener((v, o, n) -> {
                if (Objects.equals(n, Boolean.FALSE)) {
                    LOG.trace("input control lost focus: {}", entry.id);
                    updateMarker(entry);
                }
            });

            entry.control.state().addValidationListener(() -> updateMarker(entry));

            addToGrid(node, gridX, gridY, span, insets);
            gridX += span;

            addToGrid(entry.marker, gridX, gridY, 1, markerInsets);

            entry.control.init();

            // move to next position in grid
            c = (c + 1) % columns;
            if (c == 0) {
                r++;
            }
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

    private static void updateMarker(Meta<?> entry) {
        InputControl<?> control = entry.control;
        LOG.trace("updateMarker: valid={}, required={}, empty={}", control.isValid(), control.isRequired(), control.isEmpty());

        if (control.isValid()) {
            if (control.isRequired()) {
                entry.marker.setText(MARKER_REQUIRED);
                entry.marker.setTooltip(null);
            } else {
                entry.marker.setText(MARKER_OK_OPTIONAL);
                entry.marker.setTooltip(null);
            }
        } else {
            if (control.isRequired() && control.isEmpty()) {
                entry.marker.setText(MARKER_REQUIRED);
                entry.marker.setTooltip(null);
            } else {
                entry.marker.setText(MARKER_ERROR);
                entry.marker.setTooltip(new Tooltip(control.errorProperty().get()));
            }
        }
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
        final Label marker = new Label();
        final boolean hidden;

        Meta(@Nullable String id, @Nullable String label, Class<T> cls, Supplier<? extends @Nullable T> dflt, InputControl<? super T> control, boolean hidden) {
            this.id = id == null || id.isEmpty() ? null : id;
            this.label = label != null ? new Label(label) : null;
            this.cls = cls;
            this.dflt = dflt;
            this.control = control;
            this.hidden = hidden;

            marker.setText(control.isRequired() ? MARKER_REQUIRED : MARKER_OK_OPTIONAL);
            marker.setMinSize(MARKER_SIZE.getWidth(), MARKER_SIZE.getHeight());
        }

        void reset() {
            control.set(dflt.get());
        }
    }

}

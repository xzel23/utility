package com.dua3.utility.fx.controls;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * The InputGrid class is an extension of GridPane that manages input controls and
 * ensures their validation state. It organizes input controls in a grid layout and
 * provides methods for initialization, resetting, and retrieving input values.
 */
public class InputGrid extends GridPane {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(InputGrid.class);

    private static final String MARKER_INITIAL = "";
    private static final String MARKER_ERROR = "⚠";
    private static final String MARKER_OK = "";

    protected final BooleanProperty valid = new SimpleBooleanProperty(false);
    private Collection<Meta<?>> data = null;
    private int columns = 1;

    /**
     * Constructs a new instance of the InputGrid class.
     */
    public InputGrid() {
    }

    /**
     * Get valid state property.
     *
     * @return the valid state property of the input
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
    public Map<String, Object> get() {
        Map<String, Object> result = new HashMap<>();
        // Collectors.toMap() does not support null values!
        //noinspection SimplifyForEach
        data.forEach(e -> result.put(e.id, e.control.get()));
        return result;
    }

    void setContent(Collection<Meta<?>> data, int columns) {
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
        getChildren().clear();

        List<InputControl<?>> controls = new ArrayList<>();

        // create grid with input controls
        Insets insets = new Insets(2);
        Insets markerInsets = new Insets(0);
        int r = 0, c = 0;
        for (var entry : data) {
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

            controls.add(entry.control);

            addToGrid(entry.control.node(), gridX, gridY, span, insets);
            gridX += span;

            addToGrid(entry.marker, gridX, gridY, 1, markerInsets);

            entry.control.init();

            // move to next position in grid
            c = (c + 1) % columns;
            if (c == 0) {
                r++;
            }
        }

        // valid state is true if all inputs are valid
        valid.bind(Bindings.createBooleanBinding(
                () -> controls.stream().allMatch(control -> {
                    boolean v = control.isValid();
                    LOG.info("validate: {} -> {}", control, v);
                    return v;
                }),
                controls.stream().flatMap(control -> Stream.of(control.valueProperty(), control.validProperty())).toArray(ObservableValue[]::new)
        ));

        // todo: request focus once to do what?
        for (var entry : data) {
            entry.control.node().requestFocus();
            break;
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
    static final class Meta<T> {
        final String id;
        final Class<T> cls;
        final Supplier<? extends T> dflt;
        final InputControl<? super T> control;
        final Label label;
        final Label marker = new Label();

        Meta(String id, @Nullable String label, Class<T> cls, Supplier<? extends T> dflt, InputControl<? super T> control) {
            this.id = id;
            this.label = label != null ? new Label(label) : null;
            this.cls = cls;
            this.dflt = dflt;
            this.control = control;

            Dimension2D dimMarker = new Dimension2D(0, 0);
            dimMarker = FxUtil.growToFit(dimMarker, marker.getBoundsInLocal());
            marker.setMinSize(dimMarker.getWidth(), dimMarker.getHeight());
            marker.setText(MARKER_INITIAL);
        }

        void reset() {
            control.set(dflt.get());
        }
    }

}

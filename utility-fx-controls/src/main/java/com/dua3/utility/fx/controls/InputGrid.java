package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
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
        GridPane.setMargin(child, insets);
    }

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

package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.SliderWithButtons;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.function.DoubleFunction;

/**
 * Demonstrates all display modes of {@link SliderWithButtons}.
 * <p>
 * The property values are shown next to every slider so that the values
 * displayed by the control can be compared with its actual min, max, and
 * value properties.
 */
public class SliderWithButtonsSample extends Application {

    private static final double MIN = 10.0;
    private static final double MAX = 100.0;
    private static final double VALUE = 40.0;

    /**
     * The main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public SliderWithButtonsSample() { /* nothing to do */ }

    @Override
    public void start(Stage stage) {
        GridPane sliders = new GridPane();
        sliders.setHgap(12);
        sliders.setVgap(8);
        sliders.setPadding(new Insets(12));

        ComboBox<FormatterOption> formatter = new ComboBox<>();
        formatter.getItems().addAll(FormatterOption.values());
        formatter.getSelectionModel().select(FormatterOption.DEFAULT);
        formatter.setPrefWidth(120);

        HBox options = new HBox(8, new Label("Formatter:"), formatter);
        options.setAlignment(Pos.CENTER_LEFT);

        formatter.valueProperty().addListener((obs, oldValue, newValue) ->
                populateSliders(sliders, newValue == null ? FormatterOption.DEFAULT : newValue)
        );
        populateSliders(sliders, formatter.getValue());

        VBox root = new VBox(8, options, sliders);
        VBox.setVgrow(sliders, Priority.ALWAYS);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 1000, 330);
        stage.setTitle("SliderWithButtons Sample");
        stage.setScene(scene);
        stage.show();
    }

    private static void populateSliders(GridPane grid, FormatterOption formatterOption) {
        grid.getChildren().clear();
        addHeader(grid);

        SliderWithButtons.Mode[] modes = SliderWithButtons.Mode.values();
        for (int row = 0; row < modes.length; row++) {
            addSliderRow(grid, row + 1, modes[row], formatterOption);
        }
    }

    private static void addHeader(GridPane grid) {
        grid.add(new Label("Mode"), 0, 0);
        grid.add(new Label("SliderWithButtons"), 1, 0);
        grid.add(new Label("Min"), 2, 0);
        grid.add(new Label("Max"), 3, 0);
        grid.add(new Label("Value"), 4, 0);
    }

    private static void addSliderRow(GridPane grid, int row, SliderWithButtons.Mode mode, FormatterOption formatterOption) {
        SliderWithButtons slider = Controls.slider()
                .mode(mode)
                .min(formatterOption.min())
                .max(formatterOption.max())
                .value(formatterOption.value())
                .blockIncrement(formatterOption.blockIncrement())
                .formatter(formatterOption.formatter())
                .build();

        Label modeLabel = new Label(mode.name());
        modeLabel.setMinWidth(190);

        Label minLabel = new Label();
        minLabel.textProperty().bind(slider.minProperty().asString("%.1f"));

        Label maxLabel = new Label();
        maxLabel.textProperty().bind(slider.maxProperty().asString("%.1f"));

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> format(slider.get()),
                slider.valueProperty()
        ));

        GridPane.setHgrow(slider, Priority.ALWAYS);
        grid.add(modeLabel, 0, row);
        grid.add(slider, 1, row);
        grid.add(minLabel, 2, row);
        grid.add(maxLabel, 3, row);
        grid.add(valueLabel, 4, row);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private enum FormatterOption {
        DEFAULT("default", SliderWithButtons.formatDouble(3,1)),
        INTEGER("Integer", SliderWithButtons.formatInteger(3)),
        PERCENT("Percent", SliderWithButtons.formatPercent(), 0.0, 1.0, 0.4, 0.05),
        PERCENT_WITH_FRACTIONAL("Percent with frational", SliderWithButtons.formatPercent(1), 0.0, 1.0, 0.4, 0.05);

        private final String displayName;
        private final DoubleFunction<String> formatter;
        private final double min;
        private final double max;
        private final double value;
        private final double blockIncrement;

        FormatterOption(String displayName, DoubleFunction<String> formatter) {
            this(displayName, formatter, MIN, MAX, VALUE, 5.0);
        }

        FormatterOption(String displayName, DoubleFunction<String> formatter, double min, double max, double value, double blockIncrement) {
            this.displayName = displayName;
            this.formatter = formatter;
            this.min = min;
            this.max = max;
            this.value = value;
            this.blockIncrement = blockIncrement;
        }

        private DoubleFunction<String> formatter() {
            return formatter;
        }

        private double min() {
            return min;
        }

        private double max() {
            return max;
        }

        private double value() {
            return value;
        }

        private double blockIncrement() {
            return blockIncrement;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}

package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.SliderWithButtons;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;

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

        addHeader(sliders);

        SliderWithButtons.Mode[] modes = SliderWithButtons.Mode.values();
        for (int row = 0; row < modes.length; row++) {
            addSliderRow(sliders, row + 1, modes[row]);
        }

        VBox root = new VBox(8, sliders);
        VBox.setVgrow(sliders, Priority.ALWAYS);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 1000, 330);
        stage.setTitle("SliderWithButtons Sample");
        stage.setScene(scene);
        stage.show();
    }

    private static void addHeader(GridPane grid) {
        grid.add(new Label("Mode"), 0, 0);
        grid.add(new Label("SliderWithButtons"), 1, 0);
        grid.add(new Label("Min"), 2, 0);
        grid.add(new Label("Max"), 3, 0);
        grid.add(new Label("Value"), 4, 0);
    }

    private static void addSliderRow(GridPane grid, int row, SliderWithButtons.Mode mode) {
        SliderWithButtons slider = Controls.slider()
                .mode(mode)
                .min(MIN)
                .max(MAX)
                .value(VALUE)
                .blockIncrement(5.0)
                .formatter(SliderWithButtonsSample::format)
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
}

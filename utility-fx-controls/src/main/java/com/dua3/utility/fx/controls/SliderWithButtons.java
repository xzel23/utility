package com.dua3.utility.fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * A custom UI component that combines a slider with increment and decrement buttons.
 * It supports various display modes, which can include a value label and text input field.
 */
public class SliderWithButtons extends Region {

    private static final Pattern PATTERN_DIGIT = Pattern.compile("\\d");
    private final Mode mode;
    private final BiFunction<? super Double, Double, String> formatter;
    private final Slider slider;
    private final Button btnIncrement;
    private final Button btnDecrement;
    private final List<Node> children = new ArrayList<>();
    private TextField tfValue;
    private Label label;

    /**
     * Constructor for SliderWithButtons. This class creates a slider
     * with optional decrement and increment buttons, and optional label
     * or text field to display the slider value.
     *
     * @param mode The mode in which the slider operates. Determines
     *             the presence of buttons, labels, or text fields.
     * @param formatter A formatter function to format the display values
     *                  of the slider.
     */
    SliderWithButtons(Mode mode, BiFunction<? super Double, Double, String> formatter) {
        this.mode = mode;
        this.formatter = formatter;

        this.slider = new Slider();
        this.btnDecrement = new Button("-");
        this.btnIncrement = new Button("+");

        btnDecrement.setOnAction(evt -> slider.decrement());
        btnDecrement.setFocusTraversable(false);
        children.add(btnDecrement);

        children.add(slider);

        btnIncrement.setOnAction(evt -> slider.increment());
        btnIncrement.setFocusTraversable(false);
        children.add(btnIncrement);

        switch (mode) {
            case SLIDER_ONLY -> {
                tfValue = null;
                label = null;
            }
            case SLIDER_VALUE, SLIDER_VALUE_TOTAL -> {
                tfValue = null;
                children.add(label = new Label());
            }
            case SLIDER_INPUT_TOTAL -> {
                children.add(tfValue = new TextField());
                children.add(label = new Label());
            }
        }

        slider.valueProperty().addListener((v, o, n) -> valueChanged(o, n));
        slider.maxProperty().addListener((v, o, n) -> updateLabel());

        initPane();
    }

    private void valueChanged(Number o, Number n) {
        if (label != null) {
            label.setText(formatter.apply(n.doubleValue(), getMax()));
        }
        if (tfValue != null) {
            tfValue.setText(String.valueOf(n));
        }
    }

    private void updateLabel() {
        if (label == null) {
            return;
        }

        double v = getValue();
        double m = getMax();

        String proto = PATTERN_DIGIT.matcher(formatter.apply(m, m)).replaceAll("0");
        Text text = new Text(proto);
        text.setFont(label.getFont());
        double w = text.getBoundsInLocal().getWidth();

        int paddingLeft = 2;
        int paddingRight = 4;
        label.setMinWidth(w + paddingLeft + paddingRight);
        label.setPadding(new Insets(0, paddingRight, 0, paddingLeft));

        valueChanged(v, v);
    }

    private void initPane() {
        Pane pane = box(slider.getOrientation());
        pane.getChildren().addAll(children);
        getChildren().setAll(pane);
    }

    /**
     * Retrieves the maximum value of the slider.
     *
     * @return the maximum value that the slider can represent.
     */
    public double getMax() {
        return slider.getMax();
    }

    /**
     * Sets the maximum value for the slider.
     *
     * @param value the maximum value to set on the slider.
     */
    public void setMax(double value) {
        slider.setMax(value);
    }

    /**
     * Retrieves the current value of the slider.
     *
     * @return The current value of the slider as a double.
     */
    public double getValue() {
        return slider.getValue();
    }

    private static Pane box(Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            HBox box = new HBox();
            box.setAlignment(Pos.CENTER);
            return box;
        } else {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            return box;
        }
    }

    /**
     * Sets the value of the slider.
     *
     * @param value the value to set for the slider
     */
    public void setValue(double value) {
        slider.setValue(value);
    }

    /**
     * Sets the orientation of the slider.
     *
     * @param orientation the Orientation to set for the slider
     * @see Orientation
     */
    public void setOrientation(Orientation orientation) {
        if (orientation != slider.getOrientation()) {
            slider.setOrientation(orientation);
            initPane();
        }
    }

    /**
     * Retrieves the operation mode of the slider.
     *
     * @return the current mode in which the slider is operating.
     * @see Mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the text of the decrement button in the slider.
     *
     * @param value the text to be displayed on the decrement button
     */
    public void setDecrementText(String value) {
        btnDecrement.setText(value);
    }

    /**
     * Sets the graphical representation for the decrement button of the slider.
     *
     * @param value the Node to be used as the graphic for the decrement button
     */
    public void setDecrementGraphic(Node value) {
        btnDecrement.setGraphic(value);
    }

    /**
     * Sets the text label for the increment button on the slider.
     *
     * @param value the text to set on the increment button
     */
    public void setIncrementText(String value) {
        btnIncrement.setText(value);
    }

    /**
     * Sets the graphic for the increment button on the slider.
     *
     * @param value the Node to be used as the graphic for the increment button
     */
    public void setIncrementGraphic(Node value) {
        btnIncrement.setGraphic(value);
    }

    /**
     * Configures whether to show the tick labels on the slider.
     *
     * @param value true to show tick labels, false to hide them
     */
    public void setShowTickLabels(boolean value) {
        slider.setShowTickLabels(value);
    }

    /**
     * Configures whether the slider should display tick marks.
     *
     * @param value true to show tick marks, false to hide them
     */
    public void setShowTickMarks(boolean value) {
        slider.setShowTickMarks(value);
    }

    /**
     * Retrieves the minimum value of the slider.
     *
     * @return the minimum value that the slider can represent.
     */
    public double getMin() {
        return slider.getMin();
    }

    /**
     * Sets the minimum value for the slider.
     *
     * @param value the minimum value to set for the slider.
     */
    public void setMin(double value) {
        slider.setMin(value);
    }

    /**
     * Retrieves the major tick unit for the slider.
     *
     * @return the major tick unit value of the slider as a double.
     */
    public double getMajorTickUnit() {
        return slider.getMajorTickUnit();
    }

    /**
     * Retrieves the number of minor tick marks to be displayed on the slider.
     *
     * @return the number of minor tick marks as a double.
     */
    public double getMinorTickCount() {
        return slider.getMinorTickCount();
    }

    /**
     * Retrieves the block increment value of the slider.
     *
     * @return The block increment value of the slider as a double.
     */
    public double getBlockIncrement() {
        return slider.getBlockIncrement();
    }

    /**
     * Sets the block increment value for the slider. The block increment is the amount the
     * slider's value will change when the user interacts with the slider track (for instance,
     * when using keyboard arrow keys).
     *
     * @param value the new block increment value for the slider
     */
    public void setBlockIncrement(double value) {
        slider.setBlockIncrement(value);
    }

    /**
     * Gets the value property of the slider.
     *
     * @return the DoubleProperty representing the slider's current value.
     */
    public DoubleProperty valueProperty() {
        return slider.valueProperty();
    }

    /**
     * Retrieves the minimum value property of the slider.
     *
     * @return the DoubleProperty representing the minimum value property of the slider.
     */
    public DoubleProperty minProperty() {
        return slider.minProperty();
    }

    /**
     * Returns the DoubleProperty representing the maximum value of the slider.
     *
     * @return the DoubleProperty that holds the maximum value of the slider.
     */
    public DoubleProperty maxProperty() {
        return slider.maxProperty();
    }

    /**
     * Retrieves the major tick unit property of the slider.
     *
     * @return the major tick unit property.
     */
    public DoubleProperty majorTickUnitProperty() {
        return slider.majorTickUnitProperty();
    }

    /**
     * Retrieves the property for the minor tick count of the slider.
     *
     * @return the IntegerProperty representing the minor tick count.
     */
    public IntegerProperty minorTickCountProperty() {
        return slider.minorTickCountProperty();
    }

    /**
     * Retrieves the value changing property of the slider.
     *
     * @return a BooleanProperty that indicates whether the slider's value is currently changing.
     */
    public BooleanProperty valueChangingProperty() {
        return slider.valueChangingProperty();
    }

    /**
     * Returns the label formatter property of the slider. This property allows for
     * a custom string converter to format the labels of the slider.
     *
     * @return an ObjectProperty containing the current StringConverter used for the slider labels.
     */
    public ObjectProperty<StringConverter<Double>> labelFormatterProperty() {
        return slider.labelFormatterProperty();
    }

    /**
     * Retrieves the property for showing or hiding tick labels on the slider.
     *
     * @return BooleanProperty representing whether tick labels are shown on the slider.
     */
    public BooleanProperty showTickLabelsProperty() {
        return slider.showTickLabelsProperty();
    }

    /**
     * Retrieves the property representing whether tick marks are shown on the slider.
     *
     * @return the BooleanProperty indicating if tick marks are displayed.
     */
    public BooleanProperty showTickMarksProperty() {
        return slider.showTickMarksProperty();
    }

    /**
     * Represents the property that indicates whether the slider will snap to the closest tick mark.
     *
     * @return A BooleanProperty indicating whether the slider snaps to ticks.
     */
    public BooleanProperty snapToTicksProperty() {
        return slider.snapToTicksProperty();
    }

    /**
     * Enum representing various modes of a slider component.
     */
    public enum Mode {
        /**
         * Mode where only the slider component is enabled.
         */
        SLIDER_ONLY,
        /**
         * Mode where only the current value of the slider is displayed.
         */
        SLIDER_VALUE,
        /**
         * Mode where the slider displays its current value along with the total possible value.
         */
        SLIDER_VALUE_TOTAL,
        /**
         * Mode where the slider, its current value, and the total input value are displayed or utilized.
         */
        SLIDER_INPUT_TOTAL,
    }

}

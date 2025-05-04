package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;

import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * A builder class for constructing a {@code SliderWithButtons} instance with various configuration options.
 */
public class SliderBuilder {
    private final SliderWithButtons slider;
    private boolean valueSet = false;

    SliderBuilder(SliderWithButtons.Mode mode, BiFunction<Double, Double, String> formatter) {
        slider = new SliderWithButtons(mode, formatter);
    }

    /**
     * Sets the orientation of the slider.
     *
     * @param value the orientation for the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     * @see Orientation
     */
    public SliderBuilder orientation(Orientation value) {
        slider.setOrientation(value);
        return this;
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param value the minimum value to set for the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder min(double value) {
        slider.setMin(value);
        return this;
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param value the maximum value to set for the slider.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder max(double value) {
        slider.setMax(value);
        return this;
    }

    /**
     * Sets the value of the slider to the specified value.
     *
     * @param value the value to set on the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder value(double value) {
        slider.setValue(value);
        valueSet = true;
        return this;
    }

    /**
     * Sets the text label for the increment button on the slider.
     *
     * @param value the text to set on the increment button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder incrementText(String value) {
        slider.setIncrementText(value);
        return this;
    }

    /**
     * Sets the graphic for the increment button of the slider.
     *
     * @param value the {@code Node} to be used as the graphic for the increment button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder incrementGraphic(Node value) {
        slider.setIncrementGraphic(value);
        return this;
    }

    /**
     * Sets the text for the decrement button of the slider.
     *
     * @param value the text to set for the decrement button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder decrementText(String value) {
        slider.setDecrementText(value);
        return this;
    }

    /**
     * Sets a graphical representation for the decrement button of the slider.
     *
     * @param value the Node to be used as the graphic for the decrement button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder decrementGraphic(Node value) {
        slider.setDecrementGraphic(value);
        return this;
    }

    /**
     * Sets the block increment value for the slider.
     *
     * @param value the new block increment value
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder blockIncrement(double value) {
        slider.setBlockIncrement(value);
        return this;
    }

    /**
     * Configures whether to show the tick labels on the slider.
     *
     * @param value true to show tick labels, false to hide them.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder showTickLabels(boolean value) {
        slider.setShowTickLabels(value);
        return this;
    }

    /**
     * Configures whether the slider should display tick marks.
     *
     * @param value true to show tick marks, false to hide them
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder showTickMarks(boolean value) {
        slider.setShowTickMarks(value);
        return this;
    }

    /**
     * Sets a callback to be invoked when the value of the slider changes.
     *
     * @param onChange a DoubleConsumer that will be invoked with the new slider value whenever it changes
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder onChange(DoubleConsumer onChange) {
        slider.valueProperty().addListener((v, o, n) -> onChange.accept(n));
        return this;
    }

    /**
     * Binds the slider's value property bidirectionally with the specified number property.
     *
     * @param value the property to be bound bidirectionally with the slider's value property.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder bind(Property<Number> value) {
        slider.valueAsDoubleProperty().bindBidirectional(value);
        return this;
    }

    /**
     * Binds the minimum value of the slider to the given {@code ObservableNumberValue}.
     *
     * @param value the {@code ObservableNumberValue} to bind the minimum value to
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder bindMin(ObservableNumberValue value) {
        slider.minProperty().bind(value);
        return this;
    }

    /**
     * Binds the maximum value of the slider to the given observable number value.
     *
     * @param value the {@code ObservableNumberValue} to bind to the slider's maximum property
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder bindMax(ObservableNumberValue value) {
        slider.maxProperty().bind(value);
        return this;
    }

    public SliderBuilder setDefault(double dflt) {
        slider.setDefault(dflt);
        return this;
    }

    public SliderBuilder setDefault(Supplier<Double> dflt) {
        slider.setDefault(dflt);
        return this;
    }

    /**
     * Builds the configured {@code SliderWithButtons} instance.
     *
     * @return the built {@code SliderWithButtons} instance.
     */
    public SliderWithButtons build() {
        if (!valueSet) {
            slider.reset();
        }
        return slider;
    }

}

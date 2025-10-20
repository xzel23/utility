package com.dua3.utility.fx.controls;

import com.dua3.utility.lang.LangUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A builder class for constructing a {@code SliderWithButtons} instance with various configuration options.
 */
public class SliderBuilder extends InputControlBuilder<SliderBuilder, Double> {
    private SliderWithButtons.Mode mode = SliderWithButtons.Mode.SLIDER_ONLY;
    private boolean showTickMarks = false;
    private boolean snapToTicks = false;
    private boolean showTickLabels = false;

    private @Nullable BiFunction<? super Double, ? super Double, String> formatter = null;
    private @Nullable Orientation orientation;
    private @Nullable Double min = null;
    private @Nullable Double max = null;
    private @Nullable Double value = null;
    private @Nullable String incrementText;
    private @Nullable Node incrementGraphic;
    private @Nullable String decrementText;
    private @Nullable Node decrementGraphic;
    private @Nullable Double blockIncrement = null;
    private @Nullable Double majorTickUnit = null;

    private final List<Property<Number>> valueBindings = new ArrayList<>();
    private final List<Property<Number>> minBindings = new ArrayList<>();
    private final List<Property<Number>> maxBindings = new ArrayList<>();

    SliderBuilder() {
    }

    /**
     * Sets the mode of the slider.
     *
     * @param mode the mode to set for the slider
     * @return this instance of {@code SliderBuilder} for method chaining
     */
    public SliderBuilder mode(SliderWithButtons.Mode mode) {
        this.mode = mode;
        return self();
    }

    /**
     * Sets the formatter for the slider, which determines how the slider's value is
     * formatted and displayed. The formatter is a function that takes the current
     * minimum and maximum values of the slider, as well as its current value, and
     * returns a formatted string representation.
     *
     * @param formatter a {@code BiFunction} that takes the minimum value, the
     *                  maximum value, and the current value of the slider and
     *                  returns a formatted string.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder formatter(BiFunction<Double, Double, String> formatter) {
        this.formatter = formatter;
        return self();
    }
    
    /**
     * Sets the orientation of the slider.
     *
     * @param value the orientation for the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     * @see Orientation
     */
    public SliderBuilder orientation(Orientation value) {
        this.orientation = value;
        return self();
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param value the minimum value to set for the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder min(double value) {
        this.min = value;
        return self();
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param value the maximum value to set for the slider.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder max(double value) {
        this.max = value;
        return self();
    }

    /**
     * Sets the value of the slider to the specified value.
     *
     * @param value the value to set on the slider
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder value(double value) {
        this.value = value;
        return self();
    }

    /**
     * Sets the text label for the increment button on the slider.
     *
     * @param value the text to set on the increment button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder incrementText(String value) {
        this.incrementText = value;
        return self();
    }

    /**
     * Sets the graphic for the increment button of the slider.
     *
     * @param value the {@code Node} to be used as the graphic for the increment button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder incrementGraphic(Node value) {
        this.incrementGraphic = value;
        return self();
    }

    /**
     * Sets the text for the decrement button of the slider.
     *
     * @param value the text to set for the decrement button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder decrementText(String value) {
        this.decrementText = value;
        return self();
    }

    /**
     * Sets a graphical representation for the decrement button of the slider.
     *
     * @param value the Node to be used as the graphic for the decrement button
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder decrementGraphic(Node value) {
        this.decrementGraphic = value;
        return self();
    }

    /**
     * Sets the block increment value for the slider.
     *
     * @param value the new block increment value
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder blockIncrement(double value) {
        this.blockIncrement = value;
        return self();
    }

    /**
     * Set the major tick unit
     * @param value the major tick unit as a double
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder majorTickUnit(double value) {
        this.majorTickUnit = value;
        return self();
    }

    /**
     * Set to {@code true} to snap to tick marks.
     * @param value {@code true} to snap to tick marks
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder snapToTicks(boolean value) {
        this.snapToTicks = value;
        return self();
    }

    /**
     * Configures whether to show the tick labels on the slider.
     *
     * @param value true to show tick labels, false to hide them.
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder showTickLabels(boolean value) {
        this.showTickLabels = value;
        return self();
    }

    /**
     * Configures whether the slider should display tick marks.
     *
     * @param value true to show tick marks, false to hide them
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public SliderBuilder showTickMarks(boolean value) {
        this.showTickMarks = value;
        return self();
    }

    /**
     * Binds the slider to the specified {@code ObservableNumberValue}, enabling the slider to react to changes in the bound value.
     *
     * @param value the {@code ObservableNumberValue} to bind to the slider
     * @return this instance of {@code SliderBuilder} for method chaining
     */
    public SliderBuilder bindBidirectional(Property<Number> value) {
        valueBindings.add(value);
        return self();
    }

    /**
     * Binds the minimum value of the slider to the specified {@code ObservableNumberValue}, ensuring
     * that the slider will respond dynamically to changes in the bound value.
     *
     * @param value the {@code ObservableNumberValue} to bind to the slider's minimum value
     * @return this instance of {@code SliderBuilder} for method chaining
     */
    public SliderBuilder bindMin(Property<Number> value) {
        minBindings.add(value);
        return self();
    }

    /**
     * Binds the maximum value of the slider to the specified {@code ObservableNumberValue},
     * allowing the slider's maximum value to dynamically reflect changes in the bound value.
     *
     * @param value the {@code ObservableNumberValue} to bind to the slider's maximum value
     * @return this instance of {@code SliderBuilder} for method chaining
     */
    public SliderBuilder bindMax(Property<Number> value) {
        maxBindings.add(value);
        return self();
    }

    /**
     * Builds the configured {@code SliderWithButtons} instance.
     *
     * @return the built {@code SliderWithButtons} instance.
     */
    public SliderWithButtons build() {
        BiFunction<? super Double, ? super Double, String> fmtr = LangUtil.orElse(formatter, (a, b) -> "");

        SliderWithButtons slider = new SliderWithButtons(mode, fmtr);
        applyTo(slider);

        applyIfNonNull(orientation, slider::setOrientation);
        applyIfNonNull(min, slider::setMin);
        applyIfNonNull(max, slider::setMax);
        applyIfNonNull(value, slider::set);
        applyIfNonNull(incrementText, slider::setIncrementText);
        applyIfNonNull(incrementGraphic, slider::setIncrementGraphic);
        applyIfNonNull(decrementText, slider::setDecrementText);
        applyIfNonNull(decrementGraphic, slider::setDecrementGraphic);
        applyIfNonNull(blockIncrement, slider::setBlockIncrement);
        applyIfNonNull(majorTickUnit, slider::setMajorTickUnit);

        slider.setShowTickMarks(showTickMarks);
        slider.setSnapToTicks(snapToTicks);
        slider.setShowTickLabels(showTickLabels);

        addBindings(slider::valueAsDoubleProperty, valueBindings);
        addBindings(slider::minProperty, minBindings);
        addBindings(slider::maxProperty, maxBindings);

        return slider;
    }

    private void addBindings(Supplier<DoubleProperty> property, List<Property<Number>> bindings) {
        if (!bindings.isEmpty()) {
            DoubleProperty prop = property.get();
            for (var binding : valueBindings) {
                prop.bindBidirectional(binding);
            }
        }
    }

}

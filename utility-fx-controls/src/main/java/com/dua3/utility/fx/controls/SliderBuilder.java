package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;

import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;

public class SliderBuilder {
    private final SliderWithButtons slider;

    SliderBuilder(SliderWithButtons.Mode mode, BiFunction<Double, Double, String> formatter) {
        slider = new SliderWithButtons(mode, formatter);
    }

    public SliderBuilder orientation(Orientation value) {
        slider.setOrientation(value);
        return this;
    }

    public SliderBuilder min(double value) {
        slider.setMin(value);
        return this;
    }

    public SliderBuilder max(double value) {
        slider.setMax(value);
        return this;
    }

    public SliderBuilder value(double value) {
        slider.setValue(value);
        return this;
    }

    public SliderBuilder incrementText(String value) {
        slider.setIncrementText(value);
        return this;
    }

    public SliderBuilder incrementGraphic(Node value) {
        slider.setIncrementGraphic(value);
        return this;
    }

    public SliderBuilder decrementText(String value) {
        slider.setDecrementText(value);
        return this;
    }

    public SliderBuilder decrementGraphic(Node value) {
        slider.setDecrementGraphic(value);
        return this;
    }

    public SliderBuilder blockIncrement(double value) {
        slider.setBlockIncrement(value);
        return this;
    }

    public SliderBuilder showTickLabels(boolean value) {
        slider.setShowTickLabels(value);
        return this;
    }

    public SliderBuilder showTickMarks(boolean value) {
        slider.setShowTickMarks(value);
        return this;
    }

    public SliderBuilder onChange(DoubleConsumer onChange) {
        slider.valueProperty().addListener((v, o, n) -> onChange.accept(n.doubleValue()));
        return this;
    }

    public SliderBuilder bind(ObservableNumberValue value) {
        slider.valueProperty().bind(value);
        return this;
    }

    public SliderBuilder bindBidirectional(Property<Number> value) {
        slider.valueProperty().bindBidirectional(value);
        return this;
    }

    public SliderBuilder bindMin(ObservableNumberValue value) {
        slider.minProperty().bind(value);
        return this;
    }

    public SliderBuilder bindMax(ObservableNumberValue value) {
        slider.maxProperty().bind(value);
        return this;
    }

    public SliderWithButtons build() {
        return slider;
    }
}

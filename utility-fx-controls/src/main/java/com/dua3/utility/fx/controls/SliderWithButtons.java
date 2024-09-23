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

    public double getMax() {
        return slider.getMax();
    }

    public void setMax(double value) {
        slider.setMax(value);
    }

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

    public void setValue(double value) {
        slider.setValue(value);
    }

    public void setOrientation(Orientation orientation) {
        if (orientation != slider.getOrientation()) {
            slider.setOrientation(orientation);
            initPane();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setDecrementText(String value) {
        btnDecrement.setText(value);
    }

    public void setDecrementGraphic(Node value) {
        btnDecrement.setGraphic(value);
    }

    public void setIncrementText(String value) {
        btnIncrement.setText(value);
    }

    public void setIncrementGraphic(Node value) {
        btnIncrement.setGraphic(value);
    }

    public void setShowTickLabels(boolean value) {
        slider.setShowTickLabels(value);
    }

    public void setShowTickMarks(boolean value) {
        slider.setShowTickMarks(value);
    }

    public double getMin() {
        return slider.getMin();
    }

    public void setMin(double value) {
        slider.setMin(value);
    }

    public double getMajorTickUnit() {
        return slider.getMajorTickUnit();
    }

    public double getMinorTickCount() {
        return slider.getMinorTickCount();
    }

    public double getBlockIncrement() {
        return slider.getBlockIncrement();
    }

    public void setBlockIncrement(double value) {
        slider.setBlockIncrement(value);
    }

    public DoubleProperty valueProperty() {
        return slider.valueProperty();
    }

    public DoubleProperty minProperty() {
        return slider.minProperty();
    }

    public DoubleProperty maxProperty() {
        return slider.maxProperty();
    }

    public DoubleProperty majorTickUnitProperty() {
        return slider.majorTickUnitProperty();
    }

    public IntegerProperty minorTickCountProperty() {
        return slider.minorTickCountProperty();
    }

    public BooleanProperty valueChangingProperty() {
        return slider.valueChangingProperty();
    }

    public ObjectProperty<StringConverter<Double>> labelFormatterProperty() {
        return slider.labelFormatterProperty();
    }

    public BooleanProperty showTickLabelsProperty() {
        return slider.showTickLabelsProperty();
    }

    public BooleanProperty showTickMarksProperty() {
        return slider.showTickMarksProperty();
    }

    public BooleanProperty snapToTicksProperty() {
        return slider.snapToTicksProperty();
    }

    public enum Mode {
        SLIDER_ONLY,
        SLIDER_VALUE,
        SLIDER_VALUE_TOTAL,
        SLIDER_INPUT_TOTAL,
    }

}

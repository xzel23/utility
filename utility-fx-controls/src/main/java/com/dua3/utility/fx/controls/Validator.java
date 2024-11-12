package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.ValidationResult;
import com.dua3.utility.fx.icons.IconView;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * The Validator class is used for validating controls in a JavaFX application. It provides methods for setting
 * validation rules for different types of controls, such as TextInputControl and Control. It also allows for
 * custom validation rules to be set.
 * <p>
 * The Validator class supports the use of a ResourceBundle for looking up message texts. If a ResourceBundle is
 * provided during initialization, it will be used to retrieve the messages for validation errors. Otherwise,
 * the message will be used as is.
 * <p>
 * The Validator class also supports decorating nodes with invalid values. When a control fails validation, an
 * icon and tooltip can be added to the control to indicate the validation error.
 * <p>
 * The Validator class is meant to be used in a JavaFX application and should be initialized with a valid
 * ResourceBundle if localized validation messages are required.
 */
public class Validator {
    private static final Logger LOG = LogManager.getLogger(Validator.class);

    private final @Nullable  ResourceBundle resources;
    private final LinkedHashMap<Control, List<Supplier<ValidationResult>>> controls = new LinkedHashMap<>();
    private final MapProperty<Control, ValidationResult> validationResultProperty = new SimpleMapProperty<>();
    private final BooleanProperty validProperty = new SimpleBooleanProperty();
    private final List<Runnable> disposeList = new ArrayList<>();
    private int iconSize = (int) Math.round(Font.getDefault().getSize());
    private String iconError = "fth-alert-triangle";
    private boolean decorateNodes = false;

    /**
     * Creates a Validator instance without assigning a resource bundle.
     */
    public Validator() {
        this(null);
    }

    /**
     * Run cleanup actions.
     */
    public void dispose() {
        disposeList.forEach(Runnable::run);
        disposeList.clear();
    }

    /**^
     * Creates a Validator instance.
     *
     * @param resources the resource bundle to look up message texts
     */
    public Validator(@Nullable ResourceBundle resources) {
        this.resources = resources;
    }

    /**
     * Get list of rules for control. If the control is not registered yet, it will be assigned a newly generated
     * empty list.
     *
     * @param c the control
     * @return the list of rules
     */
    private List<Supplier<ValidationResult>> rules(Control c) {
        return controls.computeIfAbsent(c, this::createRuleList);
    }

    /**
     * Creates a new rule list and sets the control to be validated on focus change.
     *
     * @param control the control to create a rule list for
     * @return new rule list for the control
     */
    private List<Supplier<ValidationResult>> createRuleList(Control control) {
        control.setFocusTraversable(true);
        ChangeListener<Object> changeListener = (v, o, n) -> validateNode(control);
        if (control instanceof InputControl<?> c) {
            c.valueProperty().addListener(changeListener);
            disposeList.add(() -> c.valueProperty().removeListener(changeListener));
        } else if (control instanceof TextInputControl c) {
            c.textProperty().addListener(changeListener);
            disposeList.add(() -> c.textProperty().removeListener(changeListener));
        } else {
            control.focusedProperty().addListener((v, o, n) -> validateNode(control));
            disposeList.add(() -> control.focusedProperty().removeListener(changeListener));
        }
        return new ArrayList<>();
    }

    /**
     * Add a rule that assures the control is not empty.
     *
     * @param c       the control
     * @param message the message to display if validation fails
     */
    public void disallowEmpty(TextInputControl c, String message) {
        rules(c).add(() -> !c.getText().isEmpty() ? ValidationResult.ok(c) : ValidationResult.error(c, message));
    }

    /**
     * Add a rule that assures the content matches a regular expression.
     *
     * @param c       the control
     * @param message the message to display if validation fails
     * @param regex   the regular expression to test the control's text
     */
    public void setRegex(TextInputControl c, String message, String regex) {
        rules(c).add(() -> c.getText().matches(regex) ? ValidationResult.ok(c) : ValidationResult.error(c, message));
    }

    /**
     * Custom validation.
     *
     * @param c       the control
     * @param message the message to display if validation fails
     * @param test    the test to perform the validation
     * @param trigger the {@link Observable}s that trigger validation
     */
    public void addCheck(Control c, String message, BooleanSupplier test, Observable... trigger) {
        rules(c).add(() -> test.getAsBoolean() ? ValidationResult.ok(c) : ValidationResult.error(c, message));
        Arrays.stream(trigger).forEach(t -> {
            InvalidationListener il = tt -> validateNode(c);
            t.addListener(il);
            disposeList.add(() -> t.removeListener(il));
        });
    }

    /**
     * Custom validation.
     *
     * @param c       the control
     * @param message the message to display if validation fails
     * @param test    ObservableValue that triggers updates and provides the validation result
     */
    public void addCheck(Control c, String message, ObservableValue<Boolean> test) {
        rules(c).add(() -> test.getValue() ? ValidationResult.ok(c) : ValidationResult.error(c, message));
        test.addListener((v, o, n) -> validateNode(c));
    }

    private String getMessage(@Nullable String m) {
        if (m == null || m.isEmpty()) {
            return "";
        }

        if (resources == null) {
            return m;
        }

        try {
            return resources.getString(m);
        } catch (MissingResourceException e) {
            LOG.warn("resource string not found: {}", m, e);
            return m;
        }
    }

    /**
     * Validate all rules of this validator, update decorations, and update value of validProperty.
     */
    public void validateAll() {
        Map<Control, ValidationResult> resultMap = new IdentityHashMap<>(controls.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), this::validate)));
        validationResultProperty.set(FXCollections.observableMap(resultMap));
        validProperty.set(resultMap.values().stream().anyMatch(entry -> !entry.isOk()));
    }

    private ValidationResult validateNode(Control c) {
        validateAll();
        return Optional.ofNullable(validationResultProperty.get(c)).orElseGet(() -> ValidationResult.ok(c));
    }

    private ValidationResult validate(Control c) {
        ValidationResult validationResult = rules(c).stream()
                .map(Supplier::get)
                .reduce(ValidationResult::merge)
                .orElseGet(() -> ValidationResult.ok(c));

        LOG.debug("validate(): {}", validationResult);

        // update control decorations
        updateDecoration(c, validationResult);

        return validationResult;
    }

    /**
     * Remove all validation decorations from controls.
     */
    public void clearDecorations() {
        controls.keySet().forEach(c -> Decoration.removeDecoration(c, getClass().getName()));
    }

    /**
     * Set to true to enable decorating nodes with invalid values.
     * @param decorateNodes true, if decoration shall be added to controls with invalid values
     */
    public void setDecorateNodes(boolean decorateNodes) {
        this.decorateNodes = decorateNodes;
    }

    /**
     * Check if decorations are enabled.
     * @return true, if decorations are enabled
     */
    public boolean isDecorateNodes() {
        return decorateNodes;
    }

    private void updateDecoration(Control c, ValidationResult vr) {
        if (!decorateNodes) {
            return;
        }

        // remove decorations
        Decoration.getDecorations(c).clear();

        String iconId = null;
        Paint paint = null;
        switch (vr.level()) {
            case OK:
                break;
            case ERROR:
                iconId = iconError;
                paint = Color.RED;
                break;
        }

        if (iconId != null) {
            IconView icon = new IconView();

            String message = getMessage(vr.message());
            if (!message.isEmpty()) {
                Tooltip.install(icon, new Tooltip(message));
            }

            icon.setFocusTraversable(false);
            icon.setIconIdentifier(iconId);
            icon.setIconColor(paint);
            icon.setIconSize(iconSize);
            icon.setStyle(String.format("-fx-translate-x: -%1$d; -fx-translate-y: %1$d;", (iconSize + 1) / 2));
            Decoration.addDecoration(c, Pos.TOP_RIGHT, icon, getClass().getName());
        } else {
            Decoration.removeDecoration(c, getClass().getName());
        }
    }

    /**
     * Set icon size.
     * @param sz the new size
     */
    public void setIconSize(int sz) {
        iconSize = LangUtil.requirePositive(sz);
    }

    /**
     * Add validation for a button. If validation is added to a button, the validation will be performed when
     * the button is pressed, and if validation fails, the button clicked event will be consumed.
     *
     * @param button the button to add validation to
     */
    public void addValidation(@Nullable Button button) {
        if (button == null) {
            LOG.warn("addValidation(): button is null");
            return;
        }

        button.addEventFilter(ActionEvent.ACTION, ae -> {
            if (!validProperty.get()) {
                ae.consume(); //not valid
            }
        });
    }

    /**
     * Focuses the first control.
     */
    public void focusFirst() {
        controls.keySet().stream().findFirst().ifPresent(Control::requestFocus);
    }

    /**
     * Focuses the first control with an invalid validation result.
     */
    public void focusFirstInvalid() {
        validationResultProperty.entrySet().stream()
                .filter(entry -> !entry.getValue().isOk())
                .findFirst()
                .map(Map.Entry::getKey)
                .ifPresent(Control::requestFocus);
    }

    /**
     * Returns the read-only boolean property indicating the validity of all controls.
     *
     * @return the validProperty
     */
    public ReadOnlyBooleanProperty validProperty() {
        return validProperty;
    }
}

package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Converter;
import com.dua3.utility.fx.PropertyConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.icons.Icon;
import com.dua3.utility.fx.icons.IconUtil;
import com.dua3.utility.fx.icons.IconView;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility class for creating and managing UI controls.
 */
public final class Controls {
    private static final Logger LOG = LogManager.getLogger(Controls.class);

    private Controls() {
    }

    /**
     * Create {@link ButtonBuilder} instance for standard buttons.
     *
     * @return new ButtonBuilder
     */
    public static ButtonBuilder<Button> button() {
        return new ButtonBuilder<>(Button::new);
    }

    /**
     * Create {@link ButtonBuilder} instance for toggle buttons.
     *
     * @return new ButtonBuilder
     */
    public static ToggleButtonBuilder toggleButton() {
        return new ToggleButtonBuilder(ToggleButton::new);
    }

    /**
     * Create {@link ButtonBuilder} instance for toggle buttons.
     *
     * @param selected the initial selection state of the button
     * @return new ButtonBuilder
     */
    public static ToggleButtonBuilder toggleButton(boolean selected) {
        return new ToggleButtonBuilder(() -> {
            ToggleButton b = new ToggleButton();
            b.setSelected(selected);
            return b;
        });
    }

    /**
     * Create {@link ButtonBuilder} instance for checkboxes.
     *
     * @return new ButtonBuilder
     */
    public static CheckBoxButtonBuilder checkbox() {
        return new CheckBoxButtonBuilder(CheckBox::new);
    }

    /**
     * Create {@link ButtonBuilder} instance for checkboxes.
     *
     * @param selected the initial selection state of the button
     * @return new ButtonBuilder
     */
    public static CheckBoxButtonBuilder checkbox(boolean selected) {
        return new CheckBoxButtonBuilder(() -> {
            CheckBox b = new CheckBox();
            b.setSelected(selected);
            return b;
        });
    }

    /**
     * Creates a new FileInputBuilder instance with the given mode.
     *
     * @param parentWinndw the parent window
     * @param mode the {@link FileDialogMode} of the file dialog
     * @return a new FileInputBuilder instance
     */
    public static FileInputBuilder fileInput(@Nullable Window parentWinndw, FileDialogMode mode) {
        return new FileInputBuilder(parentWinndw, mode);
    }

    /**
     * Create {@link SliderBuilder} instance.
     *
     * @return new SliderBuilder
     */
    public static SliderBuilder slider() {
        return new SliderBuilder();
    }

    /**
     * Create {@link javafx.scene.control.Separator}.
     *
     * @param orientation the separator orientation
     * @return new {@link Separator}
     */
    public static Node separator(Orientation orientation) {
        return new Separator(orientation);
    }

    /**
     * Get graphic for an icon by icon name.
     *
     * @param name the icon name
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node graphic(String name) {
        return icon(name).node();
    }

    /**
     * Get icon by name.
     *
     * @param name the icon name
     * @return icon
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Icon icon(String name) {
        return IconUtil.iconFromName(name).orElseThrow(() -> new IllegalStateException("unknown icon: " + name));
    }

    /**
     * Create a fixed minimum width label with the given text to prevent an ellipsis being displayed.
     *
     * @param text the text to display on the label
     * @return a new Label instance with fixed miimum width
     * @see Label
     */
    public static Label rigidLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    /**
     * Create a fixed minimum width label with the given text to prevent an ellipsis being display.
     *
     * @param text the text to display on the label
     * @param node the node to display on the label
     * @return a new Label instance with fixed miimum width
     * @see Label
     */
    public static Label rigidLabel(String text, Node node) {
        Label label = new Label(text, node);
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    /**
     * Get graphic for an icon by icon name.
     *
     * @param name the icon name
     * @param size the requested size
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node graphic(String name, int size) {
        Icon icon = icon(name);
        icon.setIconSize(size);
        return icon.node();
    }

    /**
     * Get graphic for an icon by icon name.
     *
     * @param name  the icon name
     * @param size  the requested size
     * @param paint the {@link Paint} to use
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node graphic(String name, int size, Paint paint) {
        Icon icon = icon(name);
        icon.setIconSize(size);
        icon.setIconColor(paint);
        return icon.node();
    }

    /**
     * Get graphic for an icon by icon name.
     *
     * @param name  the icon name
     * @param size  the requested size
     * @param color the {@link Color} to use
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node graphic(String name, int size, Color color) {
        return graphic(name, size, FxUtil.convert(color));
    }

    /**
     * Create an Icon with a tooltip.
     *
     * @param name  the icon name
     * @param size  the requested size
     * @param paint the {@link Paint} to use
     * @param tooltipText the text to display as tooltip
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node tooltipIcon(String name, int size, Paint paint, String tooltipText) {
        IconView iv = new IconView(name, size, paint);
        if (!tooltipText.isBlank()) {
            iv.setTooltip(new Tooltip(tooltipText));
        }
        return iv;
    }

    /**
     * Create an Icon with a tooltip.
     *
     * @param name  the icon name
     * @param size  the requested size
     * @param color the {@link Color} to use
     * @param tooltipText the text to display as tooltip
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see IconUtil#iconFromName(String)
     */
    public static Node tooltipIcon(String name, int size, Color color, String tooltipText) {
        return tooltipIcon(name, size, FxUtil.convert(color), tooltipText);
    }

    /**
     * Get TextFieldBuilder for creating a TextField.
     * @param locale the locale to use
     * @return TextFieldBuilder instance
     */
    public static TextFieldBuilder textField(Locale locale) {
        return new TextFieldBuilder(locale);
    }

    /**
     * Get TextFieldBuilder for creating a TextField.
     * @return TextFieldBuilder instance
     */
    public static TextAreaBuilder textArea() {
        return new TextAreaBuilder();
    }

    /**
     * Make a region resizable by dragging its edge.
     *
     * @param region  the region
     * @param borders the borders to make draggable
     */
    public static void makeResizable(Region region, Position... borders) {
        DragResizer.makeResizable(region, 6, borders);
    }

    /**
     * Make a region resizable by dragging its edge.
     *
     * @param region       the region
     * @param resizeMargin size of the draggable margin
     * @param borders      the borders to make draggable
     */
    public static void makeResizable(Region region, int resizeMargin, Position... borders) {
        DragResizer.makeResizable(region, resizeMargin, borders);
    }

    /**
     * Create new {@link Menu}.
     *
     * @param text    the text to show
     * @param items   the menu items
     * @return new menu
     */
    public static Menu menu(String text, MenuItem... items) {
        return menu(text, null, items);
    }

    /**
     * Create new {@link Menu}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param items   the menu items
     * @return new menu
     */
    public static Menu menu(@Nullable String text, @Nullable Node graphic, MenuItem... items) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        return new Menu(text, graphic, items);
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text   the text to show
     * @param action the action to perform when the menu item is invoked
     * @return new menu item
     */
    public static MenuItem menuItem(String text, Runnable action) {
        return menuItem(text, null, action);
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @return new menu item
     */
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        MenuItem mi = new MenuItem(text, graphic);
        mi.setOnAction(evt -> action.run());
        return mi;
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the property controlling the enabled state
     * @return new menu item
     */
    public static MenuItem menuItem(String text, Runnable action, ObservableBooleanValue enabled) {
        return menuItem(text, null, action, enabled);
    }

    /**
     * Create new {@link MenuItem}.
     *
     * <p><strong>NOTE: </strong> the {@code enabled} state is permanent. Use this method only for menus where the
     * menu state will not be changed, for example, in context menus. If the state is dynamic, use the overload taking
     * an {@link ObservableBooleanValue} instead.
     *
     * @param text    the text to show
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the property controlling the enabled state
     * @return new menu item
     */
    public static MenuItem menuItem(String text, Runnable action, boolean enabled) {
        return menuItem(text, null, action, FxUtil.constant(enabled));
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the property controlling the enabled state
     * @return new menu item
     */
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action, ObservableBooleanValue enabled) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        MenuItem mi = new MenuItem(text, graphic);
        mi.disableProperty().bind(Bindings.not(enabled));
        mi.setOnAction(evt -> action.run());
        return mi;
    }

    /**
     * Create new {@link MenuItem}.
     *
     * <p><strong>NOTE: </strong> the {@code enabled} state is permanent. Use this method only for menus where the
     * menu state will not be changed, for example, in context menus. If the state is dynamic, use the overload taking
     * an {@link ObservableBooleanValue} instead.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the enabled state
     * @return new menu item
     */
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action, boolean enabled) {
        return menuItem(text, graphic, action, FxUtil.constant(enabled));
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text      the text to show
     * @param action    the action to perform when the menu item is invoked
     * @param initialState  flag indicating the initial state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(String text, Consumer<Boolean> action, boolean initialState) {
        return checkMenuItem(text, null, action, initialState);
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @param initialState  flag indicating the initial state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Consumer<Boolean> action, boolean initialState) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        CheckMenuItem mi = new CheckMenuItem(text, graphic);
        mi.setOnAction(evt -> action.accept(mi.isSelected()));
        mi.selectedProperty().set(initialState);
        return mi;
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param selected the property controlling the selected state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(String text, Property<Boolean> selected) {
        return checkMenuItem(text, (Node) null, selected);
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param selected the property controlling the selected state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Property<Boolean> selected) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        CheckMenuItem mi = new CheckMenuItem(text, graphic);
        mi.selectedProperty().bindBidirectional(selected);
        return mi;
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param selected the property controlling the selected state
     * @param enabled the property controlling the enabled state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(String text, Property<Boolean> selected, ObservableBooleanValue enabled) {
        return checkMenuItem(text, null, selected, enabled);
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param selected the property controlling the selected state
     * @param enabled the property controlling the enabled state
     * @return new menu item
     */
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Property<Boolean> selected, ObservableBooleanValue enabled) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        CheckMenuItem mi = new CheckMenuItem(text, graphic);
        mi.disableProperty().bind(Bindings.not(enabled));
        mi.selectedProperty().bindBidirectional(selected);
        return mi;
    }

    /**
     * Creates a choice menu with selectable options based on a property and a collection of possible values.
     * <p>
     * The menu allows the user to choose one of the provided values, and the selection is reflected
     * in the associated property. Each choice is represented as a check menu item, and the menu
     * can be disabled based on the provided {@code enabled} value.
     *
     * @param <T> the type of the values and the associated property
     * @param text the text to display as the label of the menu; can be null if {@code graphic} is not null
     * @param graphic a Node to display as the graphic of the menu; can be null if {@code text} is not null
     * @param enabled an observable value controlling whether the menu is enabled or disabled
     * @param property the property that reflects the currently selected menu option
     * @param values the collection of possible values to be presented as menu options
     * @return the created {@code Menu} object populated with selectable items
     * @throws IllegalArgumentException if both {@code text} and {@code graphic} are null
     */
    public static <T extends @Nullable Object> Menu choiceMenu(@Nullable String text, @Nullable Node graphic, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        if (text == null && graphic == null) {
            throw new IllegalArgumentException("text and graphic must not both be null");
        }
        Menu menu = new Menu(text, graphic);
        T current = property.getValue();
        for (T value : values) {
            Property<@Nullable Boolean> selected = new SimpleBooleanProperty(Objects.equals(current, value));
            Converter<@Nullable T, @Nullable Boolean> converter = Converter.create(
                    v -> Objects.equals(v, value),
                    b -> b != null && b ? value : property.getValue()
            );
            selected.bindBidirectional(PropertyConverter.convert(property, converter));
            CheckMenuItem mi = checkMenuItem(String.valueOf(value), selected);

            // make sure selected is not GC'ed
            FxUtil.addStrongReference(mi.selectedProperty(), selected);

            menu.getItems().add(mi);
        }
        menu.disableProperty().bind(Bindings.not(enabled));
        return menu;
    }

    /**
     * Creates a menu with a set of selectable choices based on the provided values.
     *
     * @param <T> the type of the values and the associated property
     * @param text the title or label for the menu
     * @param enabled an observable boolean value indicating if the menu should be enabled or disabled
     * @param property the property to be bound to the selected value in the menu
     * @param values the collection of values to populate the menu choices
     * @return the constructed menu object with the specified properties and choices
     */
    public static <T extends @Nullable Object> Menu choiceMenu(String text, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        return choiceMenu(text, null, enabled, property, values);
    }

    /**
     * Creates and returns a menu with choices derived from the provided collection of values.
     * This method associates the menu with a specified property that reflects the current selection.
     *
     * @param <T> the type of the values and the associated property
     * @param graphic the graphical representation to be displayed with the menu
     * @param enabled an observable boolean value indicating whether the menu is enabled
     * @param property a property that represents the selected value in the menu
     * @param values the collection of selectable values to be displayed in the menu
     * @return a new Menu instance configured with the specified options and behavior
     */
    public static <T extends @Nullable Object> Menu choiceMenu(Node graphic, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        return choiceMenu(null, graphic, enabled, property, values);
    }

    /**
     * Creates a menu with selectable choices based on the provided text, property, and values.
     * The menu allows selection of items from the given collection of values.
     *
     * @param <T> the type of the values and the associated property
     * @param text the label or prompt text for the menu
     * @param property the property to bind the selected value to
     * @param values the collection of values to populate the menu with
     * @return a Menu object populated with the specified choices
     */
    public static <T extends @Nullable Object> Menu choiceMenu(String text, Property<T> property, Collection<T> values) {
        return choiceMenu(text, null, FxUtil.ALWAYS_TRUE, property, values);
    }

    /**
     * Creates a menu allowing users to choose from a collection of values and bind the selected value to a property.
     *
     * @param <T>      the type of items in the menu
     * @param graphic  the graphic node to be displayed alongside the menu items, can be null
     * @param property the property to which the selected value will be bound
     * @param values   the collection of available values to choose from
     * @return a Menu instance populated with the provided values
     */
    public static <T extends @Nullable Object> Menu choiceMenu(Node graphic, Property<T> property, Collection<T> values) {
        return choiceMenu(null, graphic, FxUtil.ALWAYS_TRUE, property, values);
    }

    /**
     * Creates and returns a new instance of TextBuilder.
     *
     * @param text the text
     * @return a new instance of TextBuilder.
     */
    public static TextBuilder text(String text) {
        return new TextBuilder(text);
    }

    /**
     * Creates and returns a new instance of TextBuilder.
     *
     * @param text the text
     * @return a new instance of TextBuilder.
     */
    public static TextBuilder text(ObservableValue<String> text) {
        return new TextBuilder(text);
    }

    /**
     * Creates and returns a new instance of LabelBuilder.
     *
     * @param text the text
     * @return a new instance of LabelBuilder.
     */
    public static LabelBuilder label(String text) {
        return new LabelBuilder(text);
    }

    /**
     * Creates and returns a new instance of LabelBuilder.
     *
     * @param text the text
     * @return a new instance of LabelBuilder.
     */
    public static LabelBuilder label(ObservableValue<String> text) {
        return new LabelBuilder(text);
    }

    /**
     * Sets the initial directory for a file or directory chooser.
     *
     * @param setInitialDirectory a Consumer to set the initial directory
     * @param dir the initial directory path, or null if not specified
     */
    static void setInitialDirectory(Consumer<File> setInitialDirectory, @Nullable Path dir) {
        if (dir != null) {
            // NOTE there's an inconsistency between Paths.get("").toFile() and new File(""), so convert Path to File
            // before testing for directory and do not use Files.isDirectory(Path)
            try {
                File initialFile = dir.toFile();
                if (initialFile.isDirectory()) {
                    setInitialDirectory.accept(initialFile);
                }
            } catch (UnsupportedOperationException e) {
                LOG.warn("could not set initial directory", e);
            }
        }
    }

    /**
     * Creates a Background object based on the given Color.
     *
     * @param color the Color to be used for the Background
     * @return a new Background object with the specified Color
     */
    public static Background background(Color color) {
        return new Background(new BackgroundFill(FxUtil.convert(color), null, null));
    }

    /**
     * Creates a spacer node with a default growth priority.
     *
     * @return a Region object configured as a spacer with the default Priority.ALWAYS.
     */
    public static Region spacer() {
        return spacer(Priority.ALWAYS);
    }

    /**
     * Creates a spacer region with the specified priority for horizontal and vertical growth.
     *
     * @param priority the growth priority to set for the spacer in horizontal and vertical layouts
     * @return a spacer region configured with the specified growth priority
     */
    public static Region spacer(Priority priority) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, priority);
        VBox.setVgrow(spacer, priority);
        return spacer;
    }
}

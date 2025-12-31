package com.dua3.utility.fx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
     * Create a new {@link MenuBuilder}.
     *
     * @return a new MenuBuilder
     */
    public static MenuBuilder menu() {
        return new MenuBuilder(Menu::new);
    }

    /**
     * Creates and returns a new instance of {@code ChoiceMenuBuilder} with the specified collection of values.
     * <p>
     * Use an {@link ObservableList} for {@code values} to automatically update menu items.
     *
     * @param <T> The type of the elements in the collection.
     * @param values The collection of values to be presented in the choice menu.
     * @return A {@code ChoiceMenuBuilder} instance initialized with the given collection of values.
     */
    public static <T> ChoiceMenuBuilder<T> choiceMenu(Collection<T> values) {
        return new ChoiceMenuBuilder<>(values);
    }

    /**
     * Create a new {@link MenuItemBuilderImpl}.
     *
     * @return a new MenuItemBuilder
     */
    public static MenuItemBuilderImpl menuItem() {
        return new MenuItemBuilderImpl(MenuItem::new);
    }

    /**
     * Create a new {@link CheckMenuItemBuilder}.
     *
     * @return a new CheckMenuItemBuilder
     */
    public static CheckMenuItemBuilder checkMenuItem() {
        return new CheckMenuItemBuilder(CheckMenuItem::new);
    }

    /**
     * Create new {@link Menu}.
     *
     * @param text    the text to show
     * @param items   the menu items
     * @return new menu
     * @deprecated use {@link #menu()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static Menu menu(String text, MenuItem... items) {
        return menu().text(text).items(items).build();
    }

    /**
     * Create new {@link Menu}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param items   the menu items
     * @return new menu
     * @deprecated use {@link #menu()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static Menu menu(@Nullable String text, @Nullable Node graphic, MenuItem... items) {
        MenuBuilder builder = menu();
        if (text != null) builder.text(text);
        if (graphic != null) builder.graphic(graphic);
        return builder.items(items).build();
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text   the text to show
     * @param action the action to perform when the menu item is invoked
     * @return new menu item
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(String text, Runnable action) {
        return menuItem().text(text).action(action).build();
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @return new menu item
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action) {
        MenuItemBuilderImpl builder = menuItem();
        if (text != null) builder.text(text);
        if (graphic != null) builder.graphic(graphic);
        return builder.action(action).build();
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the property controlling the enabled state
     * @return new menu item
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(String text, Runnable action, ObservableBooleanValue enabled) {
        return menuItem().text(text).action(action).enabled(enabled).build();
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
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(String text, Runnable action, boolean enabled) {
        return menuItem().text(text).action(action).disabled(!enabled).build();
    }

    /**
     * Create new {@link MenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @param enabled the property controlling the enabled state
     * @return new menu item
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action, ObservableBooleanValue enabled) {
        MenuItemBuilderImpl builder = menuItem();
        if (text != null) builder.text(text);
        if (graphic != null) builder.graphic(graphic);
        return builder.action(action).enabled(enabled).build();
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
     * @deprecated use {@link #menuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static MenuItem menuItem(@Nullable String text, @Nullable Node graphic, Runnable action, boolean enabled) {
        MenuItemBuilderImpl builder = menuItem();
        if (text != null) builder.text(text);
        if (graphic != null) builder.graphic(graphic);
        return builder.action(action).disabled(!enabled).build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text      the text to show
     * @param action    the action to perform when the menu item is invoked
     * @param initialState  flag indicating the initial state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(String text, Consumer<Boolean> action, boolean initialState) {
        return checkMenuItem()
                .text(text)
                .action(action)
                .selected(initialState)
                .build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param action  the action to perform when the menu item is invoked
     * @param initialState  flag indicating the initial state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Consumer<Boolean> action, boolean initialState) {
        return checkMenuItem()
                .text(text)
                .graphic(graphic)
                .action(action)
                .selected(initialState)
                .build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param selected the property controlling the selected state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(String text, Property<Boolean> selected) {
        return checkMenuItem()
                .text(text)
                .selected(selected)
                .build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param selected the property controlling the selected state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Property<Boolean> selected) {
        return checkMenuItem()
                .text(text)
                .graphic(graphic)
                .selected(selected)
                .build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param selected the property controlling the selected state
     * @param enabled the property controlling the enabled state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(String text, Property<Boolean> selected, ObservableBooleanValue enabled) {
        return checkMenuItem()
                .text(text)
                .selected(selected)
                .enabled(enabled)
                .build();
    }

    /**
     * Create new {@link CheckMenuItem}.
     *
     * @param text    the text to show
     * @param graphic the graphic to show before the text
     * @param selected the property controlling the selected state
     * @param enabled the property controlling the enabled state
     * @return new menu item
     * @deprecated use {@link #checkMenuItem()} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static CheckMenuItem checkMenuItem(@Nullable String text, @Nullable Node graphic, Property<Boolean> selected, ObservableBooleanValue enabled) {
        return checkMenuItem()
                .text(text)
                .graphic(graphic)
                .selected(selected)
                .enabled(enabled)
                .build();
    }

    /**
     * Creates a choice menu with selectable options based on a property and a collection of possible values.
     * <p>
     * The menu allows the user to choose one of the provided values, and the selection is reflected
     * in the associated property. Each choice is represented as a check menu item, and the menu
     * can be disabled based on the provided {@code enabled} value.
     *
     * @param <T>      the type of the values and the associated property
     * @param text     the text to display as the label of the menu; can be null if {@code graphic} is not null
     * @param graphic  a Node to display as the graphic of the menu; can be null if {@code text} is not null
     * @param enabled  an observable value controlling whether the menu is enabled or disabled
     * @param property the property that reflects the currently selected menu option
     * @param values   the collection of possible values to be presented as menu options
     * @return the created {@code Menu} object populated with selectable items
     * @throws IllegalArgumentException if both {@code text} and {@code graphic} are null
     * @deprecated use {@link #choiceMenu(Collection)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static <T extends @Nullable Object> Menu choiceMenu(@Nullable String text, @Nullable Node graphic, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        ChoiceMenuBuilder<T> builder = Controls.<T>choiceMenu(values);
        if (text != null) builder.text(text);
        if (graphic != null) builder.graphic(graphic);
        return builder
                .enabled(enabled)
                .bind(property)
                .build();
    }

    /**
     * Creates a menu with a set of selectable choices based on the provided values.
     *
     * @param <T>      the type of the values and the associated property
     * @param text     the title or label for the menu
     * @param enabled  an observable boolean value indicating if the menu should be enabled or disabled
     * @param property the property to be bound to the selected value in the menu
     * @param values   the collection of values to populate the menu choices
     * @return the constructed menu object with the specified properties and choices
     * @deprecated use {@link #choiceMenu(Collection)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static <T extends @Nullable Object> Menu choiceMenu(String text, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        return choiceMenu(text, null, enabled, property, values);
    }

    /**
     * Creates and returns a menu with choices derived from the provided collection of values.
     * This method associates the menu with a specified property that reflects the current selection.
     *
     * @param <T>      the type of the values and the associated property
     * @param graphic  the graphical representation to be displayed with the menu
     * @param enabled  an observable boolean value indicating whether the menu is enabled
     * @param property a property that represents the selected value in the menu
     * @param values   the collection of selectable values to be displayed in the menu
     * @return a new Menu instance configured with the specified options and behavior
     * @deprecated use {@link #choiceMenu(Collection)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public static <T extends @Nullable Object> Menu choiceMenu(Node graphic, ObservableBooleanValue enabled, Property<T> property, Collection<T> values) {
        return choiceMenu(null, graphic, enabled, property, values);
    }

    /**
     * Creates a menu with selectable choices based on the provided text, property, and values.
     * The menu allows selection of items from the given collection of values.
     *
     * @param <T>      the type of the values and the associated property
     * @param text     the label or prompt text for the menu
     * @param property the property to bind the selected value to
     * @param values   the collection of values to populate the menu with
     * @return a Menu object populated with the specified choices
     * @deprecated use {@link #choiceMenu(Collection)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
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
     * @deprecated use {@link #choiceMenu(Collection)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
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

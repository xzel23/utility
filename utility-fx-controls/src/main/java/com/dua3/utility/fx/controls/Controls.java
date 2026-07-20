package com.dua3.utility.fx.controls;

import com.dua3.utility.lang.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.icons.Icon;
import com.dua3.utility.fx.icons.IconUtil;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for creating and managing UI controls.
 */
public final class Controls {
    private static final Logger LOG = LogManager.getLogger(Controls.class);

    private Controls() {
    }

    /**
     * Configures a MenuBar to use the system's menu bar if supported.
     * <p>
     * This method sets the {@code useSystemMenuBar} property of the provided MenuBar to true and
     * binds its managed property to the opposite of its visibility property to avoid a gap being
     * displayed in the application window.
     *
     * @param menuBar the MenuBar instance to configure
     * @param useSystemMenuBar a boolean specifying whether to use the system menu bar
     */
    public static void useSystemMenuBar(MenuBar menuBar, boolean useSystemMenuBar) {
        if (Platform.isMacOS()) {
            menuBar.setUseSystemMenuBar(useSystemMenuBar);
            menuBar.setManaged(!useSystemMenuBar);
        }
    }

    /**
     * Creates a new instance of {@link ComboBoxBuilder} using the provided collection of values.
     *
     * @param <T> the type of the items in the ComboBox
     * @param values a collection of values to be included in the combo box
     * @return a new instance of {@code ComboBoxBuilder} initialized with the given values
     */
    public static <T> ComboBoxBuilder<T> comboBox(Collection<T> values) {
        return new ComboBoxBuilder<>(values);
    }

    /**
     * Creates a new instance of {@link ComboBoxBuilder} using the provided values.
     *
     * @param <T> the type of the items in the ComboBox
     * @param values the values to be included in the combo box
     * @return a new instance of {@code ComboBoxBuilder} initialized with the given values
     */
    @SafeVarargs
    public static <T> ComboBoxBuilder<T> comboBox(T... values) {
        return new ComboBoxBuilder<>(List.of(values));
    }

    /**
     * Creates a new instance of {@link ComboBoxExBuilder} using the provided collection of values.
     *
     * @param <T> the type of the items in the {@link ComboBoxEx}
     * @param values a collection of values to be included in the combo box
     * @return a new instance of {@code ComboBoxExBuilderEx} initialized with the given values
     */
    public static <T> ComboBoxExBuilder<T> comboBoxEx(Collection<T> values) {
        return new ComboBoxExBuilder<>(values);
    }

    /**
     * Creates a new instance of {@link ComboBoxExBuilder} using the provided values.
     *
     * @param <T> the type of the items in the {@link ComboBoxEx}
     * @param values the values to be included in the combo box
     * @return a new instance of {@code ComboBoxExBuilder} initialized with the given values
     */
    @SafeVarargs
    public static <T> ComboBoxExBuilder<T> comboBoxEx(T... values) {
        return new ComboBoxExBuilder<>(List.of(values));
    }

    /**
     * Create {@link ButtonBuilder} instance for standard buttons.
     *
     * @return new {@code ButtonBuilder}
     */
    public static ButtonBuilder<Button> button() {
        return new ButtonBuilder<>(Button::new);
    }

    /**
     * Create and return a {@link ToggleButtonBuilder} to create instances of {@link ToggleButton}.
     *
     * @return new {@code ToggleButtonBuilder}
     */
    public static ToggleButtonBuilder toggleButton() {
        return new ToggleButtonBuilder(ToggleButton::new);
    }

    /**
     * Creates and return a {@link ToolBarBuilder} to create instances of {@link ToolBarEx}.
     *
     * @return a new instance of {@code ToolBarBuilder}
     */
    public static ToolBarBuilder toolBar() {
        return new ToolBarBuilder();
    }

    /**
     * Create {@link CheckBoxButtonBuilder} instance for checkboxes.
     *
     * @return new {@code CheckBoxButtonBuilder}
     */
    public static CheckBoxButtonBuilder checkbox() {
        return new CheckBoxButtonBuilder(CheckBox::new);
    }

    /**
     * Creates a new {@link FileInputBuilder} instance with the given mode.
     *
     * @param parentWinndw the parent window
     * @param mode the {@link FileDialogMode} of the file dialog
     * @return a new {@code FileInputBuilder} instance
     */
    public static FileInputBuilder fileInput(@Nullable Window parentWinndw, FileDialogMode mode) {
        return new FileInputBuilder(parentWinndw, mode);
    }

    /**
     * Create {@link SliderBuilder} instance.
     *
     * @return new {@code SliderBuilder}
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
     * Create a Node with a tooltip using default size and color.
     *
     * @param name  the icon name
     * @param tooltipText the text to display as tooltip
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see #graphic(String)
     */
    public static Node tooltipIcon(String name, String tooltipText) {
        Node node = graphic(name);
        Tooltip.install(node, new Tooltip(tooltipText));
        return node;
    }

    /**
     * Create a Node with a tooltip using default size and color.
     *
     * @param name  the icon name
     * @param size  the requested size
     * @param tooltipText the text to display as tooltip
     * @return a node for the graphic
     * @throws IllegalStateException if no icon with a matching name is found
     * @see #graphic(String)
     */
    public static Node tooltipIcon(String name, int size, String tooltipText) {
        Node node = graphic(name, size);
        Tooltip.install(node, new Tooltip(tooltipText));
        return node;
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
     * @see #graphic(String)
     */
    public static Node tooltipIcon(String name, int size, Paint paint, String tooltipText) {
        Node node = graphic(name, size, paint);
        Tooltip.install(node, new Tooltip(tooltipText));
        return node;
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
     * @see #graphic(String)
     */
    public static Node tooltipIcon(String name, int size, Color color, String tooltipText) {
        Node node = graphic(name, size, color);
        Tooltip.install(node, new Tooltip(tooltipText));
        return node;
    }

    /**
     * Get a {@link TextFieldBuilder} for creating a TextField.
     * @param locale the {@link Locale} to use
     * @return new {@code TextFieldBuilder} instance
     */
    public static TextFieldBuilder textField(Locale locale) {
        return new TextFieldBuilder(locale);
    }

    /**
     * Get {@link TextFieldBuilder} for creating a TextField.
     * @return new {@code TextFieldBuilder} instance
     */
    public static TextAreaBuilder textArea() {
        return new TextAreaBuilder();
    }

    /**
     * Make a {@link Region} resizable by dragging its edge.
     *
     * @param region  the {@code Region}
     * @param borders the borders to make draggable
     */
    public static void makeResizable(Region region, Position... borders) {
        DragResizer.makeResizable(region, 6, borders);
    }

    /**
     * Make a {@link Region} resizable by dragging its edge.
     *
     * @param region       the {@code Region}
     * @param resizeMargin size of the draggable margin
     * @param borders      the borders to make draggable
     */
    public static void makeResizable(Region region, int resizeMargin, Position... borders) {
        DragResizer.makeResizable(region, resizeMargin, borders);
    }

    /**
     * Create a new {@link MenuBuilder}.
     *
     * @return a new {@code MenuBuilder}
     */
    public static MenuBuilder menu() {
        return new MenuBuilder(Menu::new);
    }

    /**
     * Creates and returns a new instance of {@link ChoiceMenuBuilder} with the specified collection of values.
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
     * @return a new {@code MenuItemBuilder}
     */
    public static MenuItemBuilderImpl menuItem() {
        return new MenuItemBuilderImpl(MenuItem::new);
    }

    /**
     * Create a new {@link CheckMenuItemBuilder}.
     *
     * @return a new {@code CheckMenuItemBuilder}
     */
    public static CheckMenuItemBuilder checkMenuItem() {
        return new CheckMenuItemBuilder(CheckMenuItem::new);
    }

    /**
     * Creates and returns a new instance of {@link TextBuilder}.
     *
     * @param text the text
     * @return a new instance of {@code TextBuilder}
     */
    public static TextBuilder text(String text) {
        return new TextBuilder(text);
    }

    /**
     * Creates and returns a new instance of {@link TextBuilder}.
     *
     * @param text the text
     * @return a new instance of {@code TextBuilder}
     */
    public static TextBuilder text(ObservableValue<String> text) {
        return new TextBuilder(text);
    }

    /**
     * Creates and returns a new instance of {@link LabelBuilder}.
     *
     * @param text the text
     * @return a new instance of {@code LabelBuilder}
     */
    public static LabelBuilder label(String text) {
        return new LabelBuilder(text);
    }

    /**
     * Creates and returns a new instance of {@link LabelBuilder}.
     *
     * @param text the text
     * @return a new instance of {@code LabelBuilder}
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
     * Creates a {@link Background} object based on the given Color.
     *
     * @param color the {@link Color} to be used for the Background
     * @return a new {@code Background} object with the specified {@code Color}
     */
    public static Background background(Color color) {
        return new Background(new BackgroundFill(FxUtil.convert(color), null, null));
    }

    /**
     * Creates a spacer {@link Region} with a default growth priority.
     *
     * @return a {@code Region} object configured as a spacer with the default priority {@link Priority#ALWAYS}.
     */
    public static Region spacer() {
        return spacer(Priority.ALWAYS);
    }

    /**
     * Creates a spacer {@link Region} with the specified priority for horizontal and vertical growth.
     *
     * @param priority the growth {@link Priority} to set for the spacer in horizontal and vertical layouts
     * @return a spacer region configured with the specified growth priority
     */
    public static Region spacer(Priority priority) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, priority);
        VBox.setVgrow(spacer, priority);
        return spacer;
    }

    /**
     * Creates a validator {@link Function} that checks if the given value is non-null.
     * <p>
     * The returned function takes an input of type T and returns an {@code Optional<String>}
     * containing an error message if the input is {@code null}, or an empty {@link Optional} otherwise.
     *
     * @param <T> the type of the value to be validated
     * @return a {@code Function} that validates whether the input is non-null
     */
    public static <T> Function<@Nullable T, Optional<String>> validatorNonNull() {
        return v -> v == null ? Optional.of(I18NInstance.get().format("dua3.utlity.fx.controls.validator.error.null")) : Optional.empty();
    }

    /**
     * Creates a validator {@link Function} that checks if a given value lies within a specified range.
     * <p>
     * If the input value is null or outside the range, an error message is returned.
     *
     * @param <T> the type of the value to be validated; must be comparable
     * @param min the minimum allowable value in the range (inclusive)
     * @param max the maximum allowable value in the range (inclusive)
     * @return a {@code Function} that validates the value and returns either an empty {@link Optional}, if the value
     *         is valid, or an error message if validation fails
     */
    public static <T extends Comparable<T>> Function<@Nullable T, Optional<String>> validatorInRange(T min, T max) {
        return v -> {
            if (v == null) {
                return Optional.of(I18NInstance.get().format("dua3.utlity.fx.controls.validator.error.null"));
            }
            if (v.compareTo(min) < 0 || v.compareTo(max) > 0) {
                return Optional.of(I18NInstance.get().format(
                        "dua3.utlity.fx.controls.validator.out.of.range",
                        min, max, v
                ));
            }
            return Optional.empty();
        };
    }

    /**
     * Creates a validator {@link Function} that checks if the given value is non-null and not blank.
     * <p>
     * If the input value is {@code null} or blank, an error message is returned.
     *
     * @return a {@code Function} that validates whether the input is non-null and non-blank
     */
    public static Function<@Nullable String, Optional<String>> validatorNotBlank() {
        return v -> v == null || v.isBlank() ? Optional.of(I18NInstance.get().format("dua3.utlity.fx.controls.validator.error.blank")) : Optional.empty();
    }

}

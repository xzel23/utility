package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Controls utility class.
 */
class ControlsTest extends FxTestBase {

    /**
     * Test the button creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testButtonCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test standard button
            Button button = Controls.button().text("Test Button").build();
            assertNotNull(button);
            assertEquals("Test Button", button.getText());

            // Test toggle button
            ToggleButton toggleButton = Controls.toggleButton().text("Toggle").build();
            assertNotNull(toggleButton);
            assertEquals("Toggle", toggleButton.getText());
            assertFalse(toggleButton.isSelected());

            // Test toggle button with initial selection
            ToggleButton selectedToggleButton = Controls.toggleButton(true).text("Selected Toggle").build();
            assertNotNull(selectedToggleButton);
            assertEquals("Selected Toggle", selectedToggleButton.getText());
            assertTrue(selectedToggleButton.isSelected());

            // Test checkbox
            CheckBox checkbox = Controls.checkbox().text("Check Me").build();
            assertNotNull(checkbox);
            assertEquals("Check Me", checkbox.getText());
            assertFalse(checkbox.isSelected());

            // Test checkbox with initial selection
            CheckBox selectedCheckbox = Controls.checkbox(true).text("Selected Checkbox").build();
            assertNotNull(selectedCheckbox);
            assertEquals("Selected Checkbox", selectedCheckbox.getText());
            assertTrue(selectedCheckbox.isSelected());
        });
    }

    /**
     * Test the slider creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testSliderCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test default slider
            SliderWithButtons slider = Controls.slider().build();
            assertNotNull(slider);
            assertEquals(0.0, slider.getMin());
            assertEquals(100.0, slider.getMax());
            assertEquals(0.0, slider.get());

            // Test slider with custom min, max, value
            SliderWithButtons customSlider = Controls.slider().min(10.0).max(50.0).value(25.0).build();
            assertNotNull(customSlider);
            assertEquals(10.0, customSlider.getMin());
            assertEquals(50.0, customSlider.getMax());
            assertEquals(25.0, customSlider.get());

            // Test slider with formatter
            SliderWithButtons formattedSlider = Controls.slider()
                    .mode(SliderWithButtons.Mode.SLIDER_VALUE)
                    .formatter((min, max) -> String.format("%.1f - %.1f", min, max))
                    .build();
            assertNotNull(formattedSlider);
        });
    }

    /**
     * Test the separator creation method.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testSeparatorCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test horizontal separator
            Node horizontalSeparator = Controls.separator(Orientation.HORIZONTAL);
            assertNotNull(horizontalSeparator);
            assertInstanceOf(Separator.class, horizontalSeparator);
            assertEquals(Orientation.HORIZONTAL, ((Separator) horizontalSeparator).getOrientation());

            // Test vertical separator
            Node verticalSeparator = Controls.separator(Orientation.VERTICAL);
            assertNotNull(verticalSeparator);
            assertInstanceOf(Separator.class, verticalSeparator);
            assertEquals(Orientation.VERTICAL, ((Separator) verticalSeparator).getOrientation());
        });
    }

    /**
     * Test the graphic and icon creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testGraphicAndIconCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test graphic creation
            Node graphic = Controls.graphic("fas-check");
            assertNotNull(graphic);

            // Test icon creation
            com.dua3.utility.fx.icons.Icon icon = Controls.icon("fas-check");
            assertNotNull(icon);

            // Test graphic with size
            graphic = Controls.graphic("fas-check", 24);
            assertNotNull(graphic);

            // Test graphic with size and color
            graphic = Controls.graphic("fas-check", 24, Color.RED);
            assertNotNull(graphic);
        });
    }

    /**
     * Test the tooltip icon creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testTooltipIconCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test tooltip icon with Paint
            Node tooltipIcon = Controls.tooltipIcon("info", 24, javafx.scene.paint.Color.BLUE, "Information");
            assertNotNull(tooltipIcon);

            // Add to scene to ensure tooltip is created
            Scene scene = addToScene(tooltipIcon);
            assertNotNull(scene);

            // Test tooltip icon with Color
            tooltipIcon = Controls.tooltipIcon("info", 24, Color.BLUE, "Information");
            assertNotNull(tooltipIcon);

            // Add to scene to ensure tooltip is created
            scene = addToScene(tooltipIcon);
            assertNotNull(scene);
        });
    }

    /**
     * Test the label creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testLabelCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test rigid label with text
            Label rigidLabel = Controls.rigidLabel("Test Label");
            assertNotNull(rigidLabel);
            assertEquals("Test Label", rigidLabel.getText());

            // Test rigid label with text and graphic
            Node graphic = new Region();
            Label rigidLabelWithGraphic = Controls.rigidLabel("Test Label with Graphic", graphic);
            assertNotNull(rigidLabelWithGraphic);
            assertEquals("Test Label with Graphic", rigidLabelWithGraphic.getText());
            assertEquals(graphic, rigidLabelWithGraphic.getGraphic());

            // Test label builder with text
            Label label = Controls.label("Test Label Builder").build();
            assertNotNull(label);
            assertEquals("Test Label Builder", label.getText());

            // Test label builder with observable text
            SimpleStringProperty textProperty = new SimpleStringProperty("Observable Text");
            Label observableLabel = Controls.label(textProperty).build();
            assertNotNull(observableLabel);
            assertEquals("Observable Text", observableLabel.getText());

            // Test changing observable text
            textProperty.set("Changed Text");
            assertEquals("Changed Text", observableLabel.getText());
        });
    }

    /**
     * Test the text field creation method.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testTextFieldCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test text field with default locale
            TextField textField = Controls.textField(Locale.getDefault()).build();
            assertNotNull(textField);

            // Test text field with specific locale
            TextField frenchTextField = Controls.textField(Locale.FRENCH).build();
            assertNotNull(frenchTextField);

            // Test text field with initial text
            TextField textFieldWithText = Controls.textField(Locale.getDefault()).text("Initial Text").build();
            assertNotNull(textFieldWithText);
            assertEquals("Initial Text", textFieldWithText.getText());
        });
    }

    /**
     * Test the makeResizable method.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testMakeResizable() throws Exception {
        runOnFxThreadAndWait(() -> assertDoesNotThrow(() -> {
            // Create a region to make resizable
            Region region = new Region();
            region.setPrefSize(100, 100);

            // Make it resizable
            Controls.makeResizable(region, Position.BOTTOM, Position.RIGHT);

            // Test with custom resize margin
            Region region2 = new Region();
            region2.setPrefSize(100, 100);
            Controls.makeResizable(region2, 10, Position.BOTTOM, Position.RIGHT);
        }));
    }

    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testMenuBuilders() throws Exception {
        runOnFxThreadAndWait(() -> {
            // MenuItemBuilder
            AtomicBoolean miActionCalled = new AtomicBoolean(false);
            SimpleBooleanProperty miEnabled = new SimpleBooleanProperty(true);
            MenuItem mi = Controls.menuItem()
                    .text("MI")
                    .action(() -> miActionCalled.set(true))
                    .enabled(miEnabled)
                    .build();
            assertEquals("MI", mi.getText());
            assertFalse(mi.isDisable());
            mi.fire();
            assertTrue(miActionCalled.get());
            miEnabled.set(false);
            assertTrue(mi.isDisable());

            // MenuBuilder
            MenuItem item1 = new MenuItem("I1");
            MenuItem item2 = new MenuItem("I2");
            SimpleBooleanProperty menuDisabled = new SimpleBooleanProperty(false);
            Menu menu = Controls.menu()
                    .text("Menu")
                    .items(item1, item2)
                    .disabled(menuDisabled)
                    .build();
            assertEquals("Menu", menu.getText());
            assertEquals(2, menu.getItems().size());
            assertEquals(item1, menu.getItems().get(0));
            assertFalse(menu.isDisable());
            menuDisabled.set(true);
            assertTrue(menu.isDisable());

            // ChoiceMenuBuilder
            SimpleStringProperty property = new SimpleStringProperty("A");
            Menu choiceMenu = Controls.<String>choiceMenu(List.of("A", "B", "C"))
                    .text("Choices")
                    .bind(property)
                    .build();
            assertEquals("Choices", choiceMenu.getText());
            assertEquals(3, choiceMenu.getItems().size());

            // CheckMenuItemBuilder
            SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
            CheckMenuItem cmi = Controls.checkMenuItem()
                    .text("CMI")
                    .selected(selected)
                    .build();
            assertEquals("CMI", cmi.getText());
            assertFalse(cmi.isSelected());
            selected.set(true);
            assertTrue(cmi.isSelected());
            cmi.setSelected(false);
            assertFalse(selected.get());
        });
    }

    /**
     * Test the menu creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testMenuCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test menu creation with text only
            MenuItem item1 = new MenuItem("Item 1");
            MenuItem item2 = new MenuItem("Item 2");
            Menu menu = Controls.menu("Test Menu", item1, item2);
            assertNotNull(menu);
            assertEquals("Test Menu", menu.getText());
            assertEquals(2, menu.getItems().size());
            assertEquals(item1, menu.getItems().get(0));
            assertEquals(item2, menu.getItems().get(1));

            // Test menu creation with text and graphic
            Node graphic = new Region();
            Menu menuWithGraphic = Controls.menu("Test Menu with Graphic", graphic, item1, item2);
            assertNotNull(menuWithGraphic);
            assertEquals("Test Menu with Graphic", menuWithGraphic.getText());
            assertEquals(graphic, menuWithGraphic.getGraphic());
            assertEquals(2, menuWithGraphic.getItems().size());
        });
    }

    /**
     * Test the menu item creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testMenuItemCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test menu item with text and action
            AtomicBoolean actionCalled = new AtomicBoolean(false);
            MenuItem menuItem = Controls.menuItem("Test MenuItem", () -> actionCalled.set(true));
            assertNotNull(menuItem);
            assertEquals("Test MenuItem", menuItem.getText());
            assertTrue(menuItem.isVisible());
            assertFalse(menuItem.isDisable());

            // Simulate action
            menuItem.fire();
            assertTrue(actionCalled.get());

            // Test menu item with text, graphic, and action
            AtomicBoolean action2Called = new AtomicBoolean(false);
            Node graphic = new Region();
            MenuItem menuItemWithGraphic = Controls.menuItem("Test MenuItem with Graphic", graphic, () -> action2Called.set(true));
            assertNotNull(menuItemWithGraphic);
            assertEquals("Test MenuItem with Graphic", menuItemWithGraphic.getText());
            assertEquals(graphic, menuItemWithGraphic.getGraphic());

            // Simulate action
            menuItemWithGraphic.fire();
            assertTrue(action2Called.get());

            // Test menu item with enabled state
            MenuItem disabledMenuItem = Controls.menuItem("Disabled MenuItem", () -> {}, false);
            assertNotNull(disabledMenuItem);
            assertEquals("Disabled MenuItem", disabledMenuItem.getText());
            assertTrue(disabledMenuItem.isDisable());

            // Test menu item with observable enabled state
            SimpleBooleanProperty enabledProperty = new SimpleBooleanProperty(true);
            MenuItem observableMenuItem = Controls.menuItem("Observable MenuItem", () -> {}, enabledProperty);
            assertNotNull(observableMenuItem);
            assertEquals("Observable MenuItem", observableMenuItem.getText());
            assertFalse(observableMenuItem.isDisable());

            // Change enabled state
            enabledProperty.set(false);
            assertTrue(observableMenuItem.isDisable());
        });
    }

    /**
     * Test the check menu item creation methods.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testCheckMenuItemCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test check menu item with text, action, and selection
            AtomicReference<Boolean> selectionState = new AtomicReference<>();
            CheckMenuItem checkMenuItem = Controls.checkMenuItem("Test CheckMenuItem", selectionState::set, true);
            assertNotNull(checkMenuItem);
            assertEquals("Test CheckMenuItem", checkMenuItem.getText());
            assertTrue(checkMenuItem.isSelected());

            // Note: The action is only triggered when the menu item is clicked, not when setSelected() is called programmatically
            // The Controls.checkMenuItem method sets the action to be triggered on the onAction event, not when the selection state changes
            // To properly test this, we would need to simulate a click on the menu item
            checkMenuItem.setSelected(false);
            checkMenuItem.fire(); // Fire the action to trigger the consumer
            assertEquals(Boolean.FALSE, selectionState.get());

            // Test check menu item with property binding
            SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(true);
            CheckMenuItem boundCheckMenuItem = Controls.checkMenuItem("Bound CheckMenuItem", selectedProperty);
            assertNotNull(boundCheckMenuItem);
            assertEquals("Bound CheckMenuItem", boundCheckMenuItem.getText());
            assertTrue(boundCheckMenuItem.isSelected());

            // Change property
            selectedProperty.set(false);
            assertFalse(boundCheckMenuItem.isSelected());

            // Change selection
            boundCheckMenuItem.setSelected(true);
            assertTrue(selectedProperty.get());

            // Test check menu item with enabled state
            CheckMenuItem disabledCheckMenuItem = Controls.checkMenuItem("Disabled CheckMenuItem", selectedProperty, FxUtil.FALSE);
            assertNotNull(disabledCheckMenuItem);
            assertEquals("Disabled CheckMenuItem", disabledCheckMenuItem.getText());
            assertTrue(disabledCheckMenuItem.isDisable());
        });
    }

    /**
     * Test the background creation method.
     */
    @Test
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testBackgroundCreation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Test background with color
            Background background = Controls.background(Color.RED);
            assertNotNull(background);
            assertFalse(background.getFills().isEmpty());

            // Apply to a region
            Region region = new Region();
            region.setBackground(background);
            assertEquals(background, region.getBackground());
        });
    }
}
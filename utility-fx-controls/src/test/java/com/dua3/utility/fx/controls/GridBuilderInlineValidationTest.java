package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GridBuilderInlineValidationTest extends FxTestBase {

    @SuppressWarnings("unchecked")
    @Test
    void testInlineValidation() throws Exception {
        runOnFxThreadAndWait(() -> {
            MessageFormatter formatter = MessageFormatter.standard();
            GridBuilder builder = new GridBuilder(null, formatter);
            
            builder.startRow(MessageFormatter.literal("Address"));
            builder.inputText("street", MessageFormatter.literal("Street"), () -> "", s -> s != null && s.isEmpty() ? Optional.of("Enter a valid street.") : Optional.empty());
            builder.inputText("city", MessageFormatter.literal("City"), () -> "", s -> s != null && s.isEmpty() ? Optional.of("Enter a valid city.") : Optional.empty());
            builder.endRow();

            Grid grid = builder.build();
            // grid.init() is called by builder.build()

            // Initially, both street and city are empty, so validation should fail if errors are shown.
            // But Grid.init calls updateMarker(entry, false), so error symbols are not shown yet.

            // Get the Meta object for the row
            Grid.Meta<?> rowMeta = null;
            for (Grid.Meta<?> meta : grid.data()) {
                if (meta.label != null && "Address".equals(meta.label.getText())) {
                    rowMeta = meta;
                    break;
                }
            }

            assertNotNull(rowMeta, "Row Meta not found");

            // Manually trigger error display or simulate focus loss
            // Actually, if we change the value, the validation listener should trigger updateMarker(entry, true)
            
            InputControl<String> streetControl = (InputControl<String>) grid.data().stream().filter(m -> "street".equals(m.id)).findFirst().get().control;
            InputControl<String> cityControl = (InputControl<String>) grid.data().stream().filter(m -> "city".equals(m.id)).findFirst().get().control;

            // Initially they are empty, so they are invalid.
            assertFalse(streetControl.isValid(), "Street should be invalid");
            assertFalse(cityControl.isValid(), "City should be invalid");

            // The row control (ControlWrapper) is currently always valid
            assertNotNull(rowMeta.control, "Row control should not be null");

            // Expected behavior after fix:
            assertFalse(rowMeta.control.isValid(), "Row should be invalid");

            // Simulate setting values to trigger validation listeners
            streetControl.set("Some Street");
            cityControl.set("Some City");
            assertTrue(streetControl.isValid(), "Street should be valid now");
            assertTrue(cityControl.isValid(), "City should be valid now");
            assertTrue(rowMeta.control.isValid(), "Row should be valid now");
            assertNull(rowMeta.errorMarker.getTooltip(), "No tooltip if valid");

            // Test validation trigger without value change
            streetControl.set(""); // Invalid
            assertEquals("⚠", rowMeta.errorMarker.getText(), "Marker should show after becoming invalid");
            streetControl.state().validate();
            assertEquals("⚠", rowMeta.errorMarker.getText(), "Marker should show after manual validation trigger");

            cityControl.set(""); // Both invalid
            assertFalse(cityControl.isValid(), "City should be invalid again");
            assertFalse(rowMeta.control.isValid(), "Row should be invalid again");
            assertEquals("Enter a valid street.\n\nEnter a valid city.", rowMeta.errorMarker.getTooltip().getText());
            assertEquals("⚠", rowMeta.errorMarker.getText(), "Error symbol should be shown");
        });
    }
}

package com.dua3.utility.fx.controls;

import javafx.scene.control.Label;
import javafx.util.converter.DefaultStringConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

class ColumnDefBuilderTest {

    @Test
    void testTextBuilder() {
        String title = "Test Title";
        Function<String, String> getter = s -> s;
        BiConsumer<String, String> setter = (s, v) -> {};

        ColumnDefText<String, String> columnDef = ColumnDef.<String, String>builder(title, new DefaultStringConverter())
                .valueGetter(getter)
                .valueSetter(setter)
                .editable(true)
                .build();

        Assertions.assertEquals(title, columnDef.text());
        Assertions.assertEquals(getter, columnDef.valueGetter());
        Assertions.assertEquals(setter, columnDef.valueSetter());
        Assertions.assertTrue(columnDef.editable());
        // Default values
        Assertions.assertEquals(0.0, columnDef.minWidth());
        Assertions.assertEquals(Double.MAX_VALUE, columnDef.maxWidth());
        Assertions.assertEquals(ColumnDef.DEFAULT_WEIGHT, columnDef.weight());
        Assertions.assertTrue(columnDef.resizable());
        Assertions.assertTrue(columnDef.sortable());
        Assertions.assertNotNull(columnDef.converter());

        columnDef = ColumnDef.<String, String>builder(title, new DefaultStringConverter())
                .sortable(false)
                .build();
        Assertions.assertFalse(columnDef.sortable());
    }

    @Test
    void testGenericBuilder() {
        String title = "Test Title";
        Function<String, String> getter = s -> s;

        ColumnDefGeneric<String, String> columnDef = ColumnDef.<String, String>builder(title, (node, text) -> new Label(text))
                .valueGetter(getter)
                .build();

        Assertions.assertEquals(title, columnDef.text());
        Assertions.assertEquals(getter, columnDef.valueGetter());
        Assertions.assertNotNull(columnDef.valueSetter());
        Assertions.assertFalse(columnDef.editable());
        Assertions.assertNotNull(columnDef.nodeFactory());
        Assertions.assertNotNull(columnDef.startEdit());
        Assertions.assertNotNull(columnDef.cancelEdit());
        Assertions.assertTrue(columnDef.sortable());

        columnDef = ColumnDef.<String, String>builder(title, (node, text) -> new Label(text))
                .sortable(false)
                .build();
        Assertions.assertFalse(columnDef.sortable());
    }
}

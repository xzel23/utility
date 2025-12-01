package com.dua3.utility.fx;

import com.dua3.utility.data.ConversionException;
import com.dua3.utility.data.Converter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link PropertyConverter}.
 */
class PropertyConverterTest extends FxTestBase {

    /**
     * Test for {@link PropertyConverter#convert(Property, Converter)}.
     * Tests bidirectional synchronization between properties.
     */
    @Test
    void testConvertWithConverter() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a converter from String to Integer
            Converter<String, Integer> converter = Converter.create(Integer::valueOf, String::valueOf);

            // Create a property to convert
            Property<String> stringProperty = new SimpleObjectProperty<>("123");

            // Convert the property
            Property<Integer> intProperty = PropertyConverter.convert(stringProperty, converter);

            // Test initial conversion
            assertEquals(123, intProperty.getValue(), "Initial conversion should work");

            // Test updating the original property
            stringProperty.setValue("456");
            assertEquals(456, intProperty.getValue(), "Changes to original property should be reflected");

            // Test updating the converted property
            intProperty.setValue(789);
            assertEquals("789", stringProperty.getValue(), "Changes to converted property should be reflected");

            stringProperty.setValue("not a number");
            assertNull(intProperty.getValue(), "Invalid input should be mapped to null");
        });
    }

    /**
     * Test for handling null values with {@link PropertyConverter#convert(Property, Converter)}.
     */
    @Test
    void testConvertWithNullValues() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a converter from String to Integer
            Converter<String, Integer> converter = Converter.createNullAware(Integer::valueOf, String::valueOf);

            // Create a property to convert with null value
            Property<String> stringProperty = new SimpleObjectProperty<>(null);

            // Convert the property
            Property<Integer> intProperty = PropertyConverter.convert(stringProperty, converter);

            // Test that null conversion works as expected
            assertNull(intProperty.getValue(), "Conversion of null should yield null");

            // Test updating the original property to null
            stringProperty.setValue(null);
            assertNull(intProperty.getValue(), "Changes to null in the original property should be reflected as null");

            // Test updating the converted property to null
            intProperty.setValue(null);
            assertNull(stringProperty.getValue(), "Changes to null in the converted property should be reflected as null");
        });
    }

    /**
     * Test for handling conversion exceptions with {@link PropertyConverter#convert(Property, Converter)}.
     */
    @Test
    void testConvertWithConversionException() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a converter from String to Integer that throws a ConversionException
            Converter<String, Integer> converter = Converter.create(
                s -> {
                    if ("exception".equals(s)) {
                        throw new ConversionException("Forced exception");
                    }
                    return Integer.valueOf(s);
                },
                integer -> Objects.toString(integer, null)
            );

            // Create a property to convert
            Property<String> stringProperty = new SimpleObjectProperty<>("123");

            // Convert the property
            Property<Integer> intProperty = PropertyConverter.convert(stringProperty, converter);

            // Test successful conversion
            assertEquals(123, intProperty.getValue(), "Successful conversion should work");

            // Test forward conversion exception
            stringProperty.setValue("exception");
            assertNull(intProperty.getValue(), "ConversionException should result in null in the converted property");

            // Test backward conversion exception
            intProperty.setValue(null);
            assertNull(stringProperty.getValue(), "Backward ConversionException should result in null in the original property");
        });
    }

    /**
     * Test for {@link PropertyConverter#convertReadOnly(ObservableValue, Converter)}.
     * Tests one-way conversion with a Converter.
     */
    @Test
    void testConvertReadOnlyWithConverter() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a converter from String to Integer
            Converter<String, Integer> converter = Converter.create(Integer::valueOf, String::valueOf);

            // Create a property to convert
            Property<String> stringProperty = new SimpleObjectProperty<>("123");

            // Convert the property to a read-only property
            ReadOnlyProperty<Integer> intProperty = PropertyConverter.convertReadOnly(stringProperty, converter);

            // Test initial conversion
            assertEquals(123, intProperty.getValue(), "Initial conversion should work");

            // Test updating the original property
            stringProperty.setValue("456");
            assertEquals(456, intProperty.getValue(), "Changes to original property should be reflected");
        });
    }

    /**
     * Test for {@link PropertyConverter#convertReadOnly(ObservableValue, Function)}.
     * Tests one-way conversion with a Function.
     */
    @Test
    void testConvertReadOnlyWithFunction() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a function from String to Integer
            Function<String, Integer> converter = Integer::valueOf;

            // Create a property to convert
            Property<String> stringProperty = new SimpleObjectProperty<>("123");

            // Convert the property to a read-only property
            ReadOnlyProperty<Integer> intProperty = PropertyConverter.convertReadOnly(stringProperty, converter);

            // Test initial conversion
            assertEquals(123, intProperty.getValue(), "Initial conversion should work");

            // Test updating the original property
            stringProperty.setValue("456");
            assertEquals(456, intProperty.getValue(), "Changes to original property should be reflected");
        });
    }

    /**
     * Test for {@link PropertyConverter#convertToStringReadOnly(ObservableValue, Function)}.
     * Tests conversion to a read-only string property.
     */
    @Test
    void testConvertToStringReadOnly() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a function from Integer to String
            Function<Integer, String> converter = n -> "Number: " + n;

            // Create a property to convert
            Property<Integer> intProperty = new SimpleObjectProperty<>(123);

            // Convert the property to a read-only string property
            ReadOnlyStringProperty stringProperty = PropertyConverter.convertToStringReadOnly(intProperty, converter);

            // Test initial conversion
            assertEquals("Number: 123", stringProperty.getValue(), "Initial conversion should work");

            // Test updating the original property
            intProperty.setValue(456);
            assertEquals("Number: 456", stringProperty.getValue(), "Changes to original property should be reflected");
        });
    }

    /**
     * Test for {@link PropertyConverter#convertToDoubleReadOnly(ObservableValue, Function)}.
     * Tests conversion to a read-only double property.
     */
    @Test
    void testConvertToDoubleReadOnly() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a function from String to Double
            Function<String, Double> converter = s -> Double.parseDouble(s) * 2;

            // Create a property to convert
            Property<String> stringProperty = new SimpleObjectProperty<>("10.5");

            // Convert the property to a read-only double property
            ReadOnlyDoubleProperty doubleProperty = PropertyConverter.convertToDoubleReadOnly(stringProperty, converter);

            // Test initial conversion
            assertEquals(21.0, doubleProperty.getValue(), 0.001, "Initial conversion should work");

            // Test updating the original property
            stringProperty.setValue("20.25");
            assertEquals(40.5, doubleProperty.getValue(), 0.001, "Changes to original property should be reflected");
        });
    }

    /**
     * Test for {@link PropertyConverter#convert(DoubleProperty)}.
     * Tests type-safe cast of DoubleProperty to Property&lt;Double&gt;.
     */
    @Test
    void testConvertDoubleProperty() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a DoubleProperty
            DoubleProperty doubleProperty = new SimpleDoubleProperty(123.45);

            // Convert to Property<Double>
            Property<Double> convertedProperty = PropertyConverter.convert(doubleProperty);

            // Test initial conversion
            assertEquals(123.45, convertedProperty.getValue(), 0.001, "Initial conversion should work");

            // Test updating the original property
            doubleProperty.setValue(456.78);
            assertEquals(456.78, convertedProperty.getValue(), 0.001, "Changes to original property should be reflected");

            // Test updating the converted property
            convertedProperty.setValue(789.01);
            assertEquals(789.01, doubleProperty.getValue(), 0.001, "Changes to converted property should be reflected");
        });
    }

    /**
     * Test that a property must not be bound when using convert.
     */
    @Test
    void testConvertWithBoundProperty() throws Throwable {
        runOnFxThreadAndWait(() -> {
            // Create a converter from String to Integer
            Converter<String, Integer> converter = Converter.create(Integer::valueOf, String::valueOf);

            // Create two properties
            StringProperty stringProperty1 = new SimpleStringProperty("123");
            StringProperty stringProperty2 = new SimpleStringProperty("456");

            // Bind the first property to the second
            stringProperty1.bind(stringProperty2);

            // Try to convert the bound property
            Exception exception = assertThrows(IllegalArgumentException.class, () -> PropertyConverter.convert(stringProperty1, converter), "Should throw an exception for bound property");

            // Verify the exception message
            assertTrue(exception.getMessage().contains("property must not be bound"), "Exception message should mention that property must not be bound");
        });
    }
}
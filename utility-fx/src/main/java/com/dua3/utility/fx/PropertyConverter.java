package com.dua3.utility.fx;

import com.dua3.utility.data.ConversionException;
import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
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

import java.util.function.Function;

/**
 * A utility class for converting properties between different types while maintaining
 * synchronization between the original and converted properties. It provides methods
 * to convert a property to a different type and to create read-only properties with
 * conversions applied.
 */
public final class PropertyConverter {

    /**
     * Private constructor for utility class.
     */
    private PropertyConverter() { /* utility class */ }

    /**
     * Converts a property of one type to another type using the provided converter,
     * maintaining bidirectional synchronization between the original and converted properties.
     *
     * <p><strong>Note:</strong> both properties must not be bound!
     *
     * @param <A> the type of the original property's value
     * @param <B> the type of the converted property's value
     * @param <P> the type of Property containing the original value
     * @param property the original property whose value is to be converted
     * @param converter a converter that defines how to convert the property's value
     *                  from type A to type B and vice versa
     * @return a new Property with a value of type B, synchronized with the original property
     */
    public static <A, B, P extends Property<A>> Property<B> convert(P property, Converter<A, B> converter) {
        LangUtil.check(!property.isBound(), () -> new IllegalArgumentException("property must not be bound!"));

        Property<B> convertedProperty = new SimpleObjectProperty<>(converter.convert(property.getValue()));

        property.addListener((obs, oldValue, newValue) -> {
            try {
                convertedProperty.setValue(converter.convert(newValue));
            } catch (ConversionException e) {
                convertedProperty.setValue(null);
            }
        });
        convertedProperty.addListener((obs, oldValue, newValue) -> {
            try {
                property.setValue(converter.convertBack(newValue));
            } catch (ConversionException e) {
                property.setValue(null);
            }
        });

        return convertedProperty;
    }

    /**
     * Converts a given read/write property of type A to a read-only property of type B using the specified converter.
     * The conversion is automatically applied whenever the original property's value changes.
     *
     * @param <A> the type of the value held by the input property
     * @param <B> the type of the value to be held by the output read-only property
     * @param <V> the type of ObservableValue containing the original value
     * @param value the property whose value is to be converted
     * @param converter the converter used to convert the property's value from type A to type B
     * @return a read-only property containing the converted value of type B
     */
    public static <A, B, V extends ObservableValue<A>> ReadOnlyProperty<B> convertReadOnly(V value, Converter<A, B> converter) {
        return convertReadOnly(value, converter::convert);
    }

    /**
     * Converts a given read/write property of type A to a read-only property of type B using the specified converter.
     * The conversion is automatically applied whenever the original property's value changes.
     *
     * @param <A> the type of the value held by the input property
     * @param <B> the type of the value to be held by the output read-only property
     * @param <V> the type of ObservableValue containing the original value
     * @param value the property whose value is to be converted
     * @param converter the converter used to convert the property's value from type A to type B
     * @return a read-only property containing the converted value of type B
     */
    public static <A, B, V extends ObservableValue<A>> ReadOnlyProperty<B> convertReadOnly(V value, Function<A, B> converter) {
        Property<B> convertedProperty = new SimpleObjectProperty<>(converter.apply(value.getValue()));

        value.addListener((obs, oldValue, newValue) -> convertedProperty.setValue(converter.apply(newValue)));

        return convertedProperty;
    }

    /**
     * Converts a given property of type A to a read-only property of type String using the specified converter.
     * The conversion is automatically applied whenever the original property's value changes.
     *
     * @param <A> the type of the value held by the input property
     * @param <V> the type of ObservableValue containing the original value
     * @param value the property whose value is to be converted
     * @param converter the converter used to convert the property's value from type A to type String
     * @return a {@link ReadOnlyStringProperty} bound to the argument {@code value}
     */
    public static <A, V extends ObservableValue<A>> ReadOnlyStringProperty convertToStringReadOnly(V value, Function<A, String> converter) {
        StringProperty convertedProperty = new SimpleStringProperty(converter.apply(value.getValue()));
        value.addListener((obs, oldValue, newValue) -> convertedProperty.setValue(converter.apply(newValue)));
        return convertedProperty;
    }

    /**
     * Converts a given property of type A to a read-only property of type Double using the specified converter.
     * The conversion is automatically applied whenever the original property's value changes.
     *
     * @param <A> the type of the value held by the input property
     * @param <V> the type of ObservableValue containing the original value
     * @param value the property whose value is to be converted
     * @param converter the converter used to convert the property's value from type A to type String
     * @return a {@link ReadOnlyDoubleProperty} bound to the argument {@code value}
     */
    public static <A, V extends ObservableValue<A>> ReadOnlyDoubleProperty convertToDoubleReadOnly(V value, Function<A, Double> converter) {
        DoubleProperty convertedProperty = new SimpleDoubleProperty(converter.apply(value.getValue()));
        value.addListener((obs, oldValue, newValue) -> convertedProperty.setValue(converter.apply(newValue)));
        return convertedProperty;
    }

    /**
     * Converts a given {@code DoubleProperty} to a {@code Property<Double>} by performing a type-safe cast.
     * This method addresses the type mismatch caused by the Java type system where
     * {@code DoubleProperty} extends {@code Property<Number>} rather than {@code Property<Double>}.
     *
     * @param property the {@code DoubleProperty} to be converted
     * @return the {@code Property<Double>} representation of the given {@code DoubleProperty}
     */
    @SuppressWarnings("unchecked")
    public static Property<Double> convert(DoubleProperty property) {
        // Explanation
        //	- DoubleProperty extends Property<Number>, not Property<Double>, due to Java’s type system and generics erasure.
        //	- So a direct cast to Property<Double> needs a bridge cast (via Property<?>) to avoid compiler errors.
        //	- This is safe in practice because a DoubleProperty always holds a Double.
        //noinspection unchecked
        return (Property<Double>) (Property<?>) property;
    }
}

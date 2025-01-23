package com.dua3.utility.fx;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

/**
 * A utility class for converting properties between different types while maintaining
 * synchronization between the original and converted properties. It provides methods
 * to convert a property to a different type and to create read-only properties with
 * conversions applied.
 */
public class PropertyConverter {
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
        LangUtil.check(!property.isBound(), "property must not be bound!");

        Property<B> convertedProperty = new SimpleObjectProperty<>(converter.convert(property.getValue()));

        property.addListener((obs, oldValue, newValue) -> convertedProperty.setValue(converter.convert(newValue)));
        convertedProperty.addListener((obs, oldValue, newValue) -> property.setValue(converter.convertBack(newValue)));

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
}

package com.dua3.utility.fx;

import com.dua3.utility.data.Converter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class PropertyConverter {
    public static <A, B, P extends Property<A>> Property<B> convert(P property, Converter<A, B> converter) {
        Property<B> convertedProperty = new SimpleObjectProperty<>(converter.convert(property.getValue()));

        property.addListener((obs, oldValue, newValue) -> convertedProperty.setValue(converter.convert(newValue)));
        convertedProperty.addListener((obs, oldValue, newValue) -> property.setValue(converter.convertBack(newValue)));

        return convertedProperty;
    }
}

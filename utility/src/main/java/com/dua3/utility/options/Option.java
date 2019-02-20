package com.dua3.utility.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * An Option that changes the behavior of other classes. This class is intended
 * to be used to create settings dialogs at runtime.
 *
 * @param <T> the type of this option's values
 */
public class Option<T> {

    private final String name;
    private final Class<T> klass;
    private final List<Supplier<T>> choices;
    private final Supplier<T> defaultChoice;

    @SafeVarargs
    public Option(String name, Class<T> klass, Supplier<T> defaultChoice, Supplier<T>... choices) {
        this.name = Objects.requireNonNull(name);
        this.klass = Objects.requireNonNull(klass);
        this.defaultChoice = defaultChoice;

        // make sure this.choices does not contain a duplicate for defaultChoice
        List<Supplier<T>> choices_ = Arrays.asList(choices);
        if (choices_.contains(defaultChoice)) {
            this.choices = Arrays.asList(choices);
        } else {
            List<Supplier<T>> allChoices = new ArrayList<>(choices.length + 1);
            allChoices.add(defaultChoice);
            allChoices.addAll(choices_);
            this.choices = allChoices;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return name.equals(other.name) && klass.equals(other.klass);
    }

    public List<Supplier<T>> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public Supplier<T> getDefault() {
        return defaultChoice;
    }

    public String getName() {
        return name;
    }

    public Class<T> getOptionClass() {
        return klass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, klass);
    }

    @Override
    public String toString() {
        return name + "[" + klass + ",default=" + getDefault() + "]";
    }

}

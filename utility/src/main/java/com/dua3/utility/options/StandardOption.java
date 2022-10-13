package com.dua3.utility.options;

import com.dua3.utility.data.DataUtil;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Standard option class.
 * @param <T> the option's argument type.
 */
public final class StandardOption<T> extends Option<T> {

    public static <T> StandardOption<T> create(Class<? extends T> type,
                                               String... names) {
        return create(s -> DataUtil.convert(s, type), v -> DataUtil.convert(v, String.class), names);
    }

    public static <T> StandardOption<T> create(Function<String, ? extends T> mapper,
                                               String[] names) {
        return new StandardOption<>(mapper, Object::toString, names);
    }

    public static <T> StandardOption<T> create(Function<String, T> mapper,
                                               Function<? super T, String> formatter,
                                               String[] names) {
        return new StandardOption<>(mapper, formatter, names);
    }

    /**
     * Construct new StandardOption instance.
     * @param mapper the mapper used to convert the string values of arguments to the target type
     * @param names the names to be used on the command line for this option
     */
    private StandardOption(Function<String, ? extends T> mapper,
                           Function<? super T, String> formatter,
                           String... names) {
        super(mapper, formatter, names);
    }

    @Override
    public StandardOption<T> description(String description) {
        super.description(description);
        return this;
    }

    @Override
    public StandardOption<T> handler(Consumer<Collection<T>> handler) {
        super.handler(handler);
        return this;
    }

    /**
     * Set the number of occurrences for this option. This method sets the minimum and maximum number of occurrences to
     * the same value, i.e. calling {@code occurrence(n)} means the option has to be passed exactly {@code n} times.
     * @param n the number of occurrences to set
     * @return the option
     */
    public StandardOption<T> occurrence(int n) {
        return occurrence(n, n);
    }

    /**
     * Set the number of occurrences for this option.
     * @param min the minimum number of occurrences
     * @param max the maximum number of occurrences
     * @return the option
     */
    @Override
    public StandardOption<T> occurrence(int min, int max) {
        super.occurrence(min, max);
        return this;
    }

    /**
     * Set the arity for this option. This method sets the minimum and maximum arity to the same
     * value, i.e. calling {@code arity(n)} means the option takes exactly {@code n} arguments for each invocation.
     * @param n the arity to set
     * @return the option
     */
    public StandardOption<T> arity(int n) {
        return arity(n, n);
    }

    /**
     * Set the arity for this option.
     * @param min the minimum arity
     * @param max the maximum arity
     * @return the option
     */
    @Override
    public StandardOption<T> arity(int min, int max) {
        super.arity(min, max);
        return this;
    }
}

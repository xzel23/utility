package com.dua3.utility.options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A Mapping of options to values. 
 * The only difference to a normal {@code Map<Option<?>,Supplier<?>>} is that
 * {@code Options.get(option)} will return the option's default value (if set) instead
 * of {@code null}.
 */
public class Options extends HashMap<Option<?>,Supplier<?>> {
	private static final long serialVersionUID = 1L;

	private static final Options EMPTY_OPTIONS = new Options(Collections.emptyMap());

	/**
	 * An empty set of option values.
	 * @return
	 *  empty options
	 */
    public static Options empty() {
        return EMPTY_OPTIONS;
    }

    public Options() {
    }

    public Options(Map<Option<?>, Supplier<?>> options) {
        super(options);
    }

    public Supplier<?> get(Option<?> op) {
        return getOrDefault(op, op.getDefault());
    }

    public Supplier<?> put(Option<?> option, Supplier<?> value) {
        Class<?> klassO = option.getOptionClass();
        Class<?> klassV = value.get().getClass();
        if (!(klassO.isAssignableFrom(klassV))) {
            throw new IllegalArgumentException("Incompatible value for option '" + option.getName() + "' - expected: "
                    + klassO + ", is: " + klassV);
        }
        return super.put(option, value);
    }
}

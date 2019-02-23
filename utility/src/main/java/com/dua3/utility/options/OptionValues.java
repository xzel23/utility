package com.dua3.utility.options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.utility.options.Option.Value;

/**
 * A Mapping of options to values. 
 * The only difference to a normal {@code Map<Option<?>,Value<?>>} is that
 * {@code Options.get(option)} will return the option's default value (if set) instead
 * of {@code null}.
 */
public class OptionValues extends HashMap<Option<?>,Value<?>> {
	private static final long serialVersionUID = 1L;

	private static final OptionValues EMPTY_OPTIONS = new OptionValues(Collections.emptyMap());

	/**
	 * An empty set of option values.
	 * @return
	 *  empty options
	 */
    public static OptionValues empty() {
        return EMPTY_OPTIONS;
    }

    public OptionValues() {
    }

    public OptionValues(Map<Option<?>, Value<?>> options) {
        super(options);
    }

    public Value<?> get(Option<?> op) {
        return getOrDefault(op, op.getDefault());
    }

    @Override
    public Value<?> put(Option<?> option, Value<?> value) {
        Class<?> klassO = option.getOptionClass();
        Class<?> klassV = getClass(value.get());
        if (klassV != null && !(klassO.isAssignableFrom(klassV))) {
            throw new IllegalArgumentException("Incompatible value for option '" + option.getName() + "' - expected: "
                    + klassO + ", is: " + klassV);
        }
        return super.put(option, value);
    }

    private Class<?> getClass(Object o) {
        return o != null ? o.getClass() : null;
    }
    
    @Override
    public String toString() {
        var i = entrySet().iterator();
        if (! i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<Option<?>, Value<?>> e = i.next();
            String name = e.getKey().getName();
            String value = e.getValue().name();
            sb.append(name);
            sb.append('=');
            sb.append(value);
            if (! i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}

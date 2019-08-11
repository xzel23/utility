package com.dua3.utility.options;

import java.util.*;

import com.dua3.utility.options.Option.Value;

/**
 * A Mapping of options to values.
 * The only difference to a normal {@code Map<Option<?>,Value<?>>} is that
 * {@code Options.get(option)} will return the option's default value (if set)
 * instead
 * of {@code null}.
 */
public class OptionValues extends HashMap<Option<?>, Value<?>> {
    private static final long serialVersionUID = 1L;

    private static final OptionValues EMPTY_OPTIONS = new OptionValues(Collections.emptyMap());

    /**
     * Listener interface for value changes.
     * @param <T>
     *  the value type
     */
    public interface ValueChangeListener<T> {
    	/**
    	 * Called when an option's value changes.
    	 * @param option
    	 *  the option the value belongs to
    	 * @param o
    	 *  the old value
    	 * @param n
    	 *  the new value
    	 */
    	void changed(Option<T> option, Value<T> o, Value<T> n);
    }
    
    /** List of VlueChangeListeners. */
    private transient List<ValueChangeListener<?>> changeListeners = new ArrayList<>();
    
    /**
     * Add value change listener.
     * @param listener
     *  the listener to add
     */
    public void addChangeListener(ValueChangeListener<?> listener) {
    	changeListeners.add(Objects.requireNonNull(listener));
    }
    
    /**
     * Remove value change listener.
     * @param listener
     *  the listener to remove
     */
    public void removeChangeListener(ValueChangeListener<?> listener) {
    	changeListeners.remove(Objects.requireNonNull(listener));
    }

    /**
     * Fire value change event.
     * @param v
     *  the option
     * @param o
     *  the option's old value
     * @param n
     *  the option's new value
     */
    @SuppressWarnings("unchecked")
	private <T> void fireChange_(Option<T> v, Value<T> o, Value<T> n) {
    	if (!Objects.equals(o, n)) {
	    	changeListeners.forEach(listener -> ((ValueChangeListener<T>)listener).changed(v, o, n));
    	}
    }
    
    /**
     * An empty set of option values.
     *
     * @return
     *         empty options
     */
    public static OptionValues empty() {
        return EMPTY_OPTIONS;
    }

    public static OptionValues of(Option<?> option, Value<?> value) {
        OptionValues ov = new OptionValues();
        ov.put(option, value);
        return ov;
    }

    /**
     * Create a new empty instance.
     */
    public OptionValues() {
    }

    /**
     * Create a new instance with values from map.
     * @param options
     *  mapping of options and corresponding values
     */
    public OptionValues(Map<Option<?>, Value<?>> options) {
        super(options);
    }

    /**
     * Get an option's value.
     * @param op
     *  the option
     * @return
     *  the value
     */
    public Value<?> get(Option<?> op) {
        return getOrDefault(op, op.getDefault());
    }

    /**
     * Get an option's value.
     *
     * @param key
     *  the key (option)
     * @return
     *  the value
     * @deprecated
     *  Use {@link #get(Option)}.
     */
    @Override
    @Deprecated
    public Value<?> get(Object key) {
        if (key instanceof Option) {
            return get((Option<?>) key);
        }
        return null;
    }

    /**
     * Set an option's value.
     * @param option
     *  the option
     * @param value
     *  the value
     * @return
     *  the old value
     */
    @Override
    public Value<?> put(Option<?> option, Value<?> value) {
        Class<?> klassO = option.getOptionClass();
        Class<?> klassV = value!=null ? getClass(value.get()) : null;
        if (klassV != null && !(klassO.isAssignableFrom(klassV))) {
            throw new IllegalArgumentException("Incompatible value for option '" + option.getName() + "' - expected: "
                    + klassO + ", is: " + klassV);
        }
        Value<?> old = super.put(option, value);
        fireChange(option, old, value);
		return old;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void fireChange(Option v, Value o, Value n) {
		fireChange_(v, o, n);
	}

	private Class<?> getClass(Object o) {
        return o != null ? o.getClass() : null;
    }

    @Override
    public String toString() {
        var i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<Option<?>, Value<?>> e = i.next();
            String name = e.getKey().getName();
            String value = e.getValue().text();
            sb.append(name);
            sb.append('=');
            sb.append(value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }
}

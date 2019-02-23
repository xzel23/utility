package com.dua3.utility.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Option that changes the behavior of other classes. This class is intended
 * to be used to create settings dialogs at runtime.
 *
 * @param <T> the type of this option's values
 */
public abstract class Option<T> {
    
    public static interface Value<T> extends Supplier<T>, Comparable<Value<T>> {
        default int compareTo(Value<T> other) {
            return toString().compareTo(String.valueOf(other));
        }
    }

	/**
	 * A value with a substituted, human-readable name.
	 */
    private static class StaticValue<T> implements Value<T> {

        private final String name;
        private final T value;

        public StaticValue(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Create a named value.
     * @param <T>
     *  the parameter type
     * @param name
     *  the name to use
     * @param value
     *  the value
     * @return
     *  the named value
     */
    public static <T> Value<T> value(String name, T value) {
        return new StaticValue<>(name, value);
    }

    /**
     * Create a value.
     * @param <T>
     *  the parameter type
     * @param value
     *  the value
     * @return
     *  the named value
     */
    public static <T> Value<T> value(T value) {
        return new StaticValue<>(String.valueOf(value), value);
    }

	public static class StringOption extends Option<String> {
		StringOption(String name, Value<String> defaultValue) {
			super(name, String.class, defaultValue);
		}		
	}
	
	public static class ChoiceOption<T> extends Option<T> {		
		private final List<Value<T>> choices;

	    ChoiceOption(
    		String name, 
    		Class<T> klass, 
    		Value<T> defaultValue, 
    		Collection<Value<T>> choices) {
	    	super(name, klass, defaultValue);

	    	// make sure this.choices does not contain a duplicate for defaultValue
	        if (choices.contains(defaultValue)) {
	            this.choices = new ArrayList<>(choices);
	        } else {
	            List<Value<T>> allChoices = new ArrayList<>(choices.size() + 1);
	            allChoices.add(defaultValue);
	            allChoices.addAll(choices);
	            this.choices = allChoices;
	        }
	    }
	    
		public List<Value<T>> getChoices() {
	        return Collections.unmodifiableList(choices);
	    }
	}
	
	public static StringOption stringOption(String name) {
		return new StringOption(name, () -> "");
	}
	
	public static StringOption stringOption(String name, String defaultValue) {
		return new StringOption(name, () -> defaultValue);
	}
	
	public static StringOption stringOption(String name, Value<String> defaultValue) {
		return new StringOption(name, defaultValue);
	}
	
	public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue, Collection <Value<T>> choices) {
		return new ChoiceOption<T>(name, klass, defaultValue, choices);
	}
	
	public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue, @SuppressWarnings("unchecked") Value<T>... choices) {
		return new ChoiceOption<T>(name, klass, defaultValue, Arrays.asList(choices));
	}
	
    private final String name;
    private final Class<T> klass;
    private final Value<T> defaultValue;

    protected Option(String name, Class<T> klass, Value<T> defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.klass = Objects.requireNonNull(klass);
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return name.equals(other.name) && klass.equals(other.klass);
    }

    public Value<T> getDefault() {
        return defaultValue;
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
        return name + "[" + getDefault() + "]";
    }

    private static final String PATTERN_VAR_START = "\\$\\{";
    private static final String PATTERN_VAR_NAME = "\\p{Alpha}\\p{Alnum}*";
    private static final String PATTERN_VAR_END = "\\}";

    private static final Pattern PATTERN_VAR = Pattern.compile(PATTERN_VAR_START+"("+PATTERN_VAR_NAME+")"+PATTERN_VAR_END);

    /**
     * Parse a configuration schema string.
     * <p>
     * Example: https://${SERVER}:${PORT}
     * @param s
     *  the scheme to parse
     * @return
     *  list of options
     */
    public static List<Option<?>> parseScheme(String s) {
    	List<Option<?>> list = new ArrayList<>();
        Matcher matcher = PATTERN_VAR.matcher(s);
        while (matcher.find()) {
            String var = matcher.group(1);
            list.add(Option.stringOption(var));
        }
        return list;
    }
}

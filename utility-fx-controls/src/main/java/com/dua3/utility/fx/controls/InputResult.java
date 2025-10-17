package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents the result of an input operation along with optional associated data.
 *
 * @param <T> the type of the result, typically representing an action (e.g., 'OK', 'CANCEL')
 * @param <K> the key type for the associated data map
 */
public class InputResult<T,K> {
    /**
     * The result, i.e., 'OK', 'CANCEL', etc..
     */
    private final T result;
    /**
     * The data input by the user.
     */
    private final Map<K, @Nullable Object> data;

    /**
     * Constructs an {@code InputResult} with the specified result and an empty data map.
     *
     * @param result the result value, typically representing an action such as 'OK' or 'CANCEL'
     */
    public InputResult(T result) {
        this.result = result;
        this.data = Collections.emptyMap();
    }

    /**
     * Constructs an {@code InputResult} with the specified result and data map.
     *
     * @param result the result value, typically representing an action such as 'OK' or 'CANCEL'
     * @param data a map containing additional input data, or {@code null} for no data
     */
    public InputResult(T result, @Nullable Map<K, @Nullable Object> data) {
        this.result = result;
        this.data = data == null ? Collections.emptyMap() : Collections.unmodifiableMap(data);
    }

    /**
     * Retrieves the result value, which typically represents an action such as 'OK' or 'CANCEL'.
     *
     * @return the result value of type {@code T}
     */
    public T result() {
        return result;
    }

    /**
     * Checks if the given result matches the stored result.
     *
     * @param r the result value to compare with the stored result
     * @return {@code true} if the given result matches the stored result, {@code false} otherwise
     */
    public boolean is(T r) {
        return Objects.equals(r, result);
    }

    /**
     * Determines if the stored result matches any of the specified results.
     *
     * @param results an array of result values to compare with the stored result
     * @return {@code true} if the stored result matches any of the specified results, {@code false} otherwise
     */
    public boolean isAny(T... results) {
        for (T r : results) {
            if (is(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes the provided consumer if the specified result matches the stored result.
     *
     * @param r the result value to compare with the stored result
     * @param consumer the consumer to invoke with the input data map if the result matches
     */
    public void onResult(T r, Consumer<Map<K, @Nullable Object>> consumer) {
        if (Objects.equals(result, r)) {
            consumer.accept(Collections.unmodifiableMap(data));
        }
    }

    /**
     * Returns an unmodifiable view of the data map.
     *
     * @return the data map (never null)
     */
    public Map<K, @Nullable Object> data() {
        return Collections.unmodifiableMap(data);
    }

    /**
     * Convenience accessor to retrieve a value from the data map by key.
     *
     * @param key the key
     * @return the value associated with the key, or null if none
     */
    public @Nullable Object get(K key) {
        return data.get(key);
    }
}

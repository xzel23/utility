package com.dua3.utility.fx.controls;

import javafx.scene.control.ButtonType;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents the result of an input operation along with optional associated data.
 *
 * @param result The result, i.e., 'OK', 'CANCEL', etc..
 * @param data   The data input by the user.
 */
public record InputResult(ButtonType result, Map<String, @Nullable Object> data) {

    /**
     * Constructs an {@code InputResult} with the specified result and an empty data map.
     *
     * @param result the result value, typically representing an action such as 'OK' or 'CANCEL'
     */
    public InputResult(ButtonType result) {
        this(result, Collections.emptyMap());
    }

    /**
     * Checks if the given result matches the stored result.
     *
     * @param r the result value to compare with the stored result
     * @return {@code true} if the given result matches the stored result, {@code false} otherwise
     */
    public boolean is(ButtonType r) {
        return Objects.equals(r, result);
    }

    /**
     * Determines if the stored result matches any of the specified results.
     *
     * @param results an array of result values to compare with the stored result
     * @return {@code true} if the stored result matches any of the specified results, {@code false} otherwise
     */
    public boolean isAny(ButtonType... results) {
        for (ButtonType r : results) {
            if (is(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes the provided consumer if the specified result matches the stored result.
     *
     * @param r        the result value to compare with the stored result
     * @param consumer the consumer to invoke with the input data map if the result matches
     */
    public void onResult(ButtonType r, Consumer<Map<String, @Nullable Object>> consumer) {
        if (Objects.equals(result, r)) {
            consumer.accept(data);
        }
    }

    /**
     * Convenience accessor to retrieve a value from the data map by key.
     *
     * @param key the key
     * @return the value associated with the key, or null if none
     */
    public @Nullable Object get(String key) {
        return data.get(key);
    }
}

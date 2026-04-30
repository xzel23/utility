package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A utility class for managing and validating interconnected fields.
 * <p>
 * Fields provide mechanisms for default values, validation, and handling dependencies. Dependent fields
 * are fields that rely on the value of other fields to determine their own validity. They are automatically
 * re-validated whenever any of their dependencies change.
 */
public final class Fields {
    /**
     * Provides a validation function that always considers the input as valid.
     * <p>
     * This method returns a {@code BiFunction} that takes an input value and a {@code Fields} instance,
     * and always produces an empty {@code Optional}, indicating that there are no validation errors.
     *
     * @param <T> the type of the input value to be validated
     * @return a {@code BiFunction} that always returns {@code Optional.empty()}, representing successful validation
     */
    public static <T> BiFunction<T, Fields, Optional<String>> alwaysValid() {
        return (t, fields) -> Optional.empty();
    }

    /**
     * Creates a new {@code Fields} instance by wiring together the provided fields.
     * <p>
     * This method sets up a Fields instance by associating each field with its dependencies and manages
     * automatic re-validation of dependent fields whenever their dependencies change. Since the Fields
     * instance will be stored in each of the wired fields, it is usually not necessary to bind it to
     * a reference. Unless needed in special cases, the return value can be safely ignored.
     *
     * @param fields the fields to be wired together
     * @return a {@code Fields} instance containing the provided fields
     */
    public static Fields wireFields(Field<?>... fields) {
        return new Fields(fields);
    }

    /**
     * A record representing a Field that holds a value of type T and supports validation and dependencies.
     *
     * @param <T>           the type of the value held by this Field
     * @param id            the unique identifier for this Field
     * @param fields        a reference to the collection of Fields this Field belongs to
     * @param value         a reference to the current value of this Field
     * @param defaultValue  a supplier providing the default value for this Field
     * @param validateField a function used to validate the value of this Field against the given Fields
     * @param dependencies  a list of Field identifiers on which this Field depends
     * @param dependents    a list of Field identifiers that depend on this Field
     */
    public record Field<T>(
            String id,
            AtomicReference<@Nullable Fields> fields,
            AtomicReference<@Nullable T> value,
            Supplier<@Nullable T> defaultValue,
            BiFunction<@Nullable T, Fields, Optional<String>> validateField,
            List<String> dependencies,
            List<String> dependents
    ) {
        /**
         * Provides a validation function for the current field.
         * <p>
         * The returned function takes a value of type T, validates it against associated fields,
         * and updates the field if validation succeeds. It is intended to be used as the validation
         * function for the {@link com.dua3.utility.fx.controls.InputControl} associated with this Field.
         *
         * @return a function that accepts a value of type T and returns an {@code Optional<String>}
         * containing an error message if validation fails, or {@code Optional.empty()} if it succeeds.
         */
        public Function<T, Optional<String>> validate() {
            return v -> updateAndValidate(fields.get(), v);
        }

        /**
         * Converts the default value of the field to a boolean representation.
         * <p>
         * The method returns a {@link BooleanSupplier} which determines whether
         * the default value is explicitly set to {@code Boolean.TRUE}. It is intended to be used
         * as the default value provider for {@link com.dua3.utility.fx.controls.InputControl}
         * instances that require a boolean default value.
         *
         * @return a {@link BooleanSupplier} that evaluates to {@code true} if the default value is {@code Boolean.TRUE},
         * otherwise {@code false}.
         */
        public BooleanSupplier defaultValueAsBoolean() {
            return () -> defaultValue.get() == Boolean.TRUE;
        }

        /**
         * Updates the field's value and triggers validation of this field and all dependent fields.
         * <p>
         * This method validates the provided new value against the current field's logic. If the validation
         * is successful, the field's value is updated. Additionally, dependent fields are validated
         * after the update. If any validation errors are encountered, an error message is returned.
         *
         * @param fields   the collection of fields used for validation; ensures dependencies are considered
         *                 during the validation process
         * @param newValue the new value of type T to be validated and potentially set as the field's value;
         *                 can be null
         * @return an {@code Optional<String>} containing a validation error message if the validation fails,
         * or {@code Optional.empty()} if the operation succeeds
         */
        public Optional<String> updateAndValidate(Fields fields, @Nullable T newValue) {
            Optional<String> error = validateField.apply(newValue, fields);
            if (error.isEmpty()) {
                value.set(newValue);
                error = validateDependents(fields);
            }
            return error;
        }

        /**
         * Revalidates the current field using the provided Fields instance and dependent validation logic.
         * The method first validates the field's value using {@code validateField} and if no error
         * is found, it proceeds to validate the dependent fields.
         *
         * @param fields the {@link Fields} instance used to perform the validation; ensures that
         *               dependencies are correctly considered during validation.
         * @return an {@code Optional<String>} containing a validation error message if any validation
         * step fails, or {@code Optional.empty()} if validation succeeds.
         */
        private Optional<String> revalidate(Fields fields) {
            Optional<String> error = validateField.apply(value().get(), fields);
            if (error.isEmpty()) {
                error = validateDependents(fields);
            }
            return error;
        }

        /**
         * Validates all dependent fields associated with the current field.
         * Iterates through each dependent field and performs a revalidation.
         * If a validation error is found in any dependent field, the error message
         * is returned immediately. If all dependents pass validation, an empty
         * {@code Optional} is returned.
         *
         * @param fields the {@code Fields} instance containing all fields, used to
         *               access and validate dependent fields.
         * @return an {@code Optional<String>} containing a validation error message
         * if any dependent field's validation fails, or {@code Optional.empty()}
         * if all dependent fields are validated successfully.
         */
        private Optional<String> validateDependents(Fields fields) {
            for (String dependent : dependents) {
                Optional<String> error = fields.getField(dependent).revalidate(fields);
                if (error.isPresent()) {
                    return error;
                }
            }
            return Optional.empty();
        }

        /**
         * Retrieves the current value of the field.
         * This value is stored in an {@link AtomicReference} and can be null.
         *
         * @return the current value of the field, or null if no value is set.
         */
        public @Nullable T getValue() {
            return value.get();
        }
    }

    private final Map<String, Field<?>> fields = new HashMap<>();

    /**
     * Creates a new {@code Field} instance with the specified configuration.
     *
     * @param <T>          the type of the field's value
     * @param id           the unique identifier for the field
     * @param defaultValue a supplier that provides the default value for the field; may return {@code null}
     * @param validate     a bi-function that validates the field value in the context of the associated {@code Fields} instance;
     *                     may return an {@code Optional} containing a validation error message, or an empty {@code Optional} if valid
     * @param dependencies an optional list of field identifiers on which this field depends
     * @return a new {@code Field} instance with the provided configuration
     */
    public static <T> Field<T> field(
            String id,
            Supplier<@Nullable T> defaultValue,
            BiFunction<@Nullable T, Fields, Optional<String>> validate,
            String... dependencies
    ) {
        return new Field<>(
                id,
                new AtomicReference<>(null),
                new AtomicReference<>(defaultValue.get()),
                defaultValue,
                validate,
                List.of(dependencies),
                new ArrayList<>()
        );
    }

    /**
     * Constructs a new {@code Fields} instance by associating the provided fields with their dependencies.
     * This constructor initializes the dependency relationships between fields and sets up the internal state.
     *
     * @param fields an array of {@code Field<?>} objects representing the fields to be wired together.
     */
    private Fields(Field<?>... fields) {
        for (Field<?> field : fields) {
            field.fields.set(this);
            this.fields.put(field.id(), field);
        }
        for (Field<?> field : fields) {
            for (String dependency : field.dependencies()) {
                this.fields.get(dependency).dependents().add(field.id());
            }
        }
    }

    /**
     * Retrieves a field with the given identifier.
     *
     * @param <T> the type of the field's value
     * @param id  the unique identifier of the field to retrieve
     * @return the field associated with the given identifier
     * @throws NullPointerException if no field with the specified identifier exists
     */
    @SuppressWarnings("unchecked")
    public <T> Field<T> getField(String id) {
        return (Field<T>) Objects.requireNonNull(fields.get(id), "no field with id: " + id);
    }
}

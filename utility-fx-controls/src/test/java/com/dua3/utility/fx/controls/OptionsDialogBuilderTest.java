package com.dua3.utility.fx.controls;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.Param;
import com.dua3.utility.text.MessageFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link OptionsDialogBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class OptionsDialogBuilderTest extends FxTestBase {

    /**
     * Test creating an OptionsDialogBuilder.
     */
    @Test
    void testCreateBuilder() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            OptionsDialogBuilder builder = Dialogs.options(MessageFormatter.standard(), null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test setting options.
     */
    @Test
    void testOptions() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            OptionsDialogBuilder builder = Dialogs.options(MessageFormatter.standard(), null);

            // Create some options
            Option<Boolean> verboseOption = Option.createFlag("Verbose", "Enable verbose output", "--verbose", "-v");

            Option<Integer> countOption = Option.createSimpleOption("Count", "Number of iterations", Param.ofInt("Count", "Number of iterations", "COUNT", Param.Required.REQUIRED), () -> 1, "--count", "-c");

            List<Option<?>> options = new ArrayList<>();
            options.add(verboseOption);
            options.add(countOption);

            // Set options
            builder.options(options);

            // Verify builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting current values.
     */
    @Test
    void testCurrentValues() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            OptionsDialogBuilder builder = Dialogs.options(MessageFormatter.standard(), null);

            // Create some options
            Option<Boolean> verboseOption = Option.createFlag("Verbose", "Enable verbose output", "--verbose", "-v");

            Option<Integer> countOption = Option.createSimpleOption("Count", "Number of iterations", Param.ofInt("Count", "Number of iterations", "COUNT", Param.Required.REQUIRED), () -> 1, "--count", "-c");

            List<Option<?>> options = new ArrayList<>();
            options.add(verboseOption);
            options.add(countOption);

            // Create current values
            Arguments currentValues = Arguments.of(Arguments.createEntry(verboseOption, true), Arguments.createEntry(countOption, 5));

            // Set options and current values
            builder.options(options);
            builder.currentValues(currentValues);

            // Verify builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test building an OptionsDialog.
     */
    @Test
    void testBuild() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            OptionsDialogBuilder builder = Dialogs.options(MessageFormatter.standard(), null);

            // Create some options
            Option<Boolean> verboseOption = Option.createFlag("Verbose", "Enable verbose output", "--verbose", "-v");

            Option<Integer> countOption = Option.createSimpleOption("Count", "Number of iterations", Param.ofInt("Count", "Number of iterations", "COUNT", Param.Required.REQUIRED), () -> 1, "--count", "-c");

            List<Option<?>> options = new ArrayList<>();
            options.add(verboseOption);
            options.add(countOption);

            // Create current values
            Arguments currentValues = Arguments.of(Arguments.createEntry(verboseOption, true), Arguments.createEntry(countOption, 5));

            // Set options and current values
            builder.options(options);
            builder.currentValues(currentValues);

            // Build dialog
            OptionsDialog dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);
        });
    }

    /**
     * Test method chaining.
     */
    @Test
    void testMethodChaining() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create some options
            Option<Boolean> verboseOption = Option.createFlag("Verbose", "Enable verbose output", "--verbose", "-v");

            Option<Integer> countOption = Option.createSimpleOption("Count", "Number of iterations", Param.ofInt("Count", "Number of iterations", "COUNT", Param.Required.REQUIRED), () -> 1, "--count", "-c");

            List<Option<?>> options = new ArrayList<>();
            options.add(verboseOption);
            options.add(countOption);

            // Create current values
            Arguments currentValues = Arguments.of(Arguments.createEntry(verboseOption, true), Arguments.createEntry(countOption, 5));

            // Create a builder and chain methods
            OptionsDialogBuilder builder = Dialogs.options(MessageFormatter.standard(), null).options(options).currentValues(currentValues).title("Options").header("Configure Options");

            // Verify builder is valid after chaining
            assertNotNull(builder);
        });
    }
}
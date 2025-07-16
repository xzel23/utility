package com.dua3.utility.options;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Arguments class.
 */
class ArgumentsTest {

    @Test
    void testEmpty() {
        Arguments args = Arguments.empty();
        assertTrue(args.isEmpty());
        assertEquals(Collections.emptyList(), args.positionalArgs());
    }

    @Test
    void testOf() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        assertFalse(args.isEmpty());
        assertEquals(Optional.of("value"), args.get(option));
    }

    @Test
    void testCreateEntry() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");

        assertEquals(option, entry.getOption());
        assertEquals("value", entry.getValue());
    }

    @Test
    void testValidate() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        // Should not throw an exception
        args.validate(option);

        // Create a required option that is not in the arguments
        Option<String> requiredOption = new OptionBuilder<>(null, "Required Option", "A required option", String.class).param(Param.ofString("Required Param", "A required parameter", "arg", Param.Required.REQUIRED)).repetitions(Repetitions.EXACTLY_ONE).build("--required", "-r");

        // Should throw an exception because requiredOption is required but not present
        assertThrows(ArgumentsException.class, () -> args.validate(requiredOption));
    }

    @Test
    void testGet() {
        Option<String> option = Option.createSimpleOption(
                "Test Option",
                "A test option",
                Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED),
                () -> "default",
                "--option",
                "-o"
        );

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        assertEquals(Optional.of("value"), args.get(option));

        Option<String> anotherOption = Option.createSimpleOption(
                "Another Option",
                "Another test option",
                Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED),
                () -> "default",
                "--another",
                "-a"
        );

        assertEquals(Optional.of("default"), args.get(anotherOption));
    }

    @Test
    void testGetOrThrow() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        assertEquals("value", args.getOrThrow(option));

        Option<String> anotherOption = Option.createSimpleOption("Another Option", "Another test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--another", "-a");

        assertEquals("default", args.getOrThrow(anotherOption));
    }

    @Test
    void testIsSet() {
        Option<Boolean> flag = Option.createFlag("Test Flag", "A test flag", "--test", "-t");
        Arguments.Entry<Boolean> entry = Arguments.createEntry(flag, true);
        Arguments args = Arguments.of(entry);

        assertTrue(args.isSet(flag));

        Option<Boolean> anotherFlag = Option.createFlag("Another Flag", "Another test flag", "--another", "-a");
        assertFalse(args.isSet(anotherFlag));
    }

    @Test
    void testIfSet() {
        Option<Boolean> flag = Option.createFlag("Test Flag", "A test flag", "--test", "-t");
        Arguments.Entry<Boolean> entry = Arguments.createEntry(flag, true);
        Arguments args = Arguments.of(entry);

        AtomicBoolean actionCalled = new AtomicBoolean(false);
        args.ifSet(flag, () -> actionCalled.set(true));
        assertTrue(actionCalled.get());

        Option<Boolean> anotherFlag = Option.createFlag("Another Flag", "Another test flag", "--another", "-a");
        AtomicBoolean anotherActionCalled = new AtomicBoolean(false);
        args.ifSet(anotherFlag, () -> anotherActionCalled.set(true));
        assertFalse(anotherActionCalled.get());
    }

    @Test
    void testStream() {
        Option<String> option1 = Option.createSimpleOption("Option 1", "First test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default1", "--option1", "-o1");

        Option<String> option2 = Option.createSimpleOption("Option 2", "Second test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default2", "--option2", "-o2");

        Arguments.Entry<String> entry1 = Arguments.createEntry(option1, "value1");
        Arguments.Entry<String> entry2 = Arguments.createEntry(option2, "value2");
        Arguments args = Arguments.of(entry1, entry2);

        List<Arguments.Entry<?>> entries = args.stream().toList();
        assertEquals(2, entries.size());
        assertTrue(entries.contains(entry1));
        assertTrue(entries.contains(entry2));
    }

    @Test
    void testStreamWithOption() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        List<String> values = args.stream(option).toList();
        assertEquals(1, values.size());
        assertEquals("value", values.get(0));

        Option<String> anotherOption = Option.createSimpleOption("Another Option", "Another test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--another", "-a");

        List<String> emptyValues = args.stream(anotherOption).toList();
        assertTrue(emptyValues.isEmpty());
    }

    @Test
    void testIfPresent() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        AtomicBoolean actionCalled = new AtomicBoolean(false);
        AtomicBoolean correctValue = new AtomicBoolean(false);
        args.ifPresent(option, value -> {
            actionCalled.set(true);
            correctValue.set("value".equals(value));
        });

        assertTrue(actionCalled.get());
        assertTrue(correctValue.get());

        Option<String> anotherOption = Option.createSimpleOption("Another Option", "Another test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--another", "-a");

        AtomicBoolean anotherActionCalled = new AtomicBoolean(false);
        args.ifPresent(anotherOption, value -> anotherActionCalled.set(true));
        assertFalse(anotherActionCalled.get());
    }

    @Test
    void testHandle() {
        AtomicInteger handlerCalled = new AtomicInteger(0);

        Option<String> option = new OptionBuilder<>(null, "Test Option", "A test option", String.class).param(Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED)).handler(value -> handlerCalled.incrementAndGet()).build("--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        args.handle();
        assertEquals(1, handlerCalled.get());
    }

    @Test
    void testIterator() {
        Option<String> option1 = Option.createSimpleOption("Option 1", "First test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default1", "--option1", "-o1");

        Option<String> option2 = Option.createSimpleOption("Option 2", "Second test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default2", "--option2", "-o2");

        Arguments.Entry<String> entry1 = Arguments.createEntry(option1, "value1");
        Arguments.Entry<String> entry2 = Arguments.createEntry(option2, "value2");
        Arguments args = Arguments.of(entry1, entry2);

        Iterator<Arguments.Entry<?>> iterator = args.iterator();
        assertTrue(iterator.hasNext());
        Arguments.Entry<?> firstEntry = iterator.next();
        assertTrue(iterator.hasNext());
        Arguments.Entry<?> secondEntry = iterator.next();
        assertFalse(iterator.hasNext());

        // Check that we got both entries (order may vary)
        assertTrue((firstEntry.equals(entry1) && secondEntry.equals(entry2)) || (firstEntry.equals(entry2) && secondEntry.equals(entry1)));
    }

    @Test
    void testToString() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.createEntry(option, "value");
        Arguments args = Arguments.of(entry);

        String argsString = args.toString();
        assertNotNull(argsString);
        assertFalse(argsString.isEmpty());

        System.out.println("[DEBUG_LOG] Arguments.toString() output: " + argsString);
        assertEquals("""
                        Arguments{
                          --option "value"
                        }""",
                argsString
        );
    }

    @Test
    void testEntryCreate() {
        Option<String> option = Option.createSimpleOption("Test Option", "A test option", Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED), () -> "default", "--option", "-o");

        Arguments.Entry<String> entry = Arguments.Entry.create(option, "value");

        assertEquals(option, entry.getOption());
        assertEquals("value", entry.getValue());
    }

    @Test
    void testEntryHandle() {
        AtomicInteger handlerCalled = new AtomicInteger(0);

        Option<String> option = new OptionBuilder<>(null, "Test Option", "A test option", String.class).param(Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED)).handler(value -> handlerCalled.incrementAndGet()).build("--option", "-o");

        Arguments.Entry<String> entry = Arguments.Entry.create(option, "value");

        entry.handle();
        assertEquals(1, handlerCalled.get());
    }
}

package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Utility class providing functionalities for processing and grouping elements of a stream
 * based on defined predicates and actions. The utility methods in this class simplify
 * the task of grouping consecutive elements in a stream.
 * <p>
 * <strong>This class is only available when using language level 24+!</strong>
 */
public final class StreamGathererUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private StreamGathererUtil() {
        // utility class
    }

    /**
     * Groups consecutive elements from a stream based on the provided predicate,
     * creating new groups whenever the result of the predicate changes.
     *
     * @param <T>           the type of elements in the stream
     * @param <U>           the type of the resulting group
     * @param predicate     a predicate that determines the grouping condition; elements for which the predicate evaluates
     *                      to the same result will be grouped together
     * @param groupSupplier a supplier to create a new group of type U
     * @param accumulator   a function that takes the current group and the next element in the stream
     *                      and returns the updated group
     * @return a Gatherer that processes the stream into groups of consecutive elements based on the predicate
     */
    public static <T, U> Gatherer<T, ?, U>
    groupConsecutive(
            Predicate<? super T> predicate,
            Supplier<? extends U> groupSupplier,
            BiFunction<? super U, ? super T, ? extends U> accumulator
    ) {
        class State {
            boolean currentGroupType;
            @Nullable
            U currentGroup;
        }

        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    boolean matches = predicate.test(element);

                    if (state.currentGroup == null) {
                        // Initialize first group
                        state.currentGroupType = matches;
                        state.currentGroup = accumulator.apply(
                                groupSupplier.get(),
                                element
                        );
                    } else if (state.currentGroupType == matches) {
                        // Continue current group
                        state.currentGroup = accumulator.apply(
                                state.currentGroup,
                                element
                        );
                    } else {
                        // Finalize the current group and start new
                        downstream.push(state.currentGroup);
                        state.currentGroupType = matches;
                        state.currentGroup = accumulator.apply(
                                groupSupplier.get(),
                                element
                        );
                    }
                    return true;
                }),
                (state, downstream) -> {
                    if (state.currentGroup != null) {
                        downstream.push(state.currentGroup);
                    }
                }
        );
    }

    /**
     * Represents actions that can be taken when filtering and grouping stream elements into groups.
     * These actions dictate how each element in the stream should be handled during grouping:
     * whether it should be ignored, dropped, added to the current group, or used to start a new group.
     * <p>
     * The possible actions are:
     * <ul>
     * <li>IGNORE: The element is completely ignored, i. e., it is not part of any group and does
     *     not affect the internal state.
     * <li>DROP: The element is excluded and not part of any group. Resets the current element to null.
     * <li>JOIN_GROUP: The current element is added to the current group; if no group exists, a new group is greated.
     * <li>START_GROUP: A new group is started with the current element as its first member.
     * </ul>
     */
    public enum GroupingGathererAction {
        /**
         * Ignore the element.
         */
        IGNORE,
        /**
         * Drop the element and reset the current element to null.
         */
        DROP,
        /**
         * Add the element to the current group.
         */
        JOIN_GROUP,
        /**
         * Start a new group with the current element as its first member.
         */
        START_GROUP
    }

    /**
     * Filters and groups consecutive elements from a stream based on the provided action returned
     * by the {@code }actionSelector}. Elements can be joined into the current group, start a new group,
     * or be dropped based on the action.
     *
     * @param <T>            the type of elements in the stream
     * @param <U>            the type of the resulting group
     * @param actionSelector a BiFunction that takes the previous element (can be null) and the current element,
     *                       and returns an {@link GroupingGathererAction} to determine whether to drop, join, or split
     *                       the elements into groups
     * @param groupSupplier  a supplier to create a new group of type U
     * @param accumulator    a function that takes the current group and the current element,
     *                       and returns the updated group
     * @return a Gatherer that processes the stream into groups of consecutive elements based on the action
     * returned by the predicate
     */
    public static <T, U> Gatherer<T, ?, U>
    filterAndGroupConsecutive(
            BiFunction<? super @Nullable T, ? super T, GroupingGathererAction> actionSelector,
            Supplier<? extends U> groupSupplier,
            BiFunction<? super U, ? super T, ? extends U> accumulator
    ) {
        class State {
            @Nullable
            T currentElement;
            @Nullable
            U currentGroup;
        }

        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    GroupingGathererAction action = actionSelector.apply(state.currentElement, element);

                    switch (action) {
                        case DROP -> state.currentElement = null;
                        case IGNORE -> { /* do nothing, just keep the current element */ }
                        case JOIN_GROUP -> {
                            state.currentGroup = accumulator.apply(
                                    state.currentGroup,
                                    element
                            );
                            state.currentElement = element;
                        }
                        case START_GROUP -> {
                            // Finalize the current group and start new
                            if (state.currentGroup != null) {
                                downstream.push(state.currentGroup);
                            }
                            state.currentGroup = accumulator.apply(
                                    groupSupplier.get(),
                                    element
                            );
                            state.currentElement = element;
                        }
                    }
                    return true;
                }),
                (state, downstream) -> {
                    if (state.currentGroup != null) {
                        downstream.push(state.currentGroup);
                    }
                }
        );
    }
}

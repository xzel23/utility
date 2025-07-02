package com.dua3.utility.text;

/**
 * Represents a predicate (boolean-valued function) that tests a single
 * character.
 */
@FunctionalInterface
interface CharPredicate {
    boolean test(char c);
}

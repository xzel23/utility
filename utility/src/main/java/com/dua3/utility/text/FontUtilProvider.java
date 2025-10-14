package com.dua3.utility.text;

import java.util.function.Supplier;

/**
 * This interface represents a provider for FontUtil instances.
 */
@FunctionalInterface
public interface FontUtilProvider extends Supplier<FontUtil> {}

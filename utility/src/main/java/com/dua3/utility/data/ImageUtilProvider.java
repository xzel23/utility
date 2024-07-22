package com.dua3.utility.data;

import java.util.function.Supplier;

/**
 * A functional interface for a provider of ImageUtil instances.
 */
@FunctionalInterface
public interface ImageUtilProvider extends Supplier<ImageUtil<?>> {}

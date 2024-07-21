package com.dua3.utility.data;

import java.util.function.Supplier;

@FunctionalInterface
public interface ImageUtilProvider extends Supplier<ImageUtil<?>> {}

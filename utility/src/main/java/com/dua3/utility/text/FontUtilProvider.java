package com.dua3.utility.text;

import java.util.function.Supplier;

@FunctionalInterface
public interface FontUtilProvider extends Supplier<FontUtil<?>> {}

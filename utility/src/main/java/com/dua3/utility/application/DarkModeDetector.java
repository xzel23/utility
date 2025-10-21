package com.dua3.utility.application;

import java.util.function.Consumer;

public interface DarkModeDetector {
    boolean isDarkModeDetectionSupported();
    boolean isDarkMode();
    void addListener(Consumer<Boolean> listener);
    void removeListener(Consumer<Boolean> listener);
}

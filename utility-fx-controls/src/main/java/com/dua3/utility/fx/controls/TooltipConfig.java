package com.dua3.utility.fx.controls;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A record holding tooltip durations.
 * @param showDelay the delay before a tooltip is shown after the pointer enters the node
 * @param showDuration how long the tooltip is shown
 * @param hideDelay the delay before the tooltip is hidden when the pointer is moved outside the node
 */
public record TooltipConfig(Duration showDelay, Duration showDuration, Duration hideDelay) {
    private static final TooltipConfig DEFAULT_CONFIG = from(new Tooltip());

    /**
     * Get the configuration with the default values as defined by JavaFX.
     * @return configuration with default values
     */
    public static TooltipConfig getDefault() {
        return DEFAULT_CONFIG;
    }

    /**
     * Apply this configuration to a tooltip.
     * @param tt the tooltip
     */
    public void applyTo(Tooltip tt) {
        tt.setShowDelay(showDelay);
        tt.setShowDuration(showDuration);
        tt.setHideDelay(hideDelay);
    }

    /**
     * Obtain configuration from a tooltip.
     * @param tt the tooltip
     * @return configuration with values from the tooltip
     */
    public static TooltipConfig from(Tooltip tt) {
        return new TooltipConfig(tt.getShowDelay(), tt.getShowDuration(), tt.getHideDelay());
    }
}

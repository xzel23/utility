package com.dua3.utility.encryption;

/**
 * Defines how input buffers containing sensitive data should be handled after processing.
 */
public enum InputBufferHandling {
    /**
     * Clear (overwrite with zeros) the input buffer after use to prevent sensitive data
     * from remaining in memory. Choose this option when the input contains sensitive data
     * like passwords, private keys, or confidential content.
     * <p>
     * <strong>Warning:</strong> The input array will be modified and cannot be reused.
     */
    CLEAR_AFTER_USE,

    /**
     * Preserve the input buffer unchanged after use. Choose this option when you need
     * to reuse the input data or when the data is not sensitive.
     * <p>
     * <strong>Security Note:</strong> Sensitive data will remain in memory until
     * garbage collected, which may pose a security risk.
     */
    PRESERVE
}

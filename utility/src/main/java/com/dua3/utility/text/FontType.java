package com.dua3.utility.text;

/**
 * The font type, describing of character widths are fixed or proportional.
 */
public enum FontType {
    /**
     * Represents a font type where the spacing between characters
     * is proportional to their widths. This means each character takes up
     * space according to its visual size, providing a more natural text appearance.
     */
    PROPORTIONAL,
    /**
     * Represents a font type  where the spacing between characters
     * is the same for all characters, like a font used by a typewriter.
     */
    MONOSPACED
}

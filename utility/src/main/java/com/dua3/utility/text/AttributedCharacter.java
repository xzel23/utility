package com.dua3.utility.text;

import com.dua3.cabe.annotations.NotNull;

/**
 * A styled caracter interface.
 */
public interface AttributedCharacter {
    /**
     * Create instance.
     * @param c char
     * @param a attributes to apply to character
     * @return attributed character
     */
    static AttributedCharacter create(char c, @NotNull TextAttributes a) {
        return new AttributedCharacter() {
            @Override
            public char character() {
                return c;
            }

            @Override
            public TextAttributes attributes() {
                return a;
            }
        };
    }
    
    /**
     * Get character.
     * @return the unstyled char value
     */
    char character();

    /**
     * Get style.
     * @return the style
     */
    TextAttributes attributes();
}

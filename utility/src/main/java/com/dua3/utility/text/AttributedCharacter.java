package com.dua3.utility.text;

/**
 * A styled character interface.
 */
public interface AttributedCharacter {
    /**
     * Create instance.
     * @param c char
     * @param a attributes to apply to character
     * @return attributed character
     */
    static AttributedCharacter create(char c, TextAttributes a) {
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
     * @return the char value
     */
    char character();

    /**
     * Get style.
     * @return the style
     */
    TextAttributes attributes();
}

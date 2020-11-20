package com.dua3.utility.text;

/**
 * A styled caracter interface.
 */
public interface AttributedCharacter {
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
     * @return the unstyled char value
     */
    char character();

    /**
     * Get style.
     * @return the style
     */
    TextAttributes attributes();
}

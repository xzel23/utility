package com.dua3.utility.text;

import java.util.Collections;
import java.util.List;

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
     * Get text attributes.
     * @return the text attributes
     */
    TextAttributes attributes();

    /**
     * Get list of styles.
     * @return the list of styles
     */
    @SuppressWarnings("unchecked")
    default List<Style> getStyles() {
        return (List<Style>) attributes().getOrDefault(RichText.ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList());
    }
}

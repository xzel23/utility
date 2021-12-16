package com.dua3.utility.text;

import com.dua3.cabe.annotations.NotNull;

/**
 * Represents an HTML tag.
 */
public interface HtmlTag {
    /**
     * Get text for the opening tag.
     *
     * @return opening tag
     */
    String open();

    /**
     * Get text for the closing tag.
     *
     * @return closing tag
     */
    String close();

    /**
     * Create a new simple tag.
     * @param open text of the opening tag
     * @param close text of the closing tag
     * @return the new tag
     */
    static HtmlTag tag(@NotNull String open, @NotNull String close) {
        return new HtmlTag() {
            @Override
            public String open() {
                return open;
            }

            @Override
            public String close() {
                return close;
            }
        };
    }

    /**
     * The empty tag. 
     * @return the empty tag
     */
    static HtmlTag emptyTag() {
        return Empty.EMPTY_TAG;
    }

}

@SuppressWarnings("ClassNameDiffersFromFileName")
final class Empty {
    static final HtmlTag EMPTY_TAG = HtmlTag.tag("","");

    private Empty() {
    }
}

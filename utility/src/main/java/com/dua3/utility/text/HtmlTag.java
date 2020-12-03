package com.dua3.utility.text;

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

    static HtmlTag tag(String open, String close) {
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
}

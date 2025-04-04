package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

/**
 * Represents an HTML tag.
 */
public interface HtmlTag {

    enum FormattingHint {
        NO_LINE_BREAK(false, false),
        LINE_BREAK_AFTER_TAG(false, true),
        LINE_BREAK_BEFORE_TAG(true, false),
        LINE_BREAK_BEFORE_AND_AFTER_TAG(true, true);

        private final boolean linebreakBeforeTag;
        private final boolean linebreakAfterTag;

        FormattingHint(boolean linebreakBeforeTag, boolean linebreakAfterTag) {
            this.linebreakBeforeTag = linebreakBeforeTag;
            this.linebreakAfterTag = linebreakAfterTag;
        }

        public boolean linebreakBeforeTag() {
            return linebreakBeforeTag;
        }

        public boolean linebreakAfterTag() {
            return linebreakAfterTag;
        }

        public static FormattingHint from(boolean linebreakBeforeTag, boolean linebreakAfterTag) {
            if (linebreakBeforeTag) {
                return linebreakAfterTag ? LINE_BREAK_BEFORE_AND_AFTER_TAG : LINE_BREAK_BEFORE_TAG;
            } else {
                return linebreakAfterTag ? LINE_BREAK_AFTER_TAG : NO_LINE_BREAK;
            }
        }
    }

    /**
     * Create a new simple tag.
     *
     * @param open  text of the opening tag
     * @param close text of the closing tag
     * @return the new tag
     */
    static HtmlTag tag(String open, String close) {
        return tag(open, close, FormattingHint.NO_LINE_BREAK);
    }

    /**
     * Create a new simple tag.
     *
     * @param open  text of the opening tag
     * @param close text of the closing tag
     * @return the new tag
     */
    static HtmlTag tag(String open, String close, FormattingHint formattingHint) {
        return new SimpleHtmlTag(open, close, formattingHint);
    }

    static HtmlTag combineTags(HtmlTag... tags) {
        return switch (tags.length) {
            case 0 -> HtmlTag.emptyTag();
            case 1 -> tags[0];
            default -> new CompoundHtmlTag(tags);
        };
    }

    /**
     * The empty tag.
     *
     * @return the empty tag
     */
    static HtmlTag emptyTag() {
        return EmptyHtmlTag.INSTANCE;
    }

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

    FormattingHint formattingHint();

    /**
     * Retrieves the representation of a tag based on the specified tag type.
     *
     * @param type the {@link TagType} that indicates whether to retrieve the opening or closing tag
     * @return the corresponding tag as a {@link String}, either the opening tag or the closing tag
     */
    default String getTag(TagType type) {
        return switch (type) {
            case OPEN_TAG -> open();
            case CLOSE_TAG -> close();
        };
    }

    /**
     * Enum representing the type of an HTML tag.
     * The tag can either be an opening tag or a closing tag.
     */
    enum TagType {
        OPEN_TAG, CLOSE_TAG
    }
}

/**
 * Represents an HTML tag that is empty, meaning it has no content or visible representation.
 * This class is used to return a blank or placeholder for an HTML tag when no tag is needed.
 * All methods in this class return default values corresponding to an empty tag.
 */
@SuppressWarnings("ClassNameDiffersFromFileName")
final class EmptyHtmlTag implements HtmlTag {
    static final EmptyHtmlTag INSTANCE = new EmptyHtmlTag();

    private EmptyHtmlTag() {
    }

    @Override
    public String open() {
        return "";
    }

    @Override
    public String close() {
        return "";
    }

    @Override
    public FormattingHint formattingHint() {
        return FormattingHint.NO_LINE_BREAK;
    }

    @Override
    public String toString() {
        return "";
    }
}

/**
 * Represents a simple HTML tag with predefined opening and closing tags and
 * an option to place the tags on an extra line.
 *
 * This class implements the {@link HtmlTag} interface, providing basic functionality
 * for handling HTML tags such as retrieving the opening and closing tags, and determining
 * whether the tag should be placed on an extra line.
 */
final class SimpleHtmlTag implements HtmlTag {
    private final String open;
    private final String close;
    private final FormattingHint formattingHint;

    public SimpleHtmlTag(String open, String close, FormattingHint formattingHint) {
        this.open = open;
        this.close = close;
        this.formattingHint = formattingHint;
    }

    @Override
    public String open() {
        return open;
    }

    @Override
    public String close() {
        return close;
    }

    @Override
    public FormattingHint formattingHint() {
        return formattingHint;
    }

    @Override
    public String toString() {
        return open + close;
    }
}

/**
 * Represents a compound HTML tag, which combines multiple individual tags
 * to construct a unified HTML structure. This class implements the {@link HtmlTag} interface.
 * The opening and closing tags of this compound tag are constructed from the respective
 * opening and closing tags of the combined {@link HtmlTag} elements.
 * <p>
 * This implementation lazily initializes the `open` and `close` values to enhance performance.
 * The implementation is thread safe.
 */
final class CompoundHtmlTag implements HtmlTag {
    private final HtmlTag[] tags;
    private volatile @Nullable String open = null;
    private volatile @Nullable String close = null;

    public CompoundHtmlTag(HtmlTag... tags) {this.tags = tags;}

    @Override
    public String open() {
        if (open == null) {
            synchronized (this) {
                if (open == null) {
                    StringBuilder sb = new StringBuilder(tags.length * 16);
                    String d = "";
                    for (HtmlTag tag : tags) {
                        if (tag.formattingHint().linebreakBeforeTag()) {
                            d = "\n";
                        }
                        sb.append(d);
                        sb.append(tag.open());
                        d = tag.formattingHint().linebreakAfterTag() ? "\n" : "";
                    }
                    open = sb.toString().trim();
                }
            }
        }
        return open;
    }

    @Override
    public String close() {
        if (close == null) {
            synchronized (this) {
                if (close == null) {
                    StringBuilder sb = new StringBuilder(tags.length * 16);
                    for (int i = tags.length - 1; i >= 0; i--) {
                        HtmlTag tag = tags[i];
                        sb.append(tag.close());
                    }
                    close = sb.toString();
                }
            }
        }
        return close;
    }

    @Override
    public FormattingHint formattingHint() {
        if (tags.length == 0) {
            return FormattingHint.NO_LINE_BREAK;
        }
        return FormattingHint.from(tags[0].formattingHint().linebreakBeforeTag(), tags[tags.length - 1].formattingHint().linebreakAfterTag());
    }

    @Override
    public String toString() {
        return open() + close();
    }
}

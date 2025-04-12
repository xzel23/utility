package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Represents an HTML tag.
 */
public interface HtmlTag {

    /**
     * Represents formatting hints for handling line breaks around tags.
     * This enum provides predefined combinations of whether line breaks should appear
     * before or after a specific tag in the context of text processing or rendering.
     */
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

    static HtmlTag headerTag(String open, String close, OptionalInt level) {
        return new SimpleHtmlTag(open, close, FormattingHint.LINE_BREAK_BEFORE_TAG, level);
    }

    /**
     * Creates a new HTML tag with the specified opening and closing tags and a formatting hint.
     *
     * @param open            the text for the opening tag
     * @param close           the text for the closing tag
     * @param formattingHint  the {@link FormattingHint} to specify whether line breaks are allowed
     *                        before or after the tag
     * @return the newly created HTML tag as an instance of {@code HtmlTag}
     */
    static HtmlTag tag(String open, String close, FormattingHint formattingHint) {
        return new SimpleHtmlTag(open, close, formattingHint);
    }

    /**
     * Combines multiple {@code HtmlTag} instances into a single {@link HtmlTag}.
     * If no tags are provided, the method returns an empty tag.
     * If exactly one tag is provided, it is returned as is.
     * If multiple tags are provided, they are combined into a compound tag.
     *
     * @param tags an array of {@code HtmlTag} instances to be combined
     * @return a single {@code HtmlTag} that represents the combination of the input tags
     */
    static HtmlTag combineTags(HtmlTag... tags) {
        return switch (tags.length) {
            case 0 -> emptyTag();
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

    /**
     * Retrieves the {@link FormattingHint} associated with this HTML tag.
     * The formatting hint specifies whether line breaks are allowed
     * before and/or after the tag in text processing or rendering.
     *
     * @return the {@link FormattingHint} indicating line break preferences for the tag
     */
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

    /**
     * Retrieves the header level associated with the tag, if available.
     * A header level is typically used to represent hierarchical importance
     * of a section in HTML (e.g., h1, h2, etc.).
     *
     * @return an {@link OptionalInt} containing the header level
     *         if it is defined; otherwise, an empty {@link OptionalInt}.
     */
    OptionalInt headerChange();
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

    @Override
    public OptionalInt headerChange() {
        return OptionalInt.empty();
    }
}

/**
 * Represents a simple HTML tag with predefined opening and closing tags and
 * an option to place the tags on an extra line.
 * <p>
 * This class implements the {@link HtmlTag} interface, providing basic functionality
 * for handling HTML tags such as retrieving the opening and closing tags, and determining
 * whether the tag should be placed on an extra line.
 */
record SimpleHtmlTag(String open, String close, FormattingHint formattingHint,
                     OptionalInt headerChange) implements HtmlTag {
    public SimpleHtmlTag(String open, String close, FormattingHint formattingHint) {
        this(open, close, formattingHint, OptionalInt.empty());
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
    private volatile int headerChange = -1;

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
        assert open != null; // should never happen
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
        assert close != null; // should never happen
        return close;
    }

    @Override
    public OptionalInt headerChange() {
        if (headerChange < 0) {
            synchronized (this) {
                if (headerChange < 0) {
                    int change = 0;
                    for (HtmlTag tag : tags) {
                        change = tag.headerChange().orElse(change);
                    }
                    headerChange = change;
                }
            }
        }
        return OptionalInt.of(headerChange);
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

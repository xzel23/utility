package com.dua3.utility.text;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;

public abstract class AbstractStringBasedBuilder extends RichTextConverter<String> {

    /*
     * Definition of option names
     */

    /** Header */
    public static final String TAG_DOC_START = "TAG_DOC_START";
    /** Header */
    public static final String TAG_DOC_END = "TAG_DOC_END";
    /** Header */
    public static final String TAG_TEXT_START = "TAG_TEXT_START";
    /** Header */
    public static final String TAG_TEXT_END="TAG_TEXT_END";
    /** Where to open external links */
    public static final String TARGET_FOR_EXTERN_LINKS = "TARGET_FOR_EXTERN_LINKS";
    /** Replace '.md' file extension in local links (i.e. with ".html") */
    public static final String REPLACEMENT_FOR_MD_EXTENSION_IN_LINK = "REPLACEMENT_FOR_MD_EXTENSION_IN_LINK";

    /**
     * Set up default styles and how they are rendered.
     * @see #styleTags
     * @return map with the default styles
     */
    protected abstract Map<String, Pair<Function<Style, String>, Function<Style, String>>> defaultStyleTags();

    /**
     * Add information about opening and closing tags for a style.
     *
     * @param tags
     *            the map that stores the mapping style name -&gt; (opening_tag, closing_tag)
     * @param styleName
     *            the style name
     * @param opening
     *            the opening tag(s)
     * @param closing
     *            the closing tag(s), should be in reverse order of the corresponding opening tags
     */
    protected static void putTags(Map<String, Pair<Function<Style, String>, Function<Style, String>>> tags, String styleName, Function<Style, String> opening, Function<Style, String> closing) {
        tags.put(styleName, Pair.of(opening, closing));
    }

    /** The internal buffer that stores the HTML data. */
    protected StringBuilder buffer = new StringBuilder();
    /** Text to place at document start (i.e. document type declaration in HTML). */
    protected final String docStart;
    /** Text to place at document end. */
    protected final String docEnd;
    /** The text that starts HTML code (something like "{@code <html>}"). */
    protected final String textStart;
    /** The text that ends HTML code (something like "{@code </html>}"). */
    protected final String textEnd;
    protected final String targetForExternLinks;
    /** The extension to use for MD-files (i.e. so that links point to the translated HTML). */
    protected final String replaceMdExtensionWith;

    protected enum ListType {
        UNORDERED,
        ORDERED
    }

    private final Deque<Pair<ListType,AtomicInteger>> listStack = new LinkedList<>();

    /**
     * Update the item count for this list.
     * @return pair with list type and the item number for the new item (1-based)
     */
    protected Pair<ListType,Integer> newListItem() {
        LangUtil.check(!listStack.isEmpty(), "item definition is not inside list");
        Pair<ListType, AtomicInteger> current = listStack.peekLast();
        ListType type = current.first;
        int nr = current.second.incrementAndGet();
        return Pair.of(type, nr);
    }

    /**
     * Starts a new list definition.
     * @param type the type of the list
     */
    protected void startList(ListType type) {
        listStack.add(Pair.of(type, new AtomicInteger()));
    }

    /**
     * Closes the current list definition
     */
    protected void endList() {
        LangUtil.check(!listStack.isEmpty(), "there is no list open");
        listStack.removeLast();
    }

    /**
     * A map with default style information.
     * <ul>
     * <li> key: the style name
     * <li> value: a pair consisting of two {@code Function<Style, String>}. The first part generates
     * HTML to be inserted before the text the style is being applied to (i.e. opening tags); the second part
     * generates HTML to be appended after the text  (i.e. closing tags).
     * </ul>
     * Both functions take an argument of type {@link Style} and return the generated HTML code as a {@code String}.
     */
    protected final Map<String, Pair<Function<Style, String>, Function<Style, String>>> styleTags = defaultStyleTags();

    protected AbstractStringBasedBuilder(Map<String, String> options) {
        this.docStart = options.getOrDefault(TAG_DOC_START, "");
        this.docEnd = options.getOrDefault(TAG_DOC_END, "");
        this.textStart = options.getOrDefault(TAG_TEXT_START, "");
        this.textEnd = options.getOrDefault(TAG_TEXT_END, "");
        this.targetForExternLinks = options.getOrDefault(TARGET_FOR_EXTERN_LINKS, "");
        this.replaceMdExtensionWith = options.getOrDefault(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, "");

        buffer.append(docStart);
        buffer.append(textStart);
    }

    /**
     * Append tags to set the style for this run.
     *
     * @param run
     *      the run
     * @return
     *      the closing tags that have to be inserted after the run to reset all styles
     */
    private String appendStyleTags(Run run) {
        StringBuilder openingTag = new StringBuilder();
        StringBuilder closingTag = new StringBuilder();

        // process styles whose runs terminate at this position and insert their closing tags (p.second)
        appendTagsForRun(openingTag, run, TextAttributes.STYLE_END_RUN, p -> p.second);
        // process styles whose runs start at this position and insert their opening tags (p.first)
        appendTagsForRun(openingTag, run, TextAttributes.STYLE_START_RUN, p -> p.first);

        String separator = "<span style=\"";
        String closing = "";
        String closeThisElement = "";
        for (Map.Entry<String, Object> e : run.getStyle().properties().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            if (key.startsWith("__")) {
                // don't create spans for meta info
                continue;
            }

            openingTag.append(separator).append(key).append(":").append(value);
            separator = "; ";
            closing = "\">";
            closeThisElement = "</span>";
        }
        openingTag.append(closing);
        closingTag.append(closeThisElement);

        buffer.append(openingTag);
        return closingTag.toString();
    }

    private void appendTagsForRun(StringBuilder openingTag, Run run, String property, Function<Pair<Function<Style, String>, Function<Style, String>>, Function<Style, String>> selector) {
        TextAttributes attrs = run.getStyle();
        Object value = attrs.properties().get(property);

        if (value != null) {
            if (!(value instanceof List)) {
                throw new IllegalStateException(
                        "expected a value of class List but got " + value.getClass() + " (property=" + property + ")");
            }

            @SuppressWarnings("unchecked")
            List<Style> styles = (List<Style>) value;
            for (Style style : styles) {
                Pair<Function<Style, String>, Function<Style, String>> tag = styleTags.get(style.name());
                if (tag != null) {
                    openingTag.append(selector.apply(tag).apply(style));
                }
            }
        }
    }

    /**
     * Create attribute text for HTML tags.
     *
     * @param style
     *            the current style
     * @param property
     *            the TextAttribute to retrieve
     * @param htmlAttribute
     *            the attribute name of the HTML tag
     * @param dflt
     *            the value to use if attribute is not set. pass {@code null} to omit the attribute if not set
     * @return
     *         the text to set the attribute in the HTML tag
     */
    protected String attrText(Style style, String property, String htmlAttribute, String dflt) {
        Object value = style.getOrDefault(property, dflt);

        if (value == null) {
            return "";
        }

        return " " + htmlAttribute + "=\"" + value.toString() + "\"";
    }

    protected String ifSet(Style style, String textAttribute, String textIfPresent) {
        Object value = style.get(textAttribute);

        boolean isSet;
        if (value instanceof Boolean) {
            isSet = (boolean) value;
        } else {
            isSet = Boolean.valueOf(String.valueOf(value));
        }

        return isSet ? textIfPresent : "";
    }

    @Override
    protected void append(Run run) {
        // handle attributes
        String closeStyleTags = appendStyleTags(run);

        // append text (need to do characterwise because of escaping)
        appendChars(run);

        // add end tag
        buffer.append(closeStyleTags);
    }

    protected void appendChars(CharSequence run) {
        buffer.append(run);
    }

    @Override
    public String get() {
        buffer.append(textEnd);
        String html = buffer.toString();
        buffer = null;
        return html;
    }

    @Override
    protected boolean wasGetCalled() {
        return buffer == null;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}

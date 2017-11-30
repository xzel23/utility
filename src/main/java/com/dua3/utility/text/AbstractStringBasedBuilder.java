package com.dua3.utility.text;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractStringBasedBuilder extends RichTextConverterBase<String> {

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
    public static final String TARGET_FOR_EXTERNAL_LINKS = "TARGET_FOR_EXTERN_LINKS";
    /** Replace '.md' file extension in local links (i.e. with ".html") */
    public static final String REPLACEMENT_FOR_MD_EXTENSION_IN_LINK = "REPLACEMENT_FOR_MD_EXTENSION_IN_LINK";

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
    /** The extension to use for MD-files (i.e. so that links point to the translated HTML). */
    protected final String replaceMdExtensionWith;
    /** Target for external links (i.e. in HTML). */
    protected final String targetForExternalLinks;

    protected AbstractStringBasedBuilder(Function<Style, TextAttributes> styleTraits, Map<String, String> options) {
        super(styleTraits);

        this.docStart = options.getOrDefault(TAG_DOC_START, "");
        this.docEnd = options.getOrDefault(TAG_DOC_END, "");
        this.textStart = options.getOrDefault(TAG_TEXT_START, "");
        this.textEnd = options.getOrDefault(TAG_TEXT_END, "");
        this.replaceMdExtensionWith = options.getOrDefault(REPLACEMENT_FOR_MD_EXTENSION_IN_LINK, "");
        this.targetForExternalLinks = options.getOrDefault(TARGET_FOR_EXTERNAL_LINKS, "_blank");

        buffer.append(docStart);
        buffer.append(textStart);
    }

    @Override
    protected void appendUnquoted(CharSequence chars) {
        buffer.append(chars);
    }

    @Override
    public String get() {
        buffer.append(textEnd);
        String text = buffer.toString();
        buffer = null;
        return text;
    }

    @Override
    protected boolean isValid() {
        return buffer != null;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}

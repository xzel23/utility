package com.dua3.utility.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.utility.Pair;

public enum MarkDownStyle {
	BLOCK_QUOTE,
    BULLET_LIST,
	CODE,
	DOCUMENT,
	EMPHASIS,
	FENCED_CODE_BLOCK,
	HARD_LINE_BREAK,
	HEADING,
	THEMATIC_BREAK,
	HTML_BLOCK,
	HTML_INLINE,
	IMAGE,
	INDENTED_CODE_BLOCK,
	LINK,
	LIST_ITEM,
	ORDERED_LIST,
	PARAGRAPH,
	SOFT_LINE_BREAK,
	STRONG_EMPHASIS,
	TEXT,
	CUSTOM_BLOCK,
	CUSTOM_NODE;
	
	private static final Map<String, Style> DEFAULT_STYLES = createDefaultStyles();
	
	private static Map<String, Style> createDefaultStyles() {
		Map<String, Style> styles = new HashMap<>();
		// BLOCK_QUOTE
	    // BULLET_LIST
	    put(styles, CODE, Pair.of(Style.FONT_FAMILY, "monospaced"));
		// DOCUMENT
	    put(styles, EMPHASIS, Pair.of(Style.FONT_STYLE, "italic"));
		// FENCED_CODE_BLOCK
		// HARD_LINE_BREAK
	    put(styles, HEADING, Pair.of(Style.FONT_WEIGHT, "bold"));
		// THEMATIC_BREAK
		// HTML_BLOCK
		// HTML_INLINE
		// IMAGE
		// INDENTED_CODE_BLOCK
	    put(styles, LINK, Pair.of(Style.COLOR, "blue"));
		// LIST_ITEM
		// ORDERED_LIST
		// PARAGRAPH
		// SOFT_LINE_BREAK
	    put(styles, EMPHASIS, Pair.of(Style.FONT_WEIGHT, "bold"));
		// TEXT
		// CUSTOM_BLOCK
		// CUSTOM_NODE
		return Collections.unmodifiableMap(styles);
	}

	@SafeVarargs
	private static void put(Map<String, Style> styles, MarkDownStyle mds, Pair<String, String>... properties) {
		styles.put(mds.name(), Style.of(properties));
	}
	
	public static Map<String, Style> defaultStyles() {
		return DEFAULT_STYLES;
	}
}
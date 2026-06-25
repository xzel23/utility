package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;

/**
 * Result of replacing a document range.
 *
 * @param start normalized start offset
 * @param end normalized end offset
 * @param removed removed text slice
 * @param changed {@code true} if the document changed
 */
public record DocumentReplaceResult(int start, int end, RichText removed, boolean changed) {}

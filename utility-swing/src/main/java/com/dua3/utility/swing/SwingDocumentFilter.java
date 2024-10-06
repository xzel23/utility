package com.dua3.utility.swing;

import org.jspecify.annotations.Nullable;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.Locale;
import java.util.function.Function;

/**
 * A {@link DocumentFilter} implementation useful for creating JTextFields that restrict input.
 */
public class SwingDocumentFilter extends DocumentFilter {

    /**
     * Create an instance that converts all input to uppercase.
     * @param locale the locale to use in conversion
     * @return new instance
     */
    public static DocumentFilter getUppercaseInstance(Locale locale) {
        return new SwingDocumentFilter(s -> s.toUpperCase(locale));
    }

    /**
     * Create an instance that converts all input to lowercase.
     * @param locale the locale to use in conversion
     * @return new instance
     */
    public static DocumentFilter getLowercaseInstance(Locale locale) {
        return new SwingDocumentFilter(s -> s.toLowerCase(locale));
    }

    private final Function<? super String, String> processor;

    SwingDocumentFilter(Function<? super String, String> processor) {
        this.processor = processor;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String s, @Nullable AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, processor.apply(s), attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String s, @Nullable AttributeSet attrs) throws BadLocationException {
        fb.replace(offset, length, processor.apply(s), attrs);
    }

}

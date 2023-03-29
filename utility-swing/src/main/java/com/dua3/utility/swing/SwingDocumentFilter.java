package com.dua3.utility.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.Locale;
import java.util.function.Function;

public class SwingDocumentFilter extends DocumentFilter {

    public static DocumentFilter getUppercaseInstance(Locale locale) {
        return new SwingDocumentFilter(s -> s.toUpperCase(locale));
    }

    public static DocumentFilter getLowercaseInstance(Locale locale) {
        return new SwingDocumentFilter(s -> s.toLowerCase(locale));
    }

    private final Function<String, String> processor;

    SwingDocumentFilter(Function<String, String> processor) {
        this.processor = processor;
    }

    public void insertString(DocumentFilter.FilterBypass fb, int offset, String s, AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, processor.apply(s), attr);
    }

    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String s, AttributeSet attrs) throws BadLocationException {
        fb.replace(offset, length, processor.apply(s), attrs);
    }

}

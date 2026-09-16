package com.dua3.utility.text;

import com.dua3.utility.i18n.I18N;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MessageFormatterTest {

    @Test
    void argumentFactoriesCreateExpectedArguments() {
        MessageFormatter.MessageFormatterArgs args = MessageFormatter.args("Hello, %s!", "World");

        assertEquals("Hello, %s!", args.fmt());
        assertArrayEquals(new Object[]{"World"}, args.args());
        assertEquals(args, new MessageFormatter.MessageFormatterArgs("Hello, %s!", "World"));
        assertEquals(args.hashCode(), new MessageFormatter.MessageFormatterArgs("Hello, %s!", "World").hashCode());

        assertEquals(new MessageFormatter.MessageFormatterArgs("\0", "literal"), MessageFormatter.literal("literal"));
        assertEquals(new MessageFormatter.MessageFormatterArgs("\0", (Object) null), MessageFormatter.literal(null));
        assertEquals(new MessageFormatter.MessageFormatterArgs("\0plain %s", "text"), MessageFormatter.nonI18N("plain %s", "text"));
        assertEquals(new MessageFormatter.MessageFormatterArgs(""), MessageFormatter.empty());
    }

    @Test
    void standardFormatterUsesLocaleAndSpecialArguments() {
        MessageFormatter formatter = MessageFormatter.localized(Locale.GERMANY);

        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("Hello, %s!", "World"));
        assertEquals("1.234,50", formatter.format("%,.2f", 1234.5));
        assertEquals("plain text", formatter.format(MessageFormatter.nonI18N("plain %s", "text")));
        assertEquals("100% complete", formatter.format(MessageFormatter.literal("100% complete")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
        assertEquals("unchanged", formatter.text("unchanged"));
    }

    @Test
    void messageFormatFormatterFormatsMessageFormatPatterns() {
        MessageFormatter formatter = MessageFormatter.messageFormat();

        assertSame(formatter, MessageFormatter.messageFormat());
        assertEquals(MessageFormatter.FormatStyle.MESSAGE_FORMAT, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("Hello, {0}!", "World"));
        assertEquals("There are 2 items.", formatter.format("There are {0,number} items.", 2));
        assertEquals("plain text", formatter.format(MessageFormatter.nonI18N("plain", "text")));
        assertEquals("100% complete", formatter.format(MessageFormatter.literal("100% complete")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
    }

    @Test
    void i18nFormatterLooksUpMessagesAndBypassesLookupForNonI18nText() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"greeting", "Hello, {0}!"}};
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        };
        MessageFormatter formatter = MessageFormatter.i18n(I18N.create(bundle));

        assertEquals(MessageFormatter.FormatStyle.I18N, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("greeting", "World"));
        assertEquals("greeting World", formatter.format(MessageFormatter.nonI18N("greeting", "World")));
        assertEquals("greeting", formatter.format(MessageFormatter.literal("greeting")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void standardFormatterIsSingletonAndUsesDefaultLocale() {
        assertSame(MessageFormatter.standard(), MessageFormatter.standard());
        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, MessageFormatter.standard().getFormatStyle());
    }
}
